package com.aj.sendall.network.runnable;

import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Created by ajilal on 10/7/17.
 */

public class NewConnCreationSender implements Runnable, Updatable {
    public static final String UPDATE_CONST_SENDER = "sender";
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private Updatable updatableActivity;
    private SharedPrefUtil sharedPrefUtil;

    NewConnCreationSender(Socket socket, Updatable updatableActivity, SharedPrefUtil sharedPrefUtil){
        this.socket = socket;
        this.updatableActivity = updatableActivity;
        this.sharedPrefUtil = sharedPrefUtil;
    }
    @Override
    public void run() {
        try {
            dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            String username = dataInputStream.readUTF();
            String idOfOtherDevice = dataInputStream.readUTF();

            UpdateEvent event = new UpdateEvent();

            event.source = this.getClass();

            event.data.put(SharedPrefConstants.USER_NAME, username);
            event.data.put(SharedPrefConstants.DEVICE_ID, idOfOtherDevice);
            event.data.put(UPDATE_CONST_SENDER, this);
            updatableActivity.update(event);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.ACCEPT_CONN.equals(updateEvent.data.get(Constants.ACTION))){
            String username = sharedPrefUtil.getUserName();
            String uniqueId = sharedPrefUtil.getThisDeviceId();
            try {
                dataOutputStream.writeUTF(username);
                dataOutputStream.writeUTF(uniqueId);
                dataOutputStream.flush();
            } catch (Exception e){
                e.printStackTrace();
            }
        } else if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){
            try {
                dataInputStream.close();
                dataOutputStream.close();
                socket.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
