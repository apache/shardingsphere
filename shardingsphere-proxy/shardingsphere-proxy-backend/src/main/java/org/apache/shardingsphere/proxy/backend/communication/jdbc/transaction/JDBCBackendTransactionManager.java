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
import org.apache.shardingsphere.transaction.TransactionHolder;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ShardingSphereTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import java.sql.SQLException;

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
        ShardingSphereTransactionManagerEngine engine = ProxyContext.getInstance().getContextManager().getTransactionContexts().getEngines().get(connection.getConnectionSession().getSchemaName());
        shardingSphereTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
    }
    
    @Override
    public Void begin() throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            connection.getConnectionSession().getTransactionStatus().setInTransaction(true);
            TransactionHolder.setInTransaction();
            connection.closeDatabaseCommunicationEngines(true);
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
                TransactionHolder.clear();
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
                TransactionHolder.clear();
            }
        }
        return null;
    }
    
    @Override
    public Void setSavepoint(final String savepointName) throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return null;
        }
        if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
            localTransactionManager.setSavepoint(savepointName);
        }
        return null;
        // TODO Non-local transaction manager
    }
    
    @Override
    public Void rollbackTo(final String savepointName) throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return null;
        }
        if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
            localTransactionManager.rollbackTo(savepointName);
        }
        return null;
        // TODO Non-local transaction manager
    }
    
    @Override
    public Void releaseSavepoint(final String savepointName) throws SQLException {
        if (!connection.getConnectionSession().getTransactionStatus().isInTransaction()) {
            return null;
        }
        if (TransactionType.LOCAL == transactionType || null == shardingSphereTransactionManager) {
            localTransactionManager.releaseSavepoint(savepointName);
        }
        return null;
        // TODO Non-local transaction manager
    }
}
