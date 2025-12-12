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

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.GlobalDataSourceRegistry;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.UnsupportedActualDataNodeStructureException;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Schema meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SchemaMetaDataUtils {
    
    /**
     * Get meta data loader materials.
     *
     * @param tableNames table name collection
     * @param material material
     * @return meta data loader materials
     */
    public static Collection<MetaDataLoaderMaterial> getMetaDataLoaderMaterials(final Collection<String> tableNames, final GenericSchemaBuilderMaterial material) {
        Map<String, Collection<String>> dataSourceTableGroups = new LinkedHashMap<>();
        Collection<DatabaseType> unsupportedThreeTierStorageStructureDatabaseTypes = getUnsupportedThreeTierStorageStructureDatabaseTypes(material.getStorageUnits().values());
        DataNodes dataNodes = new DataNodes(material.getRules());
        boolean checkMetaDataEnable = material.getProps().getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED);
        for (String each : tableNames) {
            checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(unsupportedThreeTierStorageStructureDatabaseTypes, dataNodes, each);
            if (checkMetaDataEnable) {
                addAllActualTableDataNode(material, dataSourceTableGroups, dataNodes, each);
            } else {
                addOneActualTableDataNode(material, dataSourceTableGroups, dataNodes, each);
            }
        }
        Collection<MetaDataLoaderMaterial> result = new LinkedList<>();
        int loadTableMetadataBatchSize = material.getProps().getValue(ConfigurationPropertyKey.LOAD_TABLE_METADATA_BATCH_SIZE);
        for (Entry<String, Collection<String>> entry : dataSourceTableGroups.entrySet()) {
            DatabaseType storageType = material.getStorageUnits().get(entry.getKey()).getStorageType();
            String defaultSchemaName = new DatabaseTypeRegistry(storageType).getDefaultSchemaName(material.getDefaultSchemaName());
            result.addAll(buildMaterials(material, entry.getKey(), entry.getValue(), storageType, defaultSchemaName, loadTableMetadataBatchSize));
        }
        return result;
    }
    
    private static Collection<MetaDataLoaderMaterial> buildMaterials(final GenericSchemaBuilderMaterial material, final String dataSourceName, final Collection<String> actualTableNames,
                                                                     final DatabaseType storageType, final String defaultSchemaName, final int loadTableMetadataBatchSize) {
        Collection<MetaDataLoaderMaterial> result = new LinkedList<>();
        DataSource dataSource = getDataSource(material, dataSourceName);
        for (List<String> each : Lists.partition(new ArrayList<>(actualTableNames), loadTableMetadataBatchSize)) {
            result.add(new MetaDataLoaderMaterial(each, dataSourceName, dataSource, storageType, defaultSchemaName));
        }
        return result;
    }
    
    private static DataSource getDataSource(final GenericSchemaBuilderMaterial material, final String dataSourceName) {
        return material.getStorageUnits().get(dataSourceName.contains(".") ? dataSourceName.split("\\.")[0] : dataSourceName).getDataSource();
    }
    
    private static void checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(final Collection<DatabaseType> notSupportThreeTierStructureStorageTypes,
                                                                                 final DataNodes dataNodes, final String tableName) {
        for (DataNode dataNode : dataNodes.getDataNodes(tableName)) {
            ShardingSpherePreconditions.checkState(notSupportThreeTierStructureStorageTypes.isEmpty() || !dataNode.getDataSourceName().contains("."),
                    () -> new UnsupportedActualDataNodeStructureException(
                            dataNode.getDataSourceName(), dataNode.getTableName(), notSupportThreeTierStructureStorageTypes.iterator().next().getJdbcUrlPrefixes()));
            if (dataNode.getDataSourceName().contains(".")) {
                String database = dataNode.getDataSourceName().split("\\.")[1];
                GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().put(dataNode.getTableName(), database);
            }
        }
    }
    
    private static Collection<DatabaseType> getUnsupportedThreeTierStorageStructureDatabaseTypes(final Collection<StorageUnit> storageUnits) {
        return storageUnits.stream().map(StorageUnit::getStorageType)
                .filter(each -> !new DatabaseTypeRegistry(each).getDialectDatabaseMetaData().getConnectionOption().isSupportThreeTierStorageStructure()).collect(Collectors.toList());
    }
    
    private static void addOneActualTableDataNode(final GenericSchemaBuilderMaterial material,
                                                  final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Optional<DataNode> dataNode = dataNodes.getDataNodes(table).stream().filter(each -> isSameDataSourceNameSchemaName(material, each)).findFirst();
        if (!dataNode.isPresent() && !material.getStorageUnits().keySet().iterator().hasNext()) {
            return;
        }
        String dataSourceName = dataNode.map(DataNode::getDataSourceName).orElseGet(() -> material.getStorageUnits().keySet().iterator().next());
        String tableName = dataNode.map(DataNode::getTableName).orElse(table);
        addDataSourceTableGroups(dataSourceName, tableName, dataSourceTableGroups);
    }
    
    private static boolean isSameDataSourceNameSchemaName(final GenericSchemaBuilderMaterial material, final DataNode dataNode) {
        String dataSourceName = dataNode.getDataSourceName().contains(".") ? dataNode.getDataSourceName().split("\\.")[0] : dataNode.getDataSourceName();
        return material.getStorageUnits().containsKey(dataSourceName) && (null == dataNode.getSchemaName() || dataNode.getSchemaName().equalsIgnoreCase(material.getDefaultSchemaName()));
    }
    
    private static void addAllActualTableDataNode(final GenericSchemaBuilderMaterial material,
                                                  final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Collection<DataNode> tableDataNodes = dataNodes.getDataNodes(table);
        if (tableDataNodes.isEmpty() && !material.getStorageUnits().isEmpty()) {
            addDataSourceTableGroups(material.getStorageUnits().keySet().iterator().next(), table, dataSourceTableGroups);
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
