package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.action.IProvider;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.action.Callback;
import com.saaavsaaa.client.zookeeper.base.BaseProvider;
import com.saaavsaaa.client.zookeeper.transaction.ZKTransaction;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 * nothing use
 * since zookeeper 3.4.0
 */
public class TransactionContendStrategy extends ContentionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(TransactionContendStrategy.class);
    public TransactionContendStrategy(IProvider provider) {
        super(provider);
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
                ZKTransaction transaction = new ZKTransaction(((BaseProvider)provider).getRootNode(), ((BaseProvider)provider).getHolder());
                createBegin(key, value, createMode, transaction);
                transaction.commit();
            }
            @Override
            public void callback(){
                if (callback != null) {
                    callback.processResult();
                }
            }
        };
    }

    private void createBegin(final String key, final String value, final CreateMode createMode, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            provider.createInTransaction(key, value, createMode, transaction);
            return;
        }
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (provider.exists(nodes.get(i))){
                logger.info("create node exist:{}", nodes.get(i));
                continue;
            }
            logger.debug("node not exist and create:", nodes.get(i));
            if (i == nodes.size() - 1){
                provider.createInTransaction(nodes.get(i), value, createMode, transaction);
            } else {
                provider.createInTransaction(nodes.get(i), Constants.NOTHING_VALUE, createMode, transaction);
            }
        }
    }

    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZKTransaction transaction = new ZKTransaction(((BaseProvider)provider).getRootNode(), ((BaseProvider)provider).getHolder());
                deleteChildren(provider.getRealPath(key), true, transaction);
                transaction.commit();
            }
        });
        logger.debug("ContentionStrategy deleteAllChildren executeContention");
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        List<String> children = provider.getChildren(key);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!provider.exists(child)){
                logger.info("delete not exist:{}", child);
                continue;
            }
            logger.debug("deleteChildren:{}", child);
            deleteChildren(child, true, transaction);
        }
        if (deleteCurrentNode){
            transaction.delete(key, Constants.VERSION);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                ZKTransaction transaction = new ZKTransaction(((BaseProvider)provider).getRootNode(), ((BaseProvider)provider).getHolder());
                deleteBranch(provider.getRealPath(key), transaction);
                transaction.commit();
            }
        });
        logger.debug("ContentionStrategy deleteCurrentBranch executeContention");
    }

    private void deleteBranch(String key, final ZKTransaction transaction) throws KeeperException, InterruptedException {
        deleteChildren(key, false, transaction);
        Stack<String> pathStack = provider.getDeletingPaths(key);
        String prePath = key;
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            // Performance needs testing
            List<String> children = provider.getChildren(node);
            boolean canDelete = children.size() == 0 || children.size() == 1;
            if (children.size() == 1){
                if (!PathUtil.getRealPath(node, children.get(0)).equals(prePath)){
                    canDelete = false;
                }
            }
            if (provider.exists(node) && canDelete){
                transaction.delete(node, Constants.VERSION);
            }
            prePath = node;
        }
    }
}
