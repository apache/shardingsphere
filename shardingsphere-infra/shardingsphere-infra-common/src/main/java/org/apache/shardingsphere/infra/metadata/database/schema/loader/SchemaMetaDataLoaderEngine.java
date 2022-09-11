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
import org.apache.shardingsphere.infra.metadata.database.schema.loader.common.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.common.ViewMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.ViewMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.spi.DialectSchemaMetaDataLoaderFactory;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnknownSQLException;

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
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaMetaDataLoaderEngine-%d").build());
    
    /**
     * Load schema meta data.
     *
     * @param materials schema meta data loader materials
     * @param databaseType database type
     * @return schema meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, SchemaMetaData> load(final Collection<SchemaMetaDataLoaderMaterials> materials, final DatabaseType databaseType) throws SQLException {
        Optional<DialectSchemaMetaDataLoader> dialectTableMetaDataLoader = DialectSchemaMetaDataLoaderFactory.findInstance(databaseType);
        if (dialectTableMetaDataLoader.isPresent()) {
            try {
                return loadByDialect(dialectTableMetaDataLoader.get(), materials);
            } catch (final SQLException ex) {
                log.error("Dialect load table meta data error.", ex);
            }
        }
        return loadByDefault(materials, databaseType);
    }
    
    private static Map<String, SchemaMetaData> loadByDefault(final Collection<SchemaMetaDataLoaderMaterials> materials, final DatabaseType databaseType) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        Collection<ViewMetaData> viewMetaData = new LinkedList<>();
        String defaultSchemaName = null;
        for (SchemaMetaDataLoaderMaterials each : materials) {
            defaultSchemaName = each.getDefaultSchemaName();
            for (String tableName : each.getActualTableNames()) {
                TableMetaDataLoader.load(each.getDataSource(), tableName, databaseType).ifPresent(tableMetaData::add);
            }
            ViewMetaDataLoader.load(each.getDataSource(), databaseType).ifPresent(viewMetaData::add);
        }
        return Collections.singletonMap(defaultSchemaName, new SchemaMetaData(defaultSchemaName, tableMetaData, viewMetaData));
    }
    
    private static Map<String, SchemaMetaData> loadByDialect(final DialectSchemaMetaDataLoader loader, final Collection<SchemaMetaDataLoaderMaterials> materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>();
        Collection<Future<Collection<SchemaMetaData>>> futures = new LinkedList<>();
        for (SchemaMetaDataLoaderMaterials each : materials) {
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
            throw new UnknownSQLException(ex).toSQLException();
        }
        return result;
    }
    
    private static void mergeSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedList<>(), new LinkedList<>()));
            schemaMetaData.getTables().addAll(each.getTables());
            schemaMetaData.getViews().addAll(each.getViews());
        }
    }
}
