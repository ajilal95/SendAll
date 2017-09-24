package com.aj.sendall.depndency.dagger;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DModule {
    public static final String NAME_WIFI_GROUP_HANDLER = "wifi_group_handler";
    Application app;

    public DModule(Application app){
        this.app = app;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return app;
    }

    @Provides
    Handler provideWifiGroupHandler(){
        return new Handler();
    }
}
