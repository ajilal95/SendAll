package com.aj.sendall.depndency.dagger;

import android.app.Application;
import android.content.Context;

import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;
import com.aj.sendall.ui.utils.CommonUiUtils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ajilal on 13/6/17.
 */

@Module
public class DModule {
    Application app;

    public DModule(Application app){
        this.app = app;
    }

    @Provides
    @Singleton
    Context provideContext(){
        return app;
    }
}
