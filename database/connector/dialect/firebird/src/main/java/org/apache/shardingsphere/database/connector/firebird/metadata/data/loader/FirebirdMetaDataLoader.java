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

import org.apache.shardingsphere.database.connector.core.metadata.data.loader.DialectMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderConnection;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.connector.firebird.metadata.data.FirebirdSizeRegistry;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Objects;

/**
 * Meta data loader for Firebird.
 */
public final class FirebirdMetaDataLoader implements DialectMetaDataLoader {

    private static final Set<String> LENGTH_AWARE_TYPES = new HashSet<>(3, 1F);

    static {
        LENGTH_AWARE_TYPES.add("VARYING");
        LENGTH_AWARE_TYPES.add("VARCHAR");
        LENGTH_AWARE_TYPES.add("LEGACY_VARYING");
    }

    @Override
    public Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        for (String each : material.getActualTableNames()) {
            TableMetaDataLoader.load(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        loadColumnSizes(material);
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }

    private void loadColumnSizes(final MetaDataLoaderMaterial material) throws SQLException {
        if (material.getActualTableNames().isEmpty()) {
            return;
        }
        DatabaseTypeRegistry databaseTypeRegistry = new DatabaseTypeRegistry(material.getStorageType());
        try (MetaDataLoaderConnection connection = new MetaDataLoaderConnection(material.getStorageType(), material.getDataSource().getConnection())) {
            for (String each : material.getActualTableNames()) {
                String formattedTableName = databaseTypeRegistry.formatIdentifierPattern(each);
                Map<String, Integer> columnSizes = new HashMap<>();
                try (ResultSet resultSet = connection.getMetaData().getColumns(connection.getCatalog(), connection.getSchema(), formattedTableName, "%")) {
                    while (resultSet.next()) {
                        if (!Objects.equals(formattedTableName, resultSet.getString("TABLE_NAME"))) {
                            continue;
                        }
                        if (!isLengthAwareType(resultSet.getString("TYPE_NAME"))) {
                            continue;
                        }
                        String columnName = resultSet.getString("COLUMN_NAME");
                        if (null != columnName) {
                            columnSizes.put(columnName, resultSet.getInt("COLUMN_SIZE"));
                        }
                    }
                }
                FirebirdSizeRegistry.refreshTable(material.getDefaultSchemaName(), each, columnSizes);
            }
        }
    }

    private boolean isLengthAwareType(final String typeName) {
        if (null == typeName) {
            return false;
        }
        String normalized = typeName.toUpperCase(Locale.ENGLISH);
        for (String each : LENGTH_AWARE_TYPES) {
            if (normalized.startsWith(each)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getDatabaseType() {
        return "Firebird";
    }
}
