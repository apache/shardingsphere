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

import com.google.common.base.Optional;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.util.ReflectiveUtil;
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.api.TransactionTypeHolder;
import io.shardingsphere.transaction.core.loader.TransactionalDataSourceConverterSPILoader;
import io.shardingsphere.transaction.spi.TransactionalDataSourceConverter;
import lombok.Getter;
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
public final class ShardingTransactionalDataSource implements AutoCloseable {
    
    @Getter
    private final Map<String, DataSource> originalDataSourceMap;
    
    private final Map<TransactionType, Map<String, DataSource>> transactionalDataSourceMap;
    
    public ShardingTransactionalDataSource(final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        originalDataSourceMap = dataSourceMap;
        transactionalDataSourceMap = new HashMap<>(TransactionType.values().length, 1);
        for (TransactionType each : TransactionType.values()) {
            Optional<TransactionalDataSourceConverter> converter = TransactionalDataSourceConverterSPILoader.findConverter(each);
            if (converter.isPresent()) {
                transactionalDataSourceMap.put(each, converter.get().convert(databaseType, dataSourceMap));
            }
        }
    }
    
    /**
     * Get data source map via transaction type from threadlocal.
     * 
     * @return data source map
     */
    public Map<String, DataSource> getDataSourceMap() {
        return transactionalDataSourceMap.containsKey(TransactionTypeHolder.get()) ? transactionalDataSourceMap.get(TransactionTypeHolder.get()) : originalDataSourceMap; 
    }
    
    @Override
    public void close() {
        close(originalDataSourceMap);
        for (Entry<TransactionType, Map<String, DataSource>> entry : transactionalDataSourceMap.entrySet()) {
            close(entry.getValue());
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
