/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.StringUtil;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.section.Listener;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/*
 * zookeeper connection holder
 *
 * @author lidongbo
 */
public class Holder {
    private static final Logger logger = LoggerFactory.getLogger(Holder.class);
    private static final CountDownLatch CONNECTED = new CountDownLatch(1);
    
    protected ZooKeeper zooKeeper;
    protected final BaseContext context;
    private boolean connected = false;
    
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
                        connected = true;
                        logger.debug("BaseClient startWatcher SyncConnected");
                        return;
                    } else if (Event.KeeperState.Expired == event.getState()){
                        connected = false;
                        try {
                            logger.warn("startWatcher Event.KeeperState.Expired");
                            reset();
                        } catch (Exception ee){
                            logger.error("event state Expired:{}", ee.getMessage(), ee);
                        }
                    }
                }
                if (context.globalListener != null){
                    context.globalListener.process(event);
                    logger.debug("BaseClient {} process", Constants.GLOBAL_LISTENER_KEY );
                }
                if (!context.getWatchers().isEmpty()){
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
            connected = false;
            logger.debug("zk closed");
            this.context.close();
        } catch (Exception ee){
            logger.warn("Holder close:{}", ee.getMessage());
        }
    }
    
    public ZooKeeper getZooKeeper() {
        return zooKeeper;
    }
    
    public boolean isConnected() {
        return connected;
    }
}
