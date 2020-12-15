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

package org.apache.shardingsphere.transaction.spi;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Sharding transaction manager.
 */
public interface ShardingTransactionManager extends AutoCloseable {
    
    /**
     * Initialize sharding transaction manager.
     *
     * @param databaseType database type
     * @param resourceDataSources resource data sources
     * @param transactionMangerType transaction manger type
     */
    void init(DatabaseType databaseType, Collection<ResourceDataSource> resourceDataSources, String transactionMangerType);
    
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
