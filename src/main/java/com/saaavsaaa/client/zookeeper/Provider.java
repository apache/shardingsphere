package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa on 18-5-3.
 */
public class Provider implements IProvider {
    protected final Client client;
    private final ZooKeeper zooKeeper;
    protected final boolean watched;
    protected final List<ACL> authorities;
    protected final String rootNode;
    
    Provider(final String rootNode, final Client client, final boolean watched, final List<ACL> authorities){
        this.rootNode = rootNode;
        this.client = client;
        this.zooKeeper = client.getZooKeeper();
        this.watched = watched;
        this.authorities = authorities;
    }
    
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return zooKeeper.getData(key, watched, null);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.getData(key, watched, callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(key, watched);
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(key, watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(key, watched);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        client.createNamespace();
        if (rootNode.equals(key)){
            return;
        }
        try {
            zooKeeper.create(key, value.getBytes(Constants.UTF_8), authorities, createMode);
        } catch (KeeperException.NoNodeException e) {
            // I don't know whether it will happen or not, if root watcher don't update rootExist timely
            if (e.getMessage().contains(key)) {
                System.out.println("rootExist : " + e.getMessage());
                Thread.sleep(50);
                this.createCurrentOnly(key, value, createMode);
            }
        }
    }

    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        zooKeeper.setData(key, value.getBytes(Constants.UTF_8), Constants.VERSION);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        zooKeeper.delete(key, Constants.VERSION);
        System.out.println("delete : " + key);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.delete(key, Constants.VERSION, callback, ctx);
    }
    
    
    @Override
    public String getRealPath(String path) {
        return PathUtil.getRealPath(rootNode, path);
    }
    
    @Override
    public List<String> getNecessaryPaths(final String key){
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        nodes.remove(rootNode);
        return nodes;
    }
    
    @Override
    public Stack<String> getDeletingPaths(String key) {
        return PathUtil.getPathReverseNodes(rootNode, key);
    }
    
    @Override
    public void executeContention(final LeaderElection election) throws KeeperException, InterruptedException {
        election.executeContention(this);
    }
    
    @Override
    public ZKTransaction transaction() {
        return new ZKTransaction(rootNode, zooKeeper);
    }
}
