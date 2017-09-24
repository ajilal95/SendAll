package com.aj.sendall.application;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.broadcastreceiver.SendallNetWifiScanBroadcastReceiver;
import com.aj.sendall.notification.util.NotificationUtil;
import com.aj.sendall.ui.consts.MediaConsts;
import com.aj.sendall.network.monitor.Updatable;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AppManager implements Serializable{
    private static final String WAIT_FOR_CURRENT_OP_TOAST = "Wait for the current operation to finish";

    public Context context;
    public DBUtil dbUtil;
    private boolean initialised = false;
    private WifiManager wifiManager;
    private SharedPrefUtil sharedPrefUtil;
    private int wifiP2pState;
    private NotificationUtil notificationUtil;

    public Permissions permissions;
    private SendallNetWifiScanBroadcastReceiver sendallNetWifiScanBroadcastReceiver = null;
    private WifiApControl wifiApControl = null;
    private WifiManager.WifiLock wifiLock = null;
    private WifiConfiguration systemWifiConfig = null;

    @Inject
    public AppManager(Context context,
                      DBUtil dbUtil,
                      SharedPrefUtil sharedPrefUtil,
                      NotificationUtil notificationUtil/*,
                      ContentProviderUtil contentProviderUtil*/){
        this.context = context;
        this.dbUtil = dbUtil;
        this.sharedPrefUtil = sharedPrefUtil;
        this.notificationUtil = notificationUtil;
        init(context);
    }

    private void init(Context context) {
        if(!initialised) {
            permissions = new Permissions();
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            initialised = true;

            enableWifi(false);
            wifiP2pState = WifiP2pManager.WIFI_P2P_STATE_DISABLED;
        }
    }

    public boolean isWifiEnabled(){
        return  wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
    }

    private void enableWifi(boolean enable){
        if(!enable && isWifiLocked()){
            releaseWifiLock();
        }
        boolean success = wifiManager.setWifiEnabled(enable);
        if(!success){
            Toast.makeText(context, "Please turn " + (enable ? "on" : "off") +" wifi", Toast.LENGTH_SHORT).show();
        } else {
            //wait for the operation to take effect
            if(enable){
                while(!wifiManager.isWifiEnabled()){}
            } else {
                while(wifiManager.isWifiEnabled()){}
            }
        }
    }

    public void enableWifi(boolean enable, int newAppStatus){
        enableWifi(enable);
        setCurrentAppStatus(newAppStatus);
    }

    /*Method to set current wifi status from wifi broadcast receiver*/
    public void setWifiP2pState(int currentWifiStatus){
        this.wifiP2pState = currentWifiStatus;
    }

    private void aquireWifiLock(){
        if(wifiLock == null){
            wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, this.getClass().getSimpleName());
        }
        wifiLock.acquire();
    }

    private void releaseWifiLock(){
        if(wifiLock != null){
            wifiLock.release();
            wifiLock = null;
        }
    }

    private boolean isWifiLocked(){
        return wifiLock != null && wifiLock.isHeld();
    }

    public int getPortNo(String SSID){
        if(SSID != null && SSID.contains("_")){
            String[] splits = SSID.split("_");
            if(splits.length == 3){
                try {
                    int portNo = Integer.valueOf(splits[1]);
                    return portNo;
                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public void initHotspot(int newAppStatus, int portNo){
        //append username to the ssid so that username is available at the other end
        String SSID = sharedPrefUtil.getThisDeviceId() + '_' + portNo + '_' +sharedPrefUtil.getUserName();
        if(SSID.length() > 32){
            SSID = SSID.substring(0, 32);
        }
        WifiConfiguration wifiConfiguration = getWifiConfiguration(SSID, sharedPrefUtil.getDefaultWifiPass(), true);
        initHotspot(newAppStatus, wifiConfiguration);
    }

    private void initHotspot(int newAppStatus, WifiConfiguration wifiConfig){
        //first of all, change the app status
        int appStatus = sharedPrefUtil.getCurrentAppStatus();
        if(appStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
            enableWifi(false);
            setCurrentAppStatus(newAppStatus);

            stopHotspot();

            wifiApControl = WifiApControl.getInstance(context);
            if(wifiApControl != null) {
                wifiApControl.setEnabled(wifiConfig, true);
            }
        } else {
            Toast.makeText(context, WAIT_FOR_CURRENT_OP_TOAST, Toast.LENGTH_SHORT).show();
        }
    }

    private void stopHotspot(){
        if(wifiApControl != null){
            //Restoring wifi ap configuration of the system
            wifiApControl.disable();
            if(systemWifiConfig != null){
                wifiApControl.setWifiApEnabled(systemWifiConfig, true);
            }
            wifiApControl.disable();
        }
        systemWifiConfig = null;
        wifiApControl = null;
    }

    public void startScanningWifi(Updatable updatable, int newAppStatus){
        int appStatus = sharedPrefUtil.getCurrentAppStatus();
        if(appStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(newAppStatus);
            sharedPrefUtil.commit();
            enableWifi(true);

            stopWifiScanning();

            sendallNetWifiScanBroadcastReceiver = new SendallNetWifiScanBroadcastReceiver(updatable, sharedPrefUtil);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            context.registerReceiver(sendallNetWifiScanBroadcastReceiver, intentFilter);
            wifiManager.disconnect();
            wifiManager.startScan();
        } else {
            Toast.makeText(context, WAIT_FOR_CURRENT_OP_TOAST, Toast.LENGTH_SHORT).show();
        }
    }

    public void stopWifiScanning(){
        if(sendallNetWifiScanBroadcastReceiver != null){
            context.unregisterReceiver(sendallNetWifiScanBroadcastReceiver);
        }
        sendallNetWifiScanBroadcastReceiver = null;
    }

    public void stopHotspotAndScanning(){
        releaseWifiLock();
        stopWifiScanning();
        stopHotspot();
        enableWifi(false);

        setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
    }

    @NonNull
    private WifiConfiguration getWifiConfiguration(String ssid, String pass, boolean create) {
        if(wifiApControl == null){
            wifiApControl = WifiApControl.getInstance(context);
        }
        if(wifiApControl != null) {
            //For restoring the configuration on exit
            systemWifiConfig = wifiApControl.getConfiguration();
        }
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        if(!create){
            ssid = "\"" + ssid + "\"";
        }
        wifiConfiguration.SSID = ssid;
        wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        return wifiConfiguration;
    }

    public class BroadcastReceiverAutoUnregister implements Runnable{
        private final Object syncObj = new Object();
        private boolean unregistered = false;
        private Context context;
        private BroadcastReceiver broadcastReceiver;
        private long lifetime;

        public BroadcastReceiverAutoUnregister(Context context, BroadcastReceiver broadcastReceiver, long lifetime){
            this.context = context;
            this.broadcastReceiver = broadcastReceiver;
            this.lifetime = lifetime;
        }

        @Override
        public void run() {
            synchronized (syncObj) {
                if (!unregistered) {
                    unregistered = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            context.unregisterReceiver(broadcastReceiver);
                        }
                    }, lifetime);
                }
            }
        }

        public void unregNow(){
            synchronized (syncObj) {
                if (!unregistered) {
                    unregistered = true;
                    context.unregisterReceiver(broadcastReceiver);
                }
            }
        }
    }

    public InetAddress connectAndGetAddressOf(String SSID, String PASS) {
        WifiConfiguration wifiConfiguration = getWifiConfiguration(SSID, PASS, false);
        if(!isWifiEnabled()){
            enableWifi(true);
        }
        aquireWifiLock();
        int res = wifiManager.addNetwork(wifiConfiguration);
        wifiManager.disconnect();
        try {
            Thread.sleep(3 * 1000);//time to disconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        wifiManager.enableNetwork(res, true);
        wifiManager.reconnect();

        try {
            Thread.sleep(3 * 1000);//time to reconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        int serverAddress = wifiManager.getDhcpInfo().serverAddress;
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

    public class Permissions {
        public String[] getDeniedPermissions() {
            Set<String> deniedPermissions = new HashSet<>();
            if (!isWriteExternalStorageGranted()) {
                deniedPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (!isAccessWifiStateGranted()) {
                deniedPermissions.add(Manifest.permission.ACCESS_WIFI_STATE);
            }
            if (!isChangeWifiStateGranted()) {
                deniedPermissions.add(Manifest.permission.CHANGE_WIFI_STATE);
            }
            if (!isWakeLockGranted()) {
                deniedPermissions.add(Manifest.permission.WAKE_LOCK);
            }
            if (!isInternetGranted()) {
                deniedPermissions.add(Manifest.permission.INTERNET);
            }
            if (!isAccessNetorkStateGranted()) {
                deniedPermissions.add(Manifest.permission.ACCESS_NETWORK_STATE);
            }
            if (!isChanegNetworkStateGranted()) {
                deniedPermissions.add(Manifest.permission.CHANGE_NETWORK_STATE);
            }

            String[] deniedPermStringArray = null;
            int permsize = deniedPermissions.size();
            if(permsize > 0){
                deniedPermStringArray = new String[permsize];
                deniedPermStringArray = deniedPermissions.toArray(deniedPermStringArray);
            }

            return deniedPermStringArray;
        }

        public boolean isPermissionGranted(String permission) {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
        }

        public boolean isWriteExternalStorageGranted() {
            return isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        public boolean isAccessWifiStateGranted() {
            return isPermissionGranted(Manifest.permission.ACCESS_WIFI_STATE);
        }

        public boolean isChangeWifiStateGranted() {
            return isPermissionGranted(Manifest.permission.CHANGE_WIFI_STATE);
        }

        public boolean isWakeLockGranted() {
            return isPermissionGranted(Manifest.permission.WAKE_LOCK);
        }

        public boolean isInternetGranted() {
            return isPermissionGranted(Manifest.permission.INTERNET);
        }

        public boolean isAccessNetorkStateGranted() {
            return isPermissionGranted(Manifest.permission.ACCESS_NETWORK_STATE);
        }

        public boolean isChanegNetworkStateGranted() {
            return isPermissionGranted(Manifest.permission.CHANGE_NETWORK_STATE);
        }

        public String getHumanReadablePermString(String permission){
            String hrps = permission;
            if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)){
                hrps = "Write external storage";
            } else if(Manifest.permission.ACCESS_WIFI_STATE.equals(permission)){
                hrps = "Access wifi state";
            } else if(Manifest.permission.CHANGE_WIFI_STATE.equals(permission)){
                hrps = "Change wifi state";
            } else if(Manifest.permission.WAKE_LOCK.equals(permission)){
                hrps = "Wake lock";
            } else if(Manifest.permission.INTERNET.equals(permission)){
                hrps = "Internet";
            } else if(Manifest.permission.ACCESS_NETWORK_STATE.equals(permission)){
                hrps = "Access network state";
            } else if(Manifest.permission.CHANGE_NETWORK_STATE.equals(permission)){
                hrps = "Change network state";
            }
            return hrps;
        }

        public void getPermissions(final Activity activity,
                                    final String[] missingPermissions,
                                    final int permissionReqCode,
                                    String negativeButtonCaption,
                                    DialogInterface.OnClickListener negativeButtonAction) {
            if (missingPermissions != null && missingPermissions.length > 0) {
                //Find the permissions that needs an explanation to be shown to the user
                boolean showDia = false;
                for (String perm : missingPermissions) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, perm)) {
                        showDia = true;
                        break;
                    }
                }

                if (showDia) {
                    //needs to show an explanation
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
                    alertBuilder.setTitle("Need permission");
                    StringBuilder permsReqrd = new StringBuilder();
                    permsReqrd.append(getHumanReadablePermString(missingPermissions[0]));
                    for (int i = 1; i < missingPermissions.length; i++) {
                        permsReqrd.append('\n');
                        permsReqrd.append(getHumanReadablePermString(missingPermissions[i]));
                    }
                    alertBuilder.setMessage(permsReqrd.toString());
                    alertBuilder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermissions(activity, missingPermissions, permissionReqCode);
                        }
                    });
                    if(negativeButtonCaption != null && negativeButtonAction != null) {
                        alertBuilder.setNegativeButton("No! Close App", negativeButtonAction);
                    }
                    alertBuilder.show();
                } else {
                    //directly request permissions
                    requestPermissions(activity, missingPermissions, permissionReqCode);
                }
            }
        }

        void requestPermissions(Activity activity, String[] perms, int permissionReqCode) {
            ActivityCompat.requestPermissions(activity, perms, permissionReqCode);
        }
    }

    private boolean isValidFileName(String fileName){
        if(fileName == null || fileName.isEmpty()){
            return false;
        }
        String part1 = fileName.substring(0, Math.max(1, fileName.lastIndexOf('.')));
        return !part1.isEmpty();
    }

    public File getTempFileToWrite(long connId, String fileName, int mediaType) throws IOException{
        if (!isValidFileName(fileName)) {
            return null;
        }
        File root = Environment.getExternalStorageDirectory();
        File subDir = new File(root.getCanonicalPath() + '/' + SharedPrefConstants.APP_NAME + '/' + "Temp");
        if(!subDir.exists()){
            subDir.mkdirs();
        }
        String tempFileName = getTempFileName(connId, fileName, mediaType);
        File file = new File(subDir.getCanonicalPath() + '/' + tempFileName);
        return file;
    }

    private String getTempFileName(long connId, String fileName, int mediaType){
        //<connid>_<mediatype>_<filename>.tmp
        return connId + '_' + mediaType + '_' + fileName + ".tmp";
    }

    public File getActualFileToWrite(String fileName, int fileType) throws IOException{
        if (!isValidFileName(fileName)) {
            return null;
        }
        File root = Environment.getExternalStorageDirectory();
        String subDirName = getSubDirName(fileType);
        File subDir = new File(root.getCanonicalPath() + '/' + SharedPrefConstants.APP_NAME + '/' + subDirName);
        if(!subDir.exists()){
            subDir.mkdirs();
        }
        return getUniqueFile(subDir, fileName);
    }

    private File getUniqueFile(File dir, String fileName) throws IOException{
        String extnRemovedFileName = removeExtension(fileName);
        String extn = getExtensionIncludingPeriod(fileName);
        String part1 = dir.getCanonicalPath() + '/' + extnRemovedFileName;
        File uniqueFile = new File(part1 + extn);
        //to avoid fileName conflict
        int count = 1;
        while(uniqueFile.exists()){
            String filePathTry = part1 + '(' + count + ')' + extn;
            uniqueFile = new File(filePathTry);
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

    public int getCurrentAppStatus(){
        return sharedPrefUtil.getCurrentAppStatus();
    }

    public void setCurrentAppStatus(int appStatus){
        sharedPrefUtil.setCurrentAppStatus(appStatus);
        sharedPrefUtil.commit();
        //command to show status notification. The notification util will decide whether to ot not to show the notification
        showStatusNotification(true);
    }

    public String getDefaultWifiPass(){
        return sharedPrefUtil.getDefaultWifiPass();
    }

    public String getUsername(){
        return sharedPrefUtil.getUserName();
    }

    public String getThisDeviceId(){
        return sharedPrefUtil.getThisDeviceId();
    }

    public void showStatusNotification(boolean show){
        if(show){
            notificationUtil.showToggleReceivingNotification();
        } else {
            notificationUtil.removeToggleNotification();
        }
    }

    public void showInsuffSpaceNotific(){

    }

    public void showNoExtMediaNotific(){

    }

    public void showTransferSuccessNotific(){

    }
}
