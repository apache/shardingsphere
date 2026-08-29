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
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.TableMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.datatype.DataTypeRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
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
            cancelAll(futures);
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while loading schema metadata.", ex);
        } catch (final ExecutionException ex) {
            cancelAll(futures);
            if (ex.getCause() instanceof SQLException) {
                throw (SQLException) ex.getCause();
            }
            throw new SQLException(ex);
        }
        return result;
    }
    
    private static Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Optional<DialectMetaDataLoader> dialectLoader = DatabaseTypedSPILoader.findService(DialectMetaDataLoader.class, material.getStorageType());
        Collection<SchemaMetaData> result;
        if (dialectLoader.isPresent()) {
            result = dialectLoader.get().load(material);
        } else {
            result = loadByDefault(material);
        }
        validate(material, result);
        for (SchemaMetaData each : result) {
            for (TableMetaData table : each.getTables()) {
                table.setStorageUnitName(material.getStorageUnitName());
            }
        }
        return result;
    }
    
    private static Collection<SchemaMetaData> loadByDefault(final MetaDataLoaderMaterial material) throws SQLException {
        Collection<TableMetaData> tableMetaData = new LinkedList<>();
        for (String each : material.getActualTableNames()) {
            Optional<TableMetaData> loaded = TableMetaDataLoader.loadNormalized(material.getDataSource(), each, material.getStorageType());
            if (!loaded.isPresent()) {
                throw new SQLException("Table metadata not found for table '" + each + "' in storage unit '"
                        + material.getStorageUnitName() + "'.");
            }
            tableMetaData.add(loaded.get());
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    private static void validate(final MetaDataLoaderMaterial material, final Collection<SchemaMetaData> loadedSchemas) throws SQLException {
        if (null == loadedSchemas) {
            throw new SQLException("Schema metadata is null for storage unit '" + material.getStorageUnitName() + "'.");
        }
        if (material.getActualTableNames().isEmpty()) {
            return;
        }
        if (loadedSchemas.isEmpty()) {
            throw new SQLException("Schema metadata is empty for storage unit '" + material.getStorageUnitName() + "', tables "
                    + material.getActualTableNames() + ".");
        }
        Collection<String> loadedTableNames = new LinkedList<>();
        for (SchemaMetaData each : loadedSchemas) {
            for (TableMetaData table : each.getTables()) {
                loadedTableNames.add(table.getName());
            }
        }
        IdentifierCasePolicy tableIdentifierPolicy = IdentifierNormalizeEngine.resolvePolicy(material.getStorageType(), material.getDataSource(), IdentifierScope.TABLE);
        Collection<String> missingTableNames = new LinkedList<>();
        for (String each : material.getActualTableNames()) {
            if (!IdentifierNormalizeEngine.findMatchedIdentifier(loadedTableNames, tableIdentifierPolicy, each).isPresent()) {
                missingTableNames.add(each);
            }
        }
        if (!missingTableNames.isEmpty()) {
            throw new SQLException("Schema metadata is incomplete for storage unit '" + material.getStorageUnitName()
                    + "', missing tables " + missingTableNames + ".");
        }
    }
    
    private static void cancelAll(final Collection<Future<Collection<SchemaMetaData>>> futures) {
        futures.forEach(each -> each.cancel(true));
    }
    
    private static void merge(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedList<>()));
            schemaMetaData.getTables().addAll(each.getTables());
        }
    }
}
