package com.saaavsaaa.client.action;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Created by aaa on 18-5-10.
 */
public interface IGroupAction {
    
    void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException ;
    
    void deleteAllChildren(final String key) throws KeeperException, InterruptedException ;
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException ;
}
