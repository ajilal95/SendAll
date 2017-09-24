package com.aj.sendall.network.monitor;

interface Monitored {
    void notifyMonitor(Updatable.UpdateEvent event);
}
