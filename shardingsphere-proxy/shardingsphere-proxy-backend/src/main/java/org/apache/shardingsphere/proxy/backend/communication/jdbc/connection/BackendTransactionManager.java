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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.connection;

import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import java.sql.SQLException;

/**
 * Backend transaction manager.
 */
public final class BackendTransactionManager implements TransactionManager {
    
    private final BackendConnection connection;
    
    private final TransactionType transactionType;
    
    private final LocalTransactionManager localTransactionManager;
    
    private final ShardingTransactionManager shardingTransactionManager;
    
    public BackendTransactionManager(final BackendConnection backendConnection) {
        connection = backendConnection;
        transactionType = connection.getTransactionType();
        localTransactionManager = new LocalTransactionManager(backendConnection);
        ShardingTransactionManagerEngine engine = ProxyContext.getInstance().getTransactionContexts().getEngines().get(connection.getSchema());
        shardingTransactionManager = null == engine ? null : engine.getTransactionManager(transactionType);
    }
    
    @Override
    public void begin() {
        if (!connection.getStateHandler().isInTransaction()) {
            connection.getStateHandler().setStatus(ConnectionStatus.TRANSACTION);
            connection.releaseConnections(false);
        }
        if (TransactionType.LOCAL == transactionType || null == shardingTransactionManager) {
            localTransactionManager.begin();
        } else {
            shardingTransactionManager.begin();
        }
    }
    
    @Override
    public void commit() throws SQLException {
        if (connection.getStateHandler().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingTransactionManager) {
                    localTransactionManager.commit();
                } else {
                    shardingTransactionManager.commit();
                }
            } finally {
                connection.getStateHandler().setStatus(ConnectionStatus.TERMINATED);
            }
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        if (connection.getStateHandler().isInTransaction()) {
            try {
                if (TransactionType.LOCAL == transactionType || null == shardingTransactionManager) {
                    localTransactionManager.rollback();
                } else {
                    shardingTransactionManager.rollback();
                }
            } finally {
                connection.getStateHandler().setStatus(ConnectionStatus.TERMINATED);
            }
        }
    }
}
