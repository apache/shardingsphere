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

import com.cedarsoftware.util.CaseInsensitiveMap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.type.SchemaMetaDataLoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.constant.SingleTableConstants;
import org.apache.shardingsphere.single.exception.SingleTablesLoadingException;
import org.apache.shardingsphere.single.util.SingleTableLoadUtils;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Single table data node loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableDataNodeLoader {
    
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
            return load(databaseName, dataSourceMap, excludedTables);
        }
        Collection<String> configuredDataSources = configuredTables.stream().map(DataNode::new).map(DataNode::getDataSourceName).collect(Collectors.toSet());
        Map<String, DataSource> configuredDataSourceMap = dataSourceMap.entrySet().stream().filter(entry -> configuredDataSources.contains(entry.getKey()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        Map<String, Collection<DataNode>> actualDataNodes = load(databaseName, configuredDataSourceMap, excludedTables);
        Map<String, Map<String, Collection<String>>> configuredTableMap = getConfiguredTableMap(databaseName, protocolType, splitTables);
        return loadSpecifiedDataNodes(actualDataNodes, featureRequiredSingleTables, configuredTableMap);
    }
    
    /**
     * Load single table data nodes.
     *
     * @param databaseName database name
     * @param dataSourceMap data source map
     * @param excludedTables excluded tables
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<String> excludedTables) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, Collection<DataNode>> dataNodeMap = load(databaseName, DatabaseTypeEngine.getStorageType(entry.getValue()), entry.getKey(), entry.getValue(), excludedTables);
            for (Entry<String, Collection<DataNode>> each : dataNodeMap.entrySet()) {
                Collection<DataNode> addedDataNodes = each.getValue();
                Collection<DataNode> existDataNodes = result.getOrDefault(each.getKey().toLowerCase(), new LinkedHashSet<>(addedDataNodes.size(), 1F));
                existDataNodes.addAll(addedDataNodes);
                result.putIfAbsent(each.getKey().toLowerCase(), existDataNodes);
            }
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType storageType, final String dataSourceName,
                                                          final DataSource dataSource, final Collection<String> excludedTables) {
        Map<String, Collection<String>> schemaTableNames = loadSchemaTableNames(databaseName, storageType, dataSource, dataSourceName, excludedTables);
        Map<String, Collection<DataNode>> result = new CaseInsensitiveMap<>();
        for (Entry<String, Collection<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                Collection<DataNode> dataNodes = result.getOrDefault(each, new LinkedList<>());
                dataNodes.add(new DataNode(dataSourceName, entry.getKey(), each));
                result.putIfAbsent(each, dataNodes);
            }
        }
        return result;
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
            if (null == configuredTablesForDataSource || configuredTablesForDataSource.isEmpty()) {
                continue;
            }
            if (configuredTablesForDataSource.containsKey(SingleTableConstants.ASTERISK)) {
                result.add(each);
                continue;
            }
            Collection<String> configuredTablesForSchema = configuredTablesForDataSource.get(each.getSchemaName());
            if (null == configuredTablesForSchema || configuredTablesForSchema.isEmpty()) {
                continue;
            }
            if (configuredTablesForSchema.contains(SingleTableConstants.ASTERISK) || configuredTablesForSchema.contains(each.getTableName().toLowerCase())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static Map<String, Map<String, Collection<String>>> getConfiguredTableMap(final String databaseName, final DatabaseType protocolType, final Collection<String> configuredTables) {
        if (configuredTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Collection<DataNode> dataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, protocolType, configuredTables);
        Map<String, Map<String, Collection<String>>> result = new LinkedHashMap<>(dataNodes.size(), 1F);
        for (DataNode each : dataNodes) {
            Map<String, Collection<String>> schemaTables = result.getOrDefault(each.getDataSourceName(), new LinkedHashMap<>());
            Collection<String> tables = schemaTables.getOrDefault(each.getSchemaName(), new LinkedList<>());
            tables.add(each.getTableName());
            schemaTables.putIfAbsent(each.getSchemaName(), tables);
            result.putIfAbsent(each.getDataSourceName(), schemaTables);
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
     * @param excludedTables excluded tables
     * @return schema table names
     * @throws SingleTablesLoadingException Single tables loading exception
     */
    public static Map<String, Collection<String>> loadSchemaTableNames(final String databaseName, final DatabaseType storageType,
                                                                       final DataSource dataSource, final String dataSourceName, final Collection<String> excludedTables) {
        try {
            return new SchemaMetaDataLoader(storageType).loadSchemaTableNames(databaseName, dataSource, excludedTables);
        } catch (final SQLException ex) {
            throw new SingleTablesLoadingException(databaseName, dataSourceName, ex);
        }
    }
}
