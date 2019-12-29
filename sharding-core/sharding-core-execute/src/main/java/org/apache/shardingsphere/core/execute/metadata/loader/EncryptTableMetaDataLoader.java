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

package org.apache.shardingsphere.core.execute.metadata.loader;

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.ConnectionManager;
import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.TableMetaDataLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Table meta data loader for encrypt.
 *
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
public final class EncryptTableMetaDataLoader implements TableMetaDataLoader<EncryptRule> {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private final DataSourceMetas dataSourceMetas;
    
    private final ConnectionManager connectionManager;
    
    @Override
    public TableMetaData load(final String logicTableName, final EncryptRule encryptRule) throws SQLException {
        return load(encryptRule, logicTableName);
    }
    
    private TableMetaData load(final EncryptRule encryptRule, final String logicTableName) throws SQLException {
        DataSourceMetaData dataSourceMetaData = dataSourceMetas.getDataSourceMetaData(dataSourceMetas.getAllInstanceDataSourceNames().iterator().next());
        return load(dataSourceMetaData, logicTableName, encryptRule);
    }
    
    private TableMetaData load(final DataSourceMetaData dataSourceMetaData, final String tableName, final EncryptRule encryptRule) throws SQLException {
        try (Connection connection = connectionManager.getConnection(dataSourceMetas.getAllInstanceDataSourceNames().iterator().next())) {
            return createTableMetaData(connection, dataSourceMetaData, tableName, encryptRule);
        }
    }
    
    private TableMetaData createTableMetaData(final Connection connection, final DataSourceMetaData dataSourceMetaData, final String tableName, final EncryptRule encryptRule) throws SQLException {
        String catalog = dataSourceMetaData.getCatalog();
        return isTableExist(connection, catalog, tableName)
                ? new TableMetaData(getColumnMetaDataList(connection, catalog, tableName, encryptRule), Collections.<String>emptyList())
                : new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptySet());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, tableName, null)) {
            return resultSet.next();
        }
    }
    
    private Collection<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String catalog, final String tableName, final EncryptRule encryptRule) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, catalog, tableName);
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(catalog, null, tableName, "%")) {
            while (resultSet.next()) {
                String columnName = resultSet.getString(COLUMN_NAME);
                String columnType = resultSet.getString(TYPE_NAME);
                boolean isPrimaryKey = primaryKeys.contains(columnName);
                Optional<ColumnMetaData> columnMetaData = getColumnMetaData(tableName, columnName, columnType, isPrimaryKey, encryptRule, derivedColumns);
                if (columnMetaData.isPresent()) {
                    result.add(columnMetaData.get());
                }
            }
        }
        return result;
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String catalog, final String tableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(catalog, null, tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
    
    private Optional<ColumnMetaData> getColumnMetaData(final String tableName, final String columnName, 
                                                       final String columnType, final boolean isPrimaryKey, final EncryptRule encryptRule, final Collection<String> derivedColumns) {
        if (derivedColumns.contains(columnName)) {
            return Optional.absent();
        }
        if (encryptRule.isCipherColumn(tableName, columnName)) {
            String logicColumnName = encryptRule.getLogicColumnOfCipher(tableName, columnName);
            String plainColumnName = encryptRule.findPlainColumn(tableName, logicColumnName).orNull();
            String assistedQueryColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName).orNull();
            return Optional.<ColumnMetaData>of(new EncryptColumnMetaData(logicColumnName, columnType, isPrimaryKey, columnName, plainColumnName, assistedQueryColumnName));
        }
        return Optional.of(new ColumnMetaData(columnName, columnType, isPrimaryKey));
    }
}
