package com.saaavsaaa.client.zookeeper;
/**
 * Created by aaa on 18-4-18.
 */

import com.saaavsaaa.client.untils.Constants;
import com.saaavsaaa.client.untils.PathUtil;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/*
* cache
* todo Sequential
* todo org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists
*/
public class UsualClient extends Client {
    private final boolean watched = true; //false
    
    UsualClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return new String(getData(key));
    }

    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return zooKeeper.getData(PathUtil.getRealPath(rootNode, key), watched, null);
    }
    
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.getData(PathUtil.getRealPath(rootNode, key), watched, callback, ctx);
    }
    
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(PathUtil.getRealPath(rootNode, key), watched);
    }
    
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return null != zooKeeper.exists(PathUtil.getRealPath(rootNode, key), watcher);
    }
    
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return zooKeeper.getChildren(PathUtil.getRealPath(rootNode, key), watched);
    }
    
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        createNamespace();
        String path = PathUtil.getRealPath(rootNode, key);
        if (rootNode.equals(path)){
            return;
        }
        try {
            zooKeeper.create(path, value.getBytes(Constants.UTF_8), authorities, createMode);
        } catch (KeeperException.NoNodeException e) {
            // I don't know whether it will happen or not, if root watcher don't update rootExist timely
            if (e.getMessage().contains(path)) {
                System.out.println("rootExist : " + e.getMessage());
                Thread.sleep(50);
                this.createCurrentOnly(key, value, createMode);
            }
        }
    }
    
    /*
    * todo exception recursion
    */
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
    
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        zooKeeper.setData(PathUtil.getRealPath(rootNode, key), value.getBytes(Constants.UTF_8), Constants.VERSION);
    }
    
    public void updateWithCheck(final String key, final String value) throws KeeperException, InterruptedException {
        String realPath = PathUtil.getRealPath(rootNode, key);
        zooKeeper.transaction().check(realPath, Constants.VERSION).setData(realPath, value.getBytes(Constants.UTF_8), Constants.VERSION).commit();
    }
    
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        zooKeeper.delete(PathUtil.getRealPath(rootNode, key), Constants.VERSION);
        System.out.println("delete : " + PathUtil.getRealPath(rootNode, key));
    }
    
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        zooKeeper.delete(PathUtil.getRealPath(rootNode, key), Constants.VERSION, callback, ctx);
    }
    
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        String realPath = PathUtil.getRealPath(rootNode, key);
        try {
            this.deleteOnlyCurrent(realPath);
        }catch (KeeperException.NotEmptyException ee){
            List<String> children = this.getChildren(realPath);
            for (String child : children) {
                child = realPath + Constants.PATH_SEPARATOR + child;
                this.deleteAllChildren(child);
            }
            this.deleteOnlyCurrent(realPath);
        } catch (KeeperException.NoNodeException ee){
            System.out.println(ee.getMessage());
            return;
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        this.deleteAllChildren(path);
        String superPath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
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

