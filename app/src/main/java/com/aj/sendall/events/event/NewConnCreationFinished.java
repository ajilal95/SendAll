package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

public class NewConnCreationFinished implements EventRouter.Event {
    public String status;
    public String username;
    public String deviceId;
}
