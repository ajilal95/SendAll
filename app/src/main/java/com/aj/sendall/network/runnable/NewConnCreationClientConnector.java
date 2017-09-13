package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.network.runnable.abstr.AbstractClientConnector;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

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
            event.data.put(Constants.ACTION, Constants.ACCEPT_CONN);
            event.data.put(SharedPrefConstants.USER_NAME, otherUserName);
            event.data.put(SharedPrefConstants.DEVICE_ID, otherDeviceId);
            event.data.put(UPDATE_CONST_SENDER, this);
            updatableActivity.update(event);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdate(Updatable.UpdateEvent updateEvent) {
        if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    acceptConnComm();
                }
            }).start();
        }
    }

    private void acceptConnComm() {
        String thisUserName = appManager.sharedPrefUtil.getUserName();
        String thisDeviceId = appManager.sharedPrefUtil.getThisDeviceId();
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
                event.data.put(Constants.ACTION, Constants.SUCCESS);
            } else {
                dataOutputStream.writeUTF(Constants.FAILED);
                dataOutputStream.flush();
                event.data.put(Constants.ACTION, Constants.FAILED);
            }

            event.data.put(SharedPrefConstants.USER_NAME, otherUserName);
            updatableActivity.update(event);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
