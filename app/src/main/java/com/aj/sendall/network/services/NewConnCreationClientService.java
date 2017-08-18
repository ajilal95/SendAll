package com.aj.sendall.network.services;

import com.aj.sendall.application.AndroidApplication;
import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.services.abstr.AbstractClientService;

import javax.inject.Inject;

public class NewConnCreationClientService extends AbstractClientService {
    @Inject
    public AppManager appManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ((AndroidApplication) getApplication()).getDaggerInjector().inject(this);
    }

    public NewConnCreationClientService() {
        super("ConnCreationClientService");
    }

    @Override
    protected AppManager getAppManager(){
        return appManager;
    }
}
