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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
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
 * Proxy backend transaction manager.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ProxyBackendTransactionManager {
    
    private final ProxyDatabaseConnectionManager connection;
    
    private final TransactionType transactionType;
    
    private final LocalTransactionManager localTransactionManager;
    
    private final ShardingSphereDistributedTransactionManager distributedTransactionManager;
    
    private final Map<ShardingSphereRule, TransactionHook> transactionHooks;
    
    private final TransactionConnectionContext transactionContext;
    
    public ProxyBackendTransactionManager(final ProxyDatabaseConnectionManager databaseConnectionManager) {
        connection = databaseConnectionManager;
        localTransactionManager = new LocalTransactionManager(databaseConnectionManager);
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        transactionContext = connection.getConnectionSession().getConnectionContext().getTransactionContext();
        transactionType = transactionRule.getDefaultType();
        ShardingSphereTransactionManagerEngine engine = transactionRule.getResource();
        if (transactionContext.getTransactionManager().isPresent()) {
            distributedTransactionManager = (ShardingSphereDistributedTransactionManager) transactionContext.getTransactionManager().get();
        } else {
            distributedTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
        }
        transactionHooks = OrderedSPILoader.getServices(TransactionHook.class, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
    }
    
    /**
     * Begin transaction.
     */
    public void begin() {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            connection.closeHandlers(true);
            connection.closeConnections(false);
            connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
            transactionContext.beginTransaction(transactionType.name(), distributedTransactionManager);
        }
        doBegin();
    }
    
    private void doBegin() {
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getDatabaseType();
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().beforeBegin(entry.getKey(), databaseType, transactionContext);
        }
        if (TransactionType.LOCAL == transactionType || null == distributedTransactionManager) {
            localTransactionManager.begin();
        } else {
            distributedTransactionManager.begin();
        }
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().afterBegin(entry.getKey(), databaseType, transactionContext);
        }
    }
    
    /**
     * Commit transaction.
     *
     * @throws SQLException SQL exception
     */
    public void commit() throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return;
        }
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getDatabaseType();
        boolean isNeedLock = isNeedLockWhenCommit();
        if (isNeedLock) {
            // FIXME if timeout when lock required, TSO not assigned, but commit will continue, solution is use redis lock in impl to instead of reg center's lock. #35041
            ProxyContext.getInstance().getContextManager().getExclusiveOperatorEngine().operate(new TransactionCommitOperation(), 200L, () -> commit(databaseType));
        } else {
            commit(databaseType);
        }
    }
    
    private void commit(final DatabaseType databaseType) throws SQLException {
        try {
            // FIXME if timeout when lock required, TSO not assigned, but commit will continue, solution is use redis lock in impl to instead of reg center's lock. #35041
            for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                entry.getValue().beforeCommit(entry.getKey(), databaseType, connection.getCachedConnections().values(), transactionContext);
            }
            if (TransactionType.LOCAL == TransactionUtils.getTransactionType(transactionContext) || null == distributedTransactionManager) {
                localTransactionManager.commit();
            } else {
                distributedTransactionManager.commit(transactionContext.isExceptionOccur());
            }
        } finally {
            clear();
        }
    }
    
    private boolean isNeedLockWhenCommit() {
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            if (entry.getValue().isNeedLockWhenCommit(entry.getKey())) {
                return true;
            }
        }
        return false;
    }
    
    private void clear() {
        for (Connection each : connection.getCachedConnections().values()) {
            ConnectionSavepointManager.getInstance().transactionFinished(each);
        }
        connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
        connection.getConnectionSession().getConnectionContext().close();
    }
    
    /**
     * Rollback transaction.
     *
     * @throws SQLException SQL exception
     */
    public void rollback() throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return;
        }
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getDatabaseType();
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().beforeRollback(entry.getKey(), databaseType, connection.getCachedConnections().values(), transactionContext);
        }
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == TransactionUtils.getTransactionType(transactionContext) || null == distributedTransactionManager) {
                    localTransactionManager.rollback();
                } else {
                    distributedTransactionManager.rollback();
                }
            } finally {
                for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
                    entry.getValue().afterRollback(entry.getKey(), databaseType, connection.getCachedConnections().values(), transactionContext);
                }
                clear();
            }
        }
    }
    
    /**
     * Set savepoint.
     *
     * @param savepointName savepoint name
     * @throws SQLException SQL exception
     */
    public void setSavepoint(final String savepointName) throws SQLException {
        for (Connection each : connection.getCachedConnections().values()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, savepointName);
        }
        connection.getConnectionPostProcessors().add(target -> ConnectionSavepointManager.getInstance().setSavepoint(target, savepointName));
    }
    
    /**
     * Rollback to savepoint.
     *
     * @param savepointName savepoint name
     * @throws SQLException SQL exception
     */
    public void rollbackTo(final String savepointName) throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                ConnectionSavepointManager.getInstance().rollbackToSavepoint(each, savepointName);
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (result.isEmpty() && transactionContext.isExceptionOccur()) {
            transactionContext.setExceptionOccur(false);
        }
        throwSQLExceptionIfNecessary(result);
    }
    
    /**
     * Release savepoint.
     *
     * @param savepointName savepoint name
     * @throws SQLException SQL exception
     */
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
