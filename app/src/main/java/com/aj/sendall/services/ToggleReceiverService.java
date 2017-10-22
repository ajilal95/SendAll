package com.aj.sendall.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.aj.sendall.application.ThisApplication;
import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.FileTransfersFinished;
import com.aj.sendall.events.event.SendallNetsAvailable;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

public class ToggleReceiverService extends IntentService {
    public static final String ACTION = "action";
    public static final String ACTION_START = "action-startReceivingFiles";
    public static final String ACTION_STOP = "action-stop";

    @Inject
    public AppController appController;
    private static SendallNetAvailabilityReceiver sendallNetAvailabilityReceiver;

    public ToggleReceiverService() {
        super("ToggleReceiverService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((ThisApplication)getApplication()).getDaggerInjector().inject(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getStringExtra(ACTION);
        if(ACTION_START.equals(action)){
            startReceivingFiles(appController);
        } else if(ACTION_STOP.equals(action)){
            stopReceivingFiles(appController);
        }
    }

    private static void stopReceivingFiles(AppController appController) {
        stopCurrentScanning();
        appController.setSystemIdle();
    }

    private static void startReceivingFiles(AppController appController) {
        stopCurrentScanning();
        sendallNetAvailabilityReceiver = new SendallNetAvailabilityReceiver(appController);
        sendallNetAvailabilityReceiver.subscribeEvents();
        appController.scanForFileTransferServer();
    }

    private static void stopCurrentScanning() {
        if(sendallNetAvailabilityReceiver != null) {
            sendallNetAvailabilityReceiver.unsubscribeEvents();
            sendallNetAvailabilityReceiver = null;
        }
    }

    private static class SendallNetAvailabilityReceiver implements EventRouter.Receiver<SendallNetsAvailable>{
        private EventRouter eventRouter = EventRouterFactory.getInstance();
        private AppController appController;
        private FileTransferStatusReceiver fileTransferStatusReceiver = new FileTransferStatusReceiver();
        private List<String> contactedSSIDs = new LinkedList<>();//to keep track of already connected devices in this receive attempt

        private SendallNetAvailabilityReceiver(AppController appController){
            this.appController = appController;
        }

        @Override
        public void receive(SendallNetsAvailable event) {
            this.unsubscribeEvents();
            //the update from wifi scanner
            List<String> result = event.availableSSIDs;
            /*Start with the first connection available which is not contacted yet.
            * Start client for that SSID. If file is available then receive it. If
            * no not contacted connections are available then stop for a while and restart scanning.
            * An update must be got from the Client System when its operation has finished
            * On receiving that update try connecting to another SSID iff wifi is on*/
            String sr = getNextConn(result);
            if (sr != null) {
                fileTransferStatusReceiver.subscribeEvents();
                fileTransferStatusReceiver.setCurrenConnectedSSID(sr);
                appController.connectToFileTransferServer(sr);
            } else {
                try {
                    Thread.sleep(3000);//wait 3 seconds
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //now retry scanning if wifi is still enabled
                restartScanning();
            }

        }

        void restartScanning() {
            if(appController.isWifiEnabled()){
                this.subscribeEvents();
                appController.scanForFileTransferServer();
            }
        }

        private String getNextConn(List<String> results){
            for(String sr : results){
                if(!contactedSSIDs.contains(sr)){
                    return sr;
                }
            }
            return null;
        }

        void addToContctedSSIDs(String ssid){
            contactedSSIDs.add(ssid);
        }

        private void unsubscribeEvents(){
            eventRouter.unsubscribe(SendallNetsAvailable.class, this);
            fileTransferStatusReceiver.unsubscribeEvents();
        }

        private void subscribeEvents(){
            eventRouter.subscribe(SendallNetsAvailable.class, this);
        }


        private class FileTransferStatusReceiver implements EventRouter.Receiver<FileTransfersFinished>{
            private String currenConnectedSSID;

            private FileTransferStatusReceiver(){
            }

            @Override
            public void receive(FileTransfersFinished event) {
                String clientStatus = event.status;
                if(AppConsts.FILE_TRANSFER_FAILED_AUTH_ERR.equals(clientStatus)){
                    //no need to connect to this device again
                    addToContctedSSIDs(currenConnectedSSID);
                } else if(AppConsts.FILE_TRANSFER_SUCCESS.equals(clientStatus)){
                    //file transferred. No need to go further
                    ToggleReceiverService.stopReceivingFiles(appController);
                    return;
                } else if(AppConsts.FILE_TRANSFER_FAILED_IN_SUFF_SPACE.equals(clientStatus)){
                    appController.notifyInsuffSpace();
                    return;
                } else if(AppConsts.FILE_TRANSFER_FAILED_NO_EXT_MEDIA.equals(clientStatus)){
                    appController.notifyNoExtMedia();
                    return;
                } else if(AppConsts.FILE_TRANSFER_FAILED_FILE_IO_ERR.equals(clientStatus) || AppConsts.FILE_TRANSFER_FAILED_NET_IO_ERR.equals(clientStatus)){
                    //ignore. This case will be handled on the reconnect cycle
                    Log.d(this.getClass().getSimpleName(), "Transfer failed with File/Net IO error");
                }

                //No transfer happened with this client. Go for the next one
                this.unsubscribeEvents();
                restartScanning();
            }

            void subscribeEvents(){
                eventRouter.subscribe(FileTransfersFinished.class, this);
            }

            void unsubscribeEvents(){
                eventRouter.unsubscribe(FileTransfersFinished.class, this);
            }

            void setCurrenConnectedSSID(String ssid){
                currenConnectedSSID = ssid;
            }
        }
    }

}
