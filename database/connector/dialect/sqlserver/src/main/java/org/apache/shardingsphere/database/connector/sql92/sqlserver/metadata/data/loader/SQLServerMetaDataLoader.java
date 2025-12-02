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

package org.apache.shardingsphere.database.connector.sql92.sqlserver.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.IndexMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Meta data loader for SQLServer.
 */
public final class SQLServerMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String TABLE_META_DATA_SQL_NO_ORDER = "SELECT obj.name AS TABLE_NAME, col.name AS COLUMN_NAME, t.name AS DATA_TYPE,"
            + " col.collation_name AS COLLATION_NAME, col.column_id, is_identity AS IS_IDENTITY, col.is_nullable AS IS_NULLABLE, %s"
            + " (SELECT TOP 1 ind.is_primary_key FROM sys.index_columns ic LEFT JOIN sys.indexes ind ON ic.object_id = ind.object_id"
            + " AND ic.index_id = ind.index_id AND ind.name LIKE 'PK_%%' WHERE ic.object_id = obj.object_id AND ic.column_id = col.column_id) AS IS_PRIMARY_KEY"
            + " FROM sys.objects obj INNER JOIN sys.columns col ON obj.object_id = col.object_id LEFT JOIN sys.types t ON t.user_type_id = col.user_type_id";
    
    private static final String ORDER_BY_COLUMN_ID = " ORDER BY col.column_id";
    
    private static final String TABLE_META_DATA_SQL = TABLE_META_DATA_SQL_NO_ORDER + ORDER_BY_COLUMN_ID;
    
    private static final String TABLE_META_DATA_SQL_IN_TABLES = TABLE_META_DATA_SQL_NO_ORDER + " WHERE obj.name IN (%s)" + ORDER_BY_COLUMN_ID;
    
    private static final String INDEX_META_DATA_SQL = "SELECT idx.name AS INDEX_NAME, obj.name AS TABLE_NAME, col.name AS COLUMN_NAME,"
            + " idx.is_unique AS IS_UNIQUE FROM sys.indexes idx"
            + " LEFT JOIN sys.objects obj ON idx.object_id = obj.object_id"
            + " LEFT JOIN sys.columns col ON obj.object_id = col.object_id"
            + " WHERE idx.index_id NOT IN (0, 255) AND obj.name IN (%s) ORDER BY idx.index_id";
    
    private static final int HIDDEN_COLUMN_START_MAJOR_VERSION = 15;
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(material.getDataSource(), material.getActualTableNames());
        if (!columnMetaDataMap.isEmpty()) {
            Map<String, Collection<IndexMetaData>> indexMetaDataMap = loadIndexMetaData(material.getDataSource(), columnMetaDataMap.keySet());
            for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                Collection<IndexMetaData> indexMetaDataList = indexMetaDataMap.getOrDefault(entry.getKey(), Collections.emptyList());
                tableMetaDataList.add(new TableMetaData(entry.getKey(), entry.getValue(), indexMetaDataList, Collections.emptyList()));
            }
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
    }
    
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final DataSource dataSource, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables, connection.getMetaData()))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(resultSet, connection.getMetaData());
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet, final DatabaseMetaData databaseMetaData) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        String collationName = resultSet.getString("COLLATION_NAME");
        boolean primaryKey = "1".equals(resultSet.getString("IS_PRIMARY_KEY"));
        boolean generated = "1".equals(resultSet.getString("IS_IDENTITY"));
        boolean caseSensitive = null != collationName && collationName.contains("_CS");
        boolean isVisible = !(versionContainsHiddenColumn(databaseMetaData) && "1".equals(resultSet.getString("IS_HIDDEN")));
        boolean isNullable = "1".equals(resultSet.getString("IS_NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), primaryKey, generated,"", caseSensitive, isVisible, false, isNullable);
    }
    
    private String getTableMetaDataSQL(final Collection<String> tables, final DatabaseMetaData databaseMetaData) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder(24);
        if (versionContainsHiddenColumn(databaseMetaData)) {
            stringBuilder.append("is_hidden AS IS_HIDDEN,");
        }
        String hiddenFlag = stringBuilder.toString();
        return tables.isEmpty() ? String.format(TABLE_META_DATA_SQL, hiddenFlag)
                : String.format(TABLE_META_DATA_SQL_IN_TABLES, hiddenFlag, tables.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    private boolean versionContainsHiddenColumn(final DatabaseMetaData databaseMetaData) throws SQLException {
        return databaseMetaData.getDatabaseMajorVersion() >= HIDDEN_COLUMN_START_MAJOR_VERSION;
    }
    
    private Map<String, Collection<IndexMetaData>> loadIndexMetaData(final DataSource dataSource, final Collection<String> tableNames) throws SQLException {
        Map<String, Map<String, IndexMetaData>> tableToIndex = new HashMap<>();
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(getIndexMetaDataSQL(tableNames))) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String indexName = resultSet.getString("INDEX_NAME");
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (!tableToIndex.containsKey(tableName)) {
                        tableToIndex.put(tableName, new HashMap<>());
                    }
                    Map<String, IndexMetaData> indexMap = tableToIndex.get(tableName);
                    if (indexMap.containsKey(indexName)) {
                        indexMap.get(indexName).getColumns().add(resultSet.getString("COLUMN_NAME"));
                    } else {
                        IndexMetaData indexMetaData = new IndexMetaData(indexName, new LinkedList<>(Collections.singleton(resultSet.getString("COLUMN_NAME"))));
                        indexMetaData.setUnique("1".equals(resultSet.getString("IS_UNIQUE")));
                        indexMap.put(indexName, indexMetaData);
                    }
                }
            }
        }
        Map<String, Collection<IndexMetaData>> result = new HashMap<>(tableToIndex.size(), 1F);
        for (Entry<String, Map<String, IndexMetaData>> each : tableToIndex.entrySet()) {
            result.put(each.getKey(), each.getValue().values());
        }
        return result;
    }
    
    private String getIndexMetaDataSQL(final Collection<String> tableNames) {
        return String.format(INDEX_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each)).collect(Collectors.joining(",")));
    }
    
    @Override
    public String getDatabaseType() {
        return "SQLServer";
    }
}
