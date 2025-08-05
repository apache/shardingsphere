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

package org.apache.shardingsphere.infra.database.hive.metadata.data.loader;

import org.apache.shardingsphere.infra.database.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.database.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.ColumnMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.metadata.database.datatype.DataTypeRegistry;

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
import java.util.stream.Collectors;

/**
 * Hive meta data loader.
 * As of the HiveServer2 of apache/hive 4.0.1, the table `INFORMATION_SCHEMA.INDEXES` does not exist,
 * and `INFORMATION_SCHEMA.COLUMNS` does not have a column `IS_VISIBLE`.
 * The current implementation does not record the table's index, primary keys, generated info, or column visibility.
 */
public final class HiveMetaDataLoader implements DialectMetaDataLoader {
    
    @SuppressWarnings("SqlNoDataSourceInspection")
    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        boolean informationSchemaFlag;
        try (Connection connection = material.getDataSource().getConnection()) {
            informationSchemaFlag = connection.createStatement().executeQuery("SHOW DATABASES LIKE 'INFORMATION_SCHEMA'").next();
        }
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        if (informationSchemaFlag) {
            try (Connection connection = material.getDataSource().getConnection()) {
                Map<String, Collection<ColumnMetaData>> columnMetaDataMap = loadColumnMetaDataMap(connection, material.getActualTableNames());
                for (Map.Entry<String, Collection<ColumnMetaData>> entry : columnMetaDataMap.entrySet()) {
                    tableMetaData.add(new TableMetaData(entry.getKey(), entry.getValue(), Collections.emptyList(), Collections.emptyList()));
                }
            }
            return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
        }
        for (String each : material.getActualTableNames()) {
            TableMetaDataLoader.load(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    /**
     * For apache/hive 4.0.1, `org.apache.hive.jdbc.HiveConnection` does not implement {@link java.sql.Connection#getCatalog}.
     *
     * @param connection connection
     * @param tables     tables
     * @return a map of table name to its column metadata
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("SqlSourceToSinkFlow")
    private Map<String, Collection<ColumnMetaData>> loadColumnMetaDataMap(final Connection connection, final Collection<String> tables) throws SQLException {
        Map<String, Collection<ColumnMetaData>> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(getTableMetaDataSQL(tables))) {
            preparedStatement.setString(1, "default");
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
            return "SELECT TABLE_CATALOG,\n"
                    + "       TABLE_NAME,\n"
                    + "       COLUMN_NAME,\n"
                    + "       DATA_TYPE,\n"
                    + "       ORDINAL_POSITION,\n"
                    + "       IS_NULLABLE\n"
                    + "FROM INFORMATION_SCHEMA.COLUMNS\n"
                    + "WHERE TABLE_CATALOG = ?\n"
                    + "ORDER BY ORDINAL_POSITION";
        }
        String tableNames = tables.stream().map(each -> String.format("'%s'", each).toUpperCase()).collect(Collectors.joining(","));
        return String.format("SELECT TABLE_CATALOG,\n"
                + "       TABLE_NAME,\n"
                + "       COLUMN_NAME,\n"
                + "       DATA_TYPE,\n"
                + "       ORDINAL_POSITION,\n"
                + "       IS_NULLABLE\n"
                + "FROM INFORMATION_SCHEMA.COLUMNS\n"
                + "WHERE TABLE_CATALOG = ?\n"
                + "  AND UPPER(TABLE_NAME) IN (%s)\n"
                + "ORDER BY ORDINAL_POSITION", tableNames);
    }
    
    private ColumnMetaData loadColumnMetaData(final ResultSet resultSet) throws SQLException {
        String columnName = resultSet.getString("COLUMN_NAME");
        String dataType = resultSet.getString("DATA_TYPE");
        boolean isNullable = "YES".equals(resultSet.getString("IS_NULLABLE"));
        return new ColumnMetaData(columnName, DataTypeRegistry.getDataType(getDatabaseType(), dataType).orElse(Types.OTHER), Boolean.FALSE, Boolean.FALSE, false, Boolean.TRUE, false, isNullable);
    }
    
    @Override
    public String getDatabaseType() {
        return "Hive";
    }
}
