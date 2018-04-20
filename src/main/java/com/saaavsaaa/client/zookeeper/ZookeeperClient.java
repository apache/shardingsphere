package com.saaavsaaa.client.zookeeper;
/**
 * Created by aaa on 18-4-18.
 */

import com.saaavsaaa.client.untils.PathUtil;
import org.apache.zookeeper.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

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
    
    public void createCurrentOnly(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        zooKeeper.create(PathUtil.getRealPath(rootNode, path), data, authorities, createMode);
    }
    
    /*
    * closed beta
    */
    public void createAllNeedPath(String path, byte[] data, CreateMode createMode) throws KeeperException, InterruptedException {
        if (path.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(path, data, createMode);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo sync cache
        Iterator<String> nodes = PathUtil.getPathOrderNodes(rootNode, path).iterator();
        while (nodes.hasNext()){
            String node = nodes.next();
            // contrast cache
            if (!checkExists(node)){
                try {
                    // TODO: exception
                    createInTransaction(path, data, createMode, transaction);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        transaction.commit();
    }
    
    public Transaction createInTransaction(String path, byte[] data, CreateMode createMode, Transaction transaction){
        return transaction.create(PathUtil.getRealPath(rootNode, path), data, authorities, createMode);
    }
    
    public void updateInTransaction(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.transaction().setData(PathUtil.getRealPath(rootNode, key), data, VERSION).commit();
    }
    
    public void update(String key, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.setData(PathUtil.getRealPath(rootNode, key), data, VERSION);
    }
    
    public void deleteOnlyCurrent(String key) throws KeeperException, InterruptedException {
        zooKeeper.delete(PathUtil.getRealPath(rootNode, key), VERSION);
    }
    
    public void deleteOnlyCurrent(String key, AsyncCallback.VoidCallback callback, Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.delete(PathUtil.getRealPath(rootNode, key), VERSION, callback, ctx);
    }
    
    /*
    * closed beta
    */
    public void deleteCurrentBranch(String path) throws KeeperException, InterruptedException {
        if (path.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(path);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo branch check
        Stack<String> pathStack = PathUtil.getPathReverseNodes(rootNode, path);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (!checkExists(node)){
                transaction.delete(node, VERSION);
            }
        }
        transaction.commit();
    }
    
    /*
    * closed beta
    */
    public void deleteAllChild(String path) throws KeeperException, InterruptedException {
        if (path.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(path);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo sync cache
        new DefaultMutableTreeNode();
        Iterator<String> nodes = PathUtil.depthToB(new DefaultMutableTreeNode()).iterator();
        while (nodes.hasNext()){
            String node = nodes.next();
            // contrast cache
            if (checkExists(node)){
                try {
                    // TODO: exception
                    deleteOnlyCurrent(node);
                } catch (Exception e){
                    System.out.println(e.getMessage());
                }
            }
        }
        transaction.commit();
    }
    
    private Watcher watcher;
    public void watch(final String key, final EventListener eventListener){
        watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
            }
        };
    }
    
    public void close() throws InterruptedException {
        zooKeeper.close();
    }
}
