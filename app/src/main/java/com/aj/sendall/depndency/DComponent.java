package com.aj.sendall.depndency;

import com.aj.sendall.broadcastreceiver.WifiStatusBroadcastReceiver;
import com.aj.sendall.services.FileTransferClientService;
import com.aj.sendall.services.FileTransferServerService;
import com.aj.sendall.services.NewConnCreationClientService;
import com.aj.sendall.services.NewConnCreationServerService;
import com.aj.sendall.services.ToggleReceiverService;
import com.aj.sendall.ui.activity.ConnectionCreatorActivity;
import com.aj.sendall.ui.activity.HomeActivity;
import com.aj.sendall.ui.activity.PersonalInteractionsActivity;
import com.aj.sendall.ui.fragment.ConnectionsFragment;
import com.aj.sendall.ui.fragment.GalleryFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = {DModule.class})
@Singleton
public interface DComponent {
    void inject(HomeActivity homeActivity);
    void inject(PersonalInteractionsActivity personalInteractionsActivity);
    void inject(ConnectionCreatorActivity connectionCreatorActivity);
    void inject(ConnectionsFragment connectionsFragment);
    void inject(GalleryFragment galleryFragment);

    void inject(WifiStatusBroadcastReceiver wifiStatusBroadcastReceiver);

    void inject(ToggleReceiverService toggleReceiverService);
    void inject(NewConnCreationClientService clientService);
    void inject(NewConnCreationServerService serverSevice);
    void inject(FileTransferClientService fileTransferClientService);
    void inject(FileTransferServerService fileTransferServerService);
}
