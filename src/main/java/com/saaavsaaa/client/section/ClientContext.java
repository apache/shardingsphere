package com.saaavsaaa.client.section;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by aaa
 */
public final class ClientContext {
    private static final Map<String, Listener> watchers = new ConcurrentHashMap<>();
    private DelayRetryPolicy delayRetryPolicy;
    private BaseClientFactory clientFactory;
    private IProvider provider;
    
    
    public ClientContext(final DelayRetryPolicy delayRetryPolicy, final BaseClientFactory clientFactory) {
        this(delayRetryPolicy, clientFactory, null);
    }
    
    public ClientContext(final DelayRetryPolicy delayRetryPolicy, final BaseClientFactory clientFactory, final IProvider provider) {
        this.delayRetryPolicy = delayRetryPolicy;
        this.clientFactory = clientFactory;
        this.provider = provider;
    }
    
    public DelayRetryPolicy getDelayRetryPolicy() {
        return delayRetryPolicy;
    }
    
    public BaseClientFactory getClientFactory() {
        return clientFactory;
    }
    
    public IProvider getProvider() {
        return provider;
    }
    public void setProvider(IProvider provider){
        this.provider = provider;
    }
    
    public Map<String, Listener> getWatchers(){
        return watchers;
    }
    
    public void close() {
        this.delayRetryPolicy = null;
        this.clientFactory = null;
        this.provider = null;
    }
}
