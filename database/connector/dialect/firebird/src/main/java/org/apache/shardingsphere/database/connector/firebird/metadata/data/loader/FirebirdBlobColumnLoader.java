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

package org.apache.shardingsphere.database.connector.firebird.metadata.data.loader;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Loader for Firebird blob columns.
 */
@RequiredArgsConstructor
final class FirebirdBlobColumnLoader {
    
    private static final String SELECT_BLOB_COLUMNS_SQL =
            "SELECT TRIM(rf.RDB$FIELD_NAME) AS COLUMN_NAME, f.RDB$FIELD_SUB_TYPE AS SUB_TYPE "
                    + "FROM RDB$RELATION_FIELDS rf "
                    + "JOIN RDB$FIELDS f ON rf.RDB$FIELD_SOURCE = f.RDB$FIELD_NAME "
                    + "WHERE TRIM(UPPER(rf.RDB$RELATION_NAME)) = ? "
                    + "AND f.RDB$FIELD_TYPE = 261";
    
    private final MetaDataLoaderMaterial material;
    
    Map<String, Map<String, Integer>> load() throws SQLException {
        if (material.getActualTableNames().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Integer>> result = new HashMap<>(material.getActualTableNames().size(), 1F);
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(material.getStorageType());
        try (
                MetaDataLoaderConnection connection = new MetaDataLoaderConnection(
                        material.getStorageType(), material.getDataSource().getConnection())) {
            for (String each : material.getActualTableNames()) {
                String formattedTableName = databaseTypeRegistry.formatIdentifierPattern(each);
                Map<String, Integer> blobColumns = loadTableBlobColumns(connection, formattedTableName);
                result.put(each, blobColumns);
            }
        }
        return result;
    }
    
    private Map<String, Integer> loadTableBlobColumns(final MetaDataLoaderConnection connection, final String formattedTableName) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BLOB_COLUMNS_SQL)) {
            preparedStatement.setString(1, formattedTableName.toUpperCase(Locale.ENGLISH));
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    String normalizedColumnName = normalizeColumnName(resultSet.getString("COLUMN_NAME"));
                    if (null != normalizedColumnName) {
                        result.put(normalizedColumnName, getColumnSubtype(resultSet));
                    }
                }
            }
        }
        return result.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(result);
    }
    
    private String normalizeColumnName(final String columnName) {
        if (null == columnName) {
            return null;
        }
        String trimmed = columnName.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase(Locale.ENGLISH);
    }
    
    private Integer getColumnSubtype(final ResultSet resultSet) throws SQLException {
        Object subTypeValue = resultSet.getObject("SUB_TYPE");
        return null == subTypeValue ? null : ((Number) subTypeValue).intValue();
    }
}
