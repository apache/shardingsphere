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

package org.apache.shardingsphere.single.datanode;

import com.cedarsoftware.util.CaseInsensitiveSet;
import com.google.common.base.Splitter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.SchemaMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.DefaultSchemaNameResolver;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.exception.SingleTablesLoadingException;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Single table data node loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class SingleTableDataNodeLoader {
    
    private static final String DELIMITER = ".";
    
    /**
     * Load single table data nodes.
     *
     * @param databaseName database name
     * @param protocolType protocol type
     * @param dataSourceMap data source map
     * @param builtRules built rules
     * @param configuredTables configured tables
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType protocolType, final Map<String, DataSource> dataSourceMap,
                                                         final Collection<ShardingSphereRule> builtRules, final Collection<String> configuredTables) {
        Collection<String> featureRequiredSingleTables = SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules);
        if (configuredTables.isEmpty() && featureRequiredSingleTables.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(builtRules);
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(configuredTables);
        if (splitTables.contains(SingleTableConstants.ALL_TABLES) || splitTables.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            Map<String, DatabaseType> storageTypes = dataSourceMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, each -> DatabaseTypeEngine.getStorageType(each.getValue())));
            Map<String, Collection<DataNode>> result = load(databaseName, dataSourceMap, Collections.emptySet(), excludedTables, storageTypes);
            warnIfSingleTableLoadedFromMultipleDataSources(databaseName, result);
            return result;
        }
        Collection<String> configuredDataSources = getConfiguredDataSources(splitTables);
        Map<String, DataSource> validDataSources = dataSourceMap.entrySet().stream().filter(entry -> configuredDataSources.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, DatabaseType> validStorageTypes = validDataSources.entrySet().stream().collect(Collectors.toMap(Entry::getKey, each -> DatabaseTypeEngine.getStorageType(each.getValue())));
        Map<DatabaseType, Boolean> schemaAvailability = new HashMap<>(validStorageTypes.size() + 1, 1F);
        checkConfiguredDataNodeTiers(splitTables, validStorageTypes, schemaAvailability);
        Collection<DataNode> configuredDataNodes = getConfiguredDataNodes(databaseName, protocolType, splitTables, validDataSources, validStorageTypes);
        Collection<String> includedTables = getIncludedTables(
                dataSourceMap, validStorageTypes, schemaAvailability, splitTables, configuredDataNodes, featureRequiredSingleTables);
        Map<String, Collection<DataNode>> actualDataNodes = load(databaseName, validDataSources, includedTables, excludedTables, validStorageTypes);
        Map<String, Map<String, Collection<String>>> configuredTableMap = getConfiguredTableMap(configuredDataNodes);
        Map<String, Collection<DataNode>> result = loadSpecifiedDataNodes(actualDataNodes, featureRequiredSingleTables, configuredTableMap);
        warnIfSingleTableLoadedFromMultipleDataSources(databaseName, result);
        return result;
    }
    
    /**
     * Load single table data nodes.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param includedTables included tables
     * @param excludedTables excluded tables
     * @param validStorageTypes valid storage types
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> includedTables,
                                                         final Collection<String> excludedTables, final Map<String, DatabaseType> validStorageTypes) {
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>(dataSourceMap.size(), 1F);
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, Collection<DataNode>> dataNodeMap =
                    load(databaseName, validStorageTypes.get(entry.getKey()), entry.getKey(), entry.getValue(), includedTables, excludedTables);
            for (Entry<String, Collection<DataNode>> each : dataNodeMap.entrySet()) {
                Collection<DataNode> addedDataNodes = each.getValue();
                Collection<DataNode> existDataNodes = result.getOrDefault(each.getKey(), new LinkedHashSet<>(addedDataNodes.size(), 1F));
                existDataNodes.addAll(addedDataNodes);
                result.putIfAbsent(each.getKey(), existDataNodes);
            }
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType storageType, final String dataSourceName,
                                                          final DataSource dataSource, final Collection<String> includedTables, final Collection<String> excludedTables) {
        Map<String, Collection<String>> schemaTableNames = loadSchemaTableNames(databaseName, storageType, dataSource, dataSourceName, includedTables, excludedTables);
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>(schemaTableNames.size(), 1F);
        for (Entry<String, Collection<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                Collection<DataNode> dataNodes = result.getOrDefault(each, new LinkedList<>());
                dataNodes.add(new DataNode(dataSourceName, entry.getKey(), each));
                result.putIfAbsent(each, dataNodes);
            }
        }
        return result;
    }
    
    private static void warnIfSingleTableLoadedFromMultipleDataSources(final String databaseName, final Map<String, Collection<DataNode>> dataNodes) {
        for (Entry<String, Collection<DataNode>> entry : dataNodes.entrySet()) {
            Collection<String> dataSourceNames = entry.getValue().stream().map(DataNode::getDataSourceName).collect(Collectors.toCollection(LinkedHashSet::new));
            if (1 < dataSourceNames.size()) {
                log.warn("Single table '{}' is loaded from multiple storage units {} in database '{}'.", entry.getKey(), dataSourceNames, databaseName);
            }
        }
    }
    
    private static Collection<String> getConfiguredDataSources(final Collection<String> configuredTables) {
        Collection<String> result = new HashSet<>(configuredTables.size(), 1F);
        for (String each : configuredTables) {
            checkConfiguredDataNodeFormat(each);
            result.add(getConfiguredDataSourceName(each));
        }
        return result;
    }
    
    private static void checkConfiguredDataNodeFormat(final String dataNode) {
        Collection<String> segments = Splitter.on(DELIMITER).splitToList(dataNode);
        ShardingSpherePreconditions.checkState(segments.size() >= 2 && segments.stream().noneMatch(each -> each.trim().isEmpty())
                && !dataNode.contains(" " + DELIMITER) && !dataNode.contains(DELIMITER + " "), () -> new InvalidDataNodeFormatException(dataNode));
    }
    
    private static String getConfiguredDataSourceName(final String dataNode) {
        return dataNode.substring(0, dataNode.indexOf(DELIMITER));
    }
    
    private static void checkConfiguredDataNodeTiers(final Collection<String> configuredTables, final Map<String, DatabaseType> storageTypes,
                                                     final Map<DatabaseType, Boolean> schemaAvailability) {
        for (String each : configuredTables) {
            Collection<String> segments = Splitter.on(DELIMITER).splitToList(each);
            ShardingSpherePreconditions.checkState(segments.size() <= 3
                    || isDottedTableNameSupported(storageTypes, schemaAvailability, getConfiguredDataSourceName(each)), () -> new InvalidDataNodeFormatException(each));
        }
    }
    
    private static boolean isDottedTableNameSupported(final Map<String, DatabaseType> storageTypes, final Map<DatabaseType, Boolean> schemaAvailability,
                                                      final String dataSourceName) {
        DatabaseType storageType = storageTypes.get(dataSourceName);
        return null != storageType && !isSchemaAvailable(storageType, schemaAvailability);
    }
    
    private static boolean isSchemaAvailable(final DatabaseType databaseType, final Map<DatabaseType, Boolean> schemaAvailability) {
        return schemaAvailability.computeIfAbsent(databaseType,
                each -> new DatabaseTypeRegistry(each).getDialectDatabaseMetaData().getSchemaOption().isSchemaAvailable());
    }
    
    private static Collection<DataNode> getConfiguredDataNodes(final String databaseName, final DatabaseType protocolType, final Collection<String> configuredTables,
                                                               final Map<String, DataSource> validDataSources, final Map<String, DatabaseType> validStorageTypes) {
        Map<String, String> defaultSchemaNames = getDefaultSchemaNames(databaseName, validDataSources, validStorageTypes);
        return configuredTables.stream().map(each -> getConfiguredDataNode(databaseName, protocolType, validStorageTypes, defaultSchemaNames, each)).collect(Collectors.toList());
    }
    
    private static Map<String, String> getDefaultSchemaNames(final String databaseName, final Map<String, DataSource> dataSources, final Map<String, DatabaseType> storageTypes) {
        Map<String, String> result = new LinkedHashMap<>(dataSources.size(), 1F);
        for (Entry<String, DataSource> entry : dataSources.entrySet()) {
            DatabaseType storageType = storageTypes.get(entry.getKey());
            result.put(entry.getKey(), DefaultSchemaNameResolver.resolveStorage(storageType, entry.getValue(), databaseName));
        }
        return result;
    }
    
    private static DataNode getConfiguredDataNode(final String databaseName, final DatabaseType protocolType, final Map<String, DatabaseType> storageTypes,
                                                  final Map<String, String> defaultSchemaNames, final String dataNode) {
        String dataSourceName = getConfiguredDataSourceName(dataNode);
        return defaultSchemaNames.containsKey(dataSourceName)
                ? DataNode.createWithDefaultSchemaName(defaultSchemaNames.get(dataSourceName), storageTypes.get(dataSourceName), dataNode)
                : new DataNode(databaseName, protocolType, dataNode);
    }
    
    private static Collection<String> getIncludedTables(final Map<String, DataSource> dataSourceMap, final Map<String, DatabaseType> storageTypes,
                                                        final Map<DatabaseType, Boolean> schemaAvailability, final Collection<String> configuredTables,
                                                        final Collection<DataNode> configuredDataNodes,
                                                        final Collection<String> featureRequiredSingleTables) {
        if (!isSafeToFilterBeforeLoad(storageTypes, schemaAvailability, configuredTables)
                || !isSingleDataSource(dataSourceMap) && !featureRequiredSingleTables.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<String> result = new CaseInsensitiveSet<>(configuredDataNodes.size() + featureRequiredSingleTables.size(), 1F);
        for (DataNode each : configuredDataNodes) {
            result.add(each.getTableName());
        }
        result.addAll(featureRequiredSingleTables);
        return result;
    }
    
    private static boolean isSafeToFilterBeforeLoad(final Map<String, DatabaseType> storageTypes, final Map<DatabaseType, Boolean> schemaAvailability,
                                                    final Collection<String> configuredTables) {
        for (String each : configuredTables) {
            List<String> segments = Splitter.on(DELIMITER).splitToList(each);
            if (3 == segments.size() && !SingleTableConstants.ASTERISK.equals(segments.get(1))
                    && !isDottedTableNameSupported(storageTypes, schemaAvailability, segments.get(0))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isSingleDataSource(final Map<String, DataSource> dataSourceMap) {
        return 1 == dataSourceMap.size();
    }
    
    private static Map<String, Collection<DataNode>> loadSpecifiedDataNodes(final Map<String, Collection<DataNode>> actualDataNodes, final Collection<String> featureRequiredSingleTables,
                                                                            final Map<String, Map<String, Collection<String>>> configuredTableMap) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>(actualDataNodes.size(), 1F);
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            Collection<DataNode> singleNodes = loadSpecifiedDataNode(entry.getValue(), featureRequiredSingleTables, configuredTableMap);
            if (!singleNodes.isEmpty()) {
                result.put(entry.getKey(), singleNodes);
            }
        }
        return result;
    }
    
    private static Collection<DataNode> loadSpecifiedDataNode(final Collection<DataNode> dataNodes, final Collection<String> featureRequiredSingleTables,
                                                              final Map<String, Map<String, Collection<String>>> configuredTableMap) {
        Collection<DataNode> result = new LinkedList<>();
        for (DataNode each : dataNodes) {
            if (featureRequiredSingleTables.contains(each.getTableName())) {
                result.add(each);
                continue;
            }
            Map<String, Collection<String>> configuredTablesForDataSource = configuredTableMap.get(each.getDataSourceName());
            if (null == configuredTablesForDataSource) {
                continue;
            }
            Collection<String> configuredTablesForAllSchemas = configuredTablesForDataSource.get(SingleTableConstants.ASTERISK);
            if (null != configuredTablesForAllSchemas
                    && (configuredTablesForAllSchemas.contains(SingleTableConstants.ASTERISK) || configuredTablesForAllSchemas.contains(each.getTableName()))) {
                result.add(each);
                continue;
            }
            Collection<String> configuredTablesForSchema = configuredTablesForDataSource.get(each.getSchemaName());
            if (null == configuredTablesForSchema) {
                continue;
            }
            if (configuredTablesForSchema.contains(SingleTableConstants.ASTERISK) || configuredTablesForSchema.contains(each.getTableName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static Map<String, Map<String, Collection<String>>> getConfiguredTableMap(final Collection<DataNode> configuredDataNodes) {
        if (configuredDataNodes.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Collection<String>>> result = new LinkedHashMap<>(configuredDataNodes.size(), 1F);
        for (DataNode dataNode : configuredDataNodes) {
            Map<String, Collection<String>> schemaTables = result.getOrDefault(dataNode.getDataSourceName(), new LinkedHashMap<>());
            Collection<String> tables = schemaTables.computeIfAbsent(dataNode.getSchemaName(),
                    ignored -> SingleTableConstants.ASTERISK.equals(dataNode.getSchemaName()) ? new CaseInsensitiveSet<>() : new LinkedHashSet<>());
            tables.add(dataNode.getTableName());
            result.putIfAbsent(dataNode.getDataSourceName(), schemaTables);
        }
        return result;
    }
    
    /**
     * Load schema table names.
     *
     * @param databaseName database name
     * @param storageType storage type
     * @param dataSource data source
     * @param dataSourceName data source name
     * @param includedTables included tables
     * @param excludedTables excluded tables
     * @return schema table names
     * @throws SingleTablesLoadingException Single tables loading exception
     */
    public static Map<String, Collection<String>> loadSchemaTableNames(final String databaseName, final DatabaseType storageType, final DataSource dataSource, final String dataSourceName,
                                                                       final Collection<String> includedTables, final Collection<String> excludedTables) {
        try {
            return new SchemaMetaDataLoader(storageType).loadSchemaTableNames(databaseName, dataSource, includedTables, excludedTables);
        } catch (final SQLException ex) {
            throw new SingleTablesLoadingException(databaseName, dataSourceName, ex);
        }
    }
}
