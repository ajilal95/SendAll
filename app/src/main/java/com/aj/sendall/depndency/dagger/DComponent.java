package com.aj.sendall.depndency.dagger;

import com.aj.sendall.network.broadcastreceiver.BroadcastReceiverForSender;
import com.aj.sendall.network.broadcastreceiver.WifiStatusBroadcastReceiver;
import com.aj.sendall.network.runnable.Server;
import com.aj.sendall.network.services.ToggleReceiverService;
import com.aj.sendall.ui.activity.Home;
import com.aj.sendall.ui.activity.PersonalInteractionView;
import com.aj.sendall.ui.fragment.ConnectionsFragment;
import com.aj.sendall.ui.fragment.GalleryFragment;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ajilal on 13/6/17.
 */

@Component(modules = {DModule.class})
@Singleton
public interface DComponent {
    void inject(Home home);
    void inject(ConnectionsFragment connectionsFragment);
    void inject(GalleryFragment galleryFragment);
    void inject(ToggleReceiverService toggleReceiverService);
    void inject(PersonalInteractionView personalInteractionView);
    void inject(WifiStatusBroadcastReceiver wifiStatusBroadcastReceiver);
    void inject(BroadcastReceiverForSender broadcastReceiverForSender);
    void inject(Server server);
}
