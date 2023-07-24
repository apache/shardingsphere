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

package org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.common.TableMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.SchemaMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.model.TableMetaData;
import org.apache.shardingsphere.infra.database.core.spi.DatabaseTypedSPILoader;
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
     * @return schema meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, SchemaMetaData> load(final Collection<SchemaMetaDataLoaderMaterial> materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(materials.size(), 1F);
        Collection<Future<Collection<SchemaMetaData>>> futures = new LinkedList<>();
        for (SchemaMetaDataLoaderMaterial each : materials) {
            futures.add(EXECUTOR_SERVICE.submit(() -> load(each)));
        }
        try {
            for (Future<Collection<SchemaMetaData>> each : futures) {
                mergeSchemaMetaDataMap(result, each.get());
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new UnknownSQLException(ex).toSQLException();
        }
        return result;
    }
    
    private static Collection<SchemaMetaData> load(final SchemaMetaDataLoaderMaterial material) throws SQLException {
        Optional<DialectSchemaMetaDataLoader> dialectSchemaMetaDataLoader = DatabaseTypedSPILoader.findService(DialectSchemaMetaDataLoader.class, material.getStorageType());
        if (dialectSchemaMetaDataLoader.isPresent()) {
            try {
                return dialectSchemaMetaDataLoader.get().load(material.getDataSource(), material.getActualTableNames(), material.getDefaultSchemaName());
            } catch (final SQLException ex) {
                log.debug("Dialect load schema meta data error.", ex);
            }
        }
        return loadByDefault(material);
    }
    
    private static Collection<SchemaMetaData> loadByDefault(final SchemaMetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        for (String each : material.getActualTableNames()) {
            TableMetaDataLoader.load(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        return Collections.singletonList(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    private static void mergeSchemaMetaDataMap(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedList<>()));
            schemaMetaData.getTables().addAll(each.getTables());
        }
    }
}
