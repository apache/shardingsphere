package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.cache.PathStatus;
import com.saaavsaaa.client.cache.PathTree;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.ClientTask;
import com.saaavsaaa.client.utility.section.Properties;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 */
@Deprecated
public final class CacheAllClient extends UsualClient {
    private final ScheduledExecutorService cacheService = Executors.newSingleThreadScheduledExecutor();
    private Client usualClient;
    
    protected PathTree pathTree = null;
    
    CacheAllClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public synchronized void start() throws IOException, InterruptedException {
        super.start();
        pathTree = new PathTree(rootNode);
        usualClient = this.getClientFactory().newUsualClient().start();
        cacheService.scheduleAtFixedRate(new ClientTask(usualClient) {
            @Override
            public void run(Client client) throws KeeperException, InterruptedException {
                if (PathStatus.RELEASE == pathTree.getStatus()) {
                    loadCache(client);
                }
            }
        }, Properties.INSTANCE.getThreadInitialDelay(), Properties.INSTANCE.getThreadPeriod(), TimeUnit.MILLISECONDS);
    }
    
    //用替换整树的方式更新
    private synchronized void loadCache(final Client client) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                loadCache(client);
            }
    
            @Override
            public void action() throws KeeperException, InterruptedException {
                pathTree.loading(client);
            }
        };
        election.executeContention(rootNode, client);
    }
    
    /*
    * closed beta
    */
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                createAllNeedPath(key, value, createMode);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                createBegin(key, value, createMode);
            }
        };
        election.executeContention(rootNode, this);
    }
    
    
    private void createBegin(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            super.createCurrentOnly(key, value, createMode);
            return;
        }

        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        for (int i = 0; i < nodes.size(); i++) {
            if (super.checkExists(nodes.get(i))){
                System.out.println("create :" + nodes.get(i));
                continue;
            }
            System.out.println("create not exist:" + nodes.get(i));
            if (i == nodes.size() - 1){
                super.createCurrentOnly(nodes.get(i), value, createMode);
            } else {
                this.createCurrentOnly(nodes.get(i), Constants.NOTHING_VALUE, createMode);
            }
        }
    }

    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                deleteAllChildren(key);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                String path = PathUtil.getRealPath(rootNode, key);
                deleteChildren(path);
                pathTree.delete(path);
            }
        };
        election.executeContention(rootNode, this);
    }
    
    private void deleteChildren(final String key) throws KeeperException, InterruptedException {
        List<String> children = super.getChildren(key);
        if (children.isEmpty()){
            if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
                super.deleteOnlyCurrent(key);
                return;
            }
            return;
        }
        for (int i = 0; i < children.size(); i++) {
            if (!super.checkExists(children.get(i))){
                System.out.println("delete not exist:" + children.get(i));
                continue;
            }
            this.deleteChildren(key);
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                deleteCurrentBranch(key);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                String path = PathUtil.getRealPath(rootNode, key);
                try {
                    deleteBranch(path);
                } catch (KeeperException.NotEmptyException ee){
                    System.out.println(path + " exist other children");
                    pathTree.delete(path);
                    return;
                }
            }
        };
        election.executeContention(rootNode, this);
    }
    
    private void deleteBranch(String key) throws KeeperException, InterruptedException {
        deleteChildren(PathUtil.getRealPath(rootNode, key));
        Stack<String> pathStack = PathUtil.getPathReverseNodes(rootNode, key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (checkExists(node)){
                super.deleteOnlyCurrent(key);
                System.out.println("delete : " + node);
            }
        }
    }
    
    private boolean cacheReady(){
        return PathStatus.RELEASE == pathTree.getStatus();
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        if (cacheReady()){
            return pathTree.getValue(path);
        }
        // without watcher ensure cache execute result consistency
        byte[] data = zooKeeper.getData(path, false, null);
        pathTree.put(path, new String(data));
        return data;
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        if (cacheReady()){
            return null != pathTree.getValue(path);
        }
        return null != zooKeeper.exists(PathUtil.getRealPath(rootNode, key), false);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        if (cacheReady()){
            return pathTree.getChildren(path);
        }
        return zooKeeper.getChildren(PathUtil.getRealPath(rootNode, key), false);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
//        super.createCurrentOnly(key, value, createMode);
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                createCurrentOnly(key, value, createMode);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                usualClient.createCurrentOnly(key, value, createMode);
            }
        };
        election.executeContention(rootNode, this);
        pathTree.put(PathUtil.getRealPath(rootNode, key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
//        super.deleteOnlyCurrent(key);
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                deleteOnlyCurrent(key);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                usualClient.deleteOnlyCurrent(key);
            }
        };
        election.executeContention(rootNode, this);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
//        super.deleteOnlyCurrent(key, callback, ctx);
        LeaderElection election = new LeaderElection() {
            @Override
            public void actionWhenUnreached() throws KeeperException, InterruptedException {
                deleteOnlyCurrent(key, callback, ctx);
            }
        
            @Override
            public void action() throws KeeperException, InterruptedException {
                usualClient.deleteOnlyCurrent(key, callback, ctx);
            }
        };
        election.executeContention(rootNode, this);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
}
