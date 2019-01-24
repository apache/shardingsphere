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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import java.sql.SQLException;

/**
 * Proxy transaction manager.
 *
 * @author zhaojun
 */
@RequiredArgsConstructor
public final class BackendTransactionManager implements TransactionManager {
    
    private final BackendConnection connection;
    
    @Override
    public void begin() {
        Optional<ShardingTransactionManager> shardingTransactionManager = getShardingTransactionManager(connection);
        if (!connection.getStateHandler().isInTransaction()) {
            connection.getStateHandler().getAndSetStatus(ConnectionStatus.TRANSACTION);
            connection.releaseConnections(false);
        }
        if (!shardingTransactionManager.isPresent()) {
            new LocalTransactionManager(connection).begin();
        } else {
            shardingTransactionManager.get().begin();
        }
    }
    
    @Override
    public void commit() throws SQLException {
        Optional<ShardingTransactionManager> shardingTransactionManager = getShardingTransactionManager(connection);
        try {
            if (!shardingTransactionManager.isPresent()) {
                new LocalTransactionManager(connection).commit();
            } else {
                shardingTransactionManager.get().commit();
            }
        } finally {
            connection.getStateHandler().getAndSetStatus(ConnectionStatus.TERMINATED);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        Optional<ShardingTransactionManager> shardingTransactionManager = getShardingTransactionManager(connection);
        try {
            if (!shardingTransactionManager.isPresent()) {
                new LocalTransactionManager(connection).rollback();
            } else {
                shardingTransactionManager.get().rollback();
            }
        } finally {
            connection.getStateHandler().getAndSetStatus(ConnectionStatus.TERMINATED);
        }
    }
    
    private Optional<ShardingTransactionManager> getShardingTransactionManager(final BackendConnection connection) {
        return Optional.fromNullable(ShardingTransactionManagerEngine.getTransactionManager(connection.getTransactionType()));
    }
}
