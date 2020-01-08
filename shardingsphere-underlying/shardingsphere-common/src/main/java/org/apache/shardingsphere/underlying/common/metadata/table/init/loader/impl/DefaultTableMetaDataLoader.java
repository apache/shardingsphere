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

package org.apache.shardingsphere.underlying.common.metadata.table.init.loader.impl;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.init.loader.TableMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.BaseRule;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Default table meta data loader.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class DefaultTableMetaDataLoader implements TableMetaDataLoader {
    
    private static final String TABLE_TYPE = "TABLE";
    
    private static final String TABLE_NAME = "TABLE_NAME";
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ConnectionManager connectionManager;
    
    private final ColumnMetaDataLoader columnMetaDataLoader = new ColumnMetaDataLoader();
    
    @Override
    public TableMetaData load(final String tableName, final BaseRule rule) throws SQLException {
        String dataSourceName = getDataSourceName();
        try (Connection connection = connectionManager.getConnection(dataSourceName)) {
            return createTableMetaData(connection, dataSourceMetas.getDataSourceMetaData(dataSourceName), tableName);
        }
    }
    
    private String getDataSourceName() {
        Collection<String> allInstanceDataSourceNames = dataSourceMetas.getAllInstanceDataSourceNames();
        return allInstanceDataSourceNames.iterator().next();
    }
    
    private TableMetaData createTableMetaData(final Connection connection, final DataSourceMetaData dataSourceMetaData, final String table) throws SQLException {
        String catalog = dataSourceMetaData.getCatalog();
        Collection<ColumnMetaData> columnMetaDataList = isTableExist(connection, catalog, table) ? columnMetaDataLoader.load(connection, catalog, table) : Collections.<ColumnMetaData>emptyList();
        return new TableMetaData(columnMetaDataList, Collections.<String>emptyList());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String table) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, table, null)) {
            return resultSet.next();
        }
    }
    
    @Override
    public TableMetas loadAll(final BaseRule rule) throws SQLException {
        Collection<String> tableNames = loadAllTableNames();
        Map<String, TableMetaData> result = new HashMap<>(tableNames.size(), 1);
        // TODO concurrency load via maxConnectionsSizePerQuery
        for (String each : tableNames) {
            result.put(each, load(each, rule));
        }
        return new TableMetas(result);
    }
    
    private Collection<String> loadAllTableNames() throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        String dataSourceName = getDataSourceName();
        DataSourceMetaData dataSourceMetaData = dataSourceMetas.getDataSourceMetaData(dataSourceName);
        String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getCatalog();
        String schema = null == dataSourceMetaData ? null : dataSourceMetaData.getSchema();
        try (
                Connection connection = connectionManager.getConnection(dataSourceName);
                ResultSet resultSet = connection.getMetaData().getTables(catalog, schema, null, new String[]{TABLE_TYPE})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString(TABLE_NAME);
                if (!isSystemTable(tableName)) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
    
    private boolean isSystemTable(final String tableName) {
        return tableName.contains("$") || tableName.contains("/");
    }
}
