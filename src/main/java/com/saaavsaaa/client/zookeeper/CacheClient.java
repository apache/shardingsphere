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
public final class CacheClient extends UsualClient {
    private final ScheduledExecutorService cacheService = Executors.newSingleThreadScheduledExecutor();
    protected PathTree pathTree = null;
    
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public synchronized void start() throws IOException, InterruptedException {
        super.start();
        pathTree = new PathTree(rootNode);
        cacheService.scheduleAtFixedRate(new ClientTask(strategy.getProvider()) {
            @Override
            public void run(Provider provider) throws KeeperException, InterruptedException {
                if (PathStatus.RELEASE == pathTree.getStatus()) {
                    loadCache(provider);
                }
            }
        }, Properties.INSTANCE.getThreadInitialDelay(), Properties.INSTANCE.getThreadPeriod(), TimeUnit.MILLISECONDS);
    }
    
    //用替换整树的方式更新
    private synchronized void loadCache(final Provider provider) throws KeeperException, InterruptedException {
        LeaderElection election = new LeaderElection() {
            @Override
            public void action() throws KeeperException, InterruptedException {
                pathTree.loading(provider);
            }
        };
        provider.executeContention(election);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        strategy.createCurrentOnly(key, value, createMode);
        pathTree.put(PathUtil.getRealPath(rootNode, key), value);
        System.out.println("cache put : " + key);
    }
    
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(key);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        strategy.deleteOnlyCurrent(key, callback, ctx);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
    
    //==================================================================
    
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
}
