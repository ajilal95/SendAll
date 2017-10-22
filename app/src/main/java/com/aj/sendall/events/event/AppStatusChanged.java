package com.aj.sendall.events.event;

import com.aj.sendall.controller.AppStatus;
import com.aj.sendall.events.EventRouter;

public class AppStatusChanged implements EventRouter.Event {
    public AppStatus newStatus;
}
