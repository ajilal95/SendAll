package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.ui.activity.ConnectionCreatorActivity;

public class NewClientAvailable implements EventRouter.Event {
    public String username;
    public String deviceId;
    public ConnectionCreatorActivity.Acceptable acceptable;
}
