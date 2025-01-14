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

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereDatabaseData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereSchemaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlShardingSphereRowData;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlShardingSphereRowDataSwapper;
import org.apache.shardingsphere.mode.node.path.metadata.ShardingSphereDataNodePath;
import org.apache.shardingsphere.mode.metadata.persist.service.metadata.table.TableRowDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere data persist service.
 */
public final class ShardingSphereDataPersistService {
    
    private final PersistRepository repository;
    
    private final TableRowDataPersistService tableRowDataPersistService;
    
    public ShardingSphereDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        tableRowDataPersistService = new TableRowDataPersistService(repository);
    }
    
    /**
     * Load statistics.
     *
     * @param metaData meta data
     * @return statistics
     */
    public Optional<ShardingSphereStatistics> load(final ShardingSphereMetaData metaData) {
        Collection<String> databaseNames = repository.getChildrenKeys(ShardingSphereDataNodePath.getDatabasesRootPath());
        if (databaseNames.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (String each : databaseNames.stream().filter(metaData::containsDatabase).collect(Collectors.toList())) {
            result.getDatabaseData().put(each, load(metaData.getDatabase(each)));
        }
        return Optional.of(result);
    }
    
    private ShardingSphereDatabaseData load(final ShardingSphereDatabase database) {
        ShardingSphereDatabaseData result = new ShardingSphereDatabaseData();
        for (String each : repository.getChildrenKeys(ShardingSphereDataNodePath.getSchemaRootPath(database.getName())).stream().filter(database::containsSchema).collect(Collectors.toList())) {
            result.putSchema(each, load(database.getName(), database.getSchema(each)));
        }
        return result;
    }
    
    private ShardingSphereSchemaData load(final String databaseName, final ShardingSphereSchema schema) {
        ShardingSphereSchemaData result = new ShardingSphereSchemaData();
        for (String each : repository.getChildrenKeys(ShardingSphereDataNodePath.getTableRootPath(databaseName, schema.getName())).stream().filter(schema::containsTable)
                .collect(Collectors.toList())) {
            result.getTableData().put(each, tableRowDataPersistService.load(databaseName, schema.getName(), schema.getTable(each)));
            
        }
        return result;
    }
    
    /**
     * Persist.
     *
     * @param database database
     * @param schemaName schema name
     * @param schemaData schema data
     */
    public void persist(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereSchemaData schemaData) {
        if (schemaData.getTableData().isEmpty()) {
            persistSchema(database.getName(), schemaName);
        }
        persistTableData(database, schemaName, schemaData);
    }
    
    private void persistSchema(final String databaseName, final String schemaName) {
        repository.persist(ShardingSphereDataNodePath.getSchemaPath(databaseName, schemaName), "");
    }
    
    private void persistTableData(final ShardingSphereDatabase database, final String schemaName, final ShardingSphereSchemaData schemaData) {
        schemaData.getTableData().values().forEach(each -> {
            YamlShardingSphereRowDataSwapper swapper =
                    new YamlShardingSphereRowDataSwapper(new ArrayList<>(database.getSchema(schemaName).getTable(each.getName()).getAllColumns()));
            persistTableData(database.getName(), schemaName, each.getName(), each.getRows().stream().map(swapper::swapToYamlConfiguration).collect(Collectors.toList()));
        });
    }
    
    private void persistTableData(final String databaseName, final String schemaName, final String tableName, final Collection<YamlShardingSphereRowData> rows) {
        tableRowDataPersistService.persist(databaseName, schemaName, tableName, rows);
    }
    
    /**
     * Update ShardingSphere database data.
     *
     * @param alteredData altered ShardingSphere database data
     */
    public void update(final AlteredShardingSphereDatabaseData alteredData) {
        tableRowDataPersistService.persist(alteredData.getDatabaseName(), alteredData.getSchemaName(), alteredData.getTableName(), alteredData.getAddedRows());
        tableRowDataPersistService.persist(alteredData.getDatabaseName(), alteredData.getSchemaName(), alteredData.getTableName(), alteredData.getUpdatedRows());
        tableRowDataPersistService.delete(alteredData.getDatabaseName(), alteredData.getSchemaName(), alteredData.getTableName(), alteredData.getDeletedRows());
    }
    
    /**
     * Delete sharding sphere database data.
     *
     * @param databaseName database name
     */
    public void delete(final String databaseName) {
        repository.delete(ShardingSphereDataNodePath.getDatabasePath(databaseName));
    }
}
