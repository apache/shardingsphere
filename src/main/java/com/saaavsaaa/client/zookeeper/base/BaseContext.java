package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.zookeeper.section.Listener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa
 */
public abstract class BaseContext {
    protected String servers;
    protected int sessionTimeOut;
    protected String scheme;
    protected byte[] auth;
    protected Listener globalListener;
    protected final Map<String, Listener> watchers = new ConcurrentHashMap<>();
    
    public String getServers() {
        return servers;
    }
    
    public int getSessionTimeOut() {
        return sessionTimeOut;
    }
    
    public String getScheme() {
        return scheme;
    }
    
    public byte[] getAuth() {
        return auth;
    }
    
    public Map<String, Listener> getWatchers(){
        return watchers;
    }
    
    public void close() {
        this.watchers.clear();
    }
}
