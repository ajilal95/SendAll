package com.aj.sendall.application;

import android.app.Application;

import com.aj.sendall.depndency.DComponent;
import com.aj.sendall.depndency.DModule;
import com.aj.sendall.depndency.DaggerDComponent;

public class ThisApplication extends Application {
    private DComponent dComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        dComponent = DaggerDComponent.builder().dModule(new DModule(this)).build();
    }

    public DComponent getDaggerInjector(){
        return dComponent;
    }
}
