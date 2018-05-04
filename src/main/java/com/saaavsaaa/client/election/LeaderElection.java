package com.saaavsaaa.client.election;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Properties;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.Client;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

/**
 * Created by aaa
 */
public abstract class LeaderElection {
    private boolean done = false;

    private boolean contend(final String node, final Provider provider, final Listener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            provider.createCurrentOnly(node, Properties.INSTANCE.getClientId(), CreateMode.EPHEMERAL);
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            // TODO: or changing_key node value == current client id
            provider.checkExists(node, WatcherCreator.deleteWatcher(node, listener));
        }
        return success;
    }
    
    /*
    * listener will be register when the contention of the path is unsuccessful
    */
    public void executeContention(final Provider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        String contendNode = provider.getRealPath(Constants.CHANGING_KEY);
        canBegin = this.contend(contendNode, provider, new Listener() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    executeContention(provider);
                } catch (Exception ee){
                    System.out.println("Listener Exception executeContention");
                    ee.printStackTrace();
                }
            }
        });
    
        if (canBegin){
            try {
                action();
                done = true;
            } catch (Exception ee){
                System.out.println("action Exception executeContention");
                ee.printStackTrace();
            }
            provider.deleteOnlyCurrent(contendNode);
        }
    }
    
    public void waitDone(){
        while (!done){
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
        }
    }
    
//    public abstract void actionWhenUnreached() throws KeeperException, InterruptedException;
    public abstract void action() throws KeeperException, InterruptedException;
    
    public void callBack(){}
}
