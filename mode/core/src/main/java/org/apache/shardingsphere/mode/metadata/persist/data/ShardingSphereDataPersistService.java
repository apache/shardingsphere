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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereTableData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param metadata Metadata
     * @return ShardingSphere data
     */
    public Optional<ShardingSphereData> load(final ShardingSphereMetaData metadata) {
        Collection<String> databaseNames = repository.getChildrenKeys(ShardingSphereDataNode.getShardingSphereDataNodePath());
        if (databaseNames.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereData result = new ShardingSphereData();
        for (String each : databaseNames) {
            if (metadata.containsDatabase(each)) {
                ShardingSphereDatabaseData databaseData = loadDatabaseData(each, metadata.getDatabase(each));
                result.getDatabaseData().put(each, databaseData);
            }
        }
        return Optional.of(result);
    }
    
    private ShardingSphereDatabaseData loadDatabaseData(final String databaseName, final ShardingSphereDatabase database) {
        Collection<String> schemaNames = repository.getChildrenKeys(ShardingSphereDataNode.getSchemasPath(databaseName));
        if (schemaNames.isEmpty()) {
            return new ShardingSphereDatabaseData();
        }
        ShardingSphereDatabaseData result = new ShardingSphereDatabaseData();
        for (String each : schemaNames) {
            if (database.containsSchema(each)) {
                ShardingSphereSchemaData schemaData = loadSchemaData(databaseName, each, database.getSchema(each));
                result.getSchemaData().put(each, schemaData);
            }
        }
        return result;
    }
    
    private ShardingSphereSchemaData loadSchemaData(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        Collection<String> tableNames = repository.getChildrenKeys(ShardingSphereDataNode.getTablesPath(databaseName, schemaName));
        if (tableNames.isEmpty()) {
            return new ShardingSphereSchemaData();
        }
        ShardingSphereSchemaData result = new ShardingSphereSchemaData();
        for (String each : tableNames) {
            if (schema.containsTable(each)) {
                ShardingSphereTableData tableData = loadTableData(databaseName, schemaName, each, schema.getTable(each));
                result.getTableData().put(each, tableData);
            }
        }
        return result;
    }
    
    private ShardingSphereTableData loadTableData(final String databaseName, final String schemaName, final String tableName, final ShardingSphereTable table) {
        ShardingSphereTableData result = new ShardingSphereTableData(tableName);
        YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(table.getColumns().values()));
        for (String each : repository.getChildrenKeys(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName))) {
            String yamlRow = repository.getDirectly(ShardingSphereDataNode.getTableRowPath(databaseName, schemaName, tableName, each));
            if (null != yamlRow) {
                result.getRows().add(swapper.swapToObject(YamlEngine.unmarshal(yamlRow, YamlShardingSphereRowData.class)));
            }
        }
        
        return result;
    }
    
    /**
     * Persist.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schemaData schema data
     * @param databases databases
     */
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData, final Map<String, ShardingSphereDatabase> databases) {
        if (schemaData.getTableData().isEmpty()) {
            repository.persist(ShardingSphereDataNode.getSchemaDataPath(databaseName, schemaName), "");
        } else {
            schemaData.getTableData().values().forEach(each -> {
                if (databases.containsKey(databaseName.toLowerCase()) && databases.get(databaseName.toLowerCase()).containsSchema(schemaName)
                        && databases.get(databaseName.toLowerCase()).getSchema(schemaName).containsTable(each.getName())) {
                    persistTable(databaseName, schemaName, each.getName());
                    YamlShardingSphereRowDataSwapper swapper = new YamlShardingSphereRowDataSwapper(new ArrayList<>(databases.get(databaseName.toLowerCase())
                            .getSchema(schemaName).getTable(each.getName()).getColumns().values()));
                    persistRows(databaseName, schemaName, each.getName(), each.getRows().stream().map(swapper::swapToYamlConfiguration).collect(Collectors.toList()));
                }
            });
        }
    }
    
    /**
     * Persist table.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public void persistTable(final String databaseName, final String schemaName, final String tableName) {
        repository.persist(ShardingSphereDataNode.getTablePath(databaseName, schemaName, tableName.toLowerCase()), "");
    }
    
    /**
     * Persist rows.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param rows rows
     */
    public void persistRows(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> rows) {
        rows.forEach(each -> repository.persist(ShardingSphereDataNode.getTableRowPath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey()), YamlEngine.marshal(each)));
    }
    
    /**
     * Delete rows.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     * @param deletedRows deleted rows
     */
    public void deleteRows(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> deletedRows) {
        deletedRows.forEach(each -> repository.delete(ShardingSphereDataNode.getTableRowPath(databaseName, schemaName, tableName.toLowerCase(), each.getUniqueKey())));
    }
}
