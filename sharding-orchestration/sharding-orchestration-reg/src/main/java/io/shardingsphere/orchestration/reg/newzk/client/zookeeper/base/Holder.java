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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.base;

import com.google.common.base.Strings;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper connection holder.
 *
 * @author lidongbo
 */
@RequiredArgsConstructor
@Getter
@Slf4j
public class Holder {
    
    private final CountDownLatch connectLatch = new CountDownLatch(1);
    
    @Getter(value = AccessLevel.PROTECTED)
    private final BaseContext context;
    
    private ZooKeeper zooKeeper;
    
    @Setter(value = AccessLevel.PROTECTED)
    private boolean connected;
    
    /**
     * Start.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    public void start() throws IOException, InterruptedException {
        initZookeeper();
        connectLatch.await();
    }
    
    protected void start(final int wait, final TimeUnit units) throws IOException, InterruptedException {
        initZookeeper();
        connectLatch.await(wait, units);
    }
    
    protected void initZookeeper() throws IOException {
        zooKeeper = new ZooKeeper(context.getServers(), context.getSessionTimeOut(), startWatcher());
        if (!Strings.isNullOrEmpty(context.getScheme())) {
            zooKeeper.addAuthInfo(context.getScheme(), context.getAuth());
        }
    }
    
    private Watcher startWatcher() {
        return new Watcher() {
            
            @Override
            public void process(final WatchedEvent event) {
                processConnection(event);
                if (!isConnected()) {
                    return;
                }
                processGlobalListener(event);
                // TODO filter event type or path
                if (event.getType() == Event.EventType.None) {
                    return;
                }
                if (Event.EventType.NodeDeleted == event.getType() || checkPath(event.getPath())) {
                    processUsualListener(event);
                }
            }
        };
    }
    
    protected void processConnection(final WatchedEvent event) {
        if (Watcher.Event.EventType.None == event.getType()) {
            if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
                connectLatch.countDown();
                connected = true;
            } else if (Watcher.Event.KeeperState.Expired == event.getState()) {
                connected = false;
                try {
                    reset();
                } catch (final IOException | InterruptedException ex) {
                    log.error("event state Expired: {}", ex.getMessage(), ex);
                }
            } else if (Watcher.Event.KeeperState.Disconnected == event.getState()) {
                connected = false;
            }
        }
    }
    
    private void processGlobalListener(final WatchedEvent event) {
        if (null != context.getGlobalZookeeperEventListener()) {
            context.getGlobalZookeeperEventListener().process(event);
        }
    }
    
    private void processUsualListener(final WatchedEvent event) {
        if (!context.getWatchers().isEmpty()) {
            for (ZookeeperEventListener zookeeperEventListener : context.getWatchers().values()) {
                if (null == zookeeperEventListener.getPath() || event.getPath().startsWith(zookeeperEventListener.getPath())) {
                    zookeeperEventListener.process(event);
                }
            }
        }
    }
    
    private boolean checkPath(final String path) {
        try {
            return null != zooKeeper.exists(path, true);
        } catch (final KeeperException | InterruptedException ignore) {
            return false;
        }
    }
    
    /**
     * Reset connection.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    public void reset() throws IOException, InterruptedException {
        close();
        start();
    }
    
    /**
     * Close.
     */
    public void close() {
        try {
            zooKeeper.register(new Watcher() {
                
                @Override
                public void process(final WatchedEvent watchedEvent) {
        
                }
            });
            zooKeeper.close();
            connected = false;
            context.close();
        } catch (final InterruptedException ex) {
            log.warn("Holder close:{}", ex.getMessage());
        }
    }
}
