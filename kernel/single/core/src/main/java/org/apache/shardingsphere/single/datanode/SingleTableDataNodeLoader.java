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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.common.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.single.api.constant.SingleTableConstants;
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

/**
 * Single table data node loader.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SingleTableDataNodeLoader {
    
    /**
     * Load single table data nodes.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param builtRules built rules
     * @param configuredTables configured tables
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap,
                                                         final Collection<ShardingSphereRule> builtRules, final Collection<String> configuredTables) {
        Collection<String> featureRequiredSingleTables = SingleTableLoadUtils.getFeatureRequiredSingleTables(builtRules);
        if (configuredTables.isEmpty() && featureRequiredSingleTables.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Collection<String> excludedTables = SingleTableLoadUtils.getExcludedTables(builtRules);
        Map<String, Collection<DataNode>> actualDataNodes = load(databaseName, databaseType, dataSourceMap, excludedTables);
        Collection<String> splitTables = SingleTableLoadUtils.splitTableLines(configuredTables);
        if (splitTables.contains(SingleTableConstants.ALL_TABLES) || splitTables.contains(SingleTableConstants.ALL_SCHEMA_TABLES)) {
            return actualDataNodes;
        }
        Map<String, Map<String, Collection<String>>> configuredTableMap = getConfiguredTableMap(databaseName, databaseType, splitTables);
        return loadSpecifiedDataNodes(actualDataNodes, featureRequiredSingleTables, configuredTableMap);
    }
    
    /**
     * Load single table data nodes.
     *
     * @param databaseName database name
     * @param databaseType database type
     * @param dataSourceMap data source map
     * @param excludedTables excluded tables
     * @return single table data node map
     */
    public static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType databaseType,
                                                         final Map<String, DataSource> dataSourceMap, final Collection<String> excludedTables) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>();
        for (Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
            Map<String, Collection<DataNode>> dataNodeMap = load(databaseName, databaseType, entry.getKey(), entry.getValue(), excludedTables);
            for (Entry<String, Collection<DataNode>> each : dataNodeMap.entrySet()) {
                Collection<DataNode> addedDataNodes = each.getValue();
                Collection<DataNode> existDataNodes = result.getOrDefault(each.getKey().toLowerCase(), new LinkedHashSet<>(addedDataNodes.size(), 1F));
                existDataNodes.addAll(addedDataNodes);
                result.putIfAbsent(each.getKey().toLowerCase(), existDataNodes);
            }
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> load(final String databaseName, final DatabaseType databaseType, final String dataSourceName,
                                                          final DataSource dataSource, final Collection<String> excludedTables) {
        Map<String, Collection<String>> schemaTableNames = loadSchemaTableNames(databaseName, databaseType, dataSource, dataSourceName);
        Map<String, Collection<DataNode>> result = new LinkedHashMap<>();
        for (Entry<String, Collection<String>> entry : schemaTableNames.entrySet()) {
            for (String each : entry.getValue()) {
                if (excludedTables.contains(each)) {
                    continue;
                }
                Collection<DataNode> dataNodes = result.getOrDefault(each, new LinkedList<>());
                DataNode dataNode = new DataNode(dataSourceName, each);
                dataNode.setSchemaName(entry.getKey());
                dataNodes.add(dataNode);
                result.putIfAbsent(each, dataNodes);
            }
        }
        return result;
    }
    
    private static Map<String, Collection<DataNode>> loadSpecifiedDataNodes(final Map<String, Collection<DataNode>> actualDataNodes, final Collection<String> featureRequiredSingleTables,
                                                                            final Map<String, Map<String, Collection<String>>> configuredTableMap) {
        Map<String, Collection<DataNode>> result = new ConcurrentHashMap<>();
        for (Entry<String, Collection<DataNode>> entry : actualDataNodes.entrySet()) {
            Collection<DataNode> singleNode = loadSpecifiedDataNode(entry.getValue(), featureRequiredSingleTables, configuredTableMap);
            if (!singleNode.isEmpty()) {
                result.put(entry.getKey(), singleNode);
            }
        }
        return result;
    }
    
    private static Collection<DataNode> loadSpecifiedDataNode(final Collection<DataNode> dataNodes, final Collection<String> featureRequiredSingleTables,
                                                              final Map<String, Map<String, Collection<String>>> configuredTableMap) {
        for (final DataNode each : dataNodes) {
            if (featureRequiredSingleTables.contains(each.getTableName())) {
                return getSingleDataNodeCollection(each);
            }
            Map<String, Collection<String>> configuredTablesForDataSource = configuredTableMap.get(each.getDataSourceName());
            if (null == configuredTablesForDataSource || configuredTablesForDataSource.isEmpty()) {
                continue;
            }
            if (configuredTablesForDataSource.containsKey(SingleTableConstants.ASTERISK)) {
                return getSingleDataNodeCollection(each);
            }
            Collection<String> configuredTablesForSchema = configuredTablesForDataSource.get(each.getSchemaName());
            if (null == configuredTablesForSchema || configuredTablesForSchema.isEmpty()) {
                continue;
            }
            if (configuredTablesForSchema.contains(SingleTableConstants.ASTERISK) || configuredTablesForSchema.contains(each.getTableName())) {
                return getSingleDataNodeCollection(each);
            }
        }
        return Collections.emptyList();
    }
    
    private static Collection<DataNode> getSingleDataNodeCollection(final DataNode dataNode) {
        Collection<DataNode> result = new LinkedList<>();
        result.add(dataNode);
        return result;
    }
    
    private static Map<String, Map<String, Collection<String>>> getConfiguredTableMap(final String databaseName, final DatabaseType databaseType, final Collection<String> configuredTables) {
        if (configuredTables.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, Collection<String>>> result = new LinkedHashMap<>();
        Collection<DataNode> dataNodes = SingleTableLoadUtils.convertToDataNodes(databaseName, databaseType, configuredTables);
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
     * @param databaseType database type
     * @param dataSource data source
     * @param dataSourceName data source name
     * @return schema table names
     * @throws SingleTablesLoadingException Single tables loading exception
     */
    public static Map<String, Collection<String>> loadSchemaTableNames(final String databaseName, final DatabaseType databaseType, final DataSource dataSource, final String dataSourceName) {
        try {
            return SchemaMetaDataLoader.loadSchemaTableNames(databaseName, databaseType, dataSource);
        } catch (final SQLException ex) {
            throw new SingleTablesLoadingException(databaseName, dataSourceName, ex);
        }
    }
}
