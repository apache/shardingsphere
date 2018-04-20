package com.saaavsaaa.client.zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa on 18-4-19.
 */
public abstract class BaseClient {
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    public static final int VERSION = -1;
    
    private final String servers;
    private final int sessionTimeOut;
    protected ZooKeeper zooKeeper;
    
    protected String rootNode = "/InitValue";
    protected List<ACL> authorities;
    
    protected BaseClient(String servers, int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    public void start() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(servers, sessionTimeOut, connectWatcher());
        CONNECTED.await();
    }
    
    private Watcher connectWatcher() {
        return new Watcher(){
            public void process(WatchedEvent event) {
                if(Event.KeeperState.SyncConnected == event.getState()){
                    if(Event.EventType.None == event.getType()){
                        CONNECTED.countDown();
                    }
                }
            }
        };
    }
    
    void setRootNode(String rootNode) {
        this.rootNode = rootNode;
    }
    
    void setAuthorities(String scheme, byte[] auth) {
        zooKeeper.addAuthInfo(scheme , auth);
        this.authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
}
