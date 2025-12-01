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

package org.apache.shardingsphere.transaction;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.exception.TransactionManagerNotFoundException;
import org.apache.shardingsphere.transaction.spi.ShardingSphereDistributedTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * ShardingSphere transaction manager engine.
 */
public final class ShardingSphereTransactionManagerEngine {
    
    private final TransactionType transactionType;
    
    private final ShardingSphereDistributedTransactionManager distributedTransactionManager;
    
    public ShardingSphereTransactionManagerEngine(final TransactionType transactionType) {
        this.transactionType = transactionType;
        distributedTransactionManager = TransactionType.LOCAL == transactionType ? null : TypedSPILoader.getService(ShardingSphereDistributedTransactionManager.class, transactionType.name());
    }
    
    /**
     * Initialize transaction manager.
     *
     * @param databaseTypes database types
     * @param dataSourceMap data source map
     * @param providerType transaction manager provider type
     */
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSourceMap, final String providerType) {
        if (TransactionType.LOCAL == transactionType) {
            return;
        }
        distributedTransactionManager.init(databaseTypes, dataSourceMap, providerType);
    }
    
    /**
     * Get transaction manager.
     *
     * @param transactionType transaction type
     * @return transaction manager
     */
    public ShardingSphereDistributedTransactionManager getTransactionManager(final TransactionType transactionType) {
        if (TransactionType.LOCAL != transactionType) {
            ShardingSpherePreconditions.checkNotNull(distributedTransactionManager, () -> new TransactionManagerNotFoundException(transactionType));
        }
        return distributedTransactionManager;
    }
    
    /**
     * Close transaction manager.
     */
    public void close() {
        if (TransactionType.LOCAL == transactionType) {
            return;
        }
        distributedTransactionManager.close();
    }
}
