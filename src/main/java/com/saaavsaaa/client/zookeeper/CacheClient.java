package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.cache.CacheStrategy;
import com.saaavsaaa.client.cache.PathTree;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.zookeeper.base.BaseContext;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Created by aaa
 * partially prepared products
 */
public final class CacheClient extends UsualClient {
    private static final Logger logger = LoggerFactory.getLogger(CacheClient.class);
    protected PathTree pathTree = null;
    
    CacheClient(final BaseContext context) {
        super(context);
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        super.start();
        try {
            useCacheStrategy(CacheStrategy.WATCH);
        } catch (KeeperException e) {
            logger.error("CacheClient useCacheStrategy : " + e.getMessage());
        }
    }
    
    @Override
    public void close(){
        super.close();
        this.strategies.clear();
        this.pathTree.close();
    }
    
    //todo put it here?
    void useCacheStrategy(CacheStrategy cacheStrategy) throws KeeperException, InterruptedException {
        logger.debug("use cache strategy:{}", cacheStrategy);
        switch (cacheStrategy){
            case WATCH:{
                pathTree = new PathTree(rootNode, this);
                pathTree.watch();
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
        PathTree tree = new PathTree(treeRoot, this);
        logger.debug("load path tree:{}", treeRoot);
        tree.loading();
        return tree;
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        super.createCurrentOnly(key, value, createMode);
        pathTree.put(PathUtil.getRealPath(rootNode, key), value);
    }
    
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        super.deleteOnlyCurrent(key);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        super.deleteOnlyCurrent(key, callback, ctx);
        pathTree.delete(PathUtil.getRealPath(rootNode, key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        byte[] data = pathTree.getValue(path);
        if (data != null){
            logger.debug("getData cache hit:{}", data);
            return data;
        }
        logger.debug("getData cache not hit:{}", data);
        return strategy.getData(key);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(rootNode, key);
        List<String> keys = pathTree.getChildren(path);
        if (!keys.isEmpty()){
            logger.debug("getChildren cache hit:{}", keys);
            return keys;
        }
        logger.debug("getChildren cache not hit:{}", keys);
        return strategy.getChildren(PathUtil.getRealPath(rootNode, key));
    }
}
