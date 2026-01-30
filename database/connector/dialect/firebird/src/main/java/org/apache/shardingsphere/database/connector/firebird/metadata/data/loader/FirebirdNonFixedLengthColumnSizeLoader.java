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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Loader for Firebird column sizes with not fixed length.
 */
@RequiredArgsConstructor
final class FirebirdNonFixedLengthColumnSizeLoader {
    
    private final MetaDataLoaderMaterial material;
    
    Map<String, Map<String, Integer>> load() throws SQLException {
        if (material.getActualTableNames().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Integer>> result = new HashMap<>(material.getActualTableNames().size(), 1F);
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(material.getStorageType());
        try (MetaDataLoaderConnection connection = new MetaDataLoaderConnection(material.getStorageType(), material.getDataSource().getConnection())) {
            for (String each : material.getActualTableNames()) {
                String formattedTableName = databaseTypeRegistry.formatIdentifierPattern(each);
                Map<String, Integer> columnSizes = loadTableColumnSizes(connection, formattedTableName);
                result.put(each, columnSizes);
            }
        }
        return result;
    }
    
    private Map<String, Integer> loadTableColumnSizes(final MetaDataLoaderConnection connection, final String formattedTableName) throws SQLException {
        Map<String, Integer> result = new HashMap<>();
        loadColumnSizesFromMetaData(connection, formattedTableName, result);
        return result.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(result);
    }
    
    private void loadColumnSizesFromMetaData(final MetaDataLoaderConnection connection, final String formattedTableName, final Map<String, Integer> result) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), formattedTableName, "%")) {
            while (resultSet.next()) {
                if (!Objects.equals(formattedTableName, resultSet.getString("TABLE_NAME"))) {
                    continue;
                }
                int dataType = resultSet.getInt("DATA_TYPE");
                if (!isNonFixedLengthType(dataType)) {
                    continue;
                }
                String columnName = resultSet.getString("COLUMN_NAME");
                if (null != columnName) {
                    int columnSize = resultSet.getInt("COLUMN_SIZE");
                    if (!resultSet.wasNull() && columnSize > 0) {
                        result.put(columnName.toUpperCase(Locale.ENGLISH), columnSize);
                    }
                }
            }
        }
    }
    
    private boolean isNonFixedLengthType(final int dataType) {
        switch (dataType) {
            // Binary types are included for metadata loading only.
            // They are not used in statement prepare/describe yet.
            case Types.CHAR:
            case Types.NCHAR:
            case Types.VARCHAR:
            case Types.NVARCHAR:
            case Types.LONGVARCHAR:
            case Types.LONGNVARCHAR:
            case Types.BINARY:
            case Types.VARBINARY:
            case Types.LONGVARBINARY:
                return true;
            default:
                return false;
        }
    }
}
