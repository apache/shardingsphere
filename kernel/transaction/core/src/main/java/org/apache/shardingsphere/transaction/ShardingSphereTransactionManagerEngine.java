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
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.exception.TransactionManagerNotExistedException;
import org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager;

import javax.sql.DataSource;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * ShardingSphere transaction manager engine.
 */
@Slf4j
public final class ShardingSphereTransactionManagerEngine {
    
    private final Map<TransactionType, ShardingSphereTransactionManager> transactionManagers = new EnumMap<>(TransactionType.class);
    
    public ShardingSphereTransactionManagerEngine() {
        loadTransactionManager();
    }
    
    private void loadTransactionManager() {
        for (ShardingSphereTransactionManager each : ShardingSphereServiceLoader.getServiceInstances(ShardingSphereTransactionManager.class)) {
            if (transactionManagers.containsKey(each.getTransactionType())) {
                log.warn("Find more than one {} transaction manager implementation class, use `{}` now",
                        each.getTransactionType(), transactionManagers.get(each.getTransactionType()).getClass().getName());
                continue;
            }
            transactionManagers.put(each.getTransactionType(), each);
        }
    }
    
    /**
     * Initialize transaction managers.
     *
     * @param databaseTypes database types
     * @param dataSourceMap data source map
     * @param providerType transaction manager provider type
     */
    public void init(final Map<String, DatabaseType> databaseTypes, final Map<String, DataSource> dataSourceMap, final String providerType) {
        transactionManagers.forEach((key, value) -> value.init(databaseTypes, dataSourceMap, providerType));
    }
    
    /**
     * Get transaction manager.
     *
     * @param transactionType transaction type
     * @return transaction manager
     */
    public ShardingSphereTransactionManager getTransactionManager(final TransactionType transactionType) {
        ShardingSphereTransactionManager result = transactionManagers.get(transactionType);
        if (TransactionType.LOCAL != transactionType) {
            ShardingSpherePreconditions.checkNotNull(result, () -> new TransactionManagerNotExistedException(transactionType));
        }
        return result;
    }
    
    /**
     * Close transaction managers.
     * 
     * @throws Exception exception
     */
    public void close() throws Exception {
        for (Entry<TransactionType, ShardingSphereTransactionManager> entry : transactionManagers.entrySet()) {
            entry.getValue().close();
        }
    }
}
