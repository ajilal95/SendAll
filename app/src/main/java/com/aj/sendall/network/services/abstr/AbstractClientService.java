package com.aj.sendall.network.services.abstr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.aj.sendall.application.AppManager;
import com.aj.sendall.network.monitor.SocketSystem;
import com.aj.sendall.network.monitor.Updatable;
import com.aj.sendall.network.runnable.abstr.AbstractClient;
import com.aj.sendall.network.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractClientService extends IntentService {
    private static final String ACTION_START_NEW = "com.aj.sendall.network.services.NewConnCreationClientService.START_NEW";
    private static final String ACTION_STOP = "com.aj.sendall.network.services.NewConnCreationClientService.STOP";


    private final static List<AbstractClient> clients = new ArrayList<>();
    private AppManager appManager;
    private static AbstractClient client;

    private static SocketSystem socketSystem = null;

    public AbstractClientService(String service){
        super(service);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.appManager = getAppManager();
        if(socketSystem == null){
            socketSystem = SocketSystem.getInstance();
            socketSystem.addClientModeStartListener(new Runnable() {
                @Override
                public void run() {
                    appManager.setCurrentAppStatus(getAppStatusOnCreatingClient());
                }
            });
        }
    }

    protected abstract AppManager getAppManager();

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            synchronized (clients) {
                final String action = intent.getAction();
                if (ACTION_START_NEW.equals(action)) {
                    if(socketSystem.idle()) {
                        socketSystem.startClientMode();
                        if (client != null) {
                            clients.add(client);
                            new Handler().post(client);
                            client = null;
                        }
                    }
                } else if(ACTION_STOP.equals(action)){
                    Updatable.UpdateEvent event = new Updatable.UpdateEvent();
                    event.action = Constants.CLOSE_SOCKET;
                    for(AbstractClient client : clients){
                        client.update(event);
                    }
                    clients.clear();
                    socketSystem.stopClientMode();
                }
            }
        }
    }

    abstract protected int getAppStatusOnCreatingClient();


    protected static void start(Context context, AbstractClient client, Class<?> serviceClass) {
        synchronized (clients) {
            AbstractClientService.client = client;
            Intent intent = new Intent(context, serviceClass);
            intent.setAction(ACTION_START_NEW);
            context.startService(intent);
        }
    }

    protected static void stop(Context context, Class<?> serviceClass){
        synchronized (clients) {
            Intent intent = new Intent(context, serviceClass);
            intent.setAction(ACTION_STOP);
            context.startService(intent);
        }
    }
}
