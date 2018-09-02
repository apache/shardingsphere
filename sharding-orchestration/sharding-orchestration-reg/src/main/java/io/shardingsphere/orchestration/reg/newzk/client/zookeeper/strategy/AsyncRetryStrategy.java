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
import io.shardingsphere.orchestration.reg.newzk.client.retry.AsyncRetryCenter;
import io.shardingsphere.orchestration.reg.newzk.client.retry.DelayRetryPolicy;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.CreateAllNeedOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.CreateCurrentOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.DeleteAllChildrenOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.DeleteCurrentBranchOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.DeleteCurrentOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.operation.UpdateOperation;
import io.shardingsphere.orchestration.reg.newzk.client.zookeeper.section.Connection;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

/**
 * Async retry strategy.
 *
 * @author lidongbo
 */
@Slf4j
public final class AsyncRetryStrategy extends SyncRetryStrategy {
    
    public AsyncRetryStrategy(final IProvider provider, final DelayRetryPolicy delayRetryPolicy) {
        super(provider, delayRetryPolicy);
        AsyncRetryCenter.INSTANCE.init(getDelayRetryPolicy());
        AsyncRetryCenter.INSTANCE.start();
    }
    
    @Override
    public void createCurrentOnly(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().create(path, value, createMode);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AsyncRetryStrategy SessionExpiredException createCurrentOnly: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new CreateCurrentOperation(getProvider(), path, value, createMode));
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public void update(final String key, final String value) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().update(path, value);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AsyncRetryStrategy SessionExpiredException update: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new UpdateOperation(getProvider(), path, value));
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public void deleteOnlyCurrent(final String key) throws KeeperException, InterruptedException {
        String path = getProvider().getRealPath(key);
        try {
            getProvider().delete(path);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AsyncRetryStrategy SessionExpiredException deleteOnlyCurrent: %s", path), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteCurrentOperation(getProvider(), path));
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public void createAllNeedPath(final String key, final String value, final CreateMode createMode) throws KeeperException, InterruptedException {
        try {
            super.createAllNeedPath(key, value, createMode);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AllAsyncRetryStrategy SessionExpiredException CreateAllNeedOperation: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new CreateAllNeedOperation(getProvider(), key, value, createMode));
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public void deleteAllChildren(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteAllChildren(key);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AllAsyncRetryStrategy SessionExpiredException deleteAllChildren: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteAllChildrenOperation(getProvider(), key));
            } else {
                throw ex;
            }
        }
    }
    
    @Override
    public void deleteCurrentBranch(final String key) throws KeeperException, InterruptedException {
        try {
            super.deleteCurrentBranch(key);
        } catch (KeeperException ex) {
            if (Connection.needRetry(ex)) {
                log.warn(String.format("AllAsyncRetryStrategy SessionExpiredException deleteCurrentBranch: %s", key), ex);
                AsyncRetryCenter.INSTANCE.add(new DeleteCurrentBranchOperation(getProvider(), key));
            } else {
                throw ex;
            }
        }
    }
}
