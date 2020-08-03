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

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.transaction.core.ResourceDataSource;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.spi.ShardingTransactionManager;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 * Sharding transaction manager engine.
 */
@Slf4j
public final class ShardingTransactionManagerEngine {
    
    private final Map<TransactionType, ShardingTransactionManager> transactionManagerMap = new EnumMap<>(TransactionType.class);
    
    public ShardingTransactionManagerEngine() {
        loadShardingTransactionManager();
    }
    
    private void loadShardingTransactionManager() {
        for (ShardingTransactionManager each : ServiceLoader.load(ShardingTransactionManager.class)) {
            if (transactionManagerMap.containsKey(each.getTransactionType())) {
                log.warn("Find more than one {} transaction manager implementation class, use `{}` now",
                    each.getTransactionType(), transactionManagerMap.get(each.getTransactionType()).getClass().getName());
                continue;
            }
            transactionManagerMap.put(each.getTransactionType(), each);
        }
    }
    
    /**
     * Initialize sharding transaction managers.
     *
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    public void init(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Entry<TransactionType, ShardingTransactionManager> entry : transactionManagerMap.entrySet()) {
            entry.getValue().init(databaseType, getResourceDataSources(dataSourceMap));
        }
    }
    
    private Collection<ResourceDataSource> getResourceDataSources(final Map<String, DataSource> dataSourceMap) {
        List<ResourceDataSource> result = new LinkedList<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.add(new ResourceDataSource(entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    /**
     * Get sharding transaction manager.
     *
     * @param transactionType transaction type
     * @return sharding transaction manager
     */
    public ShardingTransactionManager getTransactionManager(final TransactionType transactionType) {
        ShardingTransactionManager result = transactionManagerMap.get(transactionType);
        if (TransactionType.LOCAL != transactionType) {
            Preconditions.checkNotNull(result, "Cannot find transaction manager of [%s]", transactionType);
        }
        return result;
    }
    
    /**
     * Close sharding transaction managers.
     * 
     * @throws Exception exception
     */
    public void close() throws Exception {
        for (Entry<TransactionType, ShardingTransactionManager> entry : transactionManagerMap.entrySet()) {
            entry.getValue().close();
        }
    }
}
