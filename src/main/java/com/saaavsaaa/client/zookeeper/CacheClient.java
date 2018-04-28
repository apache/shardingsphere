package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.cache.PathNode;
import com.saaavsaaa.client.cache.PathStatus;
import com.saaavsaaa.client.cache.PathTree;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.constant.Properties;
import com.saaavsaaa.client.utility.section.ClientTask;
import com.saaavsaaa.client.utility.section.Listener;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Created by aaa
 * todo restructure the three clients to strategies
 *  todo log
 *  todo currentNodes
 */
public final class CacheClient extends UsualClient {
    private final ScheduledExecutorService cacheService = Executors.newSingleThreadScheduledExecutor();
    private Client usualClient;
    
    protected PathTree pathTree = null;
    
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
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
        }, Properties.THREAD_INITIAL_DELAY, Properties.THREAD_PERIOD, TimeUnit.MILLISECONDS);
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
        Transaction transaction = zooKeeper.transaction();

        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        for (int i = 0; i < nodes.size(); i++) {
            // todo not goog
            if (super.checkExists(nodes.get(i))){
                System.out.println("exist:" + nodes.get(i));
                continue;
            }
            System.out.println("not exist:" + nodes.get(i));
            if (i == nodes.size() - 1){
                createInTransaction(nodes.get(i), value.getBytes(Constants.UTF_8), createMode, transaction);
            } else {
                createInTransaction(nodes.get(i), Constants.NOTHING_DATA, createMode, transaction);
            }
        }
        
        transaction.commit();
    }
    
    private Transaction createInTransaction(final String key, byte[] data, final CreateMode createMode, final Transaction transaction){
        return transaction.create(PathUtil.getRealPath(rootNode, key), data, authorities, createMode);
    }
    
    /*
    * closed beta
    * 当前实现方法用于缓存方式
    * 缓存实现后此类判断换为异常方式（包括创建）
    * 用事务不能用异常
    */
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        Transaction transaction = zooKeeper.transaction();
        this.deleteAllChildren(key, transaction);
        transaction.commit();
    }
    
    private void deleteAllChildren(final String key, final Transaction transaction) throws KeeperException, InterruptedException {
    }
    
    private void deleteOnlyCurrent(final String key, final Transaction transaction) throws KeeperException, InterruptedException {
        zooKeeper.delete(PathUtil.getRealPath(rootNode, key), Constants.VERSION);
    }
    
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(key);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo branch check
        Stack<String> pathStack = PathUtil.getPathReverseNodes(rootNode, key);
        while (!pathStack.empty()){
            String node = pathStack.pop();
            // contrast cache
            if (checkExists(node)){
                transaction.delete(node, Constants.VERSION);
                System.out.println("delete : " + node);
            }
        }
        transaction.commit();
    }
    
    //===========================================================================================================
    
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
