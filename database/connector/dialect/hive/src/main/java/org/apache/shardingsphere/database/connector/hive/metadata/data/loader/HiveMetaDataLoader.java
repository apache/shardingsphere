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

package org.apache.shardingsphere.database.connector.hive.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Hive meta data loader.
 * As of the HiveServer2 of apache/hive 4.0.1, the table `INFORMATION_SCHEMA.INDEXES` does not exist,
 * and `INFORMATION_SCHEMA.COLUMNS` does not have a column `IS_VISIBLE`.
 * The current implementation does not record the table's index, primary keys, generated info, or column visibility.
 */
public final class HiveMetaDataLoader implements DialectMetaDataLoader {
    
    private static final String VIEW_META_DATA_SQL = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES"
            + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND TABLE_TYPE='VIEW' AND UPPER(TABLE_NAME) IN (%s)";
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        if (loadInformationSchemaFlag(material)) {
            try (Connection connection = material.getDataSource().getConnection()) {
                String schemaName = connection.getSchema();
                Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, schemaName, material.getActualTableNames());
                Collection<String> viewNames = columnMetaDataMap.isEmpty() ? Collections.emptySet() : loadViewNames(connection, schemaName, columnMetaDataMap.keySet());
                for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                    tableMetaData.add(new TableMetaData(
                            entry.getKey(), entry.getValue(), Collections.emptyList(), Collections.emptyList(), viewNames.contains(entry.getKey()) ? TableType.VIEW : TableType.TABLE));
                }
            }
            return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
        }
        for (String each : material.getActualTableNames()) {
            TableMetaDataLoader.loadNormalized(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    private boolean loadInformationSchemaFlag(final MetaDataLoaderMaterial material) throws SQLException {
        String sql = "SHOW DATABASES LIKE 'INFORMATION_SCHEMA'";
        try (
                Connection connection = material.getDataSource().getConnection();
                Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            return resultSet.next();
        }
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final String schemaName, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            preparedStatement.setString(1, "default");
            preparedStatement.setString(2, schemaName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String tableName = resultSet.getString("TABLE_NAME");
                    ColumnMetaData columnMetaData = loadColumnMetaData(resultSet);
                    if (!result.containsKey(tableName)) {
                        result.put(tableName, new LinkedList<>());
                    }
                    result.get(tableName).add(columnMetaData);
                }
            }
        }
        return result;
    }
    
    private String getTableMetaDataSQL(final Collection<String> tables) {
        if (tables.isEmpty()) {
            return "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS"
                    + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? ORDER BY ORDINAL_POSITION";
        }
        String tableNames = tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(","));
        return String.format("SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_CATALOG=? AND TABLE_SCHEMA=? AND UPPER(TABLE_NAME) IN (%s) ORDER BY ORDINAL_POSITION", tableNames);
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), Boolean.FALSE, Boolean.FALSE,
                isStringDataType(dataType), Boolean.TRUE, false, isNullable);
    }
    
    private boolean isStringDataType(final String dataType) {
        String normalizedDataType = dataType.toLowerCase(Locale.ENGLISH);
        return "string".equals(normalizedDataType) || normalizedDataType.startsWith("varchar") || normalizedDataType.startsWith("char");
    }
    
    private Collection<String> loadViewNames(final Connection connection, final String schemaName, final Collection<String> tableNames) throws SQLException {
        Collection<String> result = new LinkedList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getViewMetaDataSQL(tableNames))) {
            preparedStatement.setString(1, "default");
            preparedStatement.setString(2, schemaName);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(resultSet.getString("TABLE_NAME"));
                }
            }
        }
        return result;
    }
    
    private String getViewMetaDataSQL(final Collection<String> tableNames) {
        return String.format(VIEW_META_DATA_SQL, tableNames.stream().map(each -> String.format("'%s'", each).toUpperCase(Locale.ENGLISH)).collect(Collectors.joining(",")));
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}
