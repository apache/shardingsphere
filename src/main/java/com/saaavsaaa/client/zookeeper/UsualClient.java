package com.saaavsaaa.client.zookeeper;
/**
 * Created by aaa on 18-4-18.
 */

import com.saaavsaaa.client.untils.PathUtil;
import org.apache.zookeeper.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.charset.Charset;
import java.util.EventListener;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/*
* cache
* todo Sequential
*/
public class UsualClient extends BaseClient {
    UsualClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }

    public void createRootNode() throws KeeperException, InterruptedException {
        if (checkExists(rootNode)){
            return;
        }
        zooKeeper.create(rootNode, new byte[0], authorities, CreateMode.PERSISTENT);
    }
    
    public void deleteRoot() throws KeeperException, InterruptedException {
        zooKeeper.delete(rootNode, VERSION);
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
    
    public void createCurrentOnly(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException {
        zooKeeper.create(PathUtil.getRealPath(rootNode, key), value.getBytes(UTF_8), authorities, createMode);
    }
    
    /*
    * closed beta
    */
    public void createAllNeedPath(String key, String value, CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(key, value, createMode);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo sync cache
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        for (int i = 0; i < nodes.size(); i++) {
            // todo contrast cache
            if (checkExists(nodes.get(i))){
                System.out.println("exist:" + nodes.get(i));
                continue;
            }
            System.out.println("not exist:" + nodes.get(i));
            if (i == nodes.size() - 1){
                createInTransaction(nodes.get(i), value.getBytes(UTF_8), createMode, transaction);
            } else {
                createInTransaction(nodes.get(i), NOTHING_DATA, createMode, transaction);
            }
        }
        
        // org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists
        transaction.commit();
    }
    
    private Transaction createInTransaction(String key, byte[] data, CreateMode createMode, Transaction transaction){
        return transaction.create(PathUtil.getRealPath(rootNode, key), data, authorities, createMode);
    }
    
    public void update(String key, String value) throws KeeperException, InterruptedException {
        String realPath = PathUtil.getRealPath(rootNode, key);
        zooKeeper.transaction().check(realPath, VERSION).setData(realPath, value.getBytes(UTF_8), VERSION).commit();
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
    public void deleteCurrentBranch(String key) throws KeeperException, InterruptedException {
        if (key.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(key);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo branch check
        Stack<String> pathStack = PathUtil.getPathReverseNodes(rootNode, key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (checkExists(node)){
                transaction.delete(node, VERSION);
            }
        }
        transaction.commit();
    }
    
    /*
    * closed beta
    * 当前实现方法用于缓存方式
    * 缓存实现后此类判断换为异常方式（包括创建）
    */
    public void deleteAllChild(String key) throws KeeperException, InterruptedException {
        String realPath = PathUtil.getRealPath(rootNode, key);
        try {
            this.deleteOnlyCurrent(realPath);
        }catch (KeeperException.NotEmptyException ee){
            List<String> children = this.getChildren(realPath);
            for (String child : children) {
                child = realPath + PathUtil.PATH_SEPARATOR + child;
                this.deleteAllChild(child);
            }
        } catch (KeeperException.NoNodeException ee){
            System.out.println(ee.getMessage());
        }
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
