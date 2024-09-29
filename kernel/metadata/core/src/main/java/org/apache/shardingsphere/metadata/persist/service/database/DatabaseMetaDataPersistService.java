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

package org.apache.shardingsphere.metadata.persist.service.database;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.schema.TableMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.schema.ViewMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Database meta data registry service.
 */
@Getter
public final class DatabaseMetaDataPersistService {
    
    @Getter(AccessLevel.NONE)
    private final PersistRepository repository;
    
    private final TableMetaDataPersistService tableMetaDataPersistService;
    
    private final ViewMetaDataPersistService viewMetaDataPersistService;
    
    public DatabaseMetaDataPersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.repository = repository;
        tableMetaDataPersistService = new TableMetaDataPersistService(repository, metaDataVersionPersistService);
        viewMetaDataPersistService = new ViewMetaDataPersistService(repository, metaDataVersionPersistService);
    }
    
    /**
     * Add database.
     *
     * @param databaseName to be added database name
     */
    public void addDatabase(final String databaseName) {
        repository.persist(DatabaseMetaDataNode.getDatabaseNamePath(databaseName), "");
    }
    
    /**
     * Drop database.
     *
     * @param databaseName to be dropped database name
     */
    public void dropDatabase(final String databaseName) {
        repository.delete(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Load database names.
     *
     * @return loaded database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataNode());
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName to be added schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        repository.persist(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName), "");
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName to be dropped schema name
     */
    public void dropSchema(final String databaseName, final String schemaName) {
        repository.delete(DatabaseMetaDataNode.getMetaDataSchemaPath(databaseName, schemaName));
    }
    
    /**
     * Alter schema by refresh.
     *
     * @param databaseName to be altered database name
     * @param schema to be altered schema
     */
    public void alterSchemaByRefresh(final String databaseName, final ShardingSphereSchema schema) {
        String schemaName = schema.getName().toLowerCase();
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        Map<String, ShardingSphereTable> currentTables = tableMetaDataPersistService.load(databaseName, schemaName);
        tableMetaDataPersistService.persist(databaseName, schemaName, GenericSchemaManager.getToBeAddedTables(schema.getTables(), currentTables));
        GenericSchemaManager.getToBeDroppedTables(schema.getTables(), currentTables).forEach((key, value) -> tableMetaDataPersistService.delete(databaseName, schemaName, key));
    }
    
    /**
     * Alter schema by rule altered.
     *
     * @param databaseName database name
     * @param schema to be altered schema
     */
    public void alterSchemaByRuleAltered(final String databaseName, final ShardingSphereSchema schema) {
        String schemaName = schema.getName().toLowerCase();
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        tableMetaDataPersistService.persist(databaseName, schemaName, schema.getTables());
    }
    
    /**
     * Alter schema by rule dropped.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema to be altered schema
     */
    public void alterSchemaByRuleDropped(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        tableMetaDataPersistService.persist(databaseName, schemaName, schema.getTables());
    }
    
    /**
     * Load schemas.
     *
     * @param databaseName database name
     * @return schemas
     */
    public Map<String, ShardingSphereSchema> loadSchemas(final String databaseName) {
        Collection<String> schemaNames = loadAllSchemaNames(databaseName);
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaNames.size(), 1F);
        schemaNames.forEach(each -> result.put(each.toLowerCase(),
                new ShardingSphereSchema(each, tableMetaDataPersistService.load(databaseName, each), viewMetaDataPersistService.load(databaseName, each))));
        return result;
    }
    
    private Collection<String> loadAllSchemaNames(final String databaseName) {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataSchemasPath(databaseName));
    }
    
    /**
     * Drop tables.
     *
     * @param databaseName to be dropped database name
     * @param schemaName to be dropped schema name
     * @param tables to be dropped tables
     */
    public void dropTables(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        tables.forEach((key, value) -> tableMetaDataPersistService.delete(databaseName, schemaName, key));
    }
}
