package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.retry.RetryPolicy;
import com.saaavsaaa.client.section.ClientContext;
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
    protected RetryPolicy retryPolicy;
    
    protected String servers;
    protected int sessionTimeoutMilliseconds;
    
    public synchronized IClient start() throws IOException, InterruptedException {
        client.setContext(new ClientContext(retryPolicy, this)); // wait expand
        client.setRootNode(namespace);
        client.setAuthorities(scheme , auth);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
}
