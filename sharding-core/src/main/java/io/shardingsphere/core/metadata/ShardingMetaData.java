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

package io.shardingsphere.core.metadata;

import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import io.shardingsphere.core.metadata.table.ShardingTableMetaData;
import io.shardingsphere.core.metadata.table.executor.TableMetaDataConnectionManager;
import io.shardingsphere.core.metadata.table.executor.TableMetaDataInitializer;
import io.shardingsphere.core.rule.ShardingRule;
import lombok.Getter;

import java.util.Map;

/**
 * Sharding meta data.
 *
 * @author zhangliang
 */
@Getter
public final class ShardingMetaData {
    
    private final ShardingDataSourceMetaData dataSource;
    
    private final ShardingTableMetaData table;
    
    public ShardingMetaData(final Map<String, String> dataSourceURLs, final ShardingRule shardingRule,
                            final DatabaseType databaseType, final ShardingExecuteEngine executeEngine, final TableMetaDataConnectionManager connectionManager, final int maxConnectionsSizePerQuery) {
        dataSource = new ShardingDataSourceMetaData(dataSourceURLs, shardingRule, databaseType);
        table = new ShardingTableMetaData(new TableMetaDataInitializer(dataSource, executeEngine, connectionManager, maxConnectionsSizePerQuery).load(shardingRule));
    }
}
