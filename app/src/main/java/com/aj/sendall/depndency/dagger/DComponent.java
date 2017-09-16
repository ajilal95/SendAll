package com.aj.sendall.depndency.dagger;

import com.aj.sendall.network.broadcastreceiver.FileTransferGrpCreatnLstnr;
import com.aj.sendall.network.broadcastreceiver.WifiStatusBroadcastReceiver;
import com.aj.sendall.network.services.FileTransferClientService;
import com.aj.sendall.network.services.FileTransferServerService;
import com.aj.sendall.network.services.NewConnCreationClientService;
import com.aj.sendall.network.services.NewConnCreationServerService;
import com.aj.sendall.network.services.ToggleReceiverService;
import com.aj.sendall.ui.activity.Connector;
import com.aj.sendall.ui.activity.Home;
import com.aj.sendall.ui.activity.PersonalInteractionView;
import com.aj.sendall.ui.fragment.ConnectionsFragment;
import com.aj.sendall.ui.fragment.GalleryFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {DModule.class})
@Singleton
public interface DComponent {
    void inject(Home home);
    void inject(PersonalInteractionView personalInteractionView);
    void inject(Connector connector);
    void inject(ConnectionsFragment connectionsFragment);
    void inject(GalleryFragment galleryFragment);

    void inject(WifiStatusBroadcastReceiver wifiStatusBroadcastReceiver);
    void inject(FileTransferGrpCreatnLstnr fileTransferGrpCreatnLstnr);

    void inject(ToggleReceiverService toggleReceiverService);
    void inject(NewConnCreationClientService clientService);
    void inject(NewConnCreationServerService serverSevice);
    void inject(FileTransferClientService fileTransferClientService);
    void inject(FileTransferServerService fileTransferServerService);
}
