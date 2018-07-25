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

import io.shardingsphere.core.parsing.lexer.Lexer;
import io.shardingsphere.core.parsing.lexer.LexerEngine;
import io.shardingsphere.core.parsing.lexer.analyzer.Dictionary;
import io.shardingsphere.core.parsing.lexer.token.Symbol;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.UsualClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.section.WatchedDataEvent;
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
import org.apache.zookeeper.common.PathUtils;

/*
 * Zookeeper cache tree.
 *
 * @author lidongbo
 */
@Slf4j
public final class PathTree {
    
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
        this.rootNode.set(new PathNode(PathUtil.checkPath(root)));
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
        
                PathNode newRoot = new PathNode(rootNode.get().getKey());
                List<String> children = provider.getChildren(rootNode.get().getKey());
                children.remove(PathUtil.getRealPath(rootNode.get().getKey(), ZookeeperConstants.CHANGING_KEY));
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
            String childPath = PathUtil.getRealPath(pathNode.getPath(), each);
            PathNode current = new PathNode(PathUtil.checkPath(each), provider.getData(childPath));
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
                            // CHECKSTYLE:OFF
                        } catch (final Exception ex) {
                            // CHECKSTYLE:ON
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
        log.debug("stopRefresh");
    }
    
    /**
     * Watch data change.
     */
    public void watch() {
        watch(new ZookeeperEventListener(rootNode.get().getKey()) {
            @Override
            public void process(final WatchedDataEvent event) {
                String path = event.getPath();
                log.debug("PathTree Watch event:{}", event.toString());
                switch (event.getType()) {
                    case NodeCreated:
                    case NodeDataChanged:
                    case NodeChildrenChanged:
                        processNodeChange(path, event.getData());
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
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                log.debug("PathTree Unregister Watch:{}", key);
                client.unregisterWatch(key);
            }
        }));
    }
    
    private void processNodeChange(final String path, final String value) {
        try {
            put(path, value);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
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
        PathNode node = get(path);
        return null == node ? null : node.getValue();
    }
    
    private Iterator<String> keyIterator(final String path) {
        List<String> nodes = PathUtil.getShortPathNodes(path);
        log.debug("keyIterator path{},nodes:{}", path, nodes);
        Iterator<String> iterator = nodes.iterator();
        // root
        iterator.next();
        return iterator;
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
            log.info("getChildren null");
            return result;
        }
        if (node.getChildren().isEmpty()) {
            log.info("getChildren no child");
            return result;
        }
        Iterator<PathNode> children = node.getChildren().values().iterator();
        while (children.hasNext()) {
            result.add(new String(children.next().getValue()));
        }
        return result;
    }
    
    private PathNode get(final String path) {
        log.debug("PathTree get:{}", path);
        String realPath = provider.getRealPath(path);
        if (realPath.equals(rootNode.get().getKey())) {
            return rootNode.get();
        }
        // todo iterator -> token LexerEngine
        Iterator<String> iterator = keyIterator(realPath);
        if (iterator.hasNext()) {
            return rootNode.get().get(iterator);
        }
        log.debug("{} not exist", realPath);
        return null;
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
            log.debug("cache put:{},value:{}", path, value);
            PathUtils.validatePath(path);
            log.debug("put status:{}", status);
            if (status == PathStatus.RELEASE) {
                if (path.equals(rootNode.get().getKey())) {
                    rootNode.set(new PathNode(rootNode.get().getKey(), value.getBytes(ZookeeperConstants.UTF_8)));
                    return;
                }
                this.setStatus(PathStatus.CHANGING);
                rootNode.get().set(keyIterator(path), value);
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
            if (rootNode.get().getChildren().containsKey(PathUtil.checkPath(path))) {
                rootNode.get().getChildren().remove(PathUtil.checkPath(path));
                return;
            }
    
            final LexerEngine lexerEngine = new LexerEngine(new Lexer(path, new Dictionary()));
            lexerEngine.nextToken();
            lexerEngine.skipIfEqual(Symbol.SLASH);
            if (rootNode.get().getKey().equals(PathUtil.checkPath(lexerEngine.getCurrentToken().getLiterals()))) {
                lexerEngine.nextToken();
                lexerEngine.skipIfEqual(Symbol.SLASH);
            }
            rootNode.get().delete(lexerEngine.getCurrentToken().getLiterals(), lexerEngine);
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
            // CHECKSTYLE:OFF
        } catch (final Exception ex){
            // CHECKSTYLE:ON
            log.warn("PathTree close:{}", ex.getMessage());
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
