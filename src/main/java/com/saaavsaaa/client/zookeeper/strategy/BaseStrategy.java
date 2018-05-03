package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IStrategy;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.Provider;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * Created by aaa on 18-5-2.
 */
public class BaseStrategy implements IStrategy {
    protected final Provider provider;
    public BaseStrategy(final Provider provider){
        this.provider = provider;
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return provider.getData(key);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.getData(key, callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return provider.checkExists(key);
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return provider.checkExists(key, watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return provider.getChildren(key);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        provider.createCurrentOnly(key, value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        provider.update(key, value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        provider.deleteOnlyCurrent(key);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.deleteOnlyCurrent(key, callback, ctx);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            try {
//                this.deleteAllChildren(nodes.get(i));
                if (i == nodes.size() - 1){
                    this.createCurrentOnly(nodes.get(i), value, createMode);
                } else {
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
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            this.deleteOnlyCurrent(key);
        }catch (KeeperException.NotEmptyException ee){
            List<String> children;
            try{
                children = this.getChildren(key);
            } catch (KeeperException.NoNodeException e) {
                // someone else has deleted the node since we checked
                return;
            }
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
    
    @Override
    public ZKTransaction transaction() {
        return provider.transaction();
    }
}
