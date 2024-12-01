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

package org.apache.shardingsphere.metadata.persist.service.metadata.schema;

import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.metadata.table.TableMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.metadata.table.ViewMetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Schema meta data persist service.
 */
public final class SchemaMetaDataPersistService {
    
    private final PersistRepository repository;
    
    private final TableMetaDataPersistService tableMetaDataPersistService;
    
    private final ViewMetaDataPersistService viewMetaDataPersistService;
    
    public SchemaMetaDataPersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.repository = repository;
        tableMetaDataPersistService = new TableMetaDataPersistService(repository, metaDataVersionPersistService);
        viewMetaDataPersistService = new ViewMetaDataPersistService(repository, metaDataVersionPersistService);
    }
    
    /**
     * Add schema.
     *
     * @param databaseName database name
     * @param schemaName to be added schema name
     */
    public void add(final String databaseName, final String schemaName) {
        repository.persist(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName), "");
    }
    
    /**
     * Drop schema.
     *
     * @param databaseName database name
     * @param schemaName to be dropped schema name
     */
    public void drop(final String databaseName, final String schemaName) {
        repository.delete(DatabaseMetaDataNode.getMetaDataSchemaPath(databaseName, schemaName));
    }
    
    /**
     * Alter schema by refresh.
     *
     * @param databaseName to be altered database name
     * @param schema to be altered schema
     */
    public void alterByRefresh(final String databaseName, final ShardingSphereSchema schema) {
        String schemaName = schema.getName().toLowerCase();
        if (schema.isEmpty()) {
            add(databaseName, schemaName);
        }
        ShardingSphereSchema currentSchema = new ShardingSphereSchema(schemaName, tableMetaDataPersistService.load(databaseName, schemaName), Collections.emptyList());
        tableMetaDataPersistService.persist(databaseName, schemaName, GenericSchemaManager.getToBeAddedTables(schema, currentSchema));
        GenericSchemaManager.getToBeDroppedTables(schema, currentSchema).forEach(each -> tableMetaDataPersistService.drop(databaseName, schemaName, each.getName()));
    }
    
    /**
     * Alter schema by rule altered.
     *
     * @param databaseName database name
     * @param schema to be altered schema
     */
    public void alterByRuleAltered(final String databaseName, final ShardingSphereSchema schema) {
        String schemaName = schema.getName().toLowerCase();
        if (schema.isEmpty()) {
            add(databaseName, schemaName);
        }
        tableMetaDataPersistService.persist(databaseName, schemaName, schema.getAllTables());
    }
    
    /**
     * Alter schema by rule dropped.
     *
     * @param databaseName database name
     * @param schema to be altered schema
     */
    public void alterByRuleDropped(final String databaseName, final ShardingSphereSchema schema) {
        tableMetaDataPersistService.persist(databaseName, schema.getName(), schema.getAllTables());
    }
    
    /**
     * Load schemas.
     *
     * @param databaseName database name
     * @return schemas
     */
    public Collection<ShardingSphereSchema> load(final String databaseName) {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataSchemasPath(databaseName)).stream()
                .map(each -> new ShardingSphereSchema(each, tableMetaDataPersistService.load(databaseName, each), viewMetaDataPersistService.load(databaseName, each))).collect(Collectors.toList());
    }
}
