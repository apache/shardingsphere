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

package io.shardingsphere.orchestration.reg.newzk.client.cache;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Zookeeper cache tree.
 *
 * @author lidongbo
 */
@Slf4j
public final class PathTree implements AutoCloseable {
    
    private final IClient client;
    
    private final IProvider provider;
    
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    
    private final List<String> watcherKeys = new ArrayList<>();
    
    private final transient ReentrantLock lock = new ReentrantLock();
    
    private boolean executorStart;
    
    private ScheduledExecutorService cacheService;
    
    @Getter
    @Setter
    private PathStatus status;
    
    private boolean closed;
    
    public PathTree(final String root, final IClient client) {
        rootNode.set(new PathNode(root));
        status = PathStatus.RELEASE;
        // TODO consider whether to use a new client alternative to the current
        this.client = client;
        provider = client.getExecStrategy().getProvider();
    }
    
    /**
     * Load data.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void load() throws KeeperException, InterruptedException {
        ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                status = PathStatus.CHANGING;
                PathNode newRoot = new PathNode(rootNode.get().getNodeKey());
                List<String> children = provider.getChildren(PathUtil.checkPath(rootNode.get().getNodeKey()));
                children.remove(ZookeeperConstants.CHANGING_KEY);
                attachIntoNode(children, newRoot);
                rootNode.set(newRoot);
                status = PathStatus.RELEASE;
            } else {
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ex) {
                    log.error("loading sleep error: {}", ex.getMessage(), ex);
                }
                load();
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void attachIntoNode(final List<String> children, final PathNode pathNode) throws KeeperException, InterruptedException {
        if (closed) {
            return;
        }
        if (children.isEmpty()) {
            return;
        }
        for (String each : children) {
            String childPath = PathUtil.getRealPath(pathNode.getPath(), each);
            PathNode current = new PathNode(each, provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            attachIntoNode(subs, current);
        }
    }
    
    /**
     * Start thread pool period load data.
     *
     * @param period period
     */
    public void refreshPeriodic(final long period) {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            Preconditions.checkState(!executorStart, "period already set");
            long threadPeriod = period;
            if (threadPeriod < 1) {
                threadPeriod = ZookeeperConstants.THREAD_PERIOD;
            }
            cacheService = Executors.newSingleThreadScheduledExecutor();
            cacheService.scheduleAtFixedRate(new Runnable() {
                
                @Override
                public void run() {
                    if (PathStatus.RELEASE == getStatus()) {
                        try {
                            load();
                        } catch (final KeeperException | InterruptedException ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                }
            }, ZookeeperConstants.THREAD_INITIAL_DELAY, threadPeriod, TimeUnit.MILLISECONDS);
            executorStart = true;
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                
                @Override
                public void run() {
                    stopRefresh();
                }
            }));
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Stop thread pool period load data.
     */
    public void stopRefresh() {
        cacheService.shutdown();
        executorStart = false;
    }
    
    /**
     * Watch data change.
     */
    public void watch() {
        watch(new ZookeeperEventListener(rootNode.get().getNodeKey()) {
            
            @Override
            public void process(final WatchedEvent event) {
                String path = event.getPath();
                switch (event.getType()) {
                    case NodeCreated:
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                        processNodeChange(path);
                        break;
                    case NodeDeleted:
                        delete(path);
                        break;
                    default:
                        break;
                }
            }
        });
    }
    
    /**
     * Watch data change.
     *
     * @param zookeeperEventListener listener
     */
    public void watch(final ZookeeperEventListener zookeeperEventListener) {
        if (closed) {
            return;
        }
        String key = zookeeperEventListener.getKey();
        client.registerWatch(rootNode.get().getNodeKey(), zookeeperEventListener);
        watcherKeys.add(key);
    }
    
    private void processNodeChange(final String path) {
        try {
            String value = provider.getDataString(path);
            put(path, value);
        } catch (final KeeperException | InterruptedException ex) {
            if (ex instanceof KeeperException.NoNodeException || ex instanceof KeeperException.ConnectionLossException) {
                log.debug(ex.getMessage());
                return;
            }
            log.error("PathTree put error : " + ex.getMessage());
        }
    }
    
    /**
     * Get node value.
     *
     * @param path path
     * @return node data
     */
    public byte[] getValue(final String path) {
        if (closed) {
            return null;
        }
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    /**
     * Get children.
     *
     * @param path path
     * @return children
     */
    public List<String> getChildren(final String path) {
        if (closed) {
            return null;
        }
        PathNode node = get(path);
        List<String> result = new ArrayList<>();
        if (node == null) {
            return result;
        }
        if (node.getChildren().isEmpty()) {
            return result;
        }
        for (final PathNode pathNode : node.getChildren().values()) {
            result.add(new String(pathNode.getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path) {
        if (Strings.isNullOrEmpty(path) || path.equals(ZookeeperConstants.PATH_SEPARATOR)) {
            return rootNode.get();
        }
        String realPath = provider.getRealPath(path);
        PathResolve pathResolve = new PathResolve(realPath);
        pathResolve.next();
        if (pathResolve.isEnd()) {
            return rootNode.get();
        }
        return rootNode.get().get(pathResolve);
    }
    
    /**
     * Put node.
     *
     * @param path path
     * @param value value
     */
    public void put(final String path, final String value) {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                setStatus(PathStatus.CHANGING);
                String realPath = provider.getRealPath(path);
                PathResolve pathResolve = new PathResolve(realPath);
                pathResolve.next();
                rootNode.get().set(pathResolve, value);
                setStatus(PathStatus.RELEASE);
            } else {
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ex) {
                    log.error("put sleep error:{}", ex.getMessage(), ex);
                }
                put(path, value);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Delete node.
     *
     * @param path path
     */
    public void delete(final String path) {
        ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            if (rootNode.get().getChildren().containsKey(path)) {
                rootNode.get().getChildren().remove(path);
                return;
            }
            String realPath = provider.getRealPath(path);
            PathResolve pathResolve = new PathResolve(realPath);
            pathResolve.next();
            rootNode.get().delete(pathResolve);
        } finally {
            lock.unlock();
        }
    }
    
    @Override
    public void close() {
        ReentrantLock lock = this.lock;
        lock.lock();
        closed = true;
        try {
            if (executorStart) {
                stopRefresh();
            }
            deleteAllChildren(rootNode.get());
            if (!watcherKeys.isEmpty()) {
                for (String each : watcherKeys) {
                    client.unregisterWatch(each);
                }
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void deleteAllChildren(final PathNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        for (String each : node.getChildren().keySet()) {
            deleteAllChildren(node.getChildren().get(each));
            node.getChildren().remove(each);
        }
    }
}
