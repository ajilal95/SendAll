package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

import java.util.List;

public class SendallNetsAvailable implements EventRouter.Event {
    public List<String> availableSSIDs;
}
