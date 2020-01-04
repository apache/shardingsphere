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

package org.apache.shardingsphere.encrypt.metadata.loader;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.metadata.decorator.EncryptTableMetaDataDecorator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.TableMetaDataLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

/**
 * Table meta data loader for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class EncryptTableMetaDataLoader implements TableMetaDataLoader<EncryptRule> {
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ConnectionManager connectionManager;
    
    private final ColumnMetaDataLoader columnMetaDataLoader = new ColumnMetaDataLoader();
    
    private final EncryptTableMetaDataDecorator encryptTableMetaDataDecorator = new EncryptTableMetaDataDecorator();
    
    @Override
    public TableMetaData load(final String tableName, final EncryptRule encryptRule) throws SQLException {
        return encryptTableMetaDataDecorator.decorate(load(tableName), tableName, encryptRule);
    }
    
    private TableMetaData load(final String tableName) throws SQLException {
        try (Connection connection = connectionManager.getConnection(dataSourceMetas.getAllInstanceDataSourceNames().iterator().next())) {
            return createTableMetaData(connection, dataSourceMetas.getDataSourceMetaData(dataSourceMetas.getAllInstanceDataSourceNames().iterator().next()), tableName);
        }
    }
    
    private TableMetaData createTableMetaData(final Connection connection, final DataSourceMetaData dataSourceMetaData, final String tableName) throws SQLException {
        String catalog = dataSourceMetaData.getCatalog();
        return isTableExist(connection, catalog, tableName)
                ? new TableMetaData(columnMetaDataLoader.load(connection, catalog, tableName), Collections.<String>emptyList())
                : new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptySet());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, tableName, null)) {
            return resultSet.next();
        }
    }
    
    @Override
    public TableMetas loadAll(final EncryptRule encryptRule) throws SQLException {
        return new TableMetas(new HashMap<>(loadAllTables()));
    }
    
    private Map<String, TableMetaData> loadAllTables() throws SQLException {
        Collection<String> tableNames = loadAllTableNames();
        Map<String, TableMetaData> result = new HashMap<>(tableNames.size(), 1);
        for (String each : tableNames) {
            result.put(each, load(each));
        }
        return result;
    }
    
    private Collection<String> loadAllTableNames() throws SQLException {
        Collection<String> result = new LinkedHashSet<>();
        String dataSourceName = dataSourceMetas.getAllInstanceDataSourceNames().iterator().next();
        DataSourceMetaData dataSourceMetaData = dataSourceMetas.getDataSourceMetaData(dataSourceName);
        String catalog = null == dataSourceMetaData ? null : dataSourceMetaData.getCatalog();
        String schemaName = null == dataSourceMetaData ? null : dataSourceMetaData.getSchema();
        try (
                Connection connection = connectionManager.getConnection(dataSourceName);
                ResultSet resultSet = connection.getMetaData().getTables(catalog, schemaName, null, new String[]{"TABLE"})) {
            while (resultSet.next()) {
                String tableName = resultSet.getString("TABLE_NAME");
                if (!tableName.contains("$") && !tableName.contains("/")) {
                    result.add(tableName);
                }
            }
        }
        return result;
    }
}
