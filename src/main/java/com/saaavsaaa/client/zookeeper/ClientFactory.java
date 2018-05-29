package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import com.saaavsaaa.client.zookeeper.section.Listener;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by aaa
 */
public class ClientFactory extends BaseClientFactory {
    //    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
    
    public ClientFactory(){}
    
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.context = new ClientContext(servers, sessionTimeoutMilliseconds);
        client = new UsualClient(context);
        logger.debug("new usual client");
        return this;
    }
    
    /*
    * used for create new clients through a existing client
    * this client is not perhaps the client
    */
    public synchronized BaseClientFactory newClientByOriginal(boolean closeOriginal) {
        IClient oldClient = this.client;
        client = new UsualClient(context);
        if (closeOriginal){
            oldClient.close();
        }
        logger.debug("new usual client by a existing client");
        return this;
    }
    
    /*
    * partially prepared products
    */
    public ClientFactory newCacheClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.context = new ClientContext(servers, sessionTimeoutMilliseconds);
        client = new CacheClient(context);
        logger.debug("new cache client");
        return this;
    }
    
    public ClientFactory watch(final Listener listener){
        globalListener = listener;
        return this;
    }
    
    public ClientFactory setNamespace(String namespace) {
        if (!namespace.startsWith(Constants.PATH_SEPARATOR)){
            namespace = Constants.PATH_SEPARATOR + namespace;
        }
        this.namespace = namespace;
        return this;
    }
    
    public ClientFactory authorization(final String scheme, final byte[] auth){
        if (scheme == null || scheme.trim().length() == 0) {
            return this;
        }
        this.scheme = scheme;
        this.auth = auth;
        return this;
    }
    
    public ClientFactory setRetryPolicy(final DelayRetryPolicy delayRetryPolicy){
        ((ClientContext)context).setDelayRetryPolicy(delayRetryPolicy);
        return this;
    }
    
    @Override
    public IClient start() throws IOException, InterruptedException {
        ((ClientContext)context).setClientFactory(this);
        return super.start();
    }
}
