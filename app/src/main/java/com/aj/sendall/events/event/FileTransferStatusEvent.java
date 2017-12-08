package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

public class FileTransferStatusEvent implements EventRouter.Event {
    public static final long COMPLETED = -10;
    public Object connectionId;
    public String fileName;
    public long totalTransferred;
}
