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

package io.shardingsphere.transaction.core;

import com.google.common.base.Preconditions;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.spi.ShardingTransactionManager;
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
    
    private static final Map<TransactionType, ShardingTransactionManager> ENGINES = new HashMap<>();
    
    static {
        load();
    }
    
    /**
     * Load sharding transaction engines.
     */
    private static void load() {
        for (ShardingTransactionManager each : ServiceLoader.load(ShardingTransactionManager.class)) {
            if (ENGINES.containsKey(each.getTransactionType())) {
                log.warn("Find more than one {} transaction engine implementation class, use `{}` now",
                    each.getTransactionType(), ENGINES.get(each.getTransactionType()).getClass().getName());
                continue;
            }
            ENGINES.put(each.getTransactionType(), each);
        }
    }
    
    /**
     * Get sharding transaction engine.
     *
     * @param transactionType transaction type
     * @return sharding transaction engine
     */
    public static ShardingTransactionManager getEngine(final TransactionType transactionType) {
        ShardingTransactionManager result = ENGINES.get(transactionType);
        if (TransactionType.LOCAL != transactionType) {
            Preconditions.checkNotNull(result, "Cannot find transaction manager of [%s]", transactionType);
        }
        return result;
    }
    
    /**
     * Initialize sharding transaction engines.
     * 
     * @param databaseType database type
     * @param dataSourceMap data source map
     */
    public static void init(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        for (Entry<TransactionType, ShardingTransactionManager> entry : ENGINES.entrySet()) {
            entry.getValue().init(databaseType, dataSourceMap);
        }
    }
    
    /**
     * Close sharding transaction engines.
     * 
     * @throws Exception exception
     */
    public static void close() throws Exception {
        for (Entry<TransactionType, ShardingTransactionManager> entry : ENGINES.entrySet()) {
            entry.getValue().close();
        }
    }
}
