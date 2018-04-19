package com.saaavsaaa.client;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa on 18-4-19.
 */
public class BaseClient {
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    
    private final String servers;
    private final int sessionTimeOut;
    protected ZooKeeper zooKeeper;
    
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
}
