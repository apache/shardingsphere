package com.saaavsaaa.client;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa on 18-4-18.
 */
public class ClientFactory {
    
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    
    private ClientFactory(){}
    
    public static void main(String[] args) throws Exception{
        ClientFactory creator = new ClientFactory();
        creator.newClient("192.168.2.44:2181", 20000).setNamespace("test").authorization("digest", "digest".getBytes());
    }
    
    ZookeeperClient newClient(final String serverAddrs, final int sessionTimeoutMilliseconds) throws IOException, InterruptedException {
        ZookeeperClient client = new ZookeeperClient(serverAddrs, sessionTimeoutMilliseconds);
        client.start(connectWatcher());
        CONNECTED.wait();
        return client;
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
