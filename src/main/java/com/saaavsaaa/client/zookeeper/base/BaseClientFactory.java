package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * Created by aaa
 */
public abstract class BaseClientFactory {
    protected BaseClient client;
    protected Listener globalListener;
    protected String namespace;
    protected String scheme;
    protected byte[] auth;
    protected List<ACL> authorities;
    protected BaseContext context;
    
    public IClient start() throws IOException, InterruptedException {
        client.setRootNode(namespace);
        if(scheme == null) {
            authorities = OPEN_ACL_UNSAFE;
        }
        client.setAuthorities(scheme , auth, authorities);
        client.start();
        if (globalListener != null) {
            client.registerWatch(globalListener);
        }
        return client;
    }
}
