package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.retry.DelayRetryPolicy;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;
import com.saaavsaaa.client.zookeeper.section.ClientContext;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by aaa
 */
public class ClientFactory extends BaseClientFactory {
    //    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
    private DelayRetryPolicy delayRetryPolicy;
    
    public ClientFactory(){}
    
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        int wait = sessionTimeoutMilliseconds;
        if (sessionTimeoutMilliseconds == 0){
            wait = Constants.WAIT;
        }
        this.context = new ClientContext(servers, wait);
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
    
    public ClientFactory authorization(final String scheme, final byte[] auth, final List<ACL> authorities){
        this.scheme = scheme;
        this.auth = auth;
        this.authorities = authorities;
        return this;
    }
    
    public ClientFactory setRetryPolicy(final DelayRetryPolicy delayRetryPolicy){
        this.delayRetryPolicy = delayRetryPolicy;
        return this;
    }
    
    @Override
    public IClient start() throws IOException, InterruptedException {
        ((ClientContext)context).setDelayRetryPolicy(delayRetryPolicy);
        ((ClientContext)context).setClientFactory(this);
        return super.start();
    }
}
