package com.saaavsaaa.client.zookeeper.strategy;

import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.Provider;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 * Don't use it if you don't have to use it
 */
public class ContentionStrategy extends UsualStrategy {
    public ContentionStrategy(final Provider provider) {
        super(provider);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.getData(provider.getRealPath(key), callback, ctx);
            }
        };
        provider.executeContention(election);
    }

    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.createCurrentOnly(provider.getRealPath(key), value, createMode);
            }
        };
        provider.executeContention(election);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.update(provider.getRealPath(key), value);
            }
        });
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(provider.getRealPath(key));
            }
        });
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.executeContention(new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                provider.deleteOnlyCurrent(provider.getRealPath(key), callback, ctx);
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
        provider.executeContention(election);
    }
    
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            provider.createCurrentOnly(key, value, createMode);
            return;
        }
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            if (provider.checkExists(nodes.get(i))){
                System.out.println("create exist:" + nodes.get(i));
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
                deleteChildren(provider.getRealPath(key), true);
            }
        });
    }
    
    private void deleteChildren(final String key, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children = provider.getChildren(key);
        for (int i = 0; i < children.size(); i++) {
            String child = PathUtil.getRealPath(key, children.get(i));
            if (!provider.checkExists(child)){
                System.out.println("delete not exist:" + child);
                continue;
            }
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
                try {
                    deleteBranch(provider.getRealPath(key));
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
            if (provider.checkExists(node)){
                provider.deleteOnlyCurrent(node);
                System.out.println("delete : " + node);
            }
        }
    }
}
