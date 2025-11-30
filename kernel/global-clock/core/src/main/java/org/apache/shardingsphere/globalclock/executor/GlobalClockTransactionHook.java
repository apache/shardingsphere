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

package org.apache.shardingsphere.globalclock.executor;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.globalclock.provider.GlobalClockProvider;
import org.apache.shardingsphere.globalclock.rule.GlobalClockRule;
import org.apache.shardingsphere.globalclock.rule.constant.GlobalClockOrder;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

/**
 * Global clock transaction hook.
 */
public final class GlobalClockTransactionHook implements TransactionHook<GlobalClockRule> {
    
    @Override
    public void beforeBegin(final GlobalClockRule rule, final DatabaseType databaseType, final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public void afterBegin(final GlobalClockRule rule, final DatabaseType databaseType, final TransactionConnectionContext transactionContext) {
        rule.getGlobalClockProvider().ifPresent(optional -> transactionContext.setBeginMillis(optional.getCurrentTimestamp()));
    }
    
    @Override
    public void afterCreateConnections(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections,
                                       final TransactionConnectionContext transactionContext) throws SQLException {
        if (!rule.getConfiguration().isEnabled()) {
            return;
        }
        Optional<GlobalClockTransactionExecutor> globalClockTransactionExecutor = DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType);
        if (globalClockTransactionExecutor.isPresent()) {
            globalClockTransactionExecutor.get().sendSnapshotTimestamp(connections, transactionContext.getBeginMillis());
        }
    }
    
    @Override
    public void beforeExecuteSQL(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections, final TransactionConnectionContext connectionContext,
                                 final TransactionIsolationLevel isolationLevel) throws SQLException {
        if (!rule.getConfiguration().isEnabled() || null != isolationLevel && TransactionIsolationLevel.READ_COMMITTED != isolationLevel) {
            return;
        }
        Optional<GlobalClockTransactionExecutor> globalClockTransactionExecutor = DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType);
        if (!globalClockTransactionExecutor.isPresent()) {
            return;
        }
        Optional<GlobalClockProvider> globalClockProvider = rule.getGlobalClockProvider();
        Preconditions.checkState(globalClockProvider.isPresent());
        globalClockTransactionExecutor.get().sendSnapshotTimestamp(connections, globalClockProvider.get().getCurrentTimestamp());
    }
    
    @Override
    public void beforeCommit(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections,
                             final TransactionConnectionContext transactionContext) throws SQLException {
        if (!rule.getConfiguration().isEnabled()) {
            return;
        }
        Optional<GlobalClockTransactionExecutor> globalClockTransactionExecutor = DatabaseTypedSPILoader.findService(GlobalClockTransactionExecutor.class, databaseType);
        Preconditions.checkArgument(globalClockTransactionExecutor.isPresent());
        Optional<GlobalClockProvider> globalClockProvider = rule.getGlobalClockProvider();
        Preconditions.checkState(globalClockProvider.isPresent());
        globalClockTransactionExecutor.get().sendCommitTimestamp(connections, globalClockProvider.get().getCurrentTimestamp());
    }
    
    @Override
    public void afterCommit(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections, final TransactionConnectionContext transactionContext) {
        if (!rule.getConfiguration().isEnabled()) {
            return;
        }
        Optional<GlobalClockProvider> globalClockProvider = rule.getGlobalClockProvider();
        Preconditions.checkState(globalClockProvider.isPresent());
        globalClockProvider.get().getNextTimestamp();
    }
    
    @Override
    public boolean isNeedLockWhenCommit(final GlobalClockRule rule) {
        return rule.getConfiguration().isEnabled();
    }
    
    @Override
    public void beforeRollback(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections, final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public void afterRollback(final GlobalClockRule rule, final DatabaseType databaseType, final Collection<Connection> connections, final TransactionConnectionContext transactionContext) {
    }
    
    @Override
    public int getOrder() {
        return GlobalClockOrder.ORDER;
    }
    
    @Override
    public Class<GlobalClockRule> getTypeClass() {
        return GlobalClockRule.class;
    }
}
