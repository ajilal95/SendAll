package com.aj.sendall.notification.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.aj.sendall.R;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.services.ToggleReceiverService;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 18/5/17.
 */

@Singleton
public class NotificationUtil {
    Context context;

    @Inject
    public NotificationUtil(Context context){
        this.context = context;
    }

    public void showToggleReceivingNotification(){
        Intent serviceIntent = new Intent(context, ToggleReceiverService.class);
        PendingIntent pendingServiceIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context);
        notiBuilder.setContentTitle("Send all");
        boolean isCurrentlyActive = SharedPrefUtil.getCurrentReceivingStatus(context);
        if(isCurrentlyActive) {
            notiBuilder.setContentText("Stop Send all receiving");
        } else {
            notiBuilder.setContentText("Start Send all receiving");
        }
        notiBuilder.setSmallIcon(R.mipmap.def_media_thumb);
        notiBuilder.setContentIntent(pendingServiceIntent);
        notiBuilder.setOngoing(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NotifConsts.NOTIF_ID_TOGGLE_RECEIVING, notiBuilder.build());
    }
}
