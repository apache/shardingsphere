package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.cache.CacheStrategy;
import com.saaavsaaa.client.cache.PathStatus;
import com.saaavsaaa.client.cache.PathTree;
import com.saaavsaaa.client.election.LeaderElection;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.ClientTask;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Properties;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(CacheClient.class);
    protected PathTree pathTree = null;
    
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public synchronized void start() throws IOException, InterruptedException {
        super.start();
        try {
            useCacheStrategy(CacheStrategy.WATCH);
        } catch (KeeperException e) {
            logger.error("CacheClient useCacheStrategy : " + e.getMessage());
        }
    }
    
    void useCacheStrategy(CacheStrategy cacheStrategy) throws KeeperException, InterruptedException {
        logger.debug("use cache strategy:{}", cacheStrategy);
        switch (cacheStrategy){
            case WATCH:{
                pathTree  = new PathTree(rootNode, strategy.getProvider());
                pathTree.watch(new Listener() {
                    @Override
                    public void process(WatchedEvent event) {
                        String path = event.getPath();
                        logger.debug("useCacheStrategy Watch event:{}", event.toString());
                        switch (event.getType()) {
                            case NodeCreated:
                            case NodeDataChanged:
                            case NodeChildrenChanged: {
                                try {
                                    pathTree.put(path, strategy.getDataString(path));
                                } catch (Exception e) {
                                    logger.error("path tree put error : " + e.getMessage());
                                }
                                break;
                            }
                            case NodeDeleted: {
                                pathTree.delete(path);
                                break;
                            }
                            default:
                                break;
                        }
                    }
                });
                return;
            }
            case ALL:{
                pathTree = loadPathTree();
                return;
            }
            case NONE:
            default:{
                return;
            }
        }
    }
    
    public PathTree loadPathTree() throws KeeperException, InterruptedException {
        return loadPathTree(rootNode);
    }
    
    public PathTree loadPathTree(final String treeRoot) throws KeeperException, InterruptedException {
        PathTree tree = new PathTree(treeRoot, strategy.getProvider());
        logger.debug("load path tree:{}", treeRoot);
        tree.loading();
        return tree;
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
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        byte[] data = pathTree.getValue(path);
        if (data != null){
            return data;
        }
        return strategy.getData(key);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        List<String> keys = pathTree.getChildren(path);
        if (!keys.isEmpty()){
            return keys;
        }
        return zooKeeper.getChildren(PathUtil.getRealPath(rootNode, key), false);
    }
}
