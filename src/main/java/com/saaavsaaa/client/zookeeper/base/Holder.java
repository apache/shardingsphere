package com.saaavsaaa.client.zookeeper.base;

import com.saaavsaaa.client.utility.Properties;
import com.saaavsaaa.client.utility.StringUtil;
import com.saaavsaaa.client.utility.constant.Constants;
import com.saaavsaaa.client.zookeeper.section.Listener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aaa
 */
public class Holder {
    private static final Logger logger = LoggerFactory.getLogger(Holder.class);
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    
    protected ZooKeeper zooKeeper;
    protected final BaseContext context;
    
    Holder(final BaseContext context){
        this.context = context;
    }
    
    public void start() throws IOException, InterruptedException {
        logger.debug("Holder servers:{},sessionTimeOut:{}", context.servers, context.sessionTimeOut);
        zooKeeper = new ZooKeeper(context.servers, context.sessionTimeOut, startWatcher());
        if (!StringUtil.isNullOrBlank(context.scheme)) {
            zooKeeper.addAuthInfo(context.scheme, context.auth);
            logger.debug("Holder scheme:{},auth:{}", context.scheme, context.auth);
        }
        CONNECTED.await();
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
                if (context.globalListener != null){
                    context.globalListener.process(event);
                    logger.debug("BaseClient " + Constants.GLOBAL_LISTENER_KEY + " process");
                }
                if (Properties.INSTANCE.watchOn()){
                    for (Listener listener : context.getWatchers().values()) {
                        if (listener.getPath() == null || listener.getPath().equals(event.getPath())){
                            logger.debug("listener process:{}, listener:{}", listener.getPath(), listener.getKey());
                            listener.process(event);
                        }
                    }
                }
            }
        };
    }
    
    public void reset() throws IOException, InterruptedException {
        logger.debug("zk reset....................................");
        close();
        start();
        logger.debug("....................................zk reset");
    }
    
    public void close() {
        try {
            zooKeeper.close();
            logger.debug("zk closed");
            this.context.close();
        } catch (Exception ee){
            logger.warn("Holder close:{}", ee.getMessage());
        }
    }
    
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
}
