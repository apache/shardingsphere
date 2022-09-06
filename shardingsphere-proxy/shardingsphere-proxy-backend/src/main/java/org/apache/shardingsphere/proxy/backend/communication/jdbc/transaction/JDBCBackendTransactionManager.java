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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction;

import org.apache.shardingsphere.proxy.backend.communication.TransactionManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ConnectionSavepointManager;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.rule.TransactionRule;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Backend transaction manager.
 */
public final class JDBCBackendTransactionManager implements TransactionManager<Void> {
    
    private final JDBCBackendConnection connection;
    
    private final TransactionType transactionType;
    
    private final LocalTransactionManager localTransactionManager;
    
    private final ShardingSphereTransactionManager shardingSphereTransactionManager;
    
    public JDBCBackendTransactionManager(final JDBCBackendConnection backendConnection) {
        connection = backendConnection;
        transactionType = connection.getConnectionSession().getTransactionStatus().getTransactionType();
        localTransactionManager = new LocalTransactionManager(backendConnection);
        TransactionRule transactionRule = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class);
        ShardingSphereTransactionManagerEngine engine = transactionRule.getResource();
        shardingSphereTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
    }
    
    @Override
    public Void begin() throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
            connection.getConnectionSession().getConnectionContext().getTransactionConnectionContext().setInTransaction(true);
            connection.closeHandlers(true);
            connection.closeConnections(false);
        }
        if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
            localTransactionManager.begin();
        } else {
            shardingSphereTransactionManager.begin();
        }
        return null;
    }
    
    @Override
    public Void commit() throws SQLException {
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
                    localTransactionManager.commit();
                } else {
                    shardingSphereTransactionManager.commit(connection.getConnectionSession().getTransactionStatus().isRollbackOnly());
                }
            } finally {
                connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
                connection.getConnectionSession().getTransactionStatus().setRollbackOnly(false);
                connection.getConnectionSession().getConnectionContext().clearTransactionConnectionContext();
                connection.getConnectionSession().getConnectionContext().clearCursorConnectionContext();
            }
        }
        return null;
    }
    
    @Override
    public Void rollback() throws SQLException {
        if (connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
                    localTransactionManager.rollback();
                } else {
                    shardingSphereTransactionManager.rollback();
                }
            } finally {
                connection.getConnectionSession().getTransactionStatus().setInTransaction(false);
                connection.getConnectionSession().getTransactionStatus().setRollbackOnly(false);
                connection.getConnectionSession().getConnectionContext().clearTransactionConnectionContext();
                connection.getConnectionSession().getConnectionContext().clearCursorConnectionContext();
            }
        }
        return null;
    }
    
    @Override
    public Void setSavepoint(final String savepointName) throws SQLException {
        for (Connection each : connection.getCachedConnections().values()) {
            ConnectionSavepointManager.getInstance().setSavepoint(each, savepointName);
        }
        connection.getConnectionPostProcessors().add(target -> {
            try {
                ConnectionSavepointManager.getInstance().setSavepoint(target, savepointName);
            } catch (final SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        return null;
    }
    
    @Override
    public Void rollbackTo(final String savepointName) throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                ConnectionSavepointManager.getInstance().rollbackToSavepoint(each, savepointName);
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        if (result.isEmpty() && connection.getConnectionSession().getTransactionStatus().isRollbackOnly()) {
            connection.getConnectionSession().getTransactionStatus().setRollbackOnly(false);
        }
        return throwSQLExceptionIfNecessary(result);
    }
    
    @Override
    public Void releaseSavepoint(final String savepointName) throws SQLException {
        Collection<SQLException> result = new LinkedList<>();
        for (Connection each : connection.getCachedConnections().values()) {
            try {
                ConnectionSavepointManager.getInstance().releaseSavepoint(each, savepointName);
            } catch (final SQLException ex) {
                result.add(ex);
            }
        }
        return throwSQLExceptionIfNecessary(result);
    }
    
    private Void throwSQLExceptionIfNecessary(final Collection<SQLException> exceptions) throws SQLException {
        if (exceptions.isEmpty()) {
            return null;
        }
        SQLException ex = null;
        int count = 0;
        for (SQLException each : exceptions) {
            if (0 == count) {
                ex = each;
            } else {
                // TODO use recursion to setNextException with chain, not overlap
                ex.setNextException(each);
            }
            count++;
        }
        throw ex;
    }
}
