package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IStrategy;
import com.saaavsaaa.client.utility.PathUtil;
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
public class UsualStrategy extends BaseStrategy {
    public UsualStrategy(final Provider provider){
        super(provider);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return provider.getData(provider.getRealPath(key));
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.getData(provider.getRealPath(key), callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return provider.checkExists(provider.getRealPath(key));
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return provider.checkExists(provider.getRealPath(key), watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return provider.getChildren(provider.getRealPath(key));
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        provider.createCurrentOnly(provider.getRealPath(key), value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        provider.update(provider.getRealPath(key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        provider.deleteOnlyCurrent(provider.getRealPath(key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.deleteOnlyCurrent(provider.getRealPath(key), callback, ctx);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(key, value, createMode);
            return;
        }
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
        this.deleteChildren(provider.getRealPath(key), true);
    }
    
    private void deleteChildren(final String path, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children;
        try{
            children = provider.getChildren(path);
        } catch (KeeperException.NoNodeException e){ // someone else has deleted the node since we checked
            System.out.println(e.getMessage());
            return;
        }
        for (String child : children){
            child = PathUtil.getRealPath(path, child);
            this.deleteAllChildren(child);
        }
        if (deleteCurrentNode){
            try{
                this.deleteOnlyCurrent(path);
            } catch(KeeperException.NotEmptyException e){ //someone has created a new child since we checked ... delete again.
                deleteChildren(path, true);
            } catch(KeeperException.NoNodeException e){ // ignore... someone else has deleted the node since we checked
                System.out.println(e.getMessage());
            }
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(key);
            return;
        }
        String path = provider.getRealPath(key);
        this.deleteChildren(path, true);
        String superPath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        try {
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException ee){
            System.out.println(ee.getMessage());
            return;
        }
    }
    
    private void deleteRecursively(final String path) throws KeeperException, InterruptedException {
        int index = path.lastIndexOf(Constants.PATH_SEPARATOR);
        if (index == 0){
            this.deleteOnlyCurrent(path);
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
