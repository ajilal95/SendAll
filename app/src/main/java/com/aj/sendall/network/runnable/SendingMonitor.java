package com.aj.sendall.network.runnable;

import android.os.Handler;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;

import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ajilal on 2/7/17.
 */

public class SendingMonitor implements Runnable {
    private final long TIMEOUT = 5000;
    private Set<Socket> activeSocketsReference;
    private Handler handler;
    private SharedPrefUtil sharedPrefUtil;

    public SendingMonitor(Handler handler, Set<Socket> activeSocketsReference, SharedPrefUtil sharedPrefUtil){
        this.handler = handler;
        this.activeSocketsReference = activeSocketsReference;
        this.sharedPrefUtil = sharedPrefUtil;
    }

    @Override
    public void run() {
        Set<Socket> inactiveSockets = new HashSet<>();
        for(Socket socket : activeSocketsReference){
            if(socket.isClosed()){
                inactiveSockets.add(socket);
            }
        }

        activeSocketsReference.removeAll(inactiveSockets);

        if(activeSocketsReference.isEmpty()){
            terminateGroup();
        } else {
            handler.postDelayed(this, TIMEOUT);
        }
    }

    private void terminateGroup(){
        //TODO code to terminate the group created

        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
        sharedPrefUtil.commit();
    }
}
