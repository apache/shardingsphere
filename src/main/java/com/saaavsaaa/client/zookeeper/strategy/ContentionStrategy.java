package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.Client;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa on 18-5-2.
 */
public class ContentionStrategy extends BaseStrategy {
    public ContentionStrategy(final Provider provider) {
        super(provider);
    }
    
    @Override
    /*
    * Don't use it if you don't have to use it
    */
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.getData(key,callback, ctx);
            }
        };
        provider.executeContention(election);
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.createCurrentOnly(key, value, createMode);
            }
        };
        provider.executeContention(election);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.update(key, value);
            }
        });
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(key);
            }
        });
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(key, callback, ctx);
            }
        });
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                createBegin(key, value, createMode);
            }
        };
    }
    
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (provider.checkExists(nodes.get(i))){
                System.out.println("create :" + nodes.get(i));
                continue;
            }
            System.out.println("create not exist:" + nodes.get(i));
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
                deleteChildren(key, true);
            }
        });
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = provider.getChildren(key);
        if (children.isEmpty()){
            if (deleteCurrentNode){
                provider.deleteOnlyCurrent(key);
                return;
            }
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!provider.checkExists(child)){
                System.out.println("delete not exist:" + child);
                continue;
            }
            deleteChildren(child, true);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                try {
                    deleteBranch(key);
                } catch (KeeperException.NotEmptyException ee){
                    System.out.println(key + " exist other children");
                    return;
                }
            }
        });
    }
    
    private void deleteBranch(String key) throws KeeperException, InterruptedException {
        deleteChildren(key, false);
        Stack<String> pathStack = provider.getDeletingPaths(key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (checkExists(node)){
                provider.deleteOnlyCurrent(key);
                System.out.println("delete : " + node);
            }
        }
    }
}
