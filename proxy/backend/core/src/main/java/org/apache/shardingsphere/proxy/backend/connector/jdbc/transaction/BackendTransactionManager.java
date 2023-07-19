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

import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.connector.TransactionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Backend transaction manager.
 */
public final class BackendTransactionManager implements TransactionManager {
    
    private final ProxyDatabaseConnectionManager connection;
    
    private final TransactionType transactionType;
    
    private final LocalTransactionManager localTransactionManager;
    
    private final ShardingSphereTransactionManager shardingSphereTransactionManager;
    
    private final Collection<TransactionHook> transactionHooks;
    
    public BackendTransactionManager(final ProxyDatabaseConnectionManager databaseConnectionManager) {
        connection = databaseConnectionManager;
        transactionType = connection.getConnectionSession().getTransactionStatus().getTransactionType();
        localTransactionManager = new LocalTransactionManager(databaseConnectionManager);
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        ShardingSphereTransactionManagerEngine engine = transactionRule.getResource();
        shardingSphereTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
        transactionHooks = ShardingSphereServiceLoader.getServiceInstances(TransactionHook.class);
    }
    
    @Override
    public void begin() {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
            getTransactionContext().setInTransaction(true);
            connection.closeHandlers(true);
            connection.closeConnections(false);
        }
        for (TransactionHook each : transactionHooks) {
            each.beforeBegin(getTransactionContext());
        }
        if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
            localTransactionManager.begin();
        } else {
            shardingSphereTransactionManager.begin();
        }
        for (TransactionHook each : transactionHooks) {
            each.afterBegin(getTransactionContext());
        }
    }
    
    @Override
    public void commit() throws SQLException {
        for (TransactionHook each : transactionHooks) {
            each.beforeCommit(connection.getCachedConnections().values(), getTransactionContext(), ProxyContext.getInstance().getContextManager().getInstanceContext().getLockContext());
        }
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
                    localTransactionManager.commit();
                } else {
                    shardingSphereTransactionManager.commit(connection.getConnectionSession().getTransactionStatus().isExceptionOccur());
                }
            } finally {
                for (TransactionHook each : transactionHooks) {
                    each.afterCommit(connection.getCachedConnections().values(), getTransactionContext(), ProxyContext.getInstance().getContextManager().getInstanceContext().getLockContext());
                }
                connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
                connection.getConnectionSession().getTransactionStatus().setExceptionOccur(false);
                connection.getConnectionSession().getConnectionContext().clearTransactionConnectionContext();
                connection.getConnectionSession().getConnectionContext().clearCursorConnectionContext();
            }
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        for (TransactionHook each : transactionHooks) {
            each.beforeRollback(connection.getCachedConnections().values(), getTransactionContext());
        }
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
                    localTransactionManager.rollback();
                } else {
                    shardingSphereTransactionManager.rollback();
                }
            } finally {
                for (TransactionHook each : transactionHooks) {
                    each.afterRollback(connection.getCachedConnections().values(), getTransactionContext());
                }
                connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
                connection.getConnectionSession().getTransactionStatus().setExceptionOccur(false);
                connection.getConnectionSession().getConnectionContext().clearTransactionConnectionContext();
                connection.getConnectionSession().getConnectionContext().clearCursorConnectionContext();
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
        if (result.isEmpty() && connection.getConnectionSession().getTransactionStatus().isExceptionOccur()) {
            connection.getConnectionSession().getTransactionStatus().setExceptionOccur(false);
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
