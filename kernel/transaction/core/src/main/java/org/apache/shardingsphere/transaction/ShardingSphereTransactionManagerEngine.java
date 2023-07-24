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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.exception.TransactionManagerNotExistedException;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import javax.sql.DataSource;
import java.util.Map;

/**
 * ShardingSphere transaction manager engine.
 */
@Slf4j
public final class ShardingSphereTransactionManagerEngine {
    
    private final TransactionType transactionType;
    
    private final ShardingSphereTransactionManager transactionManager;
    
    public ShardingSphereTransactionManagerEngine(final TransactionType transactionType) {
        this.transactionType = transactionType;
        transactionManager = TransactionType.LOCAL == transactionType ? null : TypedSPILoader.getService(ShardingSphereTransactionManager.class, transactionType.name());
    }
    
    /**
     * Initialize transaction manager.
     *
     * @param databaseTypes database types
     * @param dataSourceMap data source map
     * @param providerType transaction manager provider type
     */
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSourceMap, final String providerType) {
        if (TransactionType.LOCAL != transactionType) {
            transactionManager.init(databaseTypes, dataSourceMap, providerType);
        }
    }
    
    /**
     * Get transaction manager.
     *
     * @param transactionType transaction type
     * @return transaction manager
     */
    public ShardingSphereTransactionManager getTransactionManager(final TransactionType transactionType) {
        if (TransactionType.LOCAL != transactionType) {
            ShardingSpherePreconditions.checkNotNull(transactionManager, () -> new TransactionManagerNotExistedException(transactionType));
        }
        return transactionManager;
    }
    
    /**
     * Close transaction manager.
     */
    public void close() {
        if (TransactionType.LOCAL != transactionType) {
            transactionManager.close();
        }
    }
}
