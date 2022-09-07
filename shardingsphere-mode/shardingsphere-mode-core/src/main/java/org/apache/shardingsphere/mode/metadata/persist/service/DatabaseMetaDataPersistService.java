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
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.schema.ViewMetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Database meta data registry service.
 */
@Getter
public final class DatabaseMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final TableMetaDataPersistService tableMetaDataPersistService;
    
    private final ViewMetaDataPersistService viewMetaDataPersistService;
    
    public DatabaseMetaDataPersistService(final PersistRepository repository) {
        this.repository = repository;
        this.tableMetaDataPersistService = new TableMetaDataPersistService(repository);
        this.viewMetaDataPersistService = new ViewMetaDataPersistService(repository);
    }
    
    /**
     * Add database name.
     * 
     * @param databaseName database name
     */
    public void addDatabase(final String databaseName) {
        repository.persist(DatabaseMetaDataNode.getDatabaseNamePath(databaseName), "");
    }
    
    /**
     * Drop database.
     *
     * @param databaseName database name to be deleted
     */
    public void dropDatabase(final String databaseName) {
        repository.delete(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Load all database names.
     *
     * @return all database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataNodePath());
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void addSchema(final String databaseName, final String schemaName) {
        repository.persist(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName), "");
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void dropSchema(final String databaseName, final String schemaName) {
        repository.delete(DatabaseMetaDataNode.getMetaDataSchemaPath(databaseName, schemaName));
    }
    
    /**
     * Compare and persist schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    public void compareAndPersist(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        tableMetaDataPersistService.compareAndPersist(databaseName, schemaName, schema.getTables());
        viewMetaDataPersistService.compareAndPersist(databaseName, schemaName, schema.getViews());
    }
    
    /**
     * Persist schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        if (schema.getTables().isEmpty() && schema.getViews().isEmpty()) {
            addSchema(databaseName, schemaName);
        }
        tableMetaDataPersistService.persist(databaseName, schemaName, schema.getTables());
        viewMetaDataPersistService.persist(databaseName, schemaName, schema.getViews());
    }
    
    /**
     * Delete schema meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param schema schema meta data
     */
    public void delete(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        schema.getTables().forEach((key, value) -> tableMetaDataPersistService.delete(databaseName, schemaName, key));
        schema.getViews().forEach((key, value) -> viewMetaDataPersistService.delete(databaseName, schemaName, key));
    }
    
    /**
     * Load schema meta data.
     *
     * @param databaseName database name
     * @return schema meta data
     */
    public Map<String, ShardingSphereSchema> loadSchemas(final String databaseName) {
        Collection<String> schemaNames = loadAllSchemaNames(databaseName);
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaNames.size(), 1);
        schemaNames.forEach(each -> result.put(each.toLowerCase(),
                new ShardingSphereSchema(tableMetaDataPersistService.load(databaseName, each), viewMetaDataPersistService.load(databaseName, each))));
        return result;
    }
    
    private Collection<String> loadAllSchemaNames(final String databaseName) {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataSchemasPath(databaseName));
    }
}
