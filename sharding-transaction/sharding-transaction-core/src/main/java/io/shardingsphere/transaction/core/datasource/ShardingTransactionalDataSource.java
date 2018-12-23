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
import io.shardingsphere.transaction.api.TransactionType;
import io.shardingsphere.transaction.core.loader.DataSourceMapConverterSPILoader;
import io.shardingsphere.transaction.spi.xa.DataSourceMapConverter;
import lombok.Getter;

import javax.sql.DataSource;
import java.util.Map;

/**
 * Sharding transactional data source.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingTransactionalDataSource {
    
    private final TransactionType type;
    
    private final Map<String, DataSource> dataSourceMap;
    
    public ShardingTransactionalDataSource(final DatabaseType databaseType, final TransactionType transactionType, final Map<String, DataSource> dataSourceMap) {
        type = transactionType;
        Optional<DataSourceMapConverter> dataSourceConverter = DataSourceMapConverterSPILoader.findDataSourceConverter(type);
        this.dataSourceMap = dataSourceConverter.isPresent() ? dataSourceConverter.get().convert(databaseType, dataSourceMap) : dataSourceMap; 
    }
}
