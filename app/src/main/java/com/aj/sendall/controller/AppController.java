package com.aj.sendall.controller;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.annotation.NonNull;

import com.aj.sendall.broadcastreceiver.WifiApScannerBR;
import com.aj.sendall.db.dto.ConnectionViewData;
import com.aj.sendall.db.dto.ConnectionsAndUris;
import com.aj.sendall.db.dto.PersonalInteractionDTO;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.model.PersonalInteraction;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.AppStatusChanged;
import com.aj.sendall.events.event.ClientModeStarted;
import com.aj.sendall.events.event.ClientModeStopped;
import com.aj.sendall.events.event.ServerModeStarted;
import com.aj.sendall.events.event.ServerModeStopped;
import com.aj.sendall.notification.NotificationUtil;
import com.aj.sendall.nw.comms.FileTransferClient;
import com.aj.sendall.nw.comms.NewConnCreationClient;
import com.aj.sendall.nw.sockets.SocketSystem;
import com.aj.sendall.nw.sockets.SocketSystemFactory;
import com.aj.sendall.services.FileTransferClientService;
import com.aj.sendall.services.FileTransferServerService;
import com.aj.sendall.services.NewConnCreationClientService;
import com.aj.sendall.services.NewConnCreationServerService;
import com.aj.sendall.services.ToggleReceiverService;
import com.aj.sendall.sharedprefs.SharedPrefConstants;
import com.aj.sendall.sharedprefs.SharedPrefUtil;
import com.aj.sendall.streams.StreamManager;
import com.aj.sendall.streams.StreamManagerFactory;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.utils.PermissionUtil;
import com.aj.sendall.utils.WifiNetUtil;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppController implements Serializable{
    private boolean initialised = false;
    private AppStatus appStatus = AppStatus.IDLE;

    private Context context;
    private DBUtil dbUtil;
    private SharedPrefUtil sharedPrefUtil;
    private NotificationUtil notificationUtil;
    private PermissionUtil permissionUtil;
    private WifiNetUtil wifiNetUtil = null;
    private SocketSystem socketSystem = SocketSystemFactory.getInstance();
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    @Inject
    public AppController(Context context,
                         DBUtil dbUtil,
                         SharedPrefUtil sharedPrefUtil,
                         NotificationUtil notificationUtil,
                         PermissionUtil permissionUtil){
        this.context = context;
        this.dbUtil = dbUtil;
        this.sharedPrefUtil = sharedPrefUtil;
        this.notificationUtil = notificationUtil;
        this.permissionUtil = permissionUtil;
        init(context);
    }

    private void init(Context context) {
        if(!initialised) {
            initialised = true;
            wifiNetUtil = WifiNetUtil.getInstance(context);

            enableWifi(false);
            setCurrentAppStatus(AppStatus.IDLE);
            notificationUtil.setAppController(this);
            eventRouter.subscribe(ServerModeStarted.class, new EventRouter.Receiver<ServerModeStarted>() {
                @Override
                public void receive(ServerModeStarted event) {
                    initHotspot(event.portNo);
                }
            });
            eventRouter.subscribe(ServerModeStopped.class, new EventRouter.Receiver<ServerModeStopped>() {
                @Override
                public void receive(ServerModeStopped event) {
                    stopHotspot();
                }
            });
            eventRouter.subscribe(ClientModeStarted.class, new EventRouter.Receiver<ClientModeStarted>() {
                @Override
                public void receive(ClientModeStarted event) {
                    enableWifi(true);
                }
            });
            eventRouter.subscribe(ClientModeStopped.class, new EventRouter.Receiver<ClientModeStopped>() {
                @Override
                public void receive(ClientModeStopped event) {
                    stopWifiScanning();
                    enableWifi(false);
                }
            });
        }
    }

    public Context getApplicationContext(){
        return context;
    }

    public boolean isWifiEnabled(){
        return wifiNetUtil.isWifiEnabled();
    }

    private void enableWifi(boolean enable){
        wifiNetUtil.enableWifi(enable);
    }

    private void enableWifi(boolean enable, AppStatus newAppStatus){
        enableWifi(enable);
        setCurrentAppStatus(newAppStatus);
    }

    /*Method to set current wifi status from wifi broadcast receiver*/
    public void setWifiP2pState(int currentWifiStatus){
        if(WifiP2pManager.WIFI_P2P_STATE_ENABLED == currentWifiStatus){
            showStatusNotification(true);
        } else {
            showStatusNotification(false);
        }
    }

    private int getPortNo(String SSID){
        if(SSID != null && SSID.contains("_")){
            String[] splits = SSID.split("_");
            if(splits.length == 3){
                try {
                    return Integer.valueOf(splits[1]);
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    private void initHotspot(int portNo){
        //append username to the ssid so that username is available at the other end
        String SSID = sharedPrefUtil.getThisDeviceId() + '_' + portNo + '_' +sharedPrefUtil.getUserName();
        if(SSID.length() > 32){
            SSID = SSID.substring(0, 32);
        }
        WifiConfiguration wifiConfiguration = getWifiConfiguration(SSID, sharedPrefUtil.getDefaultWifiPass(), true);
        initHotspot(wifiConfiguration);
    }

    private void initHotspot(WifiConfiguration wifiConfig){
        wifiNetUtil.startHotspot(wifiConfig);
    }

    private void startScanningWifi(){
        stopWifiScanning();
        wifiNetUtil.startScanningWifi(new WifiApScannerBR(sharedPrefUtil));
    }

    public void startNewConnCreationServer(){
        if(isSystemFree()) {
            setCurrentAppStatus(AppStatus.NEW_CONN_SERVER);
            NewConnCreationServerService.start(context);
        }
    }

    public void scanForConnCreationServer(){
        if(isSystemFree()) {
            setCurrentAppStatus(AppStatus.NEW_CONN_CLIENT);
            startScanningWifi();
        }
    }

    public void connectToConnCreationServer(String SSID) {
        stopWifiScanning();
        int port = getPortNo(SSID);
        String pass = getDefaultWifiPass();
        NewConnCreationClient client = new NewConnCreationClient(SSID, pass, port, this);
        NewConnCreationClientService.start(context, client);
    }

    public void startFileTransferServer(ConnectionsAndUris cau){
        if(isSystemFree()) {
            setCurrentAppStatus(AppStatus.TRANSF_SERVER);
            FileTransferServerService.start(context, cau);
        }
    }

    public void scanForFileTransferServer(){
        if(isSystemFree()) {
            setCurrentAppStatus(AppStatus.TRANSF_CLIENT);
            startScanningWifi();
        }
    }

    public void connectToFileTransferServer(String SSID) {
        stopWifiScanning();
        FileTransferClient client = new FileTransferClient(
                SSID,
                getDefaultWifiPass(),
                getPortNo(SSID), this);
        FileTransferClientService.start(context, client);
    }

    private void stopWifiScanning(){
        wifiNetUtil.stopScanningWifi();
    }

    private void stopHotspot() {
        wifiNetUtil.stopHotspot();
    }

    public void setSystemIdle(){
        switch (appStatus){
            case TRANSF_SERVER: FileTransferServerService.stop(context); break;
            case TRANSF_CLIENT: FileTransferClientService.stop(context); break;
            case NEW_CONN_SERVER: NewConnCreationServerService.stop(context); break;
            case NEW_CONN_CLIENT:  NewConnCreationClientService.stop(context); break;
        }
        setCurrentAppStatus(AppStatus.IDLE);
    }

    @NonNull
    private WifiConfiguration getWifiConfiguration(String ssid, String pass, boolean create) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        if(!create){
            ssid = "\"" + ssid + "\"";
        }
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return wifiConfiguration;
    }

    public InetAddress connectAndGetAddressOf(String SSID, String PASS) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(SSID, PASS, false);
        enableWifi(true);
        int serverAddress = wifiNetUtil.connectAndGetServerAddress(wifiConfiguration);
        return intToInetAddress(serverAddress);
    }

    private InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = {(byte) (0xff & hostAddress),
                (byte) (0xff & (hostAddress >> 8)),
                (byte) (0xff & (hostAddress >> 16)),
                (byte) (0xff & (hostAddress >> 24))};

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            return null;
        }
    }

    private boolean isValidFileName(String fileName){
        if(fileName == null || fileName.isEmpty()){
            return false;
        }
        String part1 = fileName.substring(0, Math.max(1, fileName.lastIndexOf('.')));
        return !part1.isEmpty();
    }

    public StreamManager getTempFileToWrite(long connId, String fileName, int mediaType, String MIMEType) throws IOException{
        if (!isValidFileName(fileName)) {
            return null;
        }
        StreamManager root = sharedPrefUtil.getStorageDirectory(context);
        StreamManager subDir = root.createDir(SharedPrefConstants.APP_NAME).createDir("Temp");
        if(!subDir.exists()){
            subDir.create();
            if(!subDir.exists()){
                return null;
            }
        }
        String tempFileName = getTempFileName(connId, fileName, mediaType);
        return subDir.createFile(tempFileName, MIMEType);
    }

    private String getTempFileName(long connId, String fileName, int mediaType){
        //<connid>_<mediatype>_<filename>.tmp
        return connId + '_' + mediaType + '_' + fileName + ".tmp";
    }

    public StreamManager getActualFileToWrite(String fileName, int fileType) throws IOException{
        if (!isValidFileName(fileName)) {
            return null;
        }
        StreamManager root = sharedPrefUtil.getStorageDirectory(context);
        String subDirName = getSubDirName(fileType);
        StreamManager subDir = root.createDir(SharedPrefConstants.APP_NAME).createDir(subDirName);
        if(!subDir.exists()){
            subDir.create();
            if(!subDir.exists()){
                return null;
            }
        }
        return getUniqueFile(subDir, fileName);
    }

    private StreamManager getUniqueFile(StreamManager dir, String fileName) throws IOException{
        String extnRemovedFileName = removeExtension(fileName);
        String extn = getExtensionIncludingPeriod(fileName);
        String part1 = dir.getActualPath() + '/' + extnRemovedFileName;
        StreamManager uniqueFile = StreamManagerFactory.getInstance(context, part1 + extn);
        //to avoid fileName conflict
        int count = 1;
        while(uniqueFile.exists()){
            String filePathTry = part1 + '(' + count + ')' + extn;
            uniqueFile = StreamManagerFactory.getInstance(context, filePathTry);
            count++;
        }
        return uniqueFile;
    }

    private String removeExtension(String fileName){
        int lastIndexOfPeriod = fileName.lastIndexOf('.');
        String fileNameWithoutExtn = fileName;
        if(lastIndexOfPeriod > 0){
            fileNameWithoutExtn = fileName.substring(0, lastIndexOfPeriod);
        }
        return fileNameWithoutExtn;
    }

    private String getExtensionIncludingPeriod(String fileName){
        int lastIndexOfPeriod = fileName.lastIndexOf('.');
        String extension = "";
        if(lastIndexOfPeriod > 0){
            extension = fileName.substring(lastIndexOfPeriod, fileName.length());
        }
        return extension;
    }

    private String getSubDirName(int fileType){
        switch(fileType){
            case MediaConsts.TYPE_VIDEO: return "Video";
            case MediaConsts.TYPE_AUDIO: return "Audio";
            case MediaConsts.TYPE_IMAGE: return "Image";
            default : return "Others";
        }
    }

    private AppStatus getCurrentAppStatus(){
        return appStatus;
    }

    public boolean isSystemFree(){
        return appStatus == AppStatus.IDLE;
    }

    public boolean isConnCreationServer(){
        return appStatus == AppStatus.NEW_CONN_SERVER;
    }

    public boolean isConnCreationClient(){
        return appStatus == AppStatus.NEW_CONN_CLIENT;
    }

    private boolean isFileTransferServer(){
        return appStatus == AppStatus.TRANSF_SERVER;
    }

    private boolean isFileTransferClient(){
        return appStatus == AppStatus.TRANSF_CLIENT;
    }

    public boolean isClient(){
        return isConnCreationClient() || isFileTransferClient();
    }

    private static final Object socketSystemSync = new Object();
    private void setCurrentAppStatus(AppStatus newStatus){
        if(!this.appStatus.equals(newStatus)) {
            this.appStatus = newStatus;
            synchronized (socketSystemSync) {
                Thread socketUpdator = getSocketUpdtingThread();
                if (socketUpdator != null) {
                    socketUpdator.start();
                    try {
                        socketSystemSync.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            //command to show status notification. The notification util will decide whether to ot not to show the notification
            showStatusNotification(true);
            AppStatusChanged ev = new AppStatusChanged();
            ev.newStatus = newStatus;
            eventRouter.broadcast(ev);
        }
    }

    private Thread getSocketUpdtingThread(){
        switch(appStatus){
            case IDLE:
                //to avoid NetworkInMainThreadException in main thread exception
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (socketSystemSync) {
                            socketSystem.setIdle();
                            socketSystemSync.notify();
                        }
                    }
                });
            case NEW_CONN_SERVER:
            case TRANSF_SERVER:
                //to avoid NetworkInMainThreadException in main thread exception
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            synchronized (socketSystemSync) {
                                socketSystem.startServerMode(0);
                                socketSystemSync.notify();
                            }
                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                });
            case NEW_CONN_CLIENT:
            case TRANSF_CLIENT:
                return new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (socketSystemSync) {
                            socketSystem.startClientMode();
                            socketSystemSync.notify();
                        }
                    }
                });
        }
        return null;
    }

    public void registerAppStatusListener(EventRouter.Receiver<AppStatusChanged> r){
        if(r != null){
            eventRouter.subscribe(AppStatusChanged.class, r);
        }
    }

    public void unregisterAppStatusListener(EventRouter.Receiver<AppStatusChanged> r){
        if(r != null){
            eventRouter.unsubscribe(AppStatusChanged.class, r);
        }
    }

    private String getDefaultWifiPass(){
        return sharedPrefUtil.getDefaultWifiPass();
    }

    public String getUsername(){
        return sharedPrefUtil.getUserName();
    }

    public void setUsername(String newUsername){
        sharedPrefUtil.setUserName(newUsername);
    }

    public String getThisDeviceId(){
        return sharedPrefUtil.getThisDeviceId();
    }

    private void showStatusNotification(boolean show){
        if(show){
            notificationUtil.showToggleReceivingNotification();
        } else {
            notificationUtil.removeToggleNotification();
        }
    }

    public void notifyInsuffSpace(){
        setSystemIdle();
    }

    public void notifyNoExtMedia(){
        setSystemIdle();
    }

    public void notifyTransferSuccess(){
        setSystemIdle();
    }

    public List<PersonalInteractionDTO> getPersonalInteractionDTOs(long connId){
        return dbUtil.getAllPersonalInteractionDTO(connId);
    }

    public void showToggleReceiverNotification(){
        if (isSystemFree()) {
            Intent intent = new Intent(context, ToggleReceiverService.class);
            intent.putExtra(ToggleReceiverService.ACTION, ToggleReceiverService.ACTION_START);
            context.startService(intent);
        }
    }

    private boolean isServer(){
        return isConnCreationServer() || isFileTransferServer();
    }

    public ServerSocket getRunningServerSocket(){
        if(isServer()){
            return socketSystem.getCurrentServerSocket();
        }
        return null;
    }

    public Socket createClientSocket(InetAddress a, int port){
        try {
            return socketSystem.createClientSocket(a, port);
        } catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void setServerAcceptWaitTimer(long waitTime){
        socketSystem.setServerAcceptWaitTimer(waitTime);
    }

    public void save(Connections conn){
        dbUtil.save(conn);
    }

    public void save(PersonalInteraction pi){
        dbUtil.save(pi);
    }

    public void update(Connections conn){
        dbUtil.update(conn);
    }

    public void update(PersonalInteraction pi){
        dbUtil.update(pi);
    }

    public Connections getConnectionBySSID(String SSID){
        return dbUtil.getConnectionBySSID(SSID);
    }

    public PersonalInteraction getPersonalInteraction(long id, String fileName, int mediaType, long fileSize){
        return dbUtil.getPersonalInteraction(id, fileName, mediaType, fileSize);
    }

    public String[] getDeniedSystemPermissions(){
        return permissionUtil.getDeniedPermissions();
    }

    public void requestPermissions(Activity ac,
                                       String[] permisions,
                                       int permReqCode,
                                       String negativeButtonCaption,
                                       DialogInterface.OnClickListener cl){
        permissionUtil.getPermissions(ac, permisions, permReqCode, negativeButtonCaption, cl);
    }

    public String getHumanReadablePermString(String perm){
        return permissionUtil.getHumanReadablePermString(perm);
    }

    public ConnectionViewData getConnectionViewData(long connectionId){
        Connections c = dbUtil.getConnection(connectionId);
        ConnectionViewData cvd = new ConnectionViewData();
        cvd.uniqueId = c.getSSID();
        cvd.profileName = c.getConnectionName();
        cvd.profileId = c.getConnectionId();
        cvd.profilePicPath = c.getProfPicPath();
        return cvd;
    }
}
