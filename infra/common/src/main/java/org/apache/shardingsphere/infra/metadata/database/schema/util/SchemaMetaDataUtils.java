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

package org.apache.shardingsphere.infra.metadata.database.schema.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.datasource.registry.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.exception.UnsupportedActualDataNodeStructureException;
import org.apache.shardingsphere.infra.metadata.database.schema.loader.metadata.SchemaMetaDataLoaderMaterial;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataUtils {
    
    /**
     * Get schema meta data loader materials.
     *
     * @param tableNames table name collection
     * @param material material
     * @param checkMetaDataEnable check meta data enable config
     * @return schema meta data loader materials
     */
    public static Collection<SchemaMetaDataLoaderMaterial> getSchemaMetaDataLoaderMaterials(final Collection<String> tableNames,
                                                                                            final GenericSchemaBuilderMaterial material, final boolean checkMetaDataEnable) {
        Map<String, Collection<String>> dataSourceTableGroups = new LinkedHashMap<>();
        Collection<DatabaseType> notSupportThreeTierStructureStorageTypes = getNotSupportThreeTierStructureStorageTypes(material.getStorageTypes().values());
        DataNodes dataNodes = new DataNodes(material.getRules());
        for (String each : tableNames) {
            checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(notSupportThreeTierStructureStorageTypes, dataNodes, each);
            if (checkMetaDataEnable) {
                addAllActualTableDataNode(material, dataSourceTableGroups, dataNodes, each);
            } else {
                addOneActualTableDataNode(material, dataSourceTableGroups, dataNodes, each);
            }
        }
        return dataSourceTableGroups.entrySet().stream().map(entry -> new SchemaMetaDataLoaderMaterial(entry.getValue(),
                getDataSource(material, entry.getKey()), material.getStorageTypes().get(entry.getKey()), material.getDefaultSchemaName())).collect(Collectors.toList());
    }
    
    private static DataSource getDataSource(final GenericSchemaBuilderMaterial material, final String dataSourceName) {
        return material.getDataSourceMap().get(dataSourceName.contains(".") ? dataSourceName.split("\\.")[0] : dataSourceName);
    }
    
    private static void checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(final Collection<DatabaseType> notSupportThreeTierStructureStorageTypes, final DataNodes dataNodes,
                                                                                 final String tableName) {
        for (DataNode dataNode : dataNodes.getDataNodes(tableName)) {
            ShardingSpherePreconditions.checkState(notSupportThreeTierStructureStorageTypes.isEmpty() || !dataNode.getDataSourceName().contains("."),
                    () -> new UnsupportedActualDataNodeStructureException(dataNode, notSupportThreeTierStructureStorageTypes.iterator().next().getJdbcUrlPrefixes()));
            if (dataNode.getDataSourceName().contains(".")) {
                String database = dataNode.getDataSourceName().split("\\.")[1];
                GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().put(dataNode.getTableName(), database);
            }
        }
    }
    
    private static Collection<DatabaseType> getNotSupportThreeTierStructureStorageTypes(final Collection<DatabaseType> storageTypes) {
        Collection<DatabaseType> result = new LinkedList<>();
        for (DatabaseType each : storageTypes) {
            if (!"MySQL".equals(each.getType())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private static void addOneActualTableDataNode(final GenericSchemaBuilderMaterial material,
                                                  final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Optional<DataNode> dataNode = dataNodes.getDataNodes(table).stream().filter(each -> isSameDataSourceNameSchemaName(material, each)).findFirst();
        if (!dataNode.isPresent() && !material.getDataSourceMap().keySet().iterator().hasNext()) {
            return;
        }
        String dataSourceName = dataNode.map(DataNode::getDataSourceName).orElseGet(() -> material.getDataSourceMap().keySet().iterator().next());
        String tableName = dataNode.map(DataNode::getTableName).orElse(table);
        addDataSourceTableGroups(dataSourceName, tableName, dataSourceTableGroups);
    }
    
    private static boolean isSameDataSourceNameSchemaName(final GenericSchemaBuilderMaterial material, final DataNode dataNode) {
        String dataSourceName = dataNode.getDataSourceName().contains(".") ? dataNode.getDataSourceName().split("\\.")[0] : dataNode.getDataSourceName();
        if (!material.getDataSourceMap().containsKey(dataSourceName)) {
            return false;
        }
        return null == dataNode.getSchemaName() || dataNode.getSchemaName().equalsIgnoreCase(material.getDefaultSchemaName());
    }
    
    private static void addAllActualTableDataNode(final GenericSchemaBuilderMaterial material,
                                                  final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Collection<DataNode> tableDataNodes = dataNodes.getDataNodes(table);
        if (tableDataNodes.isEmpty()) {
            addDataSourceTableGroups(material.getDataSourceMap().keySet().iterator().next(), table, dataSourceTableGroups);
        } else {
            tableDataNodes.forEach(each -> addDataSourceTableGroups(each.getDataSourceName(), each.getTableName(), dataSourceTableGroups));
        }
    }
    
    private static void addDataSourceTableGroups(final String dataSourceName, final String tableName, final Map<String, Collection<String>> dataSourceTableGroups) {
        Collection<String> tables = dataSourceTableGroups.getOrDefault(dataSourceName, new LinkedList<>());
        tables.add(tableName);
        dataSourceTableGroups.putIfAbsent(dataSourceName, tables);
    }
}
