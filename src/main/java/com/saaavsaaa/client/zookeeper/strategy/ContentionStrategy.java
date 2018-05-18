package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.section.Callback;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 * The ContentionStrategy is effective only when all the clients of the node which be competitive are using ContentionStrategy.
 */
public class ContentionStrategy extends UsualStrategy {
    private static final Logger logger = LoggerFactory.getLogger(ContentionStrategy.class);
    public ContentionStrategy(final IProvider provider) {
        super(provider);
    }
    
    @Override
    /*
    * Don't use it if you don't have to use it.
    */
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.getData(provider.getRealPath(key), callback, ctx);
                logger.debug("ContentionStrategy getData action:{}", key);
            }
        };
        provider.executeContention(election);
        logger.debug("ContentionStrategy getData executeContention");
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateElection(key, value, createMode, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy createCurrentOnly executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateElection(final String key, final String value, final CreateMode createMode, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.createCurrentOnly(provider.getRealPath(key), value, createMode);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        LeaderElection election = buildUpdateElection(key, value, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy update executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildUpdateElection(final String key, final String value, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), value);
                logger.debug("ContentionStrategy update action:{},value:{}", key, value);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = buildDeleteElection(key, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy deleteOnlyCurrent executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildDeleteElection(final String key, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(provider.getRealPath(key));
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(provider.getRealPath(key), callback, ctx);
                logger.debug("ContentionStrategy deleteOnlyCurrent action:{},ctx:{}", key, ctx);
            }
        });
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = buildCreateAllNeedElection(key, value, createMode, null);
        provider.executeContention(election);
        logger.debug("ContentionStrategy createAllNeedPath executeContention");
        election.waitDone();
    }
    
    private LeaderElection buildCreateAllNeedElection(final String key, final String value, final CreateMode createMode, final Callback callback){
        return new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                logger.debug("ContentionStrategy createAllNeedPath action:{}", key);
                createBegin(key, value, createMode);
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            provider.createCurrentOnly(key, value, createMode);
            return;
        }
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (provider.checkExists(nodes.get(i))){
                logger.info("create node exist:{}", nodes.get(i));
                continue;
            }
            logger.debug("create node not exist:", nodes.get(i));
            if (i == nodes.size() - 1){
                provider.createCurrentOnly(nodes.get(i), value, createMode);
            } else {
                provider.createCurrentOnly(nodes.get(i), Constants.NOTHING_VALUE, createMode);
            }
        }
    }
    
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteChildren(provider.getRealPath(key), true);
            }
        });
        logger.debug("ContentionStrategy deleteAllChildren executeContention");
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = provider.getChildren(key);
        logger.debug("deleteChildren:{}", children);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!provider.checkExists(child)){
                logger.info("delete not exist:{}", child);
                continue;
            }
            logger.debug("deleteChildren:{}", child);
            deleteChildren(child, true);
        }
        if (deleteCurrentNode){
            provider.deleteOnlyCurrent(key);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                deleteBranch(provider.getRealPath(key));
            }
        });
        logger.debug("ContentionStrategy deleteCurrentBranch executeContention");
    }
    
    private void deleteBranch(String key) throws KeeperException, InterruptedException {
        deleteChildren(key, false);
        Stack<String> pathStack = provider.getDeletingPaths(key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (provider.checkExists(node)){
                try {
                    provider.deleteOnlyCurrent(node);
                } catch (KeeperException.NotEmptyException ee){
                    logger.warn("deleteBranch {} exist other children:{}", node, this.getChildren(node));
                    logger.debug(ee.getMessage());
                    return;
                }
            }
            logger.info("deleteBranch node not exist:{}", node);
        }
    }
    
    
    
    //todo Use arbitrary competitive nodes
    //IExecStrategy convert to ContentionStrategy======================================================================
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildCreateElection(key, value, createMode, callback));
    }
    
    public void update(final String key, final String value, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildUpdateElection(key, value, null));
    }

    public void deleteOnlyCurrent(final String key, final Callback callback) throws KeeperException, InterruptedException {
        provider.executeContention(buildDeleteElection(key, null));
    }
}
