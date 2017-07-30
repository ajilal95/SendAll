package com.aj.sendall.network.runnable;

import android.net.wifi.WifiConfiguration;

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

                UpdateEvent event = new UpdateEvent();
                event.source = this.getClass();
                event.data.put(Constants.ACTION, Constants.SUCCESS);
                updatableActivity.update(event);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            closeSocket();
        }
    }
}
