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
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry.AsyncRetryCenter;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.retry.DelayRetryPolicy;
import io.shardingsphere.jdbc.orchestration.reg.new_zk.client.zookeeper.operation.*;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * async retry strategy
 *
 * @author lidongbo
 */
public class AsyncRetryStrategy extends SyncRetryStrategy {
    private static final Logger logger = LoggerFactory.getLogger(AsyncRetryStrategy.class);
    
    public AsyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy){
        super(provider, delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.init(this.delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.start();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.create(path, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException createCurrentOnly:{}", path);
            AsyncRetryCenter.INSTANCE.add(new CreateCurrentOperation(provider, path, value, createMode));
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.update(path, value);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException update:{}", path);
            AsyncRetryCenter.INSTANCE.add(new UpdateOperation(provider, path, value));
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = provider.getRealPath(key);
        try {
            provider.delete(path);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AsyncRetryStrategy SessionExpiredException deleteOnlyCurrent:{}", path);
            AsyncRetryCenter.INSTANCE.add(new DeleteCurrentOperation(provider, path));
        }
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllAsyncRetryStrategy SessionExpiredException CreateAllNeedOperation:{}", key);
            AsyncRetryCenter.INSTANCE.add(new CreateAllNeedOperation(provider, key, value, createMode));
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllAsyncRetryStrategy SessionExpiredException deleteAllChildren:{}", key);
            AsyncRetryCenter.INSTANCE.add(new DeleteAllChildrenOperation(provider, key));
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException.SessionExpiredException ee){
            logger.warn("AllAsyncRetryStrategy SessionExpiredException deleteCurrentBranch:{}", key);
            AsyncRetryCenter.INSTANCE.add(new DeleteCurrentBranchOperation(provider, key));
        }
    }
}
