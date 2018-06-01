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

package io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.strategy;

import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.action.IProvider;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.PathUtil;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.utility.Constants;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.base.BaseStrategy;
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
    private static final Logger logger = LoggerFactory.getLogger(UsualStrategy.class);
    public UsualStrategy(final IProvider provider){
        super(provider);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return provider.getData(provider.getRealPath(key));
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.getData(provider.getRealPath(key), callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return provider.exists(provider.getRealPath(key));
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return provider.exists(provider.getRealPath(key), watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return provider.getChildren(provider.getRealPath(key));
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        provider.create(provider.getRealPath(key), value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        provider.update(provider.getRealPath(key), value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        provider.delete(provider.getRealPath(key));
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        provider.delete(provider.getRealPath(key), callback, ctx);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.createCurrentOnly(key, value, createMode);
            return;
        }
        List<String> nodes = provider.getNecessaryPaths(key);
        for (int i = 0; i < nodes.size(); i++) {
            try {
//                this.deleteAllChildren(nodes.get(i));
                if (i == nodes.size() - 1){
                    this.createCurrentOnly(nodes.get(i), value, createMode);
                } else {
                    this.createCurrentOnly(nodes.get(i), Constants.NOTHING_VALUE, CreateMode.PERSISTENT);
                }
                logger.debug("node not exist and create:", nodes.get(i));
            } catch (KeeperException.NodeExistsException ee){
                logger.debug("create node exist:{}", nodes.get(i));
                continue;
            }
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        logger.debug("deleteAllChildren:{}", key);
        this.deleteChildren(provider.getRealPath(key), true);
    }
    
    private void deleteChildren(final String path, final boolean deleteCurrentNode) throws KeeperException, InterruptedException {
        logger.debug("deleteChildren:{}", path);
        List<String> children;
        try{
            children = provider.getChildren(path);
        } catch (KeeperException.NoNodeException e){
            logger.warn("deleteChildren node not exist:{},e:{}", path, e.getMessage());
            return;
        }
        for (String child : children){
            child = PathUtil.getRealPath(path, child);
            this.deleteAllChildren(child);
        }
        if (deleteCurrentNode){
            try{
                this.deleteOnlyCurrent(path);
            } catch(KeeperException.NotEmptyException e){
                logger.warn("deleteCurrentNode exist children:{},e:{}", path, e.getMessage());
                deleteChildren(path, true);
            } catch(KeeperException.NoNodeException e){
                logger.warn("deleteCurrentNode node not exist:{},e:{}", path, e.getMessage());
            }
        }
    }
    
    /*
    * delete the current node with force and delete the super node whose only child node is current node recursively
    */
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        logger.debug("deleteCurrentBranch:{}", key);
        if (key.indexOf(Constants.PATH_SEPARATOR) < -1){
            this.deleteOnlyCurrent(key);
            return;
        }
        String path = provider.getRealPath(key);
        this.deleteChildren(path, true);
        String superPath = path.substring(0, path.lastIndexOf(Constants.PATH_SEPARATOR));
        try {
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException ee){
            logger.warn("deleteCurrentBranch exist children:{},e:{}", path, ee.getMessage());
            return;
        } catch (KeeperException.NoNodeException ee){
            logger.warn("deleteCurrentBranch NoNodeException:{},e:{}", superPath, ee.getMessage());
        }
    }
    
    private void deleteRecursively(final String path) throws KeeperException, InterruptedException {
        logger.debug("deleteRecursively:{}", path);
        int index = path.lastIndexOf(Constants.PATH_SEPARATOR);
        if (index == 0){
            this.deleteOnlyCurrent(path);
            return;
        }
        String superPath = path.substring(0, index);
        try {
            this.deleteOnlyCurrent(path);
            this.deleteRecursively(superPath);
        } catch (KeeperException.NotEmptyException ee){
            logger.warn("deleteRecursively exist children:{},e:{}", path, ee.getMessage());
            logger.info("deleteRecursively {} exist other children:{}", path, this.getChildren(path));
            return;
        }
    }
}
