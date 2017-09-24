package com.aj.sendall.network.monitor;

import java.util.HashMap;
import java.util.Map;

public interface Updatable {

    void update(UpdateEvent updateEvent);

    class UpdateEvent{
        public Object action;
        public Class<?> source;
        private Map<String, Object> extra;

        public Object getExtra(String key){
            if(extra != null){
                return extra.get(key);
            }
            return null;
        }

        public void putExtra(String key, Object value){
            if(extra == null){
                extra = new HashMap<>();
            }
            extra.put(key, value);
        }
    }
}
