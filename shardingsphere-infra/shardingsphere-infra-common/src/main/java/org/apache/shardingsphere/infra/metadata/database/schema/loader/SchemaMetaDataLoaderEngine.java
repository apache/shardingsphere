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

package org.apache.shardingsphere.infra.metadata.database.schema.loader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.common.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoaderFactory;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
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
@Slf4j
public final class SchemaMetaDataLoaderEngine {
    
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-TableMetaDataLoaderEngine-%d").build());
    
    /**
     * Load schema meta data.
     *
     * @param materials table meta data load material
     * @param databaseType database type
     * @return schema meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, SchemaMetaData> load(final Collection<TableMetaDataLoaderMaterial> materials, final DatabaseType databaseType) throws SQLException {
        Optional<DialectSchemaMetaDataLoader> dialectTableMetaDataLoader = DialectSchemaMetaDataLoaderFactory.findInstance(databaseType);
        if (dialectTableMetaDataLoader.isPresent()) {
            try {
                return loadByDialect(dialectTableMetaDataLoader.get(), materials);
            } catch (final SQLException | ShardingSphereException ex) {
                log.error("Dialect load table meta data error", ex);
                return loadByDefault(materials, databaseType);
            }
        }
        return loadByDefault(materials, databaseType);
    }
    
    private static Map<String, SchemaMetaData> loadByDefault(final Collection<TableMetaDataLoaderMaterial> materials, final DatabaseType databaseType) throws SQLException {
        Map<String, TableMetaData> result = new LinkedHashMap<>();
        String defaultSchemaName = null;
        for (TableMetaDataLoaderMaterial each : materials) {
            defaultSchemaName = each.getDefaultSchemaName();
            for (String tableName : each.getActualTableNames()) {
                TableMetaDataLoader.load(each.getDataSource(), tableName, databaseType).ifPresent(optional -> result.put(tableName, optional));
            }
        }
        return Collections.singletonMap(defaultSchemaName, new SchemaMetaData(defaultSchemaName, result));
    }
    
    private static Map<String, SchemaMetaData> loadByDialect(final DialectSchemaMetaDataLoader loader, final Collection<TableMetaDataLoaderMaterial> materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        Collection<Future<Collection<SchemaMetaData>>> futures = new LinkedList<>();
        for (TableMetaDataLoaderMaterial each : materials) {
            futures.add(EXECUTOR_SERVICE.submit(() -> loader.load(each.getDataSource(), each.getActualTableNames(), each.getDefaultSchemaName())));
        }
        try {
            for (Future<Collection<SchemaMetaData>> each : futures) {
                mergeSchemaMetaDataMap(result, each.get());
            }
        } catch (final InterruptedException | ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new ShardingSphereException(ex);
        }
        return result;
    }
    
    private static void mergeSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedHashMap<>()));
            schemaMetaData.getTables().putAll(each.getTables());
        }
    }
}
