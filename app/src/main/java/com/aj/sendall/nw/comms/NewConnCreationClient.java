package com.aj.sendall.nw.comms;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.NewConnCreationFinished;
import com.aj.sendall.controller.AppConsts;
import com.aj.sendall.controller.AppController;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.nw.comms.abstr.AbstractClient;

import java.net.Socket;
import java.util.Date;


public class NewConnCreationClient extends AbstractClient {
    private String status;
    private String otherUserName = null;
    private String otherDeviceId = null;
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    public NewConnCreationClient(String SSID, String passPhrase, int serverPort,  AppController appController){
        super(SSID, passPhrase, serverPort, appController);
    }

    protected void configureSocket(Socket socket){
    }

    protected void communicate(){
        if(dataInputStream != null && dataOutputStream != null){
            try {
                String thisUserName = appController.getUsername();
                String thisDeviceId = appController.getThisDeviceId();

                dataOutputStream.writeUTF(thisUserName);
                dataOutputStream.writeUTF(thisDeviceId);
                dataOutputStream.flush();

                otherUserName = dataInputStream.readUTF();
                otherDeviceId = dataInputStream.readUTF();

                dataOutputStream.writeUTF(AppConsts.SUCCESS);//success
                dataOutputStream.flush();

                String response = dataInputStream.readUTF();
                if(AppConsts.SUCCESS.equals(response)){
                    Connections conn = new Connections();
                    conn.setConnectionName(otherUserName);
                    conn.setSSID(otherDeviceId);
                    conn.setLastContaced(new Date());

                    appController.saveToDB(conn);
                }
                status = AppConsts.SUCCESS;
            }catch (Exception e){
                status = AppConsts.FAILED;
                try {
                    dataOutputStream.writeUTF(AppConsts.FAILED);//failure
                    dataOutputStream.flush();
                } catch(Exception ex){
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    protected void finalAction(){
        NewConnCreationFinished event = new NewConnCreationFinished();
        event.status = status;
        event.username = otherUserName;
        event.deviceId = otherDeviceId;
        eventRouter.broadcast(event);
    }
}
