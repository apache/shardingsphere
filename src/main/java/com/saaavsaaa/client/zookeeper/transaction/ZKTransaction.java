package com.saaavsaaa.client.zookeeper.transaction;

import com.saaavsaaa.client.utility.PathUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.util.List;

/**
 * Created by aaa
 * todo transaction api
 */
public class ZKTransaction {
    private final Transaction transaction;
    private final String rootNode;

    public ZKTransaction(final String root, final ZooKeeper zk) {
        transaction = zk.transaction();
        rootNode = root;
    }
    
    public ZKTransaction create(String path, byte[] data, List<ACL> acl, CreateMode createMode) {
        this.transaction.create(PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        return this;
    }
    
    public ZKTransaction delete(String path, int version) {
        this.transaction.delete(PathUtil.getRealPath(rootNode, path), version);
        return this;
    }
    
    public ZKTransaction check(String path, int version) {
        this.transaction.check(PathUtil.getRealPath(rootNode, path), version);
        return this;
    }
    
    public ZKTransaction setData(String path, byte[] data, int version) {
        this.transaction.setData(PathUtil.getRealPath(rootNode, path), data, version);
        return this;
    }
    
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        return this.transaction.commit();
    }
    
    public void commit(AsyncCallback.MultiCallback cb, Object ctx) {
        this.transaction.commit(cb, ctx);
    }
}
