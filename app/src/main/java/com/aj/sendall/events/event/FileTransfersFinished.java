package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

public class FileTransfersFinished implements EventRouter.Event {
    public String status;
    public String deviceId;
}
