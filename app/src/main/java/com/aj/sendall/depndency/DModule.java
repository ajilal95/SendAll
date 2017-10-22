package com.aj.sendall.depndency;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class DModule {
    private Application app;

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
