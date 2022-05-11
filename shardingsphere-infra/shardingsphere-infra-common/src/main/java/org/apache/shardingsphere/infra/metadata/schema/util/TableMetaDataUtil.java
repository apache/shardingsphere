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

package org.apache.shardingsphere.infra.metadata.schema.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.datasource.registry.GlobalDataSourceRegistry;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.loader.TableMetaDataLoaderMaterial;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Table meta data utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TableMetaDataUtil {
    
    /**
     * Get table meta data load materials.
     *
     * @param tableNames table name collection
     * @param materials materials
     * @param checkMetaDataEnable config CHECK_TABLE_METADATA_ENABLED
     * @return TableMetaDataLoadMaterials
     */
    public static Collection<TableMetaDataLoaderMaterial> getTableMetaDataLoadMaterial(final Collection<String> tableNames, final SchemaBuilderMaterials materials, final boolean checkMetaDataEnable) {
        Map<String, Collection<String>> dataSourceTableGroups = new LinkedHashMap<>();
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (String each : tableNames) {
            checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(materials.getDatabaseType(), dataNodes, each);
            if (checkMetaDataEnable) {
                addAllActualTableDataNode(materials, dataSourceTableGroups, dataNodes, each);
            } else {
                addOneActualTableDataNode(materials, dataSourceTableGroups, dataNodes, each);
            }
        }
        return dataSourceTableGroups.entrySet().stream().map(entry -> new TableMetaDataLoaderMaterial(entry.getValue(), materials.getDataSourceMap().get(entry.getKey().contains(".")
                ? entry.getKey().split("\\.")[0]
                : entry.getKey()), materials.getDefaultSchemaName())).collect(Collectors.toList());
    }
    
    private static void checkDataSourceTypeIncludeInstanceAndSetDatabaseTableMap(final DatabaseType databaseType, final DataNodes dataNodes, final String tableName) {
        for (DataNode dataNode : dataNodes.getDataNodes(tableName)) {
            if (databaseType.getType() != null && !databaseType.getType().equals("MySQL") && dataNode.getDataSourceName().contains(".")) {
                throw new ShardingSphereException("Unsupported jdbc: '%s', actualDataNode:'%s', database type is not mysql, but actual data is three-tier structure",
                        databaseType.getJdbcUrlPrefixes(), dataNode.getDataSourceName());
            }
            if (dataNode.getDataSourceName().contains(".")) {
                String database = dataNode.getDataSourceName().split("\\.")[1];
                GlobalDataSourceRegistry.getInstance().getCachedDatabaseTables().put(dataNode.getTableName(), database);
            }
        }
    }
    
    private static void addOneActualTableDataNode(final SchemaBuilderMaterials materials, final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Optional<DataNode> dataNode = dataNodes.getDataNodes(table).stream().filter(each -> isSameDataSourceNameSchemaName(materials, each)).findFirst();
        String dataSourceName = dataNode.map(DataNode::getDataSourceName).orElseGet(() -> materials.getDataSourceMap().keySet().iterator().next());
        String tableName = dataNode.map(DataNode::getTableName).orElse(table);
        addDataSourceTableGroups(dataSourceName, tableName, dataSourceTableGroups);
    }
    
    private static boolean isSameDataSourceNameSchemaName(final SchemaBuilderMaterials materials, final DataNode dataNode) {
        String dataSourceName = dataNode.getDataSourceName().contains(".") ? dataNode.getDataSourceName().split("\\.")[0] : dataNode.getDataSourceName();
        if (!materials.getDataSourceMap().containsKey(dataSourceName)) {
            return false;
        }
        return null == dataNode.getSchemaName() || dataNode.getSchemaName().equalsIgnoreCase(materials.getDefaultSchemaName());
    }
    
    private static void addAllActualTableDataNode(final SchemaBuilderMaterials materials, final Map<String, Collection<String>> dataSourceTableGroups, final DataNodes dataNodes, final String table) {
        Collection<DataNode> tableDataNodes = dataNodes.getDataNodes(table);
        if (tableDataNodes.isEmpty()) {
            addDataSourceTableGroups(materials.getDataSourceMap().keySet().iterator().next(), table, dataSourceTableGroups);
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
