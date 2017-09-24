package com.aj.sendall.network.monitor;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SocketSystem implements Monitor {
    private static SocketSystem INSTANCE = new SocketSystem();

    static final String CREATED_SOCKET = "created-socket";
    static final String CLOSING_SOCKET = "closed-socket";
    static final String ACTION_START_ACCEPTING = "server-accepting";
    static final String ACTION_SERVER_ACCEPTED = "server-accepted";
    static final String ACTION_SERVER_CLOSING = "server-closing";
    static final String ACTION_SOCKET_CLOSING = "socket-closing";

    private String lastReportedServerAction;

    private boolean serverRunning = false;
    private boolean clientRunning = true;
    private ServerIdleTimeMonitor serverIdleTimeMonitor;

    private static final Object lock = new Object();

    private List<WeakReference<Updatable>> serverCloseListeners = null;
    private Map<Socket, List<WeakReference<Updatable>>> socketCloseListeners = null;

    private List<Runnable> serverModeStartListeners = null;
    private List<Runnable> serverModeStopListeners = null;
    private List<Runnable> clientModeStartListeners = null;
    private List<Runnable> clientModeStopListeners = null;

    private ServerSocket serverSocket = null;
    private List<Socket> acceptedSockets = null;
    private List<Socket> clientSockets = null;

    private SocketSystem(){
    }

    public void clear(){
        if(idle()) {
            socketCloseListeners = null;
            serverCloseListeners = null;

            serverSocket = null;
            acceptedSockets = null;
            clientSockets = null;
        }
    }

    @Override
    public void update(UpdateEvent ev) {
        synchronized (lock) {
            String action = (String) ev.action;
            if(inServerMode()) {
                if (ACTION_SERVER_ACCEPTED.equals(action)) {
                    lastReportedServerAction = action;
                    stopIdleTimeMonitor();
                    Socket acceptedSocket = (Socket) ev.getExtra(CREATED_SOCKET);
                    if (acceptedSockets == null) {
                        acceptedSockets = new ArrayList<>();
                    }
                    acceptedSockets.add(acceptedSocket);
                } else if (ACTION_SOCKET_CLOSING.equals(action)) {
                    UpdateEvent e = getSocketClosingUpdateEvent();
                    notifySocketClosing(e, (Socket) ev.getExtra(CLOSING_SOCKET));
                    clear();
                } else if (ACTION_START_ACCEPTING.equals(action)) {
                    lastReportedServerAction = action;
                    startIdleTimeMonitor();
                } else if (ACTION_SERVER_CLOSING.equals(action)) {
                    lastReportedServerAction = action;
                    notifyServerClosing(ev);
                    clear();
                    serverRunning = false;
                }
            } else if(inClientMode()){
                if (ACTION_SOCKET_CLOSING.equals(action)) {
                    UpdateEvent e = getSocketClosingUpdateEvent();
                    notifySocketClosing(e, (Socket) ev.getExtra(CLOSING_SOCKET));
                    clear();
                }
            }

        }
    }

    public void setServerAcceptWaitTimer(long maxWaitTime){
        if(maxWaitTime > 0) {
            serverIdleTimeMonitor = new ServerIdleTimeMonitor(serverSocket, maxWaitTime);
            if(ACTION_START_ACCEPTING.equals(lastReportedServerAction)){
                startIdleTimeMonitor();
            }
        }
    }

    private void startIdleTimeMonitor(){
        if(serverIdleTimeMonitor != null){
            serverIdleTimeMonitor.start();
        }
    }

    private void stopIdleTimeMonitor(){
        if(serverIdleTimeMonitor != null){
            serverIdleTimeMonitor.notifyServerAccepted();
            serverIdleTimeMonitor = null;
        }
    }

    private void notifyServerClosing(UpdateEvent ev){
        if(serverCloseListeners != null){
            List<WeakReference<Updatable>> listeners = new ArrayList<>(serverCloseListeners);//to avoid concurrent modification
            ev.source = this.getClass();
            for(WeakReference<Updatable> wu : listeners){
                Updatable u = wu.get();
                if(u != null){
                    u.update(ev);
                }
            }
        }

        //close the accepted sockets
        if(acceptedSockets != null){
            UpdateEvent scue = getSocketClosingUpdateEvent();
            List<Socket> sockets = new ArrayList<>(acceptedSockets);//to avoid concurrent modification exception
            for(Socket s : sockets){
                try {
                    s.close();
                } catch(IOException e){
                    //In case of exception, listeners are not notified automatically
                    notifySocketClosing(scue, s);
                }
            }
        }
    }

    private void notifySocketClosing(UpdateEvent ev, Socket s) {
        if(socketCloseListeners != null){
            Map<Socket, List<WeakReference<Updatable>>> listeners = new HashMap<>(socketCloseListeners);//to avoid concurrent modification
            List<WeakReference<Updatable>> sockListnr = listeners.get(s);
            if(sockListnr != null) {
                for (WeakReference<Updatable> wu : sockListnr) {
                    Updatable u = wu.get();
                    if (u != null) {
                        u.update(ev);
                    }
                }
            }
            socketCloseListeners.remove(s);
        }
        if(acceptedSockets != null){
            acceptedSockets.remove(s);
            if(acceptedSockets.isEmpty()){
                acceptedSockets = null;//cleaning up
            }
        }
        if(clientSockets != null){
            clientSockets.remove(s);
            if(clientSockets.isEmpty()){
                clientSockets = null;//cleaning up
            }
        }
    }

    @NonNull
    private UpdateEvent getSocketClosingUpdateEvent() {
        UpdateEvent socketClosed = new UpdateEvent();
        socketClosed.source = this.getClass();
        socketClosed.action = ACTION_SOCKET_CLOSING;
        return socketClosed;
    }

    public ServerSocket startServerMode(int port) throws IOException, IllegalStateException{
        synchronized (lock) {
            if(!idle()) {
                throwModeException();
            }
            serverSocket = new MonitoredServerSocket(port, this);
            serverRunning = true;
            notifyModeListeners(serverModeStartListeners);
            return serverSocket;
        }
    }

    public int getServerPortNo(){
        if(serverSocket != null){
            return serverSocket.getLocalPort();
        }
        return -1;
    }

    public void stopServerMode(){
        synchronized (lock) {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                serverRunning = false;
                notifyModeListeners(serverModeStopListeners);
                clear();
            }
        }
    }

    public ServerSocket getCurrentServerSocket(){
        return serverSocket;
    }

    public boolean inClientMode(){
        return clientRunning;
    }

    public boolean inServerMode(){
        return serverRunning;
    }

    public boolean idle(){
        return !serverRunning && !clientRunning;
    }

    public boolean startClientMode() throws IllegalStateException{
        synchronized (lock) {
            if (idle()) {
                clientRunning = true;
                notifyModeListeners(clientModeStartListeners);
                return true;
            } else {
                throwModeException();
            }
            return false;
        }
    }

    public void stopClientMode(){
        synchronized (lock) {
            clientRunning = false;
            stopAllClients();
            notifyModeListeners(clientModeStopListeners);
            clear();
        }
    }

    private void notifyModeListeners(List<Runnable> modeListener) {
        if(modeListener != null){
            List<Runnable> dupli = new ArrayList<>(modeListener);//to avoid concurrent modification exception
            boolean dirty = false;
            for(Runnable r : dupli){
                if(r != null){
                    r.run();
                } else {
                    dirty = true;
                }
            }

            if(dirty){
                cleanModeListener(modeListener);
            }
        }
    }

    private void cleanModeListener(List<Runnable> modeListener){
        if(modeListener != null) {
            List<Runnable> dupli = new ArrayList<>(modeListener);
            for(Runnable r : dupli){
                if(r == null){
                    modeListener.remove(r);
                }
            }
        }
    }

    private void stopAllClients(){
        if(clientSockets != null){
            for(Socket s : clientSockets){
                try {
                    s.close();
                } catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    public Socket createClientSocket(InetAddress add, int port) throws IOException, IllegalStateException{
        if(!inClientMode()){
            return throwModeException();
        }
        Socket s = new MonitoredSocket(new Socket(add, port), this);
        if(clientSockets == null){
            clientSockets = new ArrayList<>();
        }
        clientSockets.add(s);
        return s;
    }

    private Socket throwModeException() throws IllegalStateException{
        throw new IllegalStateException("System running in " + (inServerMode() ? "server" : "client") + " mode");
    }

    public void addServerCloseListener(Updatable listener){
        if(serverCloseListeners == null){
            serverCloseListeners = new LinkedList<>();
        }
        serverCloseListeners.add(new WeakReference<>(listener));
    }

    public void addSocketCloseListener(Socket socket, Updatable listener){
        if(socketCloseListeners == null){
            socketCloseListeners = new HashMap<>();
        }
        List<WeakReference<Updatable>> listenerList = socketCloseListeners.get(socket);
        if(listenerList == null){
            listenerList = new LinkedList<>();
            socketCloseListeners.put(socket, listenerList);
        }
        listenerList.add(new WeakReference<>(listener));
    }

    public void addServerModeStartListener(Runnable listener){
        if(serverModeStartListeners == null){
            serverModeStartListeners = new LinkedList<>();
        }
        serverModeStartListeners.add(listener);
    }

    public void addServerModeStopListener(Runnable listener){
        if(serverModeStopListeners == null){
            serverModeStopListeners = new LinkedList<>();
        }
        serverModeStopListeners.add(listener);
    }

    public void addClientModeStartListener(Runnable listener){
        if(clientModeStartListeners == null){
            clientModeStartListeners = new LinkedList<>();
        }
        clientModeStartListeners.add(listener);
    }

    public void addClientModeStopListener(Runnable listener){
        if(clientModeStopListeners == null){
            clientModeStopListeners = new LinkedList<>();
        }
        clientModeStopListeners.add(listener);
    }
    public void removeServerModeStartListener(Runnable listener){
        if(serverModeStartListeners == null){
            serverModeStartListeners.remove(listener);
        }
    }

    public void removeServerModeStopListener(Runnable listener){
        if(serverModeStopListeners == null){
            serverModeStopListeners.remove(listener);
        }
    }

    public void removeClientModeStartListener(Runnable listener){
        if(clientModeStartListeners == null){
            clientModeStartListeners.remove(listener);
        }
    }

    public void removeClientModeStopListener(Runnable listener){
        if(clientModeStopListeners == null){
            clientModeStopListeners.remove(listener);
        }
    }

    public static SocketSystem getInstance(){
        return INSTANCE;
    }

    private class ServerIdleTimeMonitor extends Thread{
        private long maxIdleTime;
        private ServerSocket serverSocket;
        private Thread idleThread;

        ServerIdleTimeMonitor(ServerSocket serverSocket, long maxIdleTime){
            this.serverSocket = serverSocket;
            this.maxIdleTime = maxIdleTime;
        }

        @Override
        public void run() {
            try {
                this.idleThread = Thread.currentThread();
                Thread.sleep(maxIdleTime);
                serverSocket.close();
            } catch (InterruptedException | IOException e){
                e.printStackTrace();
            }
        }

        void notifyServerAccepted(){
            try {
                idleThread.interrupt();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

class MonitoredServerSocket extends ServerSocket implements Monitored{
    private Monitor monitor;

    MonitoredServerSocket(int portNo, Monitor monitor) throws IOException {
        super(portNo);
        this.monitor = monitor;
    }

    private Updatable.UpdateEvent getAbstractEvent(){
        Updatable.UpdateEvent ev = new Updatable.UpdateEvent();
        ev.source = this.getClass();
        return ev;
    }

    @Override
    public Socket accept() throws IOException {
        notifyGoingToAccept();
        Socket s = super.accept();
        MonitoredSocket ms = new MonitoredSocket(s, monitor);
        notifyServerAccepted(ms);
        return ms;
    }

    private void notifyGoingToAccept(){
        Updatable.UpdateEvent ev = getAbstractEvent();
        ev.action = SocketSystem.ACTION_START_ACCEPTING;
        notifyMonitor(ev);
    }

    private void notifyServerAccepted(MonitoredSocket ms){
        Updatable.UpdateEvent ev = getAbstractEvent();
        ev.action = SocketSystem.ACTION_SERVER_ACCEPTED;
        ev.putExtra(SocketSystem.CREATED_SOCKET, ms);
        notifyMonitor(ev);
    }

    @Override
    public void close() throws IOException {
        notifyServerClosing();
        super.close();
        monitor = null;//no more communication
    }

    private void notifyServerClosing(){
        Updatable.UpdateEvent ev = getAbstractEvent();
        ev.action = SocketSystem.ACTION_SERVER_CLOSING;
        notifyMonitor(ev);
    }

    @Override
    public void notifyMonitor(Updatable.UpdateEvent event) {
        if(monitor != null){
            monitor.update(event);
        }
    }
}

class MonitoredSocket extends Socket implements Monitored{
    private Socket s;
    private Monitor monitor;

    MonitoredSocket(Socket socket, Monitor monitor){
        this.s = socket;
        this.monitor = monitor;
    }

    @Override
    public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        s.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    @Override
    public boolean isOutputShutdown() {
        return s.isOutputShutdown();
    }

    @Override
    public boolean isInputShutdown() {
        return s.isInputShutdown();
    }

    @Override
    public boolean isClosed() {
        return s.isClosed();
    }

    @Override
    public boolean isBound() {
        return s.isBound();
    }

    @Override
    public boolean isConnected() {
        return s.isConnected();
    }

    @Override
    public String toString() {
        return s.toString();
    }

    @Override
    public void shutdownOutput() throws IOException {
        s.shutdownOutput();
    }

    @Override
    public void shutdownInput() throws IOException {
        s.shutdownInput();
    }

    @Override
    public synchronized void close() throws IOException {
        Updatable.UpdateEvent event = new Updatable.UpdateEvent();
        event.source = this.getClass();
        event.action = SocketSystem.ACTION_SOCKET_CLOSING;
        event.putExtra(SocketSystem.CLOSING_SOCKET, this);
        notifyMonitor(event);
        s.close();
        monitor = null; //no more communication
    }

    @Override
    public boolean getReuseAddress() throws SocketException {
        return s.getReuseAddress();
    }

    @Override
    public void setReuseAddress(boolean on) throws SocketException {
        s.setReuseAddress(on);
    }

    @Override
    public int getTrafficClass() throws SocketException {
        return s.getTrafficClass();
    }

    @Override
    public void setTrafficClass(int tc) throws SocketException {
        s.setTrafficClass(tc);
    }

    @Override
    public boolean getKeepAlive() throws SocketException {
        return s.getKeepAlive();
    }

    @Override
    public void setKeepAlive(boolean on) throws SocketException {
        s.setKeepAlive(on);
    }

    @Override
    public synchronized int getReceiveBufferSize() throws SocketException {
        return s.getReceiveBufferSize();
    }

    @Override
    public synchronized void setReceiveBufferSize(int size) throws SocketException {
        s.setReceiveBufferSize(size);
    }

    @Override
    public synchronized int getSendBufferSize() throws SocketException {
        return s.getSendBufferSize();
    }

    @Override
    public synchronized void setSendBufferSize(int size) throws SocketException {
        s.setSendBufferSize(size);
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
        return s.getSoTimeout();
    }

    @Override
    public synchronized void setSoTimeout(int timeout) throws SocketException {
        s.setSoTimeout(timeout);
    }

    @Override
    public boolean getOOBInline() throws SocketException {
        return s.getOOBInline();
    }

    @Override
    public void setOOBInline(boolean on) throws SocketException {
        s.setOOBInline(on);
    }

    @Override
    public void sendUrgentData(int data) throws IOException {
        s.sendUrgentData(data);
    }

    @Override
    public int getSoLinger() throws SocketException {
        return s.getSoLinger();
    }

    @Override
    public void setSoLinger(boolean on, int linger) throws SocketException {
        s.setSoLinger(on, linger);
    }

    @Override
    public boolean getTcpNoDelay() throws SocketException {
        return s.getTcpNoDelay();
    }

    @Override
    public void setTcpNoDelay(boolean on) throws SocketException {
        s.setTcpNoDelay(on);
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return s.getOutputStream();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return s.getInputStream();
    }

    @Override
    public SocketChannel getChannel() {
        return s.getChannel();
    }

    @Override
    public SocketAddress getLocalSocketAddress() {
        return s.getLocalSocketAddress();
    }

    @Override
    public SocketAddress getRemoteSocketAddress() {
        return s.getRemoteSocketAddress();
    }

    @Override
    public int getLocalPort() {
        return s.getLocalPort();
    }

    @Override
    public int getPort() {
        return s.getPort();
    }

    @Override
    public InetAddress getLocalAddress() {
        return s.getLocalAddress();
    }

    @Override
    public InetAddress getInetAddress() {
        return s.getInetAddress();
    }

    @Override
    public void bind(SocketAddress bindpoint) throws IOException {
        s.bind(bindpoint);
    }

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        s.connect(endpoint, timeout);
    }

    @Override
    public void connect(SocketAddress endpoint) throws IOException {
        s.connect(endpoint);
    }

    @Override
    public void notifyMonitor(Updatable.UpdateEvent event) {
        if(monitor != null){
            monitor.update(event);
        }
    }
}
