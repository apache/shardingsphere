package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.untils.PathUtil;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Transaction;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Iterator;

/**
 * Created by aaa on 18-4-19.
 */
public class CacheClient extends UsualClient {
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    public void deleteAllChild(String key) throws KeeperException, InterruptedException {
        if (key.indexOf(PathUtil.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(key);
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
                deleteOnlyCurrent(node);
            }
        }
        
        // TODO: exception
        transaction.commit();
    }
}
