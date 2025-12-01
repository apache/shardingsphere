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

package org.apache.shardingsphere.mode.metadata.persist.statistics;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.statistics.DatabaseStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.SchemaStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.yaml.data.pojo.YamlRowStatistics;
import org.apache.shardingsphere.infra.yaml.data.swapper.YamlRowStatisticsSwapper;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableRowDataPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsDatabaseNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsSchemaNodePath;
import org.apache.shardingsphere.mode.node.path.type.database.statistics.StatisticsTableNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Statistics persist service.
 */
public final class StatisticsPersistService {
    
    private final PersistRepository repository;
    
    private final TableRowDataPersistService tableRowDataPersistService;
    
    public StatisticsPersistService(final PersistRepository repository) {
        this.repository = repository;
        tableRowDataPersistService = new TableRowDataPersistService(repository);
    }
    
    /**
     * Load statistics.
     *
     * @param metaData meta data
     * @return statistics
     */
    public ShardingSphereStatistics load(final ShardingSphereMetaData metaData) {
        Collection<String> databaseNames = repository.getChildrenKeys(NodePathGenerator.toPath(new StatisticsDatabaseNodePath(null)));
        if (databaseNames.isEmpty()) {
            return new ShardingSphereStatistics();
        }
        ShardingSphereStatistics result = new ShardingSphereStatistics();
        for (String each : databaseNames.stream().filter(metaData::containsDatabase).collect(Collectors.toList())) {
            result.putDatabaseStatistics(each, load(metaData.getDatabase(each)));
        }
        return result;
    }
    
    private DatabaseStatistics load(final ShardingSphereDatabase database) {
        DatabaseStatistics result = new DatabaseStatistics();
        for (String each : repository.getChildrenKeys(NodePathGenerator.toPath(new StatisticsSchemaNodePath(database.getName(), null))).stream()
                .filter(database::containsSchema).collect(Collectors.toList())) {
            result.putSchemaStatistics(each, load(database.getName(), database.getSchema(each)));
        }
        return result;
    }
    
    private SchemaStatistics load(final String databaseName, final ShardingSphereSchema schema) {
        SchemaStatistics result = new SchemaStatistics();
        for (String each : repository.getChildrenKeys(NodePathGenerator.toPath(new StatisticsTableNodePath(databaseName, schema.getName(), null))).stream()
                .filter(schema::containsTable).collect(Collectors.toList())) {
            result.putTableStatistics(each, tableRowDataPersistService.load(databaseName, schema.getName(), schema.getTable(each)));
            
        }
        return result;
    }
    
    /**
     * Persist.
     *
     * @param database database
     * @param schemaName schema name
     * @param schemaStatistics schema statistics
     */
    public void persist(final ShardingSphereDatabase database, final String schemaName, final SchemaStatistics schemaStatistics) {
        if (schemaStatistics.getTableStatisticsMap().isEmpty()) {
            persistSchema(database.getName(), schemaName);
        }
        persistTableData(database, schemaName, schemaStatistics);
    }
    
    private void persistSchema(final String databaseName, final String schemaName) {
        repository.persist(NodePathGenerator.toPath(new StatisticsSchemaNodePath(databaseName, schemaName)), "");
    }
    
    private void persistTableData(final ShardingSphereDatabase database, final String schemaName, final SchemaStatistics schemaStatistics) {
        schemaStatistics.getTableStatisticsMap().values().forEach(each -> {
            Collection<ShardingSphereColumn> columns = database.getSchema(schemaName)
                    .containsTable(each.getName()) ? database.getSchema(schemaName).getTable(each.getName()).getAllColumns() : Collections.emptyList();
            YamlRowStatisticsSwapper swapper = new YamlRowStatisticsSwapper(new ArrayList<>(columns));
            persistTableData(database.getName(), schemaName, each.getName(), columns.isEmpty() ? Collections.emptyList()
                    : each.getRows().stream().map(swapper::swapToYamlConfiguration).collect(Collectors.toList()));
        });
    }
    
    private void persistTableData(final String databaseName, final String schemaName, final String tableName, final Collection<YamlRowStatistics> rows) {
        tableRowDataPersistService.persist(databaseName, schemaName, tableName, rows);
    }
    
    /**
     * Update ShardingSphere database data.
     *
     * @param alteredDatabaseStatistics altered database statistics
     */
    public void update(final AlteredDatabaseStatistics alteredDatabaseStatistics) {
        tableRowDataPersistService.persist(alteredDatabaseStatistics.getDatabaseName(), alteredDatabaseStatistics.getSchemaName(), alteredDatabaseStatistics.getTableName(),
                alteredDatabaseStatistics.getAddedRows());
        tableRowDataPersistService.persist(alteredDatabaseStatistics.getDatabaseName(), alteredDatabaseStatistics.getSchemaName(), alteredDatabaseStatistics.getTableName(),
                alteredDatabaseStatistics.getUpdatedRows());
        tableRowDataPersistService.delete(alteredDatabaseStatistics.getDatabaseName(), alteredDatabaseStatistics.getSchemaName(), alteredDatabaseStatistics.getTableName(),
                alteredDatabaseStatistics.getDeletedRows());
    }
    
    /**
     * Delete sharding sphere database data.
     *
     * @param databaseName database name
     */
    public void delete(final String databaseName) {
        repository.delete(NodePathGenerator.toPath(new StatisticsDatabaseNodePath(databaseName)));
    }
}
