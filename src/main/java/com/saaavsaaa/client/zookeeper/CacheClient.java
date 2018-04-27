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
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by aaa
 * todo restructure the three clients to strategies
 *  延迟缓存 定时刷新，刷新时先判断更新根节点数据，写成功开始更新，更新后改回根数据
 */
public final class CacheClient extends UsualClient {
    private final ScheduledExecutorService cacheService = Executors.newSingleThreadScheduledExecutor();
    PathTree pathTree = null;
    
    ZooKeeper reader;
    
    CacheClient(String servers, int sessionTimeoutMilliseconds) {
        super(servers, sessionTimeoutMilliseconds);
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        super.start();
        pathTree = new PathTree(rootNode);
        cacheService.scheduleAtFixedRate(new ClientTask(this) {
            @Override
            public void run(Client client) throws KeeperException, InterruptedException {
                loadCache(client);
            }
        }, Properties.THREAD_INITIAL_DELAY, Properties.THREAD_PERIOD, TimeUnit.MILLISECONDS);
    }
    
    //用替换整树的方式更新
    private synchronized void loadCache(final Client client) throws KeeperException, InterruptedException {
        boolean canBegin;
        canBegin = LeaderElection.Contend(rootNode, client, new Listener() {
            @Override
            public void process(WatchedEvent event) {
                try {
                    loadCache(client);
                } catch (Exception ee){
                    System.out.println(ee.getMessage());
                    ee.printStackTrace();
                }
            }
        });
        
        if (canBegin){
            this.loadingCache(client);
            this.deleteOnlyCurrent(Constants.CHANGING_KEY);
        }
    }
    
    private void loadingCache(final Client client) throws KeeperException, InterruptedException {
        pathTree.setStatus(PathStatus.CHANGING);
        PathTree newTree = new PathTree(rootNode);
        List<String> children = client.getChildren(rootNode);
        children.remove(PathUtil.getRealPath(rootNode, Constants.CHANGING_KEY));
        this.attechIntoNode(children, newTree.getRootNode(), client);
        pathTree.setStatus(PathStatus.CHANGING);
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode, final Client client) throws KeeperException, InterruptedException {
        if (children.isEmpty()){
            return;
        }
        for (String child : children) {
            List<String> subs = client.getChildren(child);
            PathNode current = new PathNode(child);
            pathNode.attechChild(current);
            this.attechIntoNode(subs, current, client);
        }
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
