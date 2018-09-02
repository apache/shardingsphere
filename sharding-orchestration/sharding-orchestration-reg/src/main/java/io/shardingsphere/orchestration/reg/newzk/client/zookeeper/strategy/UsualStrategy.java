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

package io.shardingsphere.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.orchestration.reg.newzk.client.utility.PathUtil;
import io.shardingsphere.orchestration.reg.newzk.client.utility.ZookeeperConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;

import java.util.List;

/**
 * Usual strategy.
 * 
 * @author lidongbo
 */
@Slf4j
public class UsualStrategy extends BaseStrategy {
    
    public UsualStrategy(final IProvider provider) {
        super(provider);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return getProvider().getData(getProvider().getRealPath(key));
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getProvider().getData(getProvider().getRealPath(key), callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return getProvider().exists(getProvider().getRealPath(key));
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return getProvider().exists(getProvider().getRealPath(key), watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return getProvider().getChildren(getProvider().getRealPath(key));
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        getProvider().create(getProvider().getRealPath(key), value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        getProvider().update(getProvider().getRealPath(key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        getProvider().delete(getProvider().getRealPath(key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getProvider().delete(getProvider().getRealPath(key), callback, ctx);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (!key.contains(ZookeeperConstants.PATH_SEPARATOR)) {
            createCurrentOnly(key, value, createMode);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            try {
                if (i == nodes.size() - 1) {
                    createCurrentOnly(nodes.get(i), value, createMode);
                } else {
                    createCurrentOnly(nodes.get(i), ZookeeperConstants.NOTHING_VALUE, CreateMode.PERSISTENT);
                }
            } catch (final KeeperException.NodeExistsException ignored) {
            }
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        deleteChildren(getProvider().getRealPath(key), true);
    }
    
    private void deleteChildren(final String path, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        List<String> children;
        try {
            children = getProvider().getChildren(path);
        } catch (final KeeperException.NoNodeException ignored) {
            return;
        }
        for (String each : children) {
            this.deleteAllChildren(PathUtil.getRealPath(path, each));
        }
        if (deleteCurrentNode) {
            try {
                deleteOnlyCurrent(path);
            } catch (final KeeperException.NotEmptyException ex) {
                log.warn("deleteCurrentNode exist children: {}, ex: {}", path, ex.getMessage());
                deleteChildren(path, true);
            } catch (final KeeperException.NoNodeException ex) {
                log.warn("deleteCurrentNode node not exist: {}, ex: {}", path, ex.getMessage());
            }
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            deleteOnlyCurrent(path);
        } catch (final KeeperException.NotEmptyException ex) {
            deleteChildren(path, true);
        } catch (final KeeperException.NoNodeException ignored) {
        }

        String superPath = path.substring(0, path.lastIndexOf(ZookeeperConstants.PATH_SEPARATOR));
        try {
            deleteRecursively(superPath);
        } catch (final KeeperException.NotEmptyException ex) {
            log.warn("deleteCurrentBranch exist children: {}, ex: {}", path, ex.getMessage());
        } catch (final KeeperException.NoNodeException ignored) {
        }
    }
    
    private void deleteRecursively(final String path) throws KeeperException, InterruptedException {
        int index = path.lastIndexOf(ZookeeperConstants.PATH_SEPARATOR);
        if (index == 0) {
            deleteOnlyCurrent(path);
            return;
        }
        String superPath = path.substring(0, index);
        try {
            deleteOnlyCurrent(path);
            deleteRecursively(superPath);
        } catch (final KeeperException.NotEmptyException ex) {
            log.info("deleteRecursively exist children: {}, ex: {}", path, ex.getMessage());
        }
    }
}
