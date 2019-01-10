/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingjdbc.jdbc.core.fixture;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.TransactionOperationType;
import io.shardingsphere.transaction.spi.ShardingTransactionEngine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Fixed base sharding transaction engine.
 *
 * @author zhaojun
 */
public final class FixedXAShardingTransactionEngine implements ShardingTransactionEngine {
    
    private static final Map<String, TransactionOperationType> INVOKES = new HashMap<>();
    
    /**
     * Get invoke map.
     *
     * @return map
     */
    public static Map<String, TransactionOperationType> getInvokes() {
        return INVOKES;
    }
    
    @Override
    public TransactionType getTransactionType() {
        return TransactionType.XA;
    }
    
    @Override
    public void registerTransactionalResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
    }
    
    @Override
    public void clearTransactionalResources() {
    }
    
    @Override
    public Connection createConnection(final String dataSourceName, final DataSource dataSource) throws SQLException {
        return dataSource.getConnection();
    }
    
    @Override
    public void begin() {
        INVOKES.put("begin", TransactionOperationType.BEGIN);
    }
    
    @Override
    public void commit() {
        INVOKES.put("commit", TransactionOperationType.COMMIT);
    }
    
    @Override
    public void rollback() {
        INVOKES.put("rollback", TransactionOperationType.ROLLBACK);
    }
}
