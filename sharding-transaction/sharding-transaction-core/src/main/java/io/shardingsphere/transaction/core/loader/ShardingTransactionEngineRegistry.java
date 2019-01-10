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

package io.shardingsphere.transaction.core.loader;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.spi.ShardingTransactionEngine;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;

/**
 * Sharding transaction manager loader.
 *
 * @author zhaojun
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class ShardingTransactionEngineRegistry {
    
    private static final Map<TransactionType, ShardingTransactionEngine> TRANSACTION_ENGINES = new HashMap<>();
    
    static {
        load();
    }
    
    /**
     * Load sharding transaction engines.
     */
    private static void load() {
        for (ShardingTransactionEngine each : ServiceLoader.load(ShardingTransactionEngine.class)) {
            if (TRANSACTION_ENGINES.containsKey(each.getTransactionType())) {
                log.warn("Find more than one {} transaction engine implementation class, use `{}` now",
                    each.getTransactionType(), TRANSACTION_ENGINES.get(each.getTransactionType()).getClass().getName());
                continue;
            }
            TRANSACTION_ENGINES.put(each.getTransactionType(), each);
        }
    }
    
    /**
     * Get sharding transaction engine.
     *
     * @param transactionType transaction type
     * @return sharding transaction engine
     */
    public static ShardingTransactionEngine getShardingTransactionEngine(final TransactionType transactionType) {
        return TRANSACTION_ENGINES.get(transactionType);
    }
    
    /**
     * Register transaction resource.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    public static void registerTransactionResource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Entry<TransactionType, ShardingTransactionEngine> entry : TRANSACTION_ENGINES.entrySet()) {
            entry.getValue().registerTransactionalResource(databaseType, dataSourceMap);
        }
    }
}
