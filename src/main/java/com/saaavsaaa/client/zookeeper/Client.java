package com.saaavsaaa.client.zookeeper;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.utility.section.Listener;
import com.saaavsaaa.client.utility.section.Properties;
import com.saaavsaaa.client.utility.section.WatcherCreator;
import com.saaavsaaa.client.zookeeper.strategy.StrategyType;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa
 */
public abstract class Client implements IClient {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    protected static final Map<String, Watcher> watchers = new ConcurrentHashMap<>();
    
    protected final boolean watched = true; //false
    private final String servers;
    private final int sessionTimeOut;
    private String scheme;
    private byte[] auth;
    
    protected ZooKeeper zooKeeper;
    protected List<ACL> authorities;
    private boolean globalListenerRegistered = false;
    protected String rootNode = "/InitValue";
    protected boolean rootExist = false;
    private ClientFactory clientFactory;
    
    protected Client(final String servers, final int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    public synchronized void start() throws IOException, InterruptedException {
        logger.debug("Client servers:{},sessionTimeOut:{}", servers, sessionTimeOut);
        zooKeeper = new ZooKeeper(servers, sessionTimeOut, startWatcher());
        if (!StringUtil.isNullOrBlank(scheme)) {
            zooKeeper.addAuthInfo(scheme, auth);
            logger.debug("Client scheme:{},auth:{}", scheme, auth);
        }
        CONNECTED.await();
    }
    
    public abstract void useExecStrategy(StrategyType strategyType);
    
    ZooKeeper getZooKeeper(){
        return zooKeeper;
    }
    
    private Watcher startWatcher() {
        return new Watcher(){
            public void process(WatchedEvent event) {
                logger.debug("Client process event:{}", event.toString());
                if(Event.KeeperState.SyncConnected == event.getState()){
                    if(Event.EventType.None == event.getType()){
                        CONNECTED.countDown();
                        logger.debug("Client startWatcher SyncConnected");
                        return;
                    }
                }
                if (globalListenerRegistered){
                    watchers.get(Constants.GLOBAL_LISTENER_KEY).process(event);
                    logger.debug("Client " + Constants.GLOBAL_LISTENER_KEY + " process");
                }
                if (Properties.INSTANCE.watchOn() && watchers.containsKey(event.getPath())){
                     watchers.get(event.getPath()).process(event);
                }
            }
        };
    }
    
    void registerWatch(final Listener globalListener){
        if (globalListenerRegistered){
            logger.warn("global listener can only register one");
            return;
        }
        watchers.put(Constants.GLOBAL_LISTENER_KEY, new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                globalListener.process(event);
            }
        });
        globalListenerRegistered = true;
        logger.debug("globalListenerRegistered:{}", globalListenerRegistered);
    }
    
    public Watcher registerWatch(final String key, final Listener listener){
        String path = PathUtil.getRealPath(rootNode, key);
//        listener.setKey(path);
        Watcher watcher = new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                listener.process(event);
            }
        };
        watchers.put(path, watcher);
        logger.debug("register watcher:{}", path);
        return watcher;
    }
    
    public void unregisterWatch(final String key){
        if (StringUtil.isNullOrBlank(key)){
            throw new IllegalArgumentException("key should not be blank");
        }
        String path = PathUtil.getRealPath(rootNode, key);
        if (watchers.containsKey(path)){
            watchers.remove(path);
            logger.debug("unregisterWatch:{}", path);
        }
    }
    
    public void close() throws InterruptedException {
        zooKeeper.close();
        logger.debug("zk closed");
    }
    
    void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(Constants.NOTHING_DATA);
    }
    
   private void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
        if (rootExist){
            logger.debug("root exist");
            return;
        }
        try {
            zooKeeper.create(rootNode, date, authorities, CreateMode.PERSISTENT);
            logger.debug("create root:{}", rootNode);
        } catch (KeeperException.NodeExistsException ee){
            logger.warn("root create:{}", ee.getMessage());
            rootExist = true;
            return;
        }
        rootExist = true;
        zooKeeper.exists(rootNode, WatcherCreator.deleteWatcher(rootNode, new Listener() {
            @Override
            public void process(WatchedEvent event) {
                rootExist = false;
            }
        }));
        logger.debug("create root:{}", rootNode);
    }
    
    void deleteNamespace() throws KeeperException, InterruptedException {
        zooKeeper.delete(rootNode, Constants.VERSION);
        rootExist = false;
        logger.debug("create root:{},rootExist:{}", rootNode, rootExist);
    }
    
    void setRootNode(final String rootNode) {
        this.rootNode = rootNode;
    }
    
    void setAuthorities(final String scheme, final byte[] auth) {
        this.scheme = scheme;
        this.auth = auth;
        this.authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
    
    void setClientFactory(ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }
    ClientFactory getClientFactory() {
        return clientFactory;
    }
}
