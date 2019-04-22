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

package org.apache.shardingsphere.shardingjdbc.jdbc.core;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingProperties;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.ShardingExecuteEngine;
import org.apache.shardingsphere.core.execute.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.metadata.ShardingMetaData;
import org.apache.shardingsphere.core.metadata.datasource.ShardingDataSourceMetaData;
import org.apache.shardingsphere.core.metadata.table.ShardingTableMetaData;
import org.apache.shardingsphere.core.parse.cache.ParsingResultCache;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.shardingjdbc.jdbc.metadata.JDBCTableMetaDataConnectionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Sharding runtime context.
 * 
 * @author gaohongtao
 * @author panjuan
 */
@Getter
public final class ShardingContext implements AutoCloseable {
    
    private final DatabaseMetaData cachedDatabaseMetaData;
    
    private final ShardingRule shardingRule;
    
    private final DatabaseType databaseType;
    
    private final ShardingExecuteEngine executeEngine;
    
    private final ShardingProperties shardingProperties;
    
    private final ShardingMetaData metaData;
    
    private final ParsingResultCache parsingResultCache;
    
    public ShardingContext(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType, final Properties props) throws SQLException {
        this.shardingRule = shardingRule;
        this.cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap);
        this.databaseType = databaseType;
        shardingProperties = new ShardingProperties(null == props ? new Properties() : props);
        int executorSize = shardingProperties.getValue(ShardingPropertiesConstant.EXECUTOR_SIZE);
        executeEngine = new ShardingExecuteEngine(executorSize);
        ShardingDataSourceMetaData shardingDataSourceMetaData = new ShardingDataSourceMetaData(getDataSourceURLs(dataSourceMap), shardingRule, databaseType);
        ShardingTableMetaData shardingTableMetaData = new ShardingTableMetaData(getTableMetaDataInitializer(dataSourceMap, shardingDataSourceMetaData).load(shardingRule));
        metaData = new ShardingMetaData(shardingDataSourceMetaData, shardingTableMetaData);
        parsingResultCache = new ParsingResultCache();
    }
    
    private DatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData(), dataSourceMap, shardingRule);
        }
    }
    
    private Map<String, String> getDataSourceURLs(final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, String> result = new LinkedHashMap<>(dataSourceMap.size(), 1);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            result.put(entry.getKey(), getDataSourceURL(entry.getValue()));
        }
        return result;
    }
    
    private String getDataSourceURL(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return connection.getMetaData().getURL();
        }
    }
    
    private TableMetaDataInitializer getTableMetaDataInitializer(final Map<String, DataSource> dataSourceMap, final ShardingDataSourceMetaData shardingDataSourceMetaData) {
        return new TableMetaDataInitializer(shardingDataSourceMetaData, executeEngine, new JDBCTableMetaDataConnectionManager(dataSourceMap),
                shardingProperties.<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                shardingProperties.<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
    }
    
    @Override
    public void close() {
        executeEngine.close();
    }
}
