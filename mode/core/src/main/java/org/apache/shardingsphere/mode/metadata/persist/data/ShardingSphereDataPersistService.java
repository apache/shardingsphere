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

package org.apache.shardingsphere.mode.metadata.persist.data;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSpherePartitionRowData;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereTableData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereTableDataSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * ShardingSphere data persist service.
 */
@Getter
public final class ShardingSphereDataPersistService {
    
    private final PersistRepository repository;
    
    public ShardingSphereDataPersistService(final PersistRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Load.
     * 
     * @return ShardingSphere data
     */
    public Optional<ShardingSphereData> load() {
        Collection<String> databaseNames = repository.getChildrenKeys(ShardingSphereDataNode.getShardingSphereDataNodePath());
        if (databaseNames.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereData result = new ShardingSphereData();
        for (String each : databaseNames) {
            ShardingSphereDatabaseData databaseData = loadDatabaseData(each);
            result.getDatabaseData().put(each, databaseData);
        }
        return Optional.of(result);
    }
    
    private ShardingSphereDatabaseData loadDatabaseData(final String databaseName) {
        Collection<String> schemaNames = repository.getChildrenKeys(ShardingSphereDataNode.getSchemasPath(databaseName));
        if (schemaNames.isEmpty()) {
            return new ShardingSphereDatabaseData();
        }
        ShardingSphereDatabaseData result = new ShardingSphereDatabaseData();
        for (String each : schemaNames) {
            ShardingSphereSchemaData schemaData = loadSchemaData(databaseName, each);
            result.getSchemaData().put(each, schemaData);
        }
        return result;
    }
    
    private ShardingSphereSchemaData loadSchemaData(final String databaseName, final String schemaName) {
        Collection<String> tableNames = repository.getChildrenKeys(ShardingSphereDataNode.getTablesPath(databaseName, schemaName));
        if (tableNames.isEmpty()) {
            return new ShardingSphereSchemaData();
        }
        ShardingSphereSchemaData result = new ShardingSphereSchemaData();
        for (String each : tableNames) {
            ShardingSphereTableData tableData = loadTableData(databaseName, schemaName, each);
            result.getTableData().put(each, tableData);
        }
        return result;
    }
    
    private ShardingSphereTableData loadTableData(final String databaseName, final String schemaName, final String tableName) {
        String tableData = repository.getDirectly(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName));
        YamlShardingSphereTableData yamlTableData = YamlEngine.unmarshal(tableData, YamlShardingSphereTableData.class);
        Map<Integer, YamlShardingSpherePartitionRowData> partitionRowsData = new TreeMap<>();
        for (String each : repository.getChildrenKeys(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName))) {
            String partitionRows = repository.getDirectly(ShardingSphereDataNode.getTablePartitionRowsPath(databaseName, schemaName, tableName, each));
            partitionRowsData.put(Integer.parseInt(each), YamlEngine.unmarshal(partitionRows, YamlShardingSpherePartitionRowData.class));
        }
        yamlTableData.setPartitionRows(partitionRowsData);
        return new YamlShardingSphereTableDataSwapper().swapToObject(yamlTableData);
    }
    
    /**
     * Persist.
     * 
     * @param databaseName database name
     * @param schemaName schema name
     * @param schemaData schema data
     */
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData) {
        if (schemaData.getTableData().isEmpty()) {
            repository.persist(ShardingSphereDataNode.getSchemaDataPath(databaseName, schemaName), "");
        } else {
            persistTables(databaseName, schemaName, schemaData.getTableData().values());
        }
    }
    
    /**
     * Persist tables.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tables table data
     */
    public void persistTables(final String databaseName, final String schemaName, final Collection<ShardingSphereTableData> tables) {
        for (ShardingSphereTableData each : tables) {
            repository.delete(ShardingSphereDataNode.getTablePath(databaseName, schemaName, each.getName().toLowerCase()));
            YamlShardingSphereTableData yamlShardingSphereTableData = new YamlShardingSphereTableDataSwapper().swapToYamlConfiguration(each);
            YamlShardingSphereTableData yamlTableDataWithoutRows = new YamlShardingSphereTableData();
            yamlTableDataWithoutRows.setName(yamlShardingSphereTableData.getName());
            yamlTableDataWithoutRows.setColumns(yamlShardingSphereTableData.getColumns());
            repository.persist(ShardingSphereDataNode.getTablePath(databaseName, schemaName, each.getName().toLowerCase()), YamlEngine.marshal(yamlTableDataWithoutRows));
            yamlShardingSphereTableData.getPartitionRows().forEach((key, value) -> repository.persist(ShardingSphereDataNode
                    .getTablePartitionRowsPath(databaseName, schemaName, each.getName().toLowerCase(), String.valueOf(key)), YamlEngine.marshal(value)));
        }
    }
}
