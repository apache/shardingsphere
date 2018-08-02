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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.UsualClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.ZookeeperEventListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;

/*
 * Zookeeper cache tree.
 *
 * @author lidongbo
 */
@Slf4j
public final class PathTree {
    
    private final transient ReentrantLock lock = new ReentrantLock();
    
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    
    private final List<String> watcherKeys = new ArrayList<>();
    
    private boolean executorStart;
    
    private ScheduledExecutorService cacheService;
    
    private final IClient client;
    
    private final IProvider provider;
    
    @Getter
    @Setter
    private PathStatus status;
    
    private boolean closed;
    
    public PathTree(final String root, final IClient client) {
        this.rootNode.set(new PathNode(root));
        this.status = PathStatus.RELEASE;
        this.client = client;
        // todo It looks unpleasant
        this.provider = ((UsualClient) client).getStrategy().getProvider();
    }
    
    /**
     * Load data.
     *
     * @throws KeeperException Zookeeper Exception
     * @throws InterruptedException InterruptedException
     */
    public void load() throws KeeperException, InterruptedException {
        final ReentrantLock lock = this.lock;
        lock.lockInterruptibly();
        if (closed) {
            return;
        }
        try {
            if (status == PathStatus.RELEASE) {
                log.debug("loading status:{}", status);
                status = PathStatus.CHANGING;
    
                final PathNode newRoot = new PathNode(rootNode.get().getKey());
                final List<String> children = provider.getChildren(PathUtil.checkPath(rootNode.get().getKey()));
                children.remove(ZookeeperConstants.CHANGING_KEY);
                attachIntoNode(children, newRoot);
                rootNode.set(newRoot);
    
                status = PathStatus.RELEASE;
                log.debug("loading release:{}", status);
            } else {
                log.info("loading but cache status not release");
                try {
                    Thread.sleep(10L);
                } catch (final InterruptedException ex) {
                    log.error("loading sleep error:{}", ex.getMessage(), ex);
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
        log.debug("attechIntoNode children:{}", children);
        if (children.isEmpty()) {
            log.info("attechIntoNode there are no children");
            return;
        }
        for (String each : children) {
            final String childPath = PathUtil.getRealPath(pathNode.getPath(), each);
            final PathNode current = new PathNode(each, provider.getData(childPath));
            pathNode.attachChild(current);
            final List<String> subs = provider.getChildren(childPath);
            attachIntoNode(subs, current);
        }
    }
    
    /**
     * Start thread pool period load data.
     *
     * @param period period
     */
    public void refreshPeriodic(final long period) {
        final ReentrantLock lock = this.lock;
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
            log.debug("refreshPeriodic:{}", period);
            cacheService = Executors.newSingleThreadScheduledExecutor();
            cacheService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    log.debug("cacheService run:{}", getStatus());
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
                    log.debug("cacheService stop");
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
        log.debug("stopRefresh");
    }
    
    /**
     * Watch data change.
     */
    public void watch() {
        watch(new ZookeeperEventListener(rootNode.get().getKey()) {
            @Override
            public void process(final WatchedEvent event) {
                String path = event.getPath();
                log.debug("PathTree Watch event:{}", event.toString());
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
        final String key = zookeeperEventListener.getKey();
        log.debug("PathTree Watch:{}", key);
        client.registerWatch(rootNode.get().getKey(), zookeeperEventListener);
        watcherKeys.add(key);
    }
    
    private void processNodeChange(final String path) {
        try {
            final String value = provider.getDataString(path);
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
     * Get root node.
     *
     * @return root node
     */
    public PathNode getRootNode() {
        return rootNode.get();
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
        final PathNode node = get(path);
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
        final PathNode node = get(path);
        final List<String> result = new ArrayList<>();
        if (node == null) {
            log.info("getChildren null");
            return result;
        }
        if (node.getChildren().isEmpty()) {
            log.info("getChildren no child");
            return result;
        }
        final Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()) {
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path) {
        log.debug("PathTree get:{}", path);
        if (Strings.isNullOrEmpty(path) || path.equals(ZookeeperConstants.PATH_SEPARATOR)) {
            return rootNode.get();
        }
        final String realPath = provider.getRealPath(path);
        final PathResolve pathResolve = new PathResolve(realPath);
        pathResolve.next();
        if (pathResolve.isEnd()) {
            log.info("path node get() hit root!");
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
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            log.debug("cache put:{},value:{},status:{}", path, value, status);
            if (status == PathStatus.RELEASE) {
                this.setStatus(PathStatus.CHANGING);
                final String realPath = provider.getRealPath(path);
                final PathResolve pathResolve = new PathResolve(realPath);
                pathResolve.next();
                rootNode.get().set(pathResolve, value);
                this.setStatus(PathStatus.RELEASE);
            } else {
                try {
                    log.debug("put but cache status not release");
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
        log.debug("PathTree begin delete:{}", path);
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        
        try {
            if (rootNode.get().getChildren().containsKey(path)) {
                rootNode.get().getChildren().remove(path);
                return;
            }
            final String realPath = provider.getRealPath(path);
            final PathResolve pathResolve = new PathResolve(realPath);
            pathResolve.next();
            rootNode.get().delete(pathResolve);
            log.debug("PathTree end delete:{}", path);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Close.
     */
    public void close() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        this.closed = true;
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
