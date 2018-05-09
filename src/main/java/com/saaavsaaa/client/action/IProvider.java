package com.saaavsaaa.client.action;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 */
public interface IProvider extends IAction {
    String getRealPath(final String path);
    List<String> getNecessaryPaths(final String key);
    Stack<String> getDeletingPaths(final String key);
    void executeContention(final LeaderElection election) throws KeeperException, InterruptedException;
    void watch(final String key, final Listener listener);
    
    void createInTransaction(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException;
}
