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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionManager;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPI;
import org.apache.shardingsphere.transaction.api.TransactionType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * ShardingSphere distributed transaction manager.
 */
public interface ShardingSphereDistributedTransactionManager extends TypedSPI, TransactionManager, AutoCloseable {
    
    /**
     * Initialize distributed transaction manager.
     *
     * @param databaseTypes database types
     * @param dataSources data sources
     * @param providerType transaction manager provider type 
     */
    void init(Map<String, DatabaseType> databaseTypes, Map<String, DataSource> dataSources, String providerType);
    
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
     * @param databaseName database name
     * @param dataSourceName data source name
     * @return connection
     * @throws SQLException SQL exception
     */
    Connection getConnection(String databaseName, String dataSourceName) throws SQLException;
    
    /**
     * Begin transaction.
     */
    void begin();
    
    /**
     * Begin transaction with given timeout.
     *
     * @param timeout Transaction timeout in SECONDS
     */
    void begin(int timeout);
    
    /**
     * Commit transaction.
     *
     * @param rollbackOnly rollback only
     */
    void commit(boolean rollbackOnly);
    
    /**
     * Rollback transaction.
     */
    void rollback();
    
    /**
     * Judge whether contains the provider type.
     *
     * @param providerType transaction manager provider type 
     * @return contains provider type or not
     */
    boolean containsProviderType(String providerType);
    
    /**
     * Close transaction manager.
     */
    @Override
    void close();
}
