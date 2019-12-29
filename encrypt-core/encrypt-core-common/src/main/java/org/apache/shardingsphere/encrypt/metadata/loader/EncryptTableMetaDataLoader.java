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

import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.encrypt.metadata.EncryptColumnMetaData;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.spi.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.underlying.common.metadata.column.loader.ColumnMetaDataLoader;
import org.apache.shardingsphere.underlying.common.metadata.datasource.DataSourceMetas;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.ConnectionManager;
import org.apache.shardingsphere.underlying.common.metadata.table.loader.TableMetaDataLoader;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

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
                ? new TableMetaData(loadColumnMetaDataList(connection, catalog, tableName, encryptRule), Collections.<String>emptyList())
                : new TableMetaData(Collections.<ColumnMetaData>emptyList(), Collections.<String>emptySet());
    }
    
    private boolean isTableExist(final Connection connection, final String catalog, final String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(catalog, null, tableName, null)) {
            return resultSet.next();
        }
    }
    
    private Collection<ColumnMetaData> loadColumnMetaDataList(final Connection connection, final String catalog, final String tableName, final EncryptRule encryptRule) throws SQLException {
        Collection<ColumnMetaData> result = new LinkedList<>();
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        for (ColumnMetaData each : columnMetaDataLoader.load(connection, catalog, tableName)) {
            Optional<ColumnMetaData> filteredColumnMetaData = filterColumnMetaData(each, tableName, encryptRule, derivedColumns);
            if (filteredColumnMetaData.isPresent()) {
                result.add(filteredColumnMetaData.get());
            }
        }
        return result;
    }
    
    private Optional<ColumnMetaData> filterColumnMetaData(final ColumnMetaData columnMetaData, final String tableName, final EncryptRule encryptRule, final Collection<String> derivedColumns) {
        String columnName = columnMetaData.getName();
        if (derivedColumns.contains(columnName)) {
            return Optional.absent();
        }
        if (!encryptRule.isCipherColumn(tableName, columnName)) {
            return Optional.of(columnMetaData);
        }
        String logicColumnName = encryptRule.getLogicColumnOfCipher(tableName, columnName);
        String plainColumnName = encryptRule.findPlainColumn(tableName, logicColumnName).orNull();
        String assistedQueryColumnName = encryptRule.findAssistedQueryColumn(tableName, logicColumnName).orNull();
        return Optional.<ColumnMetaData>of(
                new EncryptColumnMetaData(logicColumnName, columnMetaData.getDataType(), columnMetaData.isPrimaryKey(), columnName, plainColumnName, assistedQueryColumnName));
    }
}
