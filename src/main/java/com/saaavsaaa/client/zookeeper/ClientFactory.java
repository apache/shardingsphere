package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.retry.AsyncRetryCenter;
import com.saaavsaaa.client.retry.DelayRetry;
import com.saaavsaaa.client.section.Listener;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.base.BaseClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public class ClientFactory extends BaseClientFactory {
    //    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    private static final Logger logger = LoggerFactory.getLogger(ClientFactory.class);
    
    public ClientFactory(){}
    
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
        client = new UsualClient(servers, sessionTimeoutMilliseconds);
        logger.debug("new usual client");
        return this;
    }
    
    /*
    * used for create new clients through a existing client
    */
    ClientFactory newClient() {
        client = new UsualClient(servers, sessionTimeoutMilliseconds);
        logger.debug("new usual client by a existing client");
        return this;
    }
    
    public ClientFactory newCacheClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
        client = new CacheClient(servers, sessionTimeoutMilliseconds);
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
    
    public ClientFactory setRetryPolicy(final DelayRetry retrial){
        AsyncRetryCenter.INSTANCE.init(retrial);
        return this;
    }
}
