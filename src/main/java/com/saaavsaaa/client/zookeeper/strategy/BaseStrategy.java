package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Created by aaa on 18-5-2.
 */
public class BaseStrategy implements IStrategy {
    protected final ZooKeeper zooKeeper;
    protected final boolean watched;
    protected final List<ACL> authorities;
    protected final String rootNode;
    
    public BaseStrategy(final String rootNode, final ZooKeeper zooKeeper, final boolean watched, final List<ACL> authorities){
        this.rootNode = rootNode;
        this.zooKeeper = zooKeeper;
        this.watched = watched;
        this.authorities = authorities;
    }
    
    @Override
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
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(key, value, createMode);
            return;
        }
        
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        nodes.remove(rootNode);
        for (int i = 0; i < nodes.size(); i++) {
            try {
                if (i == nodes.size() - 1){
                    this.createCurrentOnly(nodes.get(i), value, createMode);
                } else {
//                    this.deleteAllChildren(nodes.get(i));
                    this.createCurrentOnly(nodes.get(i), Constants.NOTHING_VALUE, createMode);
                }
                System.out.println("not exist and create:" + nodes.get(i));
            } catch (KeeperException.NodeExistsException ee){
                System.out.println("exist:" + nodes.get(i));
                continue;
            }
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        zooKeeper.setData(key, value.getBytes(Constants.UTF_8), Constants.VERSION);
    }
    
    @Override
    public void updateWithCheck(final String key, final String value) throws KeeperException, InterruptedException {
        zooKeeper.transaction().check(key, Constants.VERSION).setData(key, value.getBytes(Constants.UTF_8), Constants.VERSION).commit();
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
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            this.deleteOnlyCurrent(key);
        }catch (KeeperException.NotEmptyException ee){
            List<String> children = this.getChildren(key);
            for (String child : children) {
                child = key + Constants.PATH_SEPARATOR + child;
                this.deleteAllChildren(child);
            }
            this.deleteOnlyCurrent(key);
        } catch (KeeperException.NoNodeException ee){
            System.out.println(ee.getMessage());
            return;
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        this.deleteAllChildren(key);
        String superPath = key.substring(0, key.lastIndexOf(Constants.PATH_SEPARATOR));
        try {
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException ee){
            return;
        }
    }
    
    private void deleteRecursively(final String path) throws KeeperException, InterruptedException {
        int index = path.lastIndexOf(Constants.PATH_SEPARATOR);
        if (index < 0){
            return;
        }
        String superPath = path.substring(0, index);
        try {
            this.deleteOnlyCurrent(path);
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException ee){
            List<String> children = this.getChildren(path);
            children.forEach((c) -> System.out.println(path + " exist other children " + c));
            return;
        }
    }
}
