package com.aj.sendall.network.broadcastreceiver;

import android.content.Context;
import android.content.Intent;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.services.AfterGrpCreatedForNewConnService;
import com.aj.sendall.ui.interfaces.Updatable;

public class NewConnCreationGrpCreatnLstnr extends AbstractGroupCreationListener {
    private Updatable updatableActivity;

    public NewConnCreationGrpCreatnLstnr(AppManager appManager, Updatable updatableActivity){
        super(appManager);
        this.updatableActivity = updatableActivity;
    }

    @Override
    protected void onGroupInfoAvailable(final Context context, final String networkName, final String passPhrase) {
        Intent intent = new Intent(context, AfterGrpCreatedForNewConnService.class);
        intent.putExtra(AfterGrpCreatedForNewConnService.NET, networkName);
        intent.putExtra(AfterGrpCreatedForNewConnService.PASS, passPhrase);
        context.startService(intent);
    }
}


