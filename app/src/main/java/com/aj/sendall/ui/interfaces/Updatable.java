package com.aj.sendall.ui.interfaces;

import java.util.HashMap;
import java.util.Map;

public interface Updatable {

    void update(UpdateEvent updateEvent);

    class UpdateEvent{
        public Class<?> source;
        public Map<String, Object> data = new HashMap<>();
    }
}
