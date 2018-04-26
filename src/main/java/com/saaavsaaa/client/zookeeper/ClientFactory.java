package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.Listener;
import org.apache.zookeeper.Watcher;

import java.io.IOException;

/**
 * Created by aaa on 18-4-18.
 */
public class ClientFactory {
//    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    
    private Client client;
    private Listener globalListener;
    private String namespace;
    private String scheme;
    private byte[] auth;
    
    public ClientFactory(){}
    
    public ClientFactory newCacheClient(final String servers, final int sessionTimeoutMilliseconds) {
        client = new CacheClient(servers, sessionTimeoutMilliseconds);
        return this;
    }

    public ClientFactory newUsualClient(final String servers, final int sessionTimeoutMilliseconds) {
        client = new UsualClient(servers, sessionTimeoutMilliseconds);
        return this;
    }
    
    public ClientFactory watch(final Listener listener){
        globalListener = listener;
        return this;
    }
    
    public synchronized Client start() throws IOException, InterruptedException {
        client.setRootNode(namespace);
        client.start();
        client.setAuthorities(scheme , auth);
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
    
    public ClientFactory setNamespace(String namespace) {
        if (!namespace.startsWith("/")){
            namespace = "/" + namespace;
        }
        this.namespace = namespace;
        return this;
    }
    
    public ClientFactory authorization(String scheme, byte[] auth){
        if (scheme == null || scheme.trim().length() == 0) {
            return this;
        }
        this.scheme = scheme;
        this.auth = auth;
        return this;
    }
}
