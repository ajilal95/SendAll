package com.aj.sendall.notification.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.aj.sendall.R;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
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
    SharedPrefUtil sharedPrefUtil;

    @Inject
    public NotificationUtil(Context context, SharedPrefUtil sharedPrefUtil){
        this.context = context;
        this.sharedPrefUtil = sharedPrefUtil;
    }

    public void showToggleReceivingNotification(){
        int currentAppStatus = sharedPrefUtil.getCurrentAppStatus();

        //Do not show the notification of enable scanning while the application is sending
        if((currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE || currentAppStatus == SharedPrefConstants.CURR_STATUS_RECEIVABLE)) {

            Intent serviceIntent = new Intent(context, ToggleReceiverService.class);
            PendingIntent pendingServiceIntent = PendingIntent.getService(context, 0, serviceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(context);
            int actionIcon;
            String actionMessage;
            String notificationTitle;
            if (currentAppStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
                actionIcon = R.drawable.toggle_rec_start;
                actionMessage = "Enable";
                notificationTitle = "Send All(Inactive)";
            } else {
                actionIcon = R.drawable.toggle_rec_stop;
                actionMessage = "Disable";
                notificationTitle = "Send All(Active)";
            }
            notiBuilder.setContentTitle(notificationTitle);
            notiBuilder.addAction(actionIcon, actionMessage, pendingServiceIntent);
            notiBuilder.setSmallIcon(R.drawable.toggle_rec_notif_icon);
            notiBuilder.setOngoing(true);

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NotifConsts.NOTIF_ID_TOGGLE_RECEIVING, notiBuilder.build());
        } else {
            removeToggleNotification();
        }
    }

    public void removeToggleNotification(){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(NotifConsts.NOTIF_ID_TOGGLE_RECEIVING);
    }
}
