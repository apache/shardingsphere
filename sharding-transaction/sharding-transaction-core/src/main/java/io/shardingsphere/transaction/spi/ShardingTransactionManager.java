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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * Sharding transaction manager.
 *
 * @author zhaojun
 * 
 */
public interface ShardingTransactionManager extends AutoCloseable {
    
    /**
     * Initialize sharding transaction engine.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    void init(DatabaseType databaseType, Map<String, DataSource> dataSourceMap);
    
    /**
     * Get transaction type.
     *
     * @return transaction type
     */
    TransactionType getTransactionType();
    
    /**
     * Judge is in transaction or not.
     * 
     * @return in transaction or not
     */
    boolean isInTransaction();
    
    /**
     * Get transactional connection.
     *
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    Connection getConnection(String dataSourceName) throws SQLException;
    
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
}
