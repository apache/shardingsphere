package com.saaavsaaa.client;
/**
 * Created by aaa on 18-4-18.
 */

import com.saaavsaaa.client.untils.PathUtil;
import com.saaavsaaa.client.untils.StringUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

public class ZookeeperClient{
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    
    private final String servers;
    private final int sessionTimeOut;
    
    private String rootNode;
    private ZooKeeper zooKeeper;
    private List<ACL> authorities;
    
    ZookeeperClient(String servers, int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    void start() throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(servers, sessionTimeOut, connectWatcher());
        CONNECTED.await();
        /*if (StringUtil.isNullOrWhite(rootNode)){
            createRootNode();
        }*/
    }
    
    private void createRootNode() throws KeeperException, InterruptedException {
        if (checkExists(rootNode)){
            return;
        }
        zooKeeper.create(rootNode, new byte[0], authorities, CreateMode.PERSISTENT);
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
    
    public byte[] getData(String key) throws KeeperException, InterruptedException {
        return zooKeeper.getData(PathUtil.getRealPath(rootNode, key), false, null);
    }
    
    public boolean checkExists(String key) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(PathUtil.getRealPath(rootNode, key), false);
    }
    
    public List<String> getChildren(String key) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(PathUtil.getRealPath(rootNode, key), false);
    }
    
    public void createCurrentPathOnly(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        zooKeeper.create(PathUtil.getRealPath(rootNode, path), data, authorities, createMode);
    }
    
    public void createAllNeedPath(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        if (path.indexOf("/") < -1){
            this.createCurrentPathOnly(path, data, createMode);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        // sync cache
        Stack<String> pathStack = PathUtil.getPathNodes(path);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (!checkExists(node)){
                createInTransaction(path, data, createMode, transaction);
            }
        }
        transaction.commit();
    }
    
    private Transaction createInTransaction(String path, byte[] data, CreateMode createMode, Transaction transaction){
        return transaction.create(PathUtil.getRealPath(rootNode, path), data, authorities, createMode);
    }
    
    public void update(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.transaction().setData(PathUtil.getRealPath(rootNode, key), data, -1).commit();
    }
    
    public void setRootNode(String rootNode) {
        this.rootNode = rootNode;
    }
    
    public void setAuthorities(String scheme, byte[] auth) {
        zooKeeper.addAuthInfo(scheme , auth);
        this.authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
}
