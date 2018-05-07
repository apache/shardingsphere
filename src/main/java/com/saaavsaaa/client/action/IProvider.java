package com.saaavsaaa.client.action;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.section.Listener;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa on 18-5-3.
 */
public interface IProvider extends IAction {
    String getRealPath(final String path);
    List<String> getNecessaryPaths(final String key);
    Stack<String> getDeletingPaths(final String key);
    void executeContention(final LeaderElection election) throws KeeperException, InterruptedException;
    void watch(final String key, final Listener listener);
}
