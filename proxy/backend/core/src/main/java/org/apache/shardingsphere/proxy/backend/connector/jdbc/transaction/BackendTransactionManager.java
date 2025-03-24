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

package org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction;

import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.mode.lock.LockContext;
import org.apache.shardingsphere.mode.lock.LockDefinition;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.mode.manager.cluster.lock.global.GlobalLockDefinition;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.TransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.savepoint.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Backend transaction manager.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class BackendTransactionManager implements TransactionManager {
    
    private final ProxyDatabaseConnectionManager connection;
    
    private final TransactionType transactionType;
    
    private final LocalTransactionManager localTransactionManager;
    
    private final ShardingSphereDistributedTransactionManager distributedTransactionManager;
    
    private final Map<ShardingSphereRule, TransactionHook> transactionHooks;
    
    public BackendTransactionManager(final ProxyDatabaseConnectionManager databaseConnectionManager) {
        connection = databaseConnectionManager;
        localTransactionManager = new LocalTransactionManager(databaseConnectionManager);
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        TransactionConnectionContext transactionContext = getTransactionContext();
        transactionType = transactionRule.getDefaultType();
        ShardingSphereTransactionManagerEngine engine = transactionRule.getResource();
        if (transactionContext.getTransactionManager().isPresent()) {
            distributedTransactionManager = (ShardingSphereDistributedTransactionManager) transactionContext.getTransactionManager().get();
        } else {
            distributedTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
        }
        transactionHooks = OrderedSPILoader.getServices(TransactionHook.class, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
    }
    
    @Override
    public void begin() {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
            getTransactionContext().beginTransaction(transactionType.name(), distributedTransactionManager);
            connection.closeHandlers(true);
            connection.closeConnections(false);
        }
        DatabaseType databaseType = ProxyContext.getInstance().getDatabaseType();
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().beforeBegin(entry.getKey(), databaseType, getTransactionContext());
        }
        if (TransactionType.LOCAL == transactionType || null == distributedTransactionManager) {
            localTransactionManager.begin();
        } else {
            distributedTransactionManager.begin();
        }
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().afterBegin(entry.getKey(), databaseType, getTransactionContext());
        }
    }
    
    @Override
    public void commit() throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return;
        }
        DatabaseType databaseType = ProxyContext.getInstance().getDatabaseType();
        LockContext lockContext = ProxyContext.getInstance().getContextManager().getLockContext();
        boolean isNeedLock = transactionHooks.values().stream().anyMatch(TransactionHook::isNeedLockWhenCommit);
        LockDefinition lockDefinition = new GlobalLockDefinition(new TransactionCommitLock());
        try {
            // FIXME if timeout when lock required, TSO not assigned, but commit will continue, solution is use redis lock in impl to instead of reg center's lock. #35041
            if (isNeedLock && !lockContext.tryLock(lockDefinition, 200L)) {
                return;
            }
            for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                entry.getValue().beforeCommit(entry.getKey(), databaseType, connection.getCachedConnections().values(), getTransactionContext());
            }
            if (TransactionType.LOCAL == TransactionUtils.getTransactionType(getTransactionContext()) || null == distributedTransactionManager) {
                localTransactionManager.commit();
            } else {
                distributedTransactionManager.commit(getTransactionContext().isExceptionOccur());
            }
        } finally {
            for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                entry.getValue().afterCommit(entry.getKey(), databaseType, connection.getCachedConnections().values(), getTransactionContext());
            }
            if (isNeedLock) {
                lockContext.unlock(lockDefinition);
            }
            for (Connection each : connection.getCachedConnections().values()) {
                ConnectionSavepointManager.getInstance().transactionFinished(each);
            }
            connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
            connection.getConnectionSession().getConnectionContext().close();
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getDatabaseType();
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().beforeRollback(entry.getKey(), databaseType, connection.getCachedConnections().values(), getTransactionContext());
        }
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == TransactionUtils.getTransactionType(getTransactionContext()) || null == distributedTransactionManager) {
                    localTransactionManager.rollback();
                } else {
                    distributedTransactionManager.rollback();
                }
            } finally {
                for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                    entry.getValue().afterRollback(entry.getKey(), databaseType, connection.getCachedConnections().values(), getTransactionContext());
                }
                for (Connection each : connection.getCachedConnections().values()) {
                    ConnectionSavepointManager.getInstance().transactionFinished(each);
                }
                connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
                connection.getConnectionSession().getConnectionContext().close();
            }
        }
    }
    
    private TransactionConnectionContext getTransactionContext() {
        return connection.getConnectionSession().getConnectionContext().getTransactionContext();
    }
    
    @Override
    public void setSavepoint(final String savepointName) throws SQLException {
        for (Connection each : connection.getCachedConnections().values()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, savepointName);
        }
        connection.getConnectionPostProcessors().add(target -> ConnectionSavepointManager.getInstance().setSavepoint(target, savepointName));
    }
    
    @Override
    public void rollbackTo(final String savepointName) throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                ConnectionSavepointManager.getInstance().rollbackToSavepoint(each, savepointName);
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (result.isEmpty() && getTransactionContext().isExceptionOccur()) {
            getTransactionContext().setExceptionOccur(false);
        }
        throwSQLExceptionIfNecessary(result);
    }
    
    @Override
    public void releaseSavepoint(final String savepointName) throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                ConnectionSavepointManager.getInstance().releaseSavepoint(each, savepointName);
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        throwSQLExceptionIfNecessary(result);
    }
    
    private void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return;
        }
        Iterator<SQLException> iterator = exceptions.iterator();
        SQLException firstException = iterator.next();
        while (iterator.hasNext()) {
            firstException.setNextException(iterator.next());
        }
        throw firstException;
    }
}
