package com.saaavsaaa.client.untils;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa on 18-4-23.
 */
public abstract class Listener {
    private String key = null;
    public abstract void process(WatchedEvent event);
    
    public String getKey() {
        return key;
    }
    
    public void setKey(String key) {
        this.key = key;
    }
}
