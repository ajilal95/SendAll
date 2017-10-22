package com.aj.sendall.events;


public class EventRouterFactory {
    private static EventRouter  er = new EventRouterImpl();
    public static EventRouter getInstance(){
        return er;
    }
}
