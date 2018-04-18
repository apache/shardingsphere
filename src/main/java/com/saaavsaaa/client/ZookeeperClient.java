package com.saaavsaaa.client;
/**
 * Created by aaa on 18-4-18.
 */

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZookeeperClient{
    public static final String PATH_SEPARATOR = "/";
    
    private final String servers;
    private final int sessionTimeOut;
    
    private String rootNode;
    private ZooKeeper zooKeeper;
    private List<ACL> authorities;
    
    ZookeeperClient(String servers, int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    void start(final Watcher watcher) throws IOException, InterruptedException {
        zooKeeper = new ZooKeeper(servers, sessionTimeOut, watcher);
    }
    
    public ZookeeperClient setNamespace(String namespace) {
        if (!namespace.startsWith("/")){
            namespace = "/" + namespace;
        }
        this.rootNode = namespace;
        return this;
    }
    
    public void authorization(String scheme, byte[] auth){
        if (scheme == null || scheme.trim().length() == 0) {
            return;
        }
        zooKeeper.addAuthInfo(scheme , auth);
        authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
    
    public byte[] getData(String key) throws KeeperException, InterruptedException {
        return zooKeeper.getData(getRealPath(key), false, null);
    }
    
    private String getRealPath(String path){
        if (path.equals(rootNode)){
            return new StringBuilder().append(PATH_SEPARATOR).append(rootNode).toString();
        }
        return new StringBuilder().append(PATH_SEPARATOR).append(rootNode).append(PATH_SEPARATOR).append(path).toString();
    }
    
    public boolean checkExists(String key) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(getRealPath(key), false);
    }
    
    public List<String> getChildren(String key) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(getRealPath(key), false);
    }
    
    public void create(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        // TODO: 18-4-12
        if (path.indexOf("/") > -1){
            System.out.println("exist / need op ..=============================================");
        }
        zooKeeper.create(path, data, authorities, createMode);
    }
    
    private void createRootNode() throws KeeperException, InterruptedException {
        if (checkExists(rootNode)){
            return;
        }
        zooKeeper.create(getRealPath(rootNode), new byte[0], authorities, CreateMode.PERSISTENT);
    }
    
    public void update(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.transaction().setData(getRealPath(key), data, -1).commit();
    }
}
