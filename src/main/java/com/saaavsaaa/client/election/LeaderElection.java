package com.saaavsaaa.client.election;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Properties;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.Client;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public abstract class LeaderElection {

    private boolean contend(final String path, final Client client, final Listener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            client.createCurrentOnly(Constants.CHANGING_KEY, Properties.INSTANCE.getClientId(), CreateMode.EPHEMERAL);
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            // TODO: or changing_key node value == current client id
            client.checkExists(Constants.CHANGING_KEY, WatcherCreator.deleteWatcher(PathUtil.getRealPath(path, Constants.CHANGING_KEY), listener));
        }
        return success;
    }
    
    /*
    * listener will be register when the contention of the path is unsuccessful
    */
    public void executeContention(final String path, final Client client) throws KeeperException, InterruptedException {
        boolean canBegin;
        canBegin = this.contend(path, client, new Listener() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    executeContention(path, client);
                } catch (Exception ee){
                    System.out.println("Listener Exception " + path);
                    ee.printStackTrace();
                }
            }
        });
    
        if (canBegin){
            try {
                action();
            } catch (Exception ee){
                System.out.println("action Exception " + path);
                ee.printStackTrace();
            }
            client.deleteOnlyCurrent(Constants.CHANGING_KEY);
        }
    }
    
//    public abstract void actionWhenUnreached() throws KeeperException, InterruptedException;
    public abstract void action() throws KeeperException, InterruptedException;
}
