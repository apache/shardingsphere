package com.saaavsaaa.client;

import java.io.IOException;

/**
 * Created by aaa on 18-4-18.
 */
public class ClientFactory {
    private ZookeeperClient client;
    private String namespace;
    private String scheme;
    private byte[] auth;
    
    public ClientFactory(){}
    
    public static void main(String[] args) throws Exception{
        ClientFactory creator = new ClientFactory();
        ZookeeperClient client = creator.setNamespace("test").authorization("digest", "digest".getBytes()).newClient("192.168.2.44:2181", 20000).start();
    }
    
    public ClientFactory newClient(final String servers, final int sessionTimeoutMilliseconds) {
        client = new ZookeeperClient(servers, sessionTimeoutMilliseconds);
        return this;
    }
    
    public synchronized ZookeeperClient start() throws IOException, InterruptedException {
        client.start();
        client.setRootNode(namespace);
        client.setAuthorities(scheme , auth);
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
