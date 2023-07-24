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

package org.apache.shardingsphere.globalclock.core.executor;

import org.apache.shardingsphere.globalclock.core.provider.GlobalClockProvider;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.lock.GlobalLockNames;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockDefinition;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.lock.GlobalLockDefinition;
import org.apache.shardingsphere.sql.parser.sql.common.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.spi.TransactionHookAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.Properties;

/**
 * Global clock transaction hook.
 */
public final class GlobalClockTransactionHook extends TransactionHookAdapter {
    
    private final LockDefinition lockDefinition = new GlobalLockDefinition(GlobalLockNames.GLOBAL_LOCK.getLockName());
    
    private boolean enabled;
    
    private GlobalClockTransactionExecutor globalClockTransactionExecutor;
    
    private GlobalClockProvider globalClockProvider;
    
    @Override
    public void init(final Properties props) {
        if (!Boolean.parseBoolean(props.getProperty("enabled"))) {
            enabled = false;
            return;
        }
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, props.getProperty("trunkType"));
        Optional<GlobalClockTransactionExecutor> globalClockTransactionExecutor = DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType);
        if (!globalClockTransactionExecutor.isPresent()) {
            enabled = false;
            return;
        }
        enabled = true;
        this.globalClockTransactionExecutor = globalClockTransactionExecutor.get();
        globalClockProvider = TypedSPILoader.getService(GlobalClockProvider.class, String.join(".", props.getProperty("type"), props.getProperty("provider")));
        
    }
    
    @Override
    public void afterBegin(final TransactionConnectionContext transactionContext) {
        if (!enabled) {
            return;
        }
        transactionContext.setBeginMills(globalClockProvider.getCurrentTimestamp());
    }
    
    @Override
    public void afterCreateConnections(final Collection<Connection> connections, final TransactionConnectionContext transactionContext) throws SQLException {
        if (!enabled) {
            return;
        }
        globalClockTransactionExecutor.sendSnapshotTimestamp(connections, transactionContext.getBeginMills());
    }
    
    @Override
    public void beforeExecuteSQL(final Collection<Connection> connections, final TransactionConnectionContext connectionContext, final TransactionIsolationLevel isolationLevel) throws SQLException {
        if (!enabled) {
            return;
        }
        if (null == isolationLevel || TransactionIsolationLevel.READ_COMMITTED == isolationLevel) {
            globalClockTransactionExecutor.sendSnapshotTimestamp(connections, globalClockProvider.getCurrentTimestamp());
        }
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void beforeCommit(final Collection<Connection> connections, final TransactionConnectionContext transactionContext, final LockContext lockContext) throws SQLException {
        if (!enabled) {
            return;
        }
        if (lockContext.tryLock(lockDefinition, 200L)) {
            globalClockTransactionExecutor.sendCommitTimestamp(connections, globalClockProvider.getCurrentTimestamp());
        }
    }
    
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void afterCommit(final Collection<Connection> connections, final TransactionConnectionContext transactionContext, final LockContext lockContext) {
        if (!enabled) {
            return;
        }
        try {
            globalClockProvider.getNextTimestamp();
        } finally {
            lockContext.unlock(lockDefinition);
        }
    }
    
    @Override
    public String getType() {
        return "GLOBAL_CLOCK";
    }
}
