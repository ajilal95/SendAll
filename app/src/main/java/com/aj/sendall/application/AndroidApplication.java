package com.aj.sendall.application;

import android.app.Application;
import android.content.Intent;

import com.aj.sendall.depndency.dagger.DComponent;
import com.aj.sendall.depndency.dagger.DModule;
import com.aj.sendall.depndency.dagger.DaggerDComponent;

/**
 * Created by ajilal on 14/6/17.
 */

public class AndroidApplication extends Application {
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
