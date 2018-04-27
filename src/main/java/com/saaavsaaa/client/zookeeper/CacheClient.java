package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.cache.PathTree;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.List;
import java.util.Stack;

/**
 * Created by aaa
 * todo restructure the three clients to strategies
 *  延迟缓存 定时刷新，刷新时先判断更新根节点数据，写成功开始更新，更新后改回根数据
 */
public final class CacheClient extends UsualClient {
    PathTree pathTree = null;
    
    ZooKeeper reader;
    
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        super.start();
        pathTree = new PathTree(rootNode);
    }
    
    //用替换整树的方式更新
    private synchronized void loadCache(Client client) throws KeeperException, InterruptedException {
        try {
            this.createCurrentOnly(Constants.CHANGING_KEY, Constants.NOTHING_VALUE, CreateMode.EPHEMERAL);
        } catch (KeeperException.NodeExistsException e) {
            this.checkExists(Constants.CHANGING_KEY, WatcherCreator.deleteWatcher(PathUtil.getRealPath(rootNode, Constants.CHANGING_KEY), new Listener() {
                @Override
                public void process(WatchedEvent event) {
                    try {
                        loadCache(client);
                    } catch (Exception ee){
                        System.out.println(ee.getMessage());
                        ee.printStackTrace();
                    }
                }
            }));
        }
        
        this.deleteOnlyCurrent(Constants.CHANGING_KEY);
    }
    
    /*
    * closed beta
    */
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(key, value, createMode);
            return;
        }
        Transaction transaction = zooKeeper.transaction();
        //todo sync cache
        List<String> nodes = PathUtil.getPathOrderNodes(rootNode, key);
        for (int i = 0; i < nodes.size(); i++) {
            // todo contrast cache
            if (checkExists(nodes.get(i))){
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
        
        // todo org.apache.zookeeper.KeeperException$NodeExistsException: KeeperErrorCode = NodeExists
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
        this.deleteAllChild(key, transaction);
        transaction.commit();
    }
    
    private void deleteAllChild(final String key, final Transaction transaction) throws KeeperException, InterruptedException {
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
}
