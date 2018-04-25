package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.Listener;
import com.saaavsaaa.client.untils.PathUtil;
import com.saaavsaaa.client.untils.Properties;
import com.saaavsaaa.client.untils.StringUtil;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa on 18-4-19.
 */
public abstract class BaseClient {
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    protected static final Map<String, Watcher> watchers = new ConcurrentHashMap<>();
    private boolean watchRegistered = false;
    
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
    
    ZooKeeper getZooKeeper(){
        return zooKeeper;
    }
    
    private Watcher connectWatcher() {
        return new Watcher(){
            public void process(WatchedEvent event) {
                if(Event.KeeperState.SyncConnected == event.getState()){
                    if(Event.EventType.None == event.getType()){
                        CONNECTED.countDown();
                    }
                }
                // key == rootNode signify that register a watcher without appoint path when client init
                // or want to watch the whole namespace
                if (watchers.containsKey(rootNode)){
                    watchers.get(rootNode).process(event);
                }
                if (Properties.WATCH_ON && watchers.containsKey(event.getPath())){
                     watchers.get(event.getPath()).process(event);
                }
            }
        };
    }
    
    void registerWatch(final Listener listener){
        if (watchRegistered){
            return;
        }
        watchRegistered = true;
        watchers.put(rootNode, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                listener.process(event);
            }
        });
    }
    
    public Watcher registerWatch(final String key, final Listener listener){
        String path = PathUtil.getRealPath(rootNode, key);
//        listener.setKey(path);
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                listener.process(event);
            }
        };
        watchers.put(path, watcher);
        return watcher;
    }
    
    public void unregisterWatch(final String key){
        if (StringUtil.isNullOrBlank(key)){
            throw new IllegalArgumentException("key should not be blank");
        }
        String path = PathUtil.getRealPath(rootNode, key);
        if (watchers.containsKey(path)){
            watchers.remove(path);
        }
    }
    
    public void close() throws InterruptedException {
        zooKeeper.close();
    }
    
    void setRootNode(String rootNode) {
        this.rootNode = rootNode;
    }
    
    void setAuthorities(String scheme, byte[] auth) {
        zooKeeper.addAuthInfo(scheme , auth);
        this.authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
}
