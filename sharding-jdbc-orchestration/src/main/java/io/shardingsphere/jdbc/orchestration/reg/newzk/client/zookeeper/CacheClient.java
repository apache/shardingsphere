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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.CacheStrategy;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.cache.PathTree;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseContext;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/*
 * todo
 * @author lidongbo
 */
public final class CacheClient extends UsualClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(CacheClient.class);
    
    private PathTree pathTree;
    
    CacheClient(final BaseContext context) {
        super(context);
    }
    
    @Override
    public void start() throws IOException, InterruptedException {
        super.start();
        try {
            useCacheStrategy(CacheStrategy.WATCH);
        } catch (KeeperException e) {
            LOGGER.error("CacheClient useCacheStrategy : " + e.getMessage());
        }
    }
    
    @Override
    public void close() {
        super.close();
        this.pathTree.close();
    }
    
    //todo put it here?
    void useCacheStrategy(final CacheStrategy cacheStrategy) throws KeeperException, InterruptedException {
        LOGGER.debug("use cache strategy:{}", cacheStrategy);
        switch (cacheStrategy) {
            case WATCH:
                pathTree = new PathTree(getRootNode(), this);
                pathTree.watch();
                return;
            case ALL:
                pathTree = loadPathTree();
                pathTree.refreshPeriodic(Constants.THREAD_PERIOD);
                return;
            case NONE:
            default:
                return;
        }
    }
    
    private PathTree loadPathTree() throws KeeperException, InterruptedException {
        return loadPathTree(getRootNode());
    }
    
    private PathTree loadPathTree(final String treeRoot) throws KeeperException, InterruptedException {
        PathTree tree = new PathTree(treeRoot, this);
        LOGGER.debug("load path tree:{}", treeRoot);
        tree.load();
        tree.watch();
        return tree;
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        super.createCurrentOnly(key, value, createMode);
        pathTree.put(PathUtil.getRealPath(getRootNode(), key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        super.deleteOnlyCurrent(key);
        pathTree.delete(PathUtil.getRealPath(getRootNode(), key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        super.deleteOnlyCurrent(key, callback, ctx);
        pathTree.delete(PathUtil.getRealPath(getRootNode(), key));
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(getRootNode(), key);
        byte[] data = pathTree.getValue(path);
        if (data != null) {
            LOGGER.debug("getData cache hit:{}", data);
            return data;
        }
        LOGGER.debug("getData cache not hit:{}", data);
        return getStrategy().getData(key);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        String path = PathUtil.getRealPath(getRootNode(), key);
        List<String> keys = pathTree.getChildren(path);
        if (!keys.isEmpty()) {
            LOGGER.debug("getChildren cache hit:{}", keys);
            return keys;
        }
        LOGGER.debug("getChildren cache not hit:{}", keys);
        return getStrategy().getChildren(PathUtil.getRealPath(getRootNode(), key));
    }
}
