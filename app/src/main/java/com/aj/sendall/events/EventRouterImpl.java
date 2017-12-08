package com.aj.sendall.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class EventRouterImpl implements EventRouter {
    private Map<Class, Set<Receiver>> subscribers = new HashMap<>();

    @Override
    public <T extends Event> void subscribe(Class<T> eventType, Receiver<T> r) {
        if(eventType != null && r != null){
            Set<Receiver> rs = subscribers.get(eventType);
            if(rs == null){
                rs = new HashSet<>();
                subscribers.put(eventType, rs);
            }
            synchronized (rs) {
                rs.add(r);
            }
        }
    }

    @Override
    public <T extends Event> void unsubscribe(Class<T> eventType, Receiver<T> r) {
        if(r != null){
            Set<Receiver> rs = subscribers.get(eventType);
            if(rs != null) {
                synchronized (rs) {
                    rs.remove(r);
                }
            }
        }
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void broadcast(Event ev) {
        Set<Receiver> rsorg = subscribers.get(ev.getClass());
        if (rsorg != null) {
            synchronized (rsorg) {
                Set<Receiver> rs = new HashSet<>(rsorg);
                for (Receiver receiver : rs) {
                    receiver.receive(ev);
                }
            }
        }
    }

    @Override
    public <T extends Event> void clearListeners(Class<T> clazz) {
        subscribers.remove(clazz);
    }
}
