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

package org.apache.shardingsphere.metadata.persist.data;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.metadata.persist.node.ShardingSphereDataNode;
import org.apache.shardingsphere.metadata.persist.service.schema.ShardingSphereTableRowDataPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere data persist service.
 */
@Getter
public final class ShardingSphereDataPersistService implements ShardingSphereDataBasedPersistService {
    
    private final PersistRepository repository;
    
    private final ShardingSphereTableRowDataPersistService tableRowDataPersistService;
    
    public ShardingSphereDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        tableRowDataPersistService = new ShardingSphereTableRowDataPersistService(repository);
    }
    
    /**
     * Load ShardingSphere data.
     *
     * @param metaData meta data
     * @return ShardingSphere data
     */
    @Override
    public Optional<ShardingSphereStatistics> load(final ShardingSphereMetaData metaData) {
        Collection<String> databaseNames = repository.getChildrenKeys(ShardingSphereDataNode.getShardingSphereDataNodePath());
        if (databaseNames.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (String each : databaseNames.stream().filter(metaData::containsDatabase).collect(Collectors.toList())) {
            result.getDatabaseData().put(each, load(each, metaData.getDatabase(each)));
        }
        return Optional.of(result);
    }
    
    private ShardingSphereDatabaseData load(final String databaseName, final ShardingSphereDatabase database) {
        Collection<String> schemaNames = repository.getChildrenKeys(ShardingSphereDataNode.getSchemasPath(databaseName));
        if (schemaNames.isEmpty()) {
            return new ShardingSphereDatabaseData();
        }
        ShardingSphereDatabaseData result = new ShardingSphereDatabaseData();
        for (String each : schemaNames.stream().filter(database::containsSchema).collect(Collectors.toList())) {
            result.getSchemaData().put(each, load(databaseName, each, database.getSchema(each)));
        }
        return result;
    }
    
    private ShardingSphereSchemaData load(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        Collection<String> tableNames = repository.getChildrenKeys(ShardingSphereDataNode.getTablesPath(databaseName, schemaName));
        if (tableNames.isEmpty()) {
            return new ShardingSphereSchemaData();
        }
        ShardingSphereSchemaData result = new ShardingSphereSchemaData();
        for (String each : tableNames.stream().filter(schema::containsTable).collect(Collectors.toList())) {
            result.getTableData().put(each, tableRowDataPersistService.load(databaseName, schemaName, each, schema.getTable(each)));
            
        }
        return result;
    }
    
    /**
     * Persist table.
     * @param databaseName database name
     * @param schemaName schema name
     * @param schemaData schema data
     * @param databases databases
     */
    @Override
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData, final Map<String, ShardingSphereDatabase> databases) {
        if (schemaData.getTableData().isEmpty()) {
            persistSchema(databaseName, schemaName);
        }
        persistTableData(databaseName, schemaName, schemaData, databases);
    }
    
    private void persistSchema(final String databaseName, final String schemaName) {
        repository.persist(ShardingSphereDataNode.getSchemaDataPath(databaseName, schemaName), "");
    }
    
    private void persistTableData(final String databaseName, final String schemaName, final ShardingSphereSchemaData schemaData, final Map<String, ShardingSphereDatabase> databases) {
        schemaData.getTableData().values().forEach(each -> {
            YamlShardingSphereRowDataSwapper swapper =
                    new YamlShardingSphereRowDataSwapper(new ArrayList<>(databases.get(databaseName.toLowerCase()).getSchema(schemaName).getTable(each.getName()).getColumnValues()));
            persistTableData(databaseName, schemaName, each.getName(), each.getRows().stream().map(swapper::swapToYamlConfiguration).collect(Collectors.toList()));
        });
    }
    
    private void persistTableData(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> rows) {
        tableRowDataPersistService.persist(databaseName, schemaName, tableName, rows);
    }
}
