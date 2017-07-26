package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.services.ConnCreationServerService;
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
        String grpOwnerIp = grpOwnerAdd.getHostAddress();
        Map<String, String> recToAdv = new HashMap<>();
        recToAdv.put(ConnCreationServerService.NET, networkName);
        recToAdv.put(ConnCreationServerService.PASS, passPhrase);
        recToAdv.put(ConnCreationServerService.GRP_OWN_ADD, grpOwnerIp);

        ConnCreationServerService.connectorActivity = updatableActivity;
        ConnCreationServerService.start(context, recToAdv);
    }
}


