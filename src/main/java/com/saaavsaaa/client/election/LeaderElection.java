package com.saaavsaaa.client.election;

import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Properties;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by aaa
 */
public abstract class LeaderElection {
    private static final Logger logger = LoggerFactory.getLogger(LeaderElection.class);
    private boolean done = false;

    private boolean contend(final String node, final Provider provider, final Listener listener) throws KeeperException, InterruptedException {
        boolean success = false;
        try {
            provider.createCurrentOnly(node, Properties.INSTANCE.getClientId(), CreateMode.EPHEMERAL);
            success = true;
        } catch (KeeperException.NodeExistsException e) {
            logger.info("contend not success");
            // TODO: or changing_key node value == current client id
            provider.checkExists(node, WatcherCreator.deleteWatcher(node, listener));
        }
        return success;
    }
    
    /*
    * listener will be register when the contention of the path is unsuccessful
    */
    public void executeContention(final String nodeBeCompete, final Provider provider) throws KeeperException, InterruptedException {
        boolean canBegin;
        String realNode = provider.getRealPath(nodeBeCompete);
        String contendNode = PathUtil.getRealPath(realNode, Constants.CHANGING_KEY);
        canBegin = this.contend(contendNode, provider, new Listener() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    executeContention(realNode, provider);
                } catch (Exception ee){
                    logger.error("Listener Exception executeContention:{}", ee.getMessage(), ee);
                }
            }
        });
    
        if (canBegin){
            try {
                action();
                done = true;
                callback();
            } catch (Exception ee){
                logger.error("action Exception executeContention:{}", ee.getMessage(), ee);
            }
            provider.deleteOnlyCurrent(contendNode);
        }
    }
    
    public void waitDone(){
        while (!done){
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                logger.error("waitDone:{}", e.getMessage(), e);
            }
        }
    }
    
//    public abstract void actionWhenUnreached() throws KeeperException, InterruptedException;
    public abstract void action() throws KeeperException, InterruptedException;
    
    public void callback(){}
}
