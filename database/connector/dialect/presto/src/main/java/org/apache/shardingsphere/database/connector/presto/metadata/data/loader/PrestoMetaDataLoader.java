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

package org.apache.shardingsphere.database.connector.presto.metadata.data.loader;

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;

import java.sql.Connection;
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
 * Meta data loader for Presto.
 * As of the Memory Connector of prestodb/presto 0.290, the table `INFORMATION_SCHEMA.INDEXES` does not exist,
 * and `INFORMATION_SCHEMA.COLUMNS` does not have a column `IS_VISIBLE`.
 * The current implementation does not record the table's index, primary keys, generated info, or column visibility.
 */
public final class PrestoMetaDataLoader implements DialectMetaDataLoader {
    
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaDataList = new LinkedList<>();
        try (Connection connection = material.getDataSource().getConnection()) {
            Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, material.getActualTableNames());
            for (Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                tableMetaDataList.add(new TableMetaData(entry.getKey(), entry.getValue(), Collections.emptyList(), Collections.emptyList()));
            }
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaDataList));
    }
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            preparedStatement.setString(1, connection.getCatalog());
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
            return "SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_CATALOG=? ORDER BY ORDINAL_POSITION";
        }
        String tableNames = tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(","));
        return String.format("SELECT TABLE_CATALOG,TABLE_NAME,COLUMN_NAME,DATA_TYPE,ORDINAL_POSITION,IS_NULLABLE FROM INFORMATION_SCHEMA.COLUMNS"
                + " WHERE TABLE_CATALOG=? AND UPPER(TABLE_NAME) IN (%s) ORDER BY ORDINAL_POSITION", tableNames);
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), Boolean.FALSE, Boolean.FALSE, "other", false, Boolean.TRUE, false,
                isNullable);
    }
    
    @Override
    public String getDatabaseType() {
        return "Presto";
    }
}
