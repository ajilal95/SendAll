package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

public class ServerModeStarted implements EventRouter.Event {
    public int portNo = -1;
}
