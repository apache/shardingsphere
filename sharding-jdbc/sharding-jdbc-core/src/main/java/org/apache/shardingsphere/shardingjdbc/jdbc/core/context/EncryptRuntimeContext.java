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

import com.google.common.base.Optional;
import lombok.Getter;
import org.apache.shardingsphere.core.metadata.column.ColumnMetaData;
import org.apache.shardingsphere.core.metadata.column.EncryptColumnMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetaData;
import org.apache.shardingsphere.core.metadata.table.TableMetas;
import org.apache.shardingsphere.core.rule.EncryptRule;
import org.apache.shardingsphere.spi.database.DatabaseType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Runtime context for encrypt.
 *
 * @author zhangliang
 */
@Getter
public final class EncryptRuntimeContext extends AbstractRuntimeContext<EncryptRule> {
    
    private static final String COLUMN_NAME = "COLUMN_NAME";
    
    private static final String TYPE_NAME = "TYPE_NAME";
    
    private static final String INDEX_NAME = "INDEX_NAME";
    
    private final TableMetas tableMetas;
    
    public EncryptRuntimeContext(final DataSource dataSource, final EncryptRule encryptRule, final Properties props, final DatabaseType databaseType) throws SQLException {
        super(encryptRule, props, databaseType);
        tableMetas = createEncryptTableMetas(dataSource, encryptRule);
    }
    
    private TableMetas createEncryptTableMetas(final DataSource dataSource, final EncryptRule encryptRule) throws SQLException {
        Map<String, TableMetaData> tables = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            for (String each : encryptRule.getEncryptTableNames()) {
                if (isTableExist(connection, each)) {
                    tables.put(each, new TableMetaData(getColumnMetaDataList(connection, each, encryptRule), getIndexes(connection, each)));
                }
            }
        }
        return new TableMetas(tables);
    }
    
    private boolean isTableExist(final Connection connection, final String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), null, tableName, null)) {
            return resultSet.next();
        }
    }
    
    private List<ColumnMetaData> getColumnMetaDataList(final Connection connection, final String tableName, final EncryptRule encryptRule) throws SQLException {
        List<ColumnMetaData> result = new LinkedList<>();
        Collection<String> primaryKeys = getPrimaryKeys(connection, tableName);
        Collection<String> derivedColumns = encryptRule.getAssistedQueryAndPlainColumns(tableName);
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), null, tableName, "%")) {
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
    
    private Optional<ColumnMetaData> getColumnMetaData(final String logicTableName, final String columnName, final String columnType, final boolean isPrimaryKey,
                                                       final EncryptRule encryptRule, final Collection<String> derivedColumns) {
        if (derivedColumns.contains(columnName)) {
            return Optional.absent();
        }
        if (encryptRule.isCipherColumn(logicTableName, columnName)) {
            String logicColumnName = encryptRule.getLogicColumn(logicTableName, columnName);
            String plainColumnName = encryptRule.getPlainColumn(logicTableName, logicColumnName).orNull();
            String assistedQueryColumnName = encryptRule.getAssistedQueryColumn(logicTableName, logicColumnName).orNull();
            return Optional.<ColumnMetaData>of(new EncryptColumnMetaData(logicColumnName, columnType, isPrimaryKey, columnName, plainColumnName, assistedQueryColumnName));
        }
        return Optional.of(new ColumnMetaData(columnName, columnType, isPrimaryKey));
    }
    
    private Collection<String> getPrimaryKeys(final Connection connection, final String tableName) throws SQLException {
        Collection<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getPrimaryKeys(connection.getCatalog(), null, tableName)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(COLUMN_NAME));
            }
        }
        return result;
    }
    
    private Set<String> getIndexes(final Connection connection, final String tableName) throws SQLException {
        Set<String> result = new HashSet<>();
        try (ResultSet resultSet = connection.getMetaData().getIndexInfo(connection.getCatalog(), connection.getCatalog(), tableName, false, false)) {
            while (resultSet.next()) {
                result.add(resultSet.getString(INDEX_NAME));
            }
        }
        return result;
    }
}
