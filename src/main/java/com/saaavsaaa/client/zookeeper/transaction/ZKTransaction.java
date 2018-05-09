package com.saaavsaaa.client.zookeeper.transaction;

import com.saaavsaaa.client.utility.PathUtil;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by aaa
 */
public class ZKTransaction {
    private static final Logger logger = LoggerFactory.getLogger(ZKTransaction.class);
    private final Transaction transaction;
    private final String rootNode;

    public ZKTransaction(final String root, final ZooKeeper zk) {
        transaction = zk.transaction();
        rootNode = root;
        logger.debug("ZKTransaction root:{}", rootNode);
    }
    
    public ZKTransaction create(String path, byte[] data, List<ACL> acl, CreateMode createMode) {
        this.transaction.create(PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        logger.debug("wait create:{},data:{},acl:{},createMode:{}", new Object[]{path, data, acl, createMode});
        return this;
    }
    
    public ZKTransaction delete(String path, int version) {
        this.transaction.delete(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait delete:{}", path);
        return this;
    }
    
    public ZKTransaction check(String path, int version) {
        this.transaction.check(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait check:{}", path);
        return this;
    }
    
    public ZKTransaction setData(String path, byte[] data, int version) {
        this.transaction.setData(PathUtil.getRealPath(rootNode, path), data, version);
        logger.debug("wait setData:{},data:{}", path, data);
        return this;
    }
    
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        logger.debug("ZKTransaction commit");
        return this.transaction.commit();
    }
    
    public void commit(AsyncCallback.MultiCallback cb, Object ctx) {
        this.transaction.commit(cb, ctx);
        logger.debug("ZKTransaction commit ctx:{}", ctx);
    }
}
