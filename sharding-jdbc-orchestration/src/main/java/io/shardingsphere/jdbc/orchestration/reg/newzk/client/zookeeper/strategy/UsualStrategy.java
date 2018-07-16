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

package io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.utility.PathUtil;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * usual strategy
 * 
 * @author lidongbo
 */
public class UsualStrategy extends BaseStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsualStrategy.class);
    
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
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1) {
            this.createCurrentOnly(key, value, createMode);
            return;
        }
        List<String> nodes = getProvider().getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            try {
//                this.deleteAllChildren(nodes.get(i));
                if (i == nodes.size() - 1) {
                    this.createCurrentOnly(nodes.get(i), value, createMode);
                } else {
                    this.createCurrentOnly(nodes.get(i), Constants.NOTHING_VALUE, CreateMode.PERSISTENT);
                }
                LOGGER.debug("node not exist and create:", nodes.get(i));
            } catch (KeeperException.NodeExistsException e) {
                LOGGER.debug("create node exist:{}", nodes.get(i));
            }
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        LOGGER.debug("deleteAllChildren:{}", key);
        this.deleteChildren(getProvider().getRealPath(key), true);
    }
    
    private void deleteChildren(final String path, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        LOGGER.debug("deleteChildren:{}", path);
        List<String> children;
        try {
            children = getProvider().getChildren(path);
        } catch (KeeperException.NoNodeException e) {
            LOGGER.warn("deleteChildren node not exist:{},e:{}", path, e.getMessage());
            return;
        }
        for (String child : children) {
            this.deleteAllChildren(PathUtil.getRealPath(path, child));
        }
        if (deleteCurrentNode) {
            try {
                this.deleteOnlyCurrent(path);
            } catch (KeeperException.NotEmptyException e) {
                LOGGER.warn("deleteCurrentNode exist children:{},e:{}", path, e.getMessage());
                deleteChildren(path, true);
            } catch (KeeperException.NoNodeException e) {
                LOGGER.warn("deleteCurrentNode node not exist:{},e:{}", path, e.getMessage());
            }
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        LOGGER.debug("deleteCurrentBranch:{}", key);
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1) {
            this.deleteOnlyCurrent(key);
            return;
        }
        String path = getProvider().getRealPath(key);
        this.deleteChildren(path, true);
        String superPath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        try {
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException e) {
            LOGGER.warn("deleteCurrentBranch exist children:{},e:{}", path, e.getMessage());
        } catch (KeeperException.NoNodeException e) {
            LOGGER.warn("deleteCurrentBranch NoNodeException:{},e:{}", superPath, e.getMessage());
        }
    }
    
    private void deleteRecursively(final String path) throws KeeperException, InterruptedException {
        LOGGER.debug("deleteRecursively:{}", path);
        int index = path.lastIndexOf(Constants.PATH_SEPARATOR);
        if (index == 0) {
            this.deleteOnlyCurrent(path);
            return;
        }
        String superPath = path.substring(0, index);
        try {
            this.deleteOnlyCurrent(path);
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException e) {
            LOGGER.info("deleteRecursively exist children:{},e:{}", path, e.getMessage());
            LOGGER.debug("deleteRecursively {} exist other children:{}", path, this.getChildren(path));
        }
    }
}
