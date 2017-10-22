package com.aj.sendall.events.event;

import com.aj.sendall.events.EventRouter;

import java.net.Socket;

public class SocketClosing implements EventRouter.Event {
    public Socket closingSocket;
}
