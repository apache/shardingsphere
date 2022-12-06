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

package org.apache.shardingsphere.mode.metadata.persist.service;

import lombok.Getter;
import org.apache.shardingsphere.infra.metadata.database.schema.SchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetadataNode;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.TableMetadataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.ViewMetadataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Database Metadata registry service.
 */
@Getter
public final class DatabaseMetadataPersistService {
    
    private final PersistRepository repository;
    
    private final TableMetadataPersistService tableMetaDataPersistService;
    
    private final ViewMetadataPersistService viewMetaDataPersistService;
    
    public DatabaseMetadataPersistService(final PersistRepository repository) {
        this.repository = repository;
        this.tableMetaDataPersistService = new TableMetadataPersistService(repository);
        this.viewMetaDataPersistService = new ViewMetadataPersistService(repository);
    }
    
    /**
     * Add database name.
     *
     * @param databaseName database name
     */
    public void addDatabase(final String databaseName) {
        repository.persist(DatabaseMetadataNode.getDatabaseNamePath(databaseName), "");
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name to be deleted
     */
    public void dropDatabase(final String databaseName) {
        repository.delete(DatabaseMetadataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Load all database names.
     *
     * @return all database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(DatabaseMetadataNode.getMetadataNodePath());
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        repository.persist(DatabaseMetadataNode.getMetadataTablesPath(databaseName, schemaName), "");
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void dropSchema(final String databaseName, final String schemaName) {
        repository.delete(DatabaseMetadataNode.getMetadataSchemaPath(databaseName, schemaName));
    }
    
    /**
     * Compare and persist schema Metadata.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema Metadata
     */
    public void compareAndPersist(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        Map<String, ShardingSphereTable> currentTables = tableMetaDataPersistService.load(databaseName, schemaName);
        tableMetaDataPersistService.persist(databaseName, schemaName, SchemaManager.getToBeAddedTables(schema.getTables(), currentTables));
        SchemaManager.getToBeDeletedTables(schema.getTables(), currentTables).forEach((key, value) -> tableMetaDataPersistService.delete(databaseName, schemaName, key));
    }
    
    /**
     * Persist schema Metadata.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema Metadata
     */
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        tableMetaDataPersistService.persist(databaseName, schemaName, schema.getTables());
    }
    
    /**
     * Delete schema Metadata.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema Metadata
     */
    public void delete(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        schema.getTables().forEach((key, value) -> tableMetaDataPersistService.delete(databaseName, schemaName, key));
    }
    
    /**
     * Load schema Metadata.
     *
     * @param databaseName database name
     * @return schema Metadata
     */
    public Map<String, ShardingSphereSchema> loadSchemas(final String databaseName) {
        Collection<String> schemaNames = loadAllSchemaNames(databaseName);
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaNames.size(), 1);
        schemaNames.forEach(each -> result.put(each.toLowerCase(),
                new ShardingSphereSchema(tableMetaDataPersistService.load(databaseName, each), viewMetaDataPersistService.load(databaseName, each))));
        return result;
    }
    
    private Collection<String> loadAllSchemaNames(final String databaseName) {
        return repository.getChildrenKeys(DatabaseMetadataNode.getMetadataSchemasPath(databaseName));
    }
}
