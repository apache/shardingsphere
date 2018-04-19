package com.saaavsaaa.client.zookeeper;
/**
 * Created by aaa on 18-4-18.
 */

import com.saaavsaaa.client.BaseClient;
import com.saaavsaaa.client.untils.PathUtil;
import com.saaavsaaa.client.untils.StringUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

/*
* cache
*/
public class ZookeeperClient extends BaseClient {
    ZookeeperClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }

    public void createRootNode() throws KeeperException, InterruptedException {
        if (checkExists(rootNode)){
            return;
        }
        zooKeeper.create(rootNode, new byte[0], authorities, CreateMode.PERSISTENT);
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
        //todo sync cache
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
    
    public Transaction createInTransaction(String path, byte[] data, CreateMode createMode, Transaction transaction){
        return transaction.create(PathUtil.getRealPath(rootNode, path), data, authorities, createMode);
    }
    
    public void updateInTransaction(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.transaction().setData(PathUtil.getRealPath(rootNode, key), data, -1).commit();
    }
    
    public void update(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.setData(PathUtil.getRealPath(rootNode, key), data, -1);
    }
    
    public void deleteTogetherBranch(String key) {
        
    }
}
