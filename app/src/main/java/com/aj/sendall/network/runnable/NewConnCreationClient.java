package com.aj.sendall.network.runnable;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

/**
 * Created by ajilal on 20/7/17.
 */

public class NewConnCreationClient implements Runnable, Updatable {
    private String SSID;
    private String passPhrase;
    private Updatable activity;
    private AppManager appManager;

    public NewConnCreationClient(String SSID, String passPhrase, Updatable activity, AppManager appManager){
        this.SSID = SSID;
        this.passPhrase = passPhrase;
        this.activity = activity;
        this.appManager = appManager;
    }

    @Override
    public void run() {

    }

    @Override
    public void update(UpdateEvent updateEvent) {
        if(Constants.CLOSE_SOCKET.equals(updateEvent.data.get(Constants.ACTION))){

        }
    }
}
