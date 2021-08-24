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

package org.apache.shardingsphere.infra.metadata.schema.builder.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datanode.DataNodes;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.builder.TableMetaDataLoadMaterials;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
     * @return TableMetaDataLoadMaterials
     */
    public static Optional<TableMetaDataLoadMaterials> getTableMetaDataLoadMaterials(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> dataSourceTables = getDataSourceTableGroups(tableNames, materials);
        return buildTableMetaDataLoadMaterials(materials, dataSourceTables);
    }
    
    /**
     * Get all actual table meta data load materials.
     *
     * @param tableNames table name collection
     * @param materials materials
     * @return TableMetaDataLoadMaterials
     */
    public static Optional<TableMetaDataLoadMaterials> getAllTableMetaDataLoadMaterials(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> dataSourceTables = getAllDataSourceTableGroups(tableNames, materials);
        return buildTableMetaDataLoadMaterials(materials, dataSourceTables);
    }
    
    /**
     * get table meta data load materials by one table.
     *
     * @param dataSource data source
     * @param tableName table name
     * @param databaseType database type
     * @return TableMetaDataLoadMaterials
     */
    public static TableMetaDataLoadMaterials getTableMetaDataLoadMaterialsByOneTable(final DataSource dataSource, final String tableName, final DatabaseType databaseType) {
        return new TableMetaDataLoadMaterials(Collections.singleton(new TableMetaDataLoadMaterials.LoadMaterial(Collections.singleton(tableName), dataSource)), databaseType);
    }
    
    private static Map<String, Collection<String>> getDataSourceTableGroups(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        DataNodes dataNodes = new DataNodes(materials.getRules());
        for (String each : tableNames) {
            Optional<DataNode> optional = dataNodes.getDataNodes(each).stream().findFirst();
            String dataSourceName = optional.map(DataNode::getDataSourceName).orElse(materials.getDataSourceMap().keySet().iterator().next());
            String tableName = optional.map(DataNode::getTableName).orElse(each);
            Collection<String> tables = result.getOrDefault(dataSourceName, new LinkedList<>());
            tables.add(tableName);
            result.putIfAbsent(dataSourceName, tables);
        }
        return result;
    }
    
    private static Map<String, Collection<String>> getAllDataSourceTableGroups(final Collection<String> tableNames, final SchemaBuilderMaterials materials) {
        Map<String, Collection<String>> result = new LinkedHashMap<>();
        for (String each : tableNames) {
            Map<String, List<DataNode>> dataNodes = new DataNodes(materials.getRules()).getDataNodeGroups(each);
            for (Entry<String, List<DataNode>> entry : dataNodes.entrySet()) {
                Collection<String> tables = result.getOrDefault(entry.getKey(), new LinkedList<>());
                tables.addAll(entry.getValue().stream().map(DataNode::getTableName).collect(Collectors.toSet()));
                result.putIfAbsent(entry.getKey(), tables);
            }
        }
        return result;
    }
    
    private static Optional<TableMetaDataLoadMaterials> buildTableMetaDataLoadMaterials(final SchemaBuilderMaterials materials, final Map<String, Collection<String>> dataSourceTables) {
        Collection<TableMetaDataLoadMaterials.LoadMaterial> loadMaterialList = new LinkedList<>();
        for (Entry<String, Collection<String>> entry : dataSourceTables.entrySet()) {
            loadMaterialList.add(new TableMetaDataLoadMaterials.LoadMaterial(entry.getValue(), materials.getDataSourceMap().get(entry.getKey())));
        }
        return loadMaterialList.isEmpty() ? Optional.empty() : Optional.of(new TableMetaDataLoadMaterials(loadMaterialList, materials.getDatabaseType()));
    }
}
