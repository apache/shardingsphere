package com.saaavsaaa.client.section;

import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public abstract class Listener {
    private final String key;
    private String path;
    
    public Listener(){
        this(null);
    }
    public Listener(final String path){
        this.path = path;
        this.key = path + System.currentTimeMillis();
    }
    
    public abstract void process(WatchedEvent event);
    
    public String getPath() {
        return path;
    }
    
    public void setPath(final String path){
        this.path = path;
    }
    
    public String getKey() {
        return key;
    }
}
