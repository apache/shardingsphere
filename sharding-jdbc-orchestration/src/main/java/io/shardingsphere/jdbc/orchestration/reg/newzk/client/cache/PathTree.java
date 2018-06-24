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

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.UsualClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.Listener;
import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.common.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/*
 * zookeeper cache tree
 *
 * @author lidongbo
 */
public final class PathTree {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathTree.class);
    
    private final transient ReentrantLock lock = new ReentrantLock();
    
    private final AtomicReference<PathNode> rootNode = new AtomicReference<>();
    
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
     * load data.
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
                LOGGER.debug("loading status:{}", status);
                this.setStatus(PathStatus.CHANGING);
        
                PathNode newRoot = new PathNode(rootNode.get().getKey());
                List<String> children = provider.getChildren(rootNode.get().getKey());
                children.remove(PathUtil.getRealPath(rootNode.get().getKey(), Constants.CHANGING_KEY));
                this.attechIntoNode(children, newRoot);
                rootNode.set(newRoot);
        
                this.setStatus(PathStatus.RELEASE);
//                watch();
                LOGGER.debug("loading release:{}", status);
            } else {
                LOGGER.info("loading but cache status not release");
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    LOGGER.error("loading sleep error:{}", e.getMessage(), e);
                }
                load();
            }
        } finally {
            lock.unlock();
        }
    }
    
    private void attechIntoNode(final List<String> children, final PathNode pathNode) throws KeeperException, InterruptedException {
        if (closed) {
            return;
        }
        LOGGER.debug("attechIntoNode children:{}", children);
        if (children.isEmpty()) {
            LOGGER.info("attechIntoNode there are no children");
            return;
        }
        for (String child : children) {
            String childPath = PathUtil.getRealPath(pathNode.getKey(), child);
            PathNode current = new PathNode(PathUtil.checkPath(child), provider.getData(childPath));
            pathNode.attachChild(current);
            List<String> subs = provider.getChildren(childPath);
            this.attechIntoNode(subs, current);
        }
    }
    
    /**
     * start thread pool period load data.
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
            if (executorStart) {
                throw new IllegalArgumentException("period already set");
            }
            long threadPeriod = period;
            if (threadPeriod < 1) {
                threadPeriod = Constants.THREAD_PERIOD;
            }
            LOGGER.debug("refreshPeriodic:{}", period);
            cacheService = Executors.newSingleThreadScheduledExecutor();
            cacheService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    LOGGER.debug("cacheService run:{}", getStatus());
                    if (PathStatus.RELEASE == getStatus()) {
                        try {
                            load();
                            // CHECKSTYLE:OFF
                        } catch (Exception e) {
                            // CHECKSTYLE:ON
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                }
            }, Constants.THREAD_INITIAL_DELAY, threadPeriod, TimeUnit.MILLISECONDS);
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
     * stop thread pool period load data.
     */
    public void stopRefresh() {
        cacheService.shutdown();
        executorStart = false;
        LOGGER.debug("stopRefresh");
    }
    
    /**
     * watch data change.
     */
    public void watch() {
        watch(new Listener(rootNode.get().getKey()) {
            @Override
            public void process(final WatchedEvent event) {
                String path = event.getPath();
                LOGGER.debug("PathTree Watch event:{}", event.toString());
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
     * watch data change.
     *
     * @param listener listener
     */
    public void watch(final Listener listener) {
        if (closed) {
            return;
        }
        final String key = listener.getKey();
        LOGGER.debug("PathTree Watch:{}", key);
        client.registerWatch(rootNode.get().getKey(), listener);
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("PathTree Unregister Watch:{}", key);
                client.unregisterWatch(key);
            }
        }));
    }
    
    private void processNodeChange(final String path) {
        try {
            String value = Constants.NOTHING_VALUE;
            if (!path.equals(getRootNode().getKey())) {
                value = provider.getDataString(path);
            }
            put(path, value);
            // CHECKSTYLE:OFF
        } catch (Exception e) {
            // CHECKSTYLE:ON
            LOGGER.error("PathTree put error : " + e.getMessage());
        }
    }
    
    /**
     * get root node.
     *
     * @return root node
     */
    public PathNode getRootNode() {
        return rootNode.get();
    }
    
    /**
     * get node value.
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
    
    private Iterator<String> keyIterator(final String path) {
        List<String> nodes = PathUtil.getShortPathNodes(path);
        LOGGER.debug("keyIterator path{},nodes:{}", path, nodes);
        Iterator<String> iterator = nodes.iterator();
        // root
        iterator.next();
        return iterator;
    }
    
    /**
     * get children.
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
            LOGGER.info("getChildren null");
            return result;
        }
        if (node.getChildren().isEmpty()) {
            LOGGER.info("getChildren no child");
            return result;
        }
        Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()) {
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path) {
        LOGGER.debug("PathTree get:{}", path);
        PathUtils.validatePath(path);
        if (path.equals(rootNode.get().getKey())) {
            return rootNode.get();
        }
        // todo iterator -> token
        Iterator<String> iterator = keyIterator(path);
        if (iterator.hasNext()) {
            return rootNode.get().get(iterator);
        }
        LOGGER.debug("{} not exist", path);
        return null;
    }
    
    /**
     * put node.
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
            LOGGER.debug("cache put:{},value:{}", path, value);
            PathUtils.validatePath(path);
            LOGGER.debug("put status:{}", status);
            if (status == PathStatus.RELEASE) {
                if (path.equals(rootNode.get().getKey())) {
                    rootNode.set(new PathNode(rootNode.get().getKey(), value.getBytes(Constants.UTF_8)));
                    return;
                }
                this.setStatus(PathStatus.CHANGING);
                rootNode.get().set(keyIterator(path), value);
                this.setStatus(PathStatus.RELEASE);
            } else {
                try {
                    LOGGER.debug("put but cache status not release");
                    Thread.sleep(10L);
                } catch (InterruptedException e) {
                    LOGGER.error("put sleep error:{}", e.getMessage(), e);
                }
                put(path, value);
            }
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * delete node.
     *
     * @param path path
     */
    public void delete(final String path) {
        LOGGER.debug("PathTree begin delete:{}", path);
        final ReentrantLock lock = this.lock;
        lock.lock();
        if (closed) {
            return;
        }
        try {
            PathUtils.validatePath(path);
//            String prxpath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
            PathNode node = get(path);
            node.getChildren().remove(path);
            LOGGER.debug("PathTree end delete:{}", path);
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * close.
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
            // CHECKSTYLE:OFF
        } catch (Exception ee){
            // CHECKSTYLE:ON
            LOGGER.warn("PathTree close:{}", ee.getMessage());
        } finally {
            lock.unlock();
        }
    }
    
    private void deleteAllChildren(final PathNode node) {
        if (node.getChildren().isEmpty()) {
            return;
        }
        for (String one : node.getChildren().keySet()) {
            deleteAllChildren(node.getChildren().get(one));
            node.getChildren().remove(one);
        }
    }
}
