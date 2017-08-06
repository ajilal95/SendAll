package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.broadcastreceiver.abstr.AbstractGroupCreationListener;
import com.aj.sendall.network.services.NewConnCreationServerService;
import com.aj.sendall.network.utils.Constants;
import com.aj.sendall.ui.interfaces.Updatable;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class NewConnCreationGrpCreatnLstnr extends AbstractGroupCreationListener {
    private Updatable updatableActivity;

    public NewConnCreationGrpCreatnLstnr(AppManager appManager, Updatable updatableActivity){
        super(appManager);
        this.updatableActivity = updatableActivity;
    }

    @Override
    protected void onGroupInfoAvailable(final Context context, final String networkName, final String passPhrase, InetAddress grpOwnerAdd) {
        Map<String, String> recToAdv = new HashMap<>();
        recToAdv.put(Constants.ADV_KEY_NETWORK_NAME, networkName);
        recToAdv.put(Constants.ADV_KEY_NETWORK_PASSPHRASE, passPhrase);

        /*NewConnCreationServerService.connectorActivity = updatableActivity;
        NewConnCreationServerService.start(context);*/
    }
}


