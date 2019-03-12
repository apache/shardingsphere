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

package org.apache.shardingsphere.core.metadata;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.executor.ShardingExecuteEngine;
import org.apache.shardingsphere.core.executor.metadata.TableMetaDataConnectionManager;
import org.apache.shardingsphere.core.executor.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.rule.ShardingRule;

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
    
    public ShardingMetaData(final Map<String, String> dataSourceURLs, final ShardingRule shardingRule, final DatabaseType databaseType, final ShardingExecuteEngine executeEngine, 
                            final TableMetaDataConnectionManager connectionManager, final int maxConnectionsSizePerQuery, final boolean isCheckingMetaData) {
        dataSource = new ShardingDataSourceMetaData(dataSourceURLs, shardingRule, databaseType);
        TableMetaDataInitializer tableInitialize = new TableMetaDataInitializer(dataSource, executeEngine, connectionManager, maxConnectionsSizePerQuery, isCheckingMetaData);
        table = new ShardingTableMetaData(tableInitialize.load(shardingRule));
    }
}
