package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.section.Listener;

import java.io.IOException;

/**
 * Created by aaa
 */
public abstract class BaseClientFactory {
    protected BaseClient client;
    protected Listener globalListener;
    protected String namespace;
    protected String scheme;
    protected byte[] auth;
    
    protected String servers;
    protected int sessionTimeoutMilliseconds;
    
    public synchronized IClient start() throws IOException, InterruptedException {
        client.setClientFactory(this);
        client.setRootNode(namespace);
        client.setAuthorities(scheme , auth);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
}
