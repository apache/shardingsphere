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

package org.apache.shardingsphere.shardingjdbc.jdbc.core.context;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.metadata.TableMetaDataInitializer;
import org.apache.shardingsphere.core.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.core.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.metadata.CachedDatabaseMetaData;
import org.apache.shardingsphere.shardingjdbc.jdbc.metadata.JDBCTableMetaDataConnectionManager;
import org.apache.shardingsphere.spi.database.DatabaseType;
import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

/**
 * Runtime context for sharding.
 * 
 * @author gaohongtao
 * @author panjuan
 * @author zhangliang
 */
@Getter
public final class ShardingRuntimeContext extends AbstractRuntimeContext<ShardingRule> {
    
    private final DatabaseMetaData cachedDatabaseMetaData;
    
    private final ShardingSphereMetaData metaData;
    
    private final ShardingTransactionManagerEngine shardingTransactionManagerEngine;
    
    public ShardingRuntimeContext(final Map<String, DataSource> dataSourceMap, final ShardingRule rule, final Properties props, final DatabaseType databaseType) throws SQLException {
        super(rule, props, databaseType);
        cachedDatabaseMetaData = createCachedDatabaseMetaData(dataSourceMap, rule);
        metaData = createMetaData(dataSourceMap, rule, databaseType);
        shardingTransactionManagerEngine = new ShardingTransactionManagerEngine();
        shardingTransactionManagerEngine.init(databaseType, dataSourceMap);
    }
    
    private DatabaseMetaData createCachedDatabaseMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule rule) throws SQLException {
        try (Connection connection = dataSourceMap.values().iterator().next().getConnection()) {
            return new CachedDatabaseMetaData(connection.getMetaData(), dataSourceMap, rule);
        }
    }
    
    private ShardingSphereMetaData createMetaData(final Map<String, DataSource> dataSourceMap, final ShardingRule shardingRule, final DatabaseType databaseType) throws SQLException {
        DataSourceMetas dataSourceMetas = new DataSourceMetas(getDataSourceURLs(dataSourceMap), databaseType);
        TableMetas tableMetas = new TableMetas(getTableMetaDataInitializer(dataSourceMap, dataSourceMetas).load(shardingRule));
        return new ShardingSphereMetaData(dataSourceMetas, tableMetas);
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
    
    private TableMetaDataInitializer getTableMetaDataInitializer(final Map<String, DataSource> dataSourceMap, final DataSourceMetas dataSourceMetas) {
        return new TableMetaDataInitializer(dataSourceMetas, getExecuteEngine(), new JDBCTableMetaDataConnectionManager(dataSourceMap),
                this.getProps().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY),
                this.getProps().<Boolean>getValue(ShardingPropertiesConstant.CHECK_TABLE_METADATA_ENABLED));
    }
    
    @Override
    public void close() throws Exception {
        shardingTransactionManagerEngine.close();
        super.close();
    }
}
