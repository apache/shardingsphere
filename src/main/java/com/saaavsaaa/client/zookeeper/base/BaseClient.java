package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.action.IClient;
import com.saaavsaaa.client.section.ClientContext;
import com.saaavsaaa.client.utility.PathUtil;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.section.Listener;
import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.section.WatcherCreator;
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
public abstract class BaseClient implements IClient {
    private static final Logger logger = LoggerFactory.getLogger(BaseClient.class);
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
//    protected static final Map<String, Listener> watchers = new ConcurrentHashMap<>();
    
    protected final boolean watched = true; //false
    private Listener globalListener;
    protected String rootNode = "/InitValue";
    protected boolean rootExist = false;
    private final String servers;
    private final int sessionTimeOut;
    private String scheme;
    private byte[] auth;
    protected ClientContext context;
    
    protected ZooKeeper zooKeeper;
    protected List<ACL> authorities;
//    private BaseClientFactory clientFactory;
    
    protected BaseClient(final String servers, final int sessionTimeoutMilliseconds) {
        this.servers = servers;
        this.sessionTimeOut = sessionTimeoutMilliseconds;
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        logger.debug("BaseClient servers:{},sessionTimeOut:{}", servers, sessionTimeOut);
        zooKeeper = new ZooKeeper(servers, sessionTimeOut, startWatcher());
        if (!StringUtil.isNullOrBlank(scheme)) {
            zooKeeper.addAuthInfo(scheme, auth);
            logger.debug("BaseClient scheme:{},auth:{}", scheme, auth);
        }
        CONNECTED.await();
        context.setProvider(new BaseProvider(rootNode, zooKeeper, watched, authorities));
    }
    
    @Override
    public void close() {
        try {
            zooKeeper.close();
            logger.debug("zk closed");
            this.context.close();
        } catch (Exception ee){
            logger.warn("BaseClient close:{}", ee.getMessage());
        }
    }
    
    public abstract void useExecStrategy(StrategyType strategyType);
    
    ZooKeeper getZooKeeper(){
        return zooKeeper;
    }
    
    private Watcher startWatcher() {
        return new Watcher(){
            public void process(WatchedEvent event) {
                logger.debug("BaseClient process event:{}", event.toString());
                if(Event.EventType.None == event.getType()){
                    if(Event.KeeperState.SyncConnected == event.getState()){
                        CONNECTED.countDown();
                        logger.debug("BaseClient startWatcher SyncConnected");
                        return;
                    } else if (Event.KeeperState.Expired == event.getState()){
                        try {
                            logger.warn("startWatcher Event.KeeperState.Expired");
                            start();
                        } catch (Exception ee){
                            logger.error("event state Expired:{}", ee.getMessage(), ee);
                        }
                    }
                }
                if (globalListener != null){
                    globalListener.process(event);
                    logger.debug("BaseClient " + Constants.GLOBAL_LISTENER_KEY + " process");
                }
                if (Properties.INSTANCE.watchOn()){
                    for (Listener listener : context.getWatchers().values()) {
                        if (listener.getPath() == null || listener.getPath().equals(event.getPath())){
                            listener.process(event);
                        }
                    }
                }
            }
        };
    }
    
    void registerWatch(final Listener globalListener){
        if (globalListener != null){
            logger.warn("global listener can only register one");
            return;
        }
        this.globalListener = globalListener;
        logger.debug("globalListenerRegistered:{}", globalListener.getKey());
    }
    
    @Override
    public void registerWatch(final String key, final Listener listener){
        String path = PathUtil.getRealPath(rootNode, key);
        listener.setPath(path);
        context.getWatchers().put(listener.getKey(), listener);
        logger.debug("register watcher:{}", path);
    }
    
    @Override
    public void unregisterWatch(final String key){
        if (StringUtil.isNullOrBlank(key)){
            throw new IllegalArgumentException("key should not be blank");
        }
//        String path = PathUtil.getRealPath(rootNode, key);
        if (context.getWatchers().containsKey(key)){
            context.getWatchers().remove(key);
            logger.debug("unregisterWatch:{}", key);
        }
    }
    
    protected void createNamespace() throws KeeperException, InterruptedException {
        createNamespace(Constants.NOTHING_DATA);
    }
    
   private void createNamespace(final byte[] date) throws KeeperException, InterruptedException {
        if (rootExist){
            logger.debug("root exist");
            return;
        }
        try {
            zooKeeper.create(rootNode, date, authorities, CreateMode.PERSISTENT);
            logger.debug("creating root:{}", rootNode);
        } catch (KeeperException.NodeExistsException ee){
            logger.warn("root create:{}", ee.getMessage());
            rootExist = true;
            return;
        }
        rootExist = true;
        zooKeeper.exists(rootNode, WatcherCreator.deleteWatcher(new Listener(rootNode) {
            @Override
            public void process(WatchedEvent event) {
                rootExist = false;
            }
        }));
        logger.debug("created root:{}", rootNode);
    }
    
    protected void deleteNamespace() throws KeeperException, InterruptedException {
        zooKeeper.delete(rootNode, Constants.VERSION);
        rootExist = false;
        logger.debug("delete root:{},rootExist:{}", rootNode, rootExist);
    }
    
    void setRootNode(final String rootNode) {
        this.rootNode = rootNode;
    }
    
    void setAuthorities(final String scheme, final byte[] auth) {
        this.scheme = scheme;
        this.auth = auth;
        this.authorities = ZooDefs.Ids.CREATOR_ALL_ACL;
    }
    
    void setContext(final ClientContext context){
        this.context = context;
    }
    public ClientContext getContext(){
        return context;
    }
    
    BaseClientFactory getClientFactory() {
        return context.getClientFactory();
    }
}
