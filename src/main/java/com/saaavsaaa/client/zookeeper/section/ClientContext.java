package com.saaavsaaa.client.zookeeper.section;

import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;
import com.saaavsaaa.client.zookeeper.base.BaseContext;

/**
 * Created by aaa
 */
public final class ClientContext extends BaseContext {
    
    private DelayRetryPolicy delayRetryPolicy;
    private BaseClientFactory clientFactory;
    
    
    public ClientContext(final String servers, final int sessionTimeoutMilliseconds) {
        super();
        super.servers = servers;
        super.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    public void setDelayRetryPolicy(DelayRetryPolicy delayRetryPolicy) {
        this.delayRetryPolicy = delayRetryPolicy;
    }
    
    public void setClientFactory(BaseClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    
    public DelayRetryPolicy getDelayRetryPolicy() {
        return delayRetryPolicy;
    }
    
    public BaseClientFactory getClientFactory() {
        return clientFactory;
    }
    
    public void close() {
        super.close();
        this.delayRetryPolicy = null;
        this.clientFactory = null;
    }
    
    public void updateContext(final ClientContext context){
        this.delayRetryPolicy = context.getDelayRetryPolicy();
        this.clientFactory = context.clientFactory;
        this.watchers.clear();
        this.watchers.putAll(context.getWatchers());
    }
}
