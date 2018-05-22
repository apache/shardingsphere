package com.saaavsaaa.client.section;

import com.saaavsaaa.client.retry.RetryPolicy;

/**
 * Created by aaa
 */
public final class ClientContext {
    private final RetryPolicy retryPolicy;
    
    public ClientContext(final RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }
    
    public ClientContext() {
        retryPolicy = null;
    }
    
    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }
}
