package com.aj.sendall.events;


public interface EventRouter {
    <T extends Event> void subscribe(Class<T> eventType, Receiver<T> r);
    <T extends Event> void unsubscribe(Class<T> eventType, Receiver<T> r);
    void broadcast(Event ev);
    <T extends Event>void clearListeners(Class<T> clazz);


    interface Receiver<T extends Event> {
        void receive(T event);
    }

    interface Event {
    }
}
