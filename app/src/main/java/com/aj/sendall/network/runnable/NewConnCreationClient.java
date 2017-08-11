package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.db.model.Connections;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.util.Date;


public class NewConnCreationClient extends AbstractClient {

    public NewConnCreationClient(String SSID, String passPhrase, int serverPort, Updatable activity, AppManager appManager){
        super(SSID, passPhrase, serverPort, activity, appManager);
    }

    protected void communicate(){
        if(dataInputStream != null && dataOutputStream != null){
            try {
                String thisUserName = appManager.sharedPrefUtil.getUserName();
                String thisDeviceId = appManager.sharedPrefUtil.getThisDeviceId();

                dataOutputStream.writeUTF(thisUserName);
                dataOutputStream.writeUTF(thisDeviceId);
                dataOutputStream.flush();

                String otherUserName = dataInputStream.readUTF();
                String otherDeviceId = dataInputStream.readUTF();

                Connections conn = new Connections();
                conn.setConnectionName(otherUserName);
                conn.setSSID(otherDeviceId);
                conn.setLastContaced(new Date());

                appManager.dbUtil.saveConnection(conn);

                dataOutputStream.writeBoolean(true);//success
                dataOutputStream.flush();

                UpdateEvent event = new UpdateEvent();
                event.source = this.getClass();
                event.data.put(Constants.ACTION, Constants.SUCCESS);
                updatableActivity.update(event);
            }catch (Exception e){
                try {
                    dataOutputStream.writeBoolean(false);//failure
                    dataOutputStream.flush();
                } catch(Exception ex){
                    ex.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }
}
