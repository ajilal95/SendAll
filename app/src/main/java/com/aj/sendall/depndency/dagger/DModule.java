package com.aj.sendall.depndency.dagger;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.utils.LocalWifiManager;
import com.aj.sendall.notification.util.NotificationUtil;
import com.aj.sendall.ui.utils.CommonUiUtils;

import java.io.IOException;
import java.net.ServerSocket;

import javax.inject.Named;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ajilal on 13/6/17.
 */

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
    @Named(value = NAME_WIFI_GROUP_HANDLER)
    Handler provideWifiGroupHandler(){
        return new Handler();
    }
}
