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

package org.apache.shardingsphere.database.connector.core.metadata.data.loader;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;

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

/**
 * Meta data loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class MetaDataLoader {
    
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() * 2, Runtime.getRuntime().availableProcessors() * 2,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ShardingSphere-SchemaMetaDataLoaderEngine-%d").build());
    
    /**
     * Load meta data.
     *
     * @param materials meta data loader materials
     * @return meta data map
     * @throws SQLException SQL exception
     */
    public static Map<String, SchemaMetaData> load(final Collection<MetaDataLoaderMaterial> materials) throws SQLException {
        Map<String, SchemaMetaData> result = new LinkedHashMap<>(materials.size(), 1F);
        Collection<Future<Collection<SchemaMetaData>>> futures = new LinkedList<>();
        for (MetaDataLoaderMaterial each : materials) {
            DataTypeRegistry.load(each.getDataSource(), each.getStorageType().getType());
            futures.add(EXECUTOR_SERVICE.submit(() -> load(each)));
        }
        try {
            for (Future<Collection<SchemaMetaData>> each : futures) {
                merge(result, each.get());
            }
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (final ExecutionException ex) {
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new SQLException(ex);
        }
        return result;
    }
    
    private static Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Optional<DialectMetaDataLoader> dialectLoader = DatabaseTypedSPILoader.findService(DialectMetaDataLoader.class, material.getStorageType());
        if (dialectLoader.isPresent()) {
            try {
                return dialectLoader.get().load(material);
            } catch (final SQLException ex) {
                log.debug("{} Dialect load schema meta data error, load by default.", material.getStorageType(), ex);
            }
        }
        return loadByDefault(material);
    }
    
    private static Collection<SchemaMetaData> loadByDefault(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        for (String each : material.getActualTableNames()) {
            TableMetaDataLoader.load(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    private static void merge(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedList<>()));
            schemaMetaData.getTables().addAll(each.getTables());
        }
    }
}
