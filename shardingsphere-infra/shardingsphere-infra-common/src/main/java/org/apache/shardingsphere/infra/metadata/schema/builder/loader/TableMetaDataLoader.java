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

package org.apache.shardingsphere.infra.metadata.schema.builder.loader;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.adapter.MetaDataLoaderConnectionAdapter;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Table meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoader {
    
    /**
     * Load table meta data.
     *
     * @param dataSource data source
     * @param tableNamePattern table name pattern
     * @param databaseType database type
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> load(final DataSource dataSource, final String tableNamePattern, final DatabaseType databaseType) throws SQLException {
        try (MetaDataLoaderConnectionAdapter connectionAdapter = new MetaDataLoaderConnectionAdapter(databaseType, dataSource.getConnection())) {
            String formattedTableNamePattern = databaseType.formatTableNamePattern(tableNamePattern);
            return isTableExist(connectionAdapter, formattedTableNamePattern)
                    ? Optional.of(new TableMetaData(tableNamePattern, ColumnMetaDataLoader.load(
                            connectionAdapter, formattedTableNamePattern, databaseType), IndexMetaDataLoader.load(connectionAdapter, formattedTableNamePattern)))
                    : Optional.empty();
        }
    }
    
    /**
     * Load table meta data.
     *
     * @param dataSourceTable data source table name map
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> load(final Map<String, Collection<String>> dataSourceTable, final DatabaseType databaseType,
                                                  final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : dataSourceTable.entrySet()) {
            for (String each : entry.getValue()) {
                load(dataSourceMap.get(entry.getKey()), each, databaseType).ifPresent(tableMetaData -> result.put(each, tableMetaData));
            }
        }
        return result;
    }
    
    /**
     * Load table meta data.
     *
     * @param tableName table name
     * @param logicDataSourceNames logic data source names
     * @param materials materials
     * @return table meta data
     * @throws SQLException SQL exception
     */
    public static Optional<TableMetaData> load(final String tableName, final Collection<String> logicDataSourceNames, final SchemaBuilderMaterials materials) throws SQLException {
        for (String each : logicDataSourceNames) {
            DataSource dataSource = materials.getDataSourceMap().get(getActualDataSourceName(materials, each));
            if (Objects.isNull(dataSource)) {
                continue;
            }
            return load(dataSource, tableName, materials.getDatabaseType());
        }
        return Optional.empty();
    }
    
    /**
     * Load table meta data by executor service.
     *
     * @param loader dialect table meta data loader
     * @param dataSourceTables data source table names map
     * @param dataSourceMap data source map
     * @param executorService executor service
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> load(final DialectTableMetaDataLoader loader, final Map<String, Collection<String>> dataSourceTables,
                                                  final Map<String, DataSource> dataSourceMap, final ExecutorService executorService) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (Map.Entry<String, Collection<String>> each : dataSourceTables.entrySet()) {
            futures.add(executorService.submit(() -> loader.loadWithTables(dataSourceMap.get(each.getKey()), each.getValue())));
        }
        try {
            for (Future<Map<String, TableMetaData>> each : futures) {
                result.putAll(each.get());
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result;
    }
    
    private static boolean isTableExist(final Connection connection, final String tableNamePattern) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(connection.getCatalog(), connection.getSchema(), tableNamePattern, null)) {
            return resultSet.next();
        }
    }
    
    private static String getActualDataSourceName(final SchemaBuilderMaterials materials, final String logicDataSourceName) {
        for (ShardingSphereRule each : materials.getRules()) {
            if (each instanceof DataSourceContainedRule && ((DataSourceContainedRule) each).getDataSourceMapper().containsKey(logicDataSourceName)) {
                return ((DataSourceContainedRule) each).getDataSourceMapper().get(logicDataSourceName).iterator().next();
            }
        }
        return logicDataSourceName;
    }
    
    /**
     * Find dialect table meta data loader.
     *
     * @param databaseType database type
     * @return dialect table meta data loader
     */
    public static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final DatabaseType databaseType) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(databaseType.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
