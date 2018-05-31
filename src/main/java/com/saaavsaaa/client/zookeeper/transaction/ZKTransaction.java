package com.saaavsaaa.client.zookeeper.transaction;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.base.Holder;
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

    public ZKTransaction(final String root, final Holder holder) {
        transaction = holder.getZooKeeper().transaction();
        rootNode = root;
        logger.debug("ZKTransaction root:{}", rootNode);
    }
    
    public ZKTransaction create(final String path, final byte[] data, final List<ACL> acl, final CreateMode createMode) {
        this.transaction.create(PathUtil.getRealPath(rootNode, path), data, acl, createMode);
        logger.debug("wait create:{},data:{},acl:{},createMode:{}", new Object[]{path, data, acl, createMode});
        return this;
    }
    
    public ZKTransaction delete(final String path){
        return delete(path, Constants.VERSION);
    }
    public ZKTransaction delete(final String path, final int version) {
        this.transaction.delete(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait delete:{}", path);
        return this;
    }
    
    public ZKTransaction check(final String path){
        return check(path, Constants.VERSION);
    }
    public ZKTransaction check(final String path, final int version) {
        this.transaction.check(PathUtil.getRealPath(rootNode, path), version);
        logger.debug("wait check:{}", path);
        return this;
    }
    
    public ZKTransaction setData(final String path, final byte[] data){
        return setData(path, data, Constants.VERSION);
    }
    public ZKTransaction setData(final String path, final byte[] data, final int version) {
        this.transaction.setData(PathUtil.getRealPath(rootNode, path), data, version);
        logger.debug("wait setData:{},data:{}", path, data);
        return this;
    }
    
    public List<OpResult> commit() throws InterruptedException, KeeperException {
        logger.debug("ZKTransaction commit");
        return this.transaction.commit();
    }
    
    public void commit(final AsyncCallback.MultiCallback cb, final Object ctx) {
        this.transaction.commit(cb, ctx);
        logger.debug("ZKTransaction commit ctx:{}", ctx);
    }
}
