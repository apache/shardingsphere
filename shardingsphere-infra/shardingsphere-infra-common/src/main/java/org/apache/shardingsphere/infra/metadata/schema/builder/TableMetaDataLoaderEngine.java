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

package org.apache.shardingsphere.infra.metadata.schema.builder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.loader.DefaultTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.builder.spi.DialectTableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TableMetaDataLoaderEngine {

    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-TableMetaDataLoaderEngine-%d").build());

    /**
     * Load table meta data.
     *
     * @param tableNames table name
     * @param materials materials
     * @return table meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, TableMetaData> load(final Collection<String> tableNames, final SchemaBuilderMaterials materials) throws SQLException {
        Optional<DialectTableMetaDataLoader> dialectTableMetaDataLoader = findDialectTableMetaDataLoader(materials.getDatabaseType());
        Map<String, Collection<String>> dataSourceTable = getTableGroup(tableNames, materials);
        return dialectTableMetaDataLoader.isPresent() ? loadByDialect(dialectTableMetaDataLoader.get(), dataSourceTable, materials.getDataSourceMap())
                : loadByDefault(dataSourceTable, materials.getDatabaseType(), materials.getDataSourceMap());
    }

    private static Map<String, TableMetaData> loadByDefault(final Map<String, Collection<String>> dataSourceTable, final DatabaseType databaseType,
                                                            final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        for (Map.Entry<String, Collection<String>> entry : dataSourceTable.entrySet()) {
            for (String each : entry.getValue()) {
                DefaultTableMetaDataLoader.load(dataSourceMap.get(entry.getKey()), each, databaseType).ifPresent(tableMetaData -> result.put(each, tableMetaData));
            }
        }
        return result;
    }

    private static Map<String, TableMetaData> loadByDialect(final DialectTableMetaDataLoader loader, final Map<String, Collection<String>> dataSourceTables,
                                                            final Map<String, DataSource> dataSourceMap) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        Collection<Future<Map<String, TableMetaData>>> futures = new LinkedList<>();
        for (Map.Entry<String, Collection<String>> each : dataSourceTables.entrySet()) {
            futures.add(EXECUTOR_SERVICE.submit(() -> loader.load(dataSourceMap.get(each.getKey()), each.getValue())));
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

    private static Map<String, Collection<String>> getTableGroup(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (String each : tableNames) {
            Optional<DataNode> optional = dataNodes.getDataNodes(each).stream().findFirst();
            String dataSourceName = optional.map(DataNode::getDataSourceName).orElse(materials.getDataSourceMap().keySet().iterator().next());
            String tableName = optional.map(DataNode::getTableName).orElse(each);
            Collection<String> tables = result.getOrDefault(dataSourceName, new LinkedList<>());
            tables.add(tableName);
            result.putIfAbsent(dataSourceName, tables);
        }
        return result;
    }

    private static Optional<DialectTableMetaDataLoader> findDialectTableMetaDataLoader(final DatabaseType databaseType) {
        for (DialectTableMetaDataLoader each : ShardingSphereServiceLoader.getSingletonServiceInstances(DialectTableMetaDataLoader.class)) {
            if (each.getDatabaseType().equals(databaseType.getName())) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
}
