package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;

import java.io.IOException;

/**
 * Created by aaa
 */
public class ClientFactory {
//    private static final String CLIENT_EXCLUSIVE_NODE = "ZKC";
    
    private Client client;
    private Listener globalListener;
    private String namespace;
    private String scheme;
    private byte[] auth;
    private String servers;
    private int sessionTimeoutMilliseconds;
    
    public ClientFactory(){}
    
    public ClientFactory newUsualClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeoutMilliseconds = sessionTimeoutMilliseconds;
        client = new UsualClient(servers, sessionTimeoutMilliseconds);
        return this;
    }
    
    ClientFactory newUsualClient() {
        client = new UsualClient(servers, sessionTimeoutMilliseconds);
        return this;
    }
    
    public ClientFactory watch(final Listener listener){
        globalListener = listener;
        return this;
    }
    
    public synchronized Client start() throws IOException, InterruptedException {
        client.setClientFactory(this);
        client.setRootNode(namespace);
        client.setAuthorities(scheme , auth);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
    
    public ClientFactory setNamespace(String namespace) {
        if (!namespace.startsWith(Constants.PATH_SEPARATOR)){
            namespace = Constants.PATH_SEPARATOR + namespace;
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
