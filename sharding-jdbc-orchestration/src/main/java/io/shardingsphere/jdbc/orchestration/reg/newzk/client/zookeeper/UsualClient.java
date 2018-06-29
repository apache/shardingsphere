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

import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseClient;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.base.BaseContext;
import io.shardingsphere.jdbc.orchestration.reg.newzk.client.zookeeper.transaction.BaseTransaction;
import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/*
 * @author lidongbo
 */
public class UsualClient extends BaseClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsualClient.class);
    
    protected UsualClient(final BaseContext context) {
        super(context);
    }
    
    @Override
    public String getDataString(final String key) throws KeeperException, InterruptedException {
        return getStrategy().getDataString(key);
    }
    
    @Override
    public byte[] getData(final String key) throws KeeperException, InterruptedException {
        return getStrategy().getData(key);
    }
    
    @Override
    public void getData(final String key, final AsyncCallback.DataCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        getStrategy().getData(key, callback, ctx);
    }
    
    @Override
    public boolean checkExists(final String key) throws KeeperException, InterruptedException {
        return getStrategy().checkExists(key);
    }
    
    @Override
    public boolean checkExists(final String key, final Watcher watcher) throws KeeperException, InterruptedException {
        return getStrategy().checkExists(key, watcher);
    }
    
    @Override
    public List<String> getChildren(final String key) throws KeeperException, InterruptedException {
        return getStrategy().getChildren(key);
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        this.createNamespace();
        if (getRootNode().equals(key)) {
            return;
        }
        getStrategy().createCurrentOnly(key, value, createMode);
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        this.createNamespace();
        if (getRootNode().equals(key)) {
            return;
        }
        getStrategy().createAllNeedPath(key, value, createMode);
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        getStrategy().update(key, value);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        if (getRootNode().equals(key)) {
            deleteNamespace();
            return;
        }
        getStrategy().deleteOnlyCurrent(key);
    }
    
    @Override
    public void deleteOnlyCurrent(final String key, final AsyncCallback.VoidCallback callback, final Object ctx) throws KeeperException, InterruptedException {
        if (getRootNode().equals(key)) {
            deleteNamespace();
            return;
        }
        getStrategy().deleteOnlyCurrent(key, callback, ctx);
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        getStrategy().deleteAllChildren(key);
        if (getRootNode().equals(key)) {
            setRootExist(false);
            LOGGER.debug("deleteAllChildren delete root:{}", getRootNode());
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        getStrategy().deleteCurrentBranch(key);
        if (!getStrategy().checkExists(getRootNode())) {
            setRootExist(false);
            LOGGER.debug("deleteCurrentBranch delete root:{}", getRootNode());
        }
    }
    
    @Override
    public BaseTransaction transaction() {
        return getStrategy().transaction();
    }
}
