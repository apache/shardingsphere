package com.saaavsaaa.client.election;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Property;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.Client;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa on 18-4-27.
 */
public class LeaderElection {
    /*
    * listener will be register when the contention of the path is unsuccessful
    */
    public static boolean Contend(final String path, final Client client, final Listener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            client.createCurrentOnly(Constants.CHANGING_KEY, Property.INSTANCE.getClientId(), CreateMode.EPHEMERAL);
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            client.checkExists(Constants.CHANGING_KEY, WatcherCreator.deleteWatcher(PathUtil.getRealPath(path, Constants.CHANGING_KEY), listener));
        }
        return success;
    }
}
