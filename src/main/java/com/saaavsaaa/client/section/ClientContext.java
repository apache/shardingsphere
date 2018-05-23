package com.saaavsaaa.client.section;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.retry.RetryPolicy;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;

/**
 * Created by aaa
 */
public final class ClientContext {
    private final RetryPolicy retryPolicy;
    private final BaseClientFactory clientFactory;
    private final IProvider provider;
    
    public ClientContext(final RetryPolicy retryPolicy, final BaseClientFactory clientFactory, final IProvider provider) {
        this.retryPolicy = retryPolicy;
        this.clientFactory = clientFactory;
        this.provider = provider;
    }
    
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
    
    public BaseClientFactory getClientFactory() {
        return clientFactory;
    }
    
    public IProvider getProvider() {
        return provider;
    }
}
