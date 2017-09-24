package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.network.monitor.Updatable;

import java.net.Socket;
import java.util.Date;


public class NewConnCreationClientConnector extends AbstractClientConnector {
    public static final String UPDATE_CONST_SENDER = "sender";
    private String otherUserName;
    private String otherDeviceId;

    NewConnCreationClientConnector(Socket socket, Updatable updatableActivity, AppManager appManager){
        super(socket, updatableActivity, appManager);
    }

    @Override
    public void postStreamSetup() {
        try {
            otherUserName = dataInputStream.readUTF();
            otherDeviceId = dataInputStream.readUTF();

            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();
            event.action = Constants.ACCEPT_CONN;
            event.putExtra(SharedPrefConstants.USER_NAME, otherUserName);
            event.putExtra(SharedPrefConstants.DEVICE_ID, otherDeviceId);
            event.putExtra(UPDATE_CONST_SENDER, this);
            updatableActivity.update(event);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Updatable.UpdateEvent updateEvent) {
        if(Constants.ACCEPT_CONN.equals(updateEvent.action)){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    acceptConnComm();
                }
            }).start();
        }
    }

    private void acceptConnComm() {
        String thisUserName = appManager.getUsername();
        String thisDeviceId = appManager.getThisDeviceId();
        try {
            dataOutputStream.writeUTF(thisUserName);
            dataOutputStream.writeUTF(thisDeviceId);
            dataOutputStream.flush();

            UpdateEvent event = new UpdateEvent();
            event.source = this.getClass();

            String response = dataInputStream.readUTF();
            if(Constants.SUCCESS.equals(response)) {
                dataOutputStream.writeUTF(Constants.SUCCESS);
                dataOutputStream.flush();
                Connections connection = new Connections();
                connection.setConnectionName(otherUserName);
                connection.setSSID(otherDeviceId);
                connection.setLastContaced(new Date());
                appManager.dbUtil.saveOrUpdate(connection);
                event.action = Constants.SUCCESS;
            } else {
                dataOutputStream.writeUTF(Constants.FAILED);
                dataOutputStream.flush();
                event.action = Constants.FAILED;
            }

            event.putExtra(SharedPrefConstants.USER_NAME, otherUserName);
            updatableActivity.update(event);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
