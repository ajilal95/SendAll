package com.aj.sendall.nw.comms;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.NewClientAvailable;
import com.aj.sendall.events.event.NewConnCreationFinished;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.nw.comms.abstr.AbstractServerConnDelegate;
import com.aj.sendall.ui.activity.ConnectionCreatorActivity;

import java.net.Socket;
import java.util.Date;


class NewConnCreationServerConnDelegate extends AbstractServerConnDelegate implements ConnectionCreatorActivity.Acceptable{
    private String otherUserName;
    private String otherDeviceId;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    NewConnCreationServerConnDelegate(Socket socket, AppController appController){
        super(socket, appController);
    }

    @Override
    public void postStreamSetup() {
        try {
            otherUserName = dataInputStream.readUTF();
            otherDeviceId = dataInputStream.readUTF();
            NewClientAvailable event = new NewClientAvailable();
            event.username = otherUserName;
            event.deviceId = otherDeviceId;
            event.acceptable = this;
            eventRouter.broadcast(event);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void accept() {
        new Thread(new Runnable() {
                @Override
                public void run() {
                    acceptConnComm();
                }
            }).start();
    }

    private void acceptConnComm() {
        String thisUserName = appController.getUsername();
        String thisDeviceId = appController.getThisDeviceId();
        try {
            dataOutputStream.writeUTF(thisUserName);
            dataOutputStream.writeUTF(thisDeviceId);
            dataOutputStream.flush();
            String status;

            String response = dataInputStream.readUTF();
            if(AppConsts.SUCCESS.equals(response)) {
                dataOutputStream.writeUTF(AppConsts.SUCCESS);
                dataOutputStream.flush();
                Connections connection = new Connections();
                connection.setConnectionName(otherUserName);
                connection.setSSID(otherDeviceId);
                connection.setLastContaced(new Date());
                appController.save(connection);
                status = AppConsts.SUCCESS;
            } else {
                dataOutputStream.writeUTF(AppConsts.FAILED);
                dataOutputStream.flush();
                status = AppConsts.FAILED;
            }

            NewConnCreationFinished event = new NewConnCreationFinished();
            event.status = status;
            event.username = otherUserName;
            event.deviceId = otherDeviceId;
            eventRouter.broadcast(event);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
