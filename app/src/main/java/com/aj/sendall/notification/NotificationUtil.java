package com.aj.sendall.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v4.app.NotificationCompat;

import com.aj.sendall.R;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.services.ToggleReceiverService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NotificationUtil {
    private Context context;
    private AppController appController;

    @Inject
    NotificationUtil(Context context){
        this.context = context;
    }

    public void setAppController(AppController appController){
        this.appController = appController;
    }

    public void showToggleReceivingNotification(){
        if(!((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled()){
            removeToggleNotification();
            return;
        }

        //Do not show the notification of enable scanning while the application is sending
        Intent serviceIntent = new Intent(context, ToggleReceiverService.class);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context);
        int actionIcon;
        String actionMessage;
        String notificationTitle;
        if (appController.isSystemFree()) {
            actionIcon = R.drawable.toggle_rec_start;
            actionMessage = "Enable";
            notificationTitle = "Send All";
            serviceIntent.putExtra(ToggleReceiverService.ACTION, ToggleReceiverService.ACTION_START);
        } else {
            actionIcon = R.drawable.toggle_rec_stop;
            actionMessage = "Disable";
            String currentOp = (appController.isConnCreationClient() || appController.isConnCreationServer()) ?
                    "Creating connection" : "Transferring";
            notificationTitle = "Send All(" + currentOp + ')';
            serviceIntent.putExtra(ToggleReceiverService.ACTION, ToggleReceiverService.ACTION_STOP);
        }
        PendingIntent pendingServiceIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notiBuilder.setContentTitle(notificationTitle);
        notiBuilder.addAction(actionIcon, actionMessage, pendingServiceIntent);
        notiBuilder.setSmallIcon(R.drawable.toggle_rec_notif_icon);
        notiBuilder.setOngoing(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotifConsts.NOTIF_ID_TOGGLE_RECEIVING, notiBuilder.build());
    }

    public void removeToggleNotification(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotifConsts.NOTIF_ID_TOGGLE_RECEIVING);
    }
}
