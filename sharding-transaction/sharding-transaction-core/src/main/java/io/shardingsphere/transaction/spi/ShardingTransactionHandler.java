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

package io.shardingsphere.transaction.spi;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.manager.ShardingTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sharding transaction handler SPI.
 *
 * @author zhaojun
 * 
 */
public interface ShardingTransactionHandler {
    
    /**
     * Begin transaction.
     */
    void begin();
    
    /**
     * Commit transaction.
     */
    void commit();
    
    /**
     * Rollback transaction.
     */
    void rollback();
    
    /**
     * Get sharding transaction manager.
     *
     * @return sharding transaction manager
     */
    ShardingTransactionManager getShardingTransactionManager();
    
    /**
     * Get transaction type.
     *
     * @return transaction type
     */
    TransactionType getTransactionType();
    
    /**
     * Register transaction data source.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    void registerTransactionalResource(DatabaseType databaseType, Map<String, DataSource> dataSourceMap);
    
    /**
     * Clear transactional resource.
     */
    void clearTransactionalResource();
    
    /**
     * Create transactional connection.
     *
     * @param dataSourceName data source name
     * @param dataSource data source
     * @return connection
     * @throws SQLException SQL exception
     */
    Connection createConnection(String dataSourceName, DataSource dataSource) throws SQLException;
}
