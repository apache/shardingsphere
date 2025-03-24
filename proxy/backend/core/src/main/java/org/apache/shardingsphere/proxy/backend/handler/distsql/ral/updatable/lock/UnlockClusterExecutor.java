/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.lock;

import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.statement.ral.updatable.UnlockClusterStatement;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.lock.LockContext;
import org.apache.shardingsphere.mode.state.ShardingSphereState;
import org.apache.shardingsphere.mode.manager.cluster.lock.exception.NotLockedClusterException;
import org.apache.shardingsphere.mode.manager.cluster.lock.global.GlobalLockDefinition;
import org.apache.shardingsphere.mode.manager.ContextManager;

/**
 * Unlock cluster executor.
 */
@DistSQLExecutorClusterModeRequired
public final class UnlockClusterExecutor implements DistSQLUpdateExecutor<UnlockClusterStatement> {
    
    @Override
    public void executeUpdate(final UnlockClusterStatement sqlStatement, final ContextManager contextManager) {
        checkState(contextManager);
        LockContext lockContext = contextManager.getLockContext();
        GlobalLockDefinition lockDefinition = new GlobalLockDefinition(new ClusterLock());
        long timeoutMillis = sqlStatement.getTimeoutMillis().orElse(3000L);
        if (lockContext.tryLock(lockDefinition, timeoutMillis)) {
            try {
                checkState(contextManager);
                contextManager.getPersistServiceFacade().getStateService().update(ShardingSphereState.OK);
                // TODO unlock snapshot info if locked
            } finally {
                lockContext.unlock(lockDefinition);
            }
        }
    }
    
    private void checkState(final ContextManager contextManager) {
        ShardingSpherePreconditions.checkState(ShardingSphereState.OK != contextManager.getStateContext().getState(), NotLockedClusterException::new);
    }
    
    @Override
    public Class<UnlockClusterStatement> getType() {
        return UnlockClusterStatement.class;
    }
}
