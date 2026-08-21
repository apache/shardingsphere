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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProvider;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyProviderContext;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
        checkMissingTables(materials, result);
        return result;
    }
    
    private static Collection<SchemaMetaData> load(final MetaDataLoaderMaterial material) throws SQLException {
        Optional<DialectMetaDataLoader> dialectLoader = DatabaseTypedSPILoader.findService(DialectMetaDataLoader.class, material.getStorageType());
        Collection<SchemaMetaData> result;
        if (dialectLoader.isPresent()) {
            try {
                result = dialectLoader.get().load(material);
            } catch (final SQLException ex) {
                log.warn("{} Dialect load schema meta data error, load by default.", material.getStorageType(), ex);
                result = loadByDefault(material);
            }
        } else {
            result = loadByDefault(material);
        }
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
            TableMetaDataLoader.loadNormalized(material.getDataSource(), each, material.getStorageType()).ifPresent(tableMetaData::add);
        }
        return Collections.singleton(new SchemaMetaData(material.getDefaultSchemaName(), tableMetaData));
    }
    
    private static void merge(final Map<String, SchemaMetaData> schemaMetaDataMap, final Collection<SchemaMetaData> addedSchemaMetaDataList) {
        for (SchemaMetaData each : addedSchemaMetaDataList) {
            SchemaMetaData schemaMetaData = schemaMetaDataMap.computeIfAbsent(each.getName(), key -> new SchemaMetaData(each.getName(), new LinkedList<>()));
            schemaMetaData.getTables().addAll(each.getTables());
        }
    }
    
    private static void checkMissingTables(final Collection<MetaDataLoaderMaterial> materials, final Map<String, SchemaMetaData> result) {
        Map<String, Set<String>> expectedByStorageUnit = new LinkedHashMap<>(materials.size(), 1F);
        Map<String, Map<String, String>> normalizedToOriginalByStorageUnit = new LinkedHashMap<>(materials.size(), 1F);
        Map<String, IdentifierCasePolicy> policyByStorageUnit = new LinkedHashMap<>(materials.size(), 1F);
        for (MetaDataLoaderMaterial each : materials) {
            Set<String> normalizedNames = expectedByStorageUnit.computeIfAbsent(each.getStorageUnitName(), key -> new HashSet<>());
            Map<String, String> normalizedToOriginal = normalizedToOriginalByStorageUnit.computeIfAbsent(each.getStorageUnitName(), key -> new LinkedHashMap<>());
            if (!policyByStorageUnit.containsKey(each.getStorageUnitName())) {
                policyByStorageUnit.put(each.getStorageUnitName(), getTableIdentifierPolicy(each));
            }
            IdentifierCasePolicy policy = policyByStorageUnit.get(each.getStorageUnitName());
            for (String tableName : each.getActualTableNames()) {
                String normalized = policy.normalizeForLookup(tableName);
                normalizedNames.add(normalized);
                normalizedToOriginal.putIfAbsent(normalized, tableName);
            }
        }
        Map<String, Set<String>> loadedByStorageUnit = new LinkedHashMap<>(result.size(), 1F);
        for (SchemaMetaData each : result.values()) {
            for (TableMetaData table : each.getTables()) {
                String storageUnitName = table.getStorageUnitName();
                IdentifierCasePolicy policy = policyByStorageUnit.getOrDefault(storageUnitName, getDefaultIdentifierPolicy());
                loadedByStorageUnit.computeIfAbsent(storageUnitName, key -> new HashSet<>()).add(policy.normalizeForLookup(table.getName()));
            }
        }
        List<String> missingTableIdentities = new LinkedList<>();
        for (Map.Entry<String, Set<String>> entry : expectedByStorageUnit.entrySet()) {
            String storageUnitName = entry.getKey();
            Set<String> expectedNormalizedNames = entry.getValue();
            Set<String> loadedNormalizedNames = loadedByStorageUnit.getOrDefault(storageUnitName, Collections.emptySet());
            Map<String, String> normalizedToOriginal = normalizedToOriginalByStorageUnit.get(storageUnitName);
            for (String each : expectedNormalizedNames) {
                if (!loadedNormalizedNames.contains(each)) {
                    String originalName = null != normalizedToOriginal ? normalizedToOriginal.getOrDefault(each, each) : each;
                    missingTableIdentities.add(storageUnitName + "." + originalName);
                }
            }
        }
        if (!missingTableIdentities.isEmpty()) {
            log.warn("The following tables are missing from loaded metadata: {}", missingTableIdentities);
        }
    }
    
    private static IdentifierCasePolicy getTableIdentifierPolicy(final MetaDataLoaderMaterial material) {
        Optional<IdentifierCasePolicyProvider> provider = DatabaseTypedSPILoader.findService(IdentifierCasePolicyProvider.class, material.getStorageType());
        if (provider.isPresent()) {
            return provider.get().provide(new IdentifierCasePolicyProviderContext(material.getStorageType(), material.getDataSource())).getPolicy(IdentifierScope.TABLE);
        }
        return getDefaultIdentifierPolicy();
    }
    
    private static IdentifierCasePolicy getDefaultIdentifierPolicy() {
        return IdentifierCasePolicyFactory.newInsensitivePolicySet().getPolicy(IdentifierScope.TABLE);
    }
}
