package com.aj.sendall.nw.sockets;

import com.aj.sendall.events.EventRouter;
import com.aj.sendall.events.EventRouterFactory;
import com.aj.sendall.events.event.CloseAllSocketsCommand;
import com.aj.sendall.events.event.CloseServerSocketCommand;
import com.aj.sendall.events.event.ServerAcceptedConn;
import com.aj.sendall.events.event.ServerListeningForNewConn;
import com.aj.sendall.events.event.ServerSocketClosing;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketProxy extends ServerSocket {
    private EventRouter eventRouter = EventRouterFactory.getInstance();

    ServerSocketProxy(int portNo) throws IOException {
        super(portNo);
        eventRouter.subscribe(CloseServerSocketCommand.class, new EventRouter.Receiver<CloseServerSocketCommand>() {
            @Override
            public void receive(CloseServerSocketCommand event) {
                eventRouter.unsubscribe(CloseServerSocketCommand.class, this);
                eventRouter.broadcast(new CloseAllSocketsCommand());
                try {
                    close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public Socket accept() throws IOException {
        notifyListening();
        Socket s = super.accept();
        SocketProxy sp = new SocketProxy(s);
        notifyServerAccepted(sp);
        return sp;
    }

    private void notifyListening(){
        ServerListeningForNewConn event = new ServerListeningForNewConn();
        eventRouter.broadcast(event);
    }

    private void notifyServerAccepted(SocketProxy ms){
        ServerAcceptedConn event = new ServerAcceptedConn();
        event.acceptedSocket = ms;
        eventRouter.broadcast(event);
    }

    @Override
    public void close() throws IOException {
        notifyServerClosing();
        super.close();
    }

    private void notifyServerClosing(){
        eventRouter.broadcast(new ServerSocketClosing());
    }
}