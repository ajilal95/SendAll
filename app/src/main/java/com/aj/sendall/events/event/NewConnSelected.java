package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.db.dto.ConnectionViewData;

public class NewConnSelected implements EventRouter.Event{
    public ConnectionViewData selectedConn;
}
