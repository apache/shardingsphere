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

package io.shardingsphere.transaction.core.datasource;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.util.ReflectiveUtil;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.api.TransactionTypeHolder;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Sharding transactional data sources.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class ShardingTransactionalDataSources implements AutoCloseable {
    
    private final Map<TransactionType, ShardingTransactionalDataSource> shardingTransactionalDataSources;
    
    private final Map<String, DataSource> originalDataSourceMap;
    
    public ShardingTransactionalDataSources(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        shardingTransactionalDataSources = new HashMap<>(TransactionType.values().length, 1);
        for (TransactionType each : TransactionType.values()) {
            shardingTransactionalDataSources.put(each, new ShardingTransactionalDataSource(databaseType, each, dataSourceMap));
        }
        originalDataSourceMap = dataSourceMap;
    }
    
    /**
     * Get data source map via transaction type from threadlocal.
     * 
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap() {
        return shardingTransactionalDataSources.get(TransactionTypeHolder.get()).getDataSourceMap(); 
    }
    
    @Override
    public void close() {
        for (Entry<TransactionType, ShardingTransactionalDataSource> entry : shardingTransactionalDataSources.entrySet()) {
            if (originalDataSourceMap != entry.getValue().getDataSourceMap()) {
                close(entry.getValue().getDataSourceMap());
            }
        }
    }
    
    private void close(final Map<String, DataSource> dataSourceMap) {
        for (DataSource each : dataSourceMap.values()) {
            try {
                ReflectiveUtil.findMethod(each, "close").invoke(each);
            } catch (final ReflectiveOperationException ignored) {
            }
        }
    }
}
