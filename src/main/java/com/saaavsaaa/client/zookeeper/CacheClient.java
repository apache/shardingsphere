package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.PathUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Transaction;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Iterator;

/**
 * Created by aaa on 18-4-19.
 */
public final class CacheClient extends UsualClient {
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    /*
    * closed beta
    * 当前实现方法用于缓存方式
    * 缓存实现后此类判断换为异常方式（包括创建）
    * 用事务不能用异常
    */
    @Override
    public void deleteAllChild(final String key) throws KeeperException, InterruptedException {
        Transaction transaction = zooKeeper.transaction();
        this.deleteAllChild(key, transaction);
        transaction.commit();
    }
    
    private void deleteAllChild(final String key, final Transaction transaction) throws KeeperException, InterruptedException {
    }
}
