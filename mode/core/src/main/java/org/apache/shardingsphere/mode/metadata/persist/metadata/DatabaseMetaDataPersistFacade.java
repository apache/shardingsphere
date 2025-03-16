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

package org.apache.shardingsphere.mode.metadata.persist.metadata;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.exception.LoadTableMetaDataFailedException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.ViewMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Database meta data persist facade.
 */
@Getter
public final class DatabaseMetaDataPersistFacade {
    
    private final DatabaseMetaDataPersistService database;
    
    private final SchemaMetaDataPersistService schema;
    
    private final TableMetaDataPersistService table;
    
    private final ViewMetaDataPersistService view;
    
    public DatabaseMetaDataPersistFacade(final PersistRepository repository, final VersionPersistService versionPersistService) {
        database = new DatabaseMetaDataPersistService(repository);
        schema = new SchemaMetaDataPersistService(repository, versionPersistService);
        table = new TableMetaDataPersistService(repository, versionPersistService);
        view = new ViewMetaDataPersistService(repository, versionPersistService);
    }
    
    /**
     * Persist reload meta data by alter.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void persistReloadDatabaseByAlter(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Collection<ShardingSphereSchema> toBeAlteredSchemasWithTablesDropped = GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(reloadDatabase, currentDatabase);
        Collection<ShardingSphereSchema> toBeAlteredSchemasWithTablesAdded = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        toBeAlteredSchemasWithTablesAdded.forEach(each -> schema.alterByRuleAltered(databaseName, each));
        toBeAlteredSchemasWithTablesDropped.forEach(each -> table.drop(databaseName, each.getName(), each.getAllTables()));
    }
    
    /**
     * Persist reload meta data by drop.
     *
     * @param databaseName database name
     * @param reloadDatabase reload database
     * @param currentDatabase current database
     */
    public void persistReloadDatabaseByDrop(final String databaseName, final ShardingSphereDatabase reloadDatabase, final ShardingSphereDatabase currentDatabase) {
        Collection<ShardingSphereSchema> toBeAlteredSchemasWithTablesDropped = GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(reloadDatabase, currentDatabase);
        Collection<ShardingSphereSchema> toBeAlteredSchemasWithTablesAdded = GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(reloadDatabase, currentDatabase);
        toBeAlteredSchemasWithTablesAdded.forEach(each -> schema.alterByRuleDropped(databaseName, each));
        toBeAlteredSchemasWithTablesDropped.forEach(each -> table.drop(databaseName, each.getName(), each.getAllTables()));
    }
    
    /**
     * Persist schema.
     *
     * @param database database
     * @param schemaName schema name
     * @param alteredTables altered tables
     * @param alteredViews altered views
     * @param droppedTables dropped tables
     * @param droppedViews dropped views
     */
    public void alterSchema(final ShardingSphereDatabase database, final String schemaName,
                            final Collection<ShardingSphereTable> alteredTables, final Collection<ShardingSphereView> alteredViews,
                            final Collection<String> droppedTables, final Collection<String> droppedViews) {
        table.persist(database.getName(), schemaName, alteredTables);
        view.persist(database.getName(), schemaName, alteredViews);
        droppedTables.forEach(each -> table.drop(database.getName(), schemaName, each));
        droppedViews.forEach(each -> view.drop(database.getName(), schemaName, each));
    }
    
    /**
     * Rename schema.
     *
     * @param metaData meta data
     * @param database database
     * @param schemaName schema name
     * @param renameSchemaName rename schema name
     */
    public void renameSchema(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName) {
        ShardingSphereSchema schema = metaData.getDatabase(database.getName()).getSchema(schemaName);
        if (schema.isEmpty()) {
            this.schema.add(database.getName(), renameSchemaName);
        } else {
            table.persist(database.getName(), renameSchemaName, schema.getAllTables());
            view.persist(database.getName(), renameSchemaName, schema.getAllViews());
        }
        this.schema.drop(database.getName(), schemaName);
    }
    
    /**
     * Register storage units.
     *
     * @param databaseName database name
     * @param reloadMetaDataContexts reload meta data contexts
     * @throws LoadTableMetaDataFailedException if an error occurs while loading table metadata
     */
    public void unregisterStorageUnits(final String databaseName, final MetaDataContexts reloadMetaDataContexts) {
        ShardingSphereDatabase database = reloadMetaDataContexts.getMetaData().getDatabase(databaseName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(),
                database.getRuleMetaData().getRules(), reloadMetaDataContexts.getMetaData().getProps(), new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(databaseName));
        try {
            Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(database.getProtocolType(), material);
            for (Entry<String, ShardingSphereSchema> entry : schemas.entrySet()) {
                GenericSchemaManager.getToBeDroppedTables(entry.getValue(), database.getSchema(entry.getKey())).forEach(each -> table.drop(databaseName, entry.getKey(), each.getName()));
            }
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(databaseName, ex);
        }
    }
    
    /**
     * Persist altered tables.
     *
     * @param databaseName database name
     * @param reloadMetaDataContexts reload meta data contexts
     * @param needReloadTables need reload tables
     * @return altered schema and tables map
     * @throws LoadTableMetaDataFailedException if an error occurs while loading table metadata
     */
    public Map<String, Collection<ShardingSphereTable>> persistAlteredTables(final String databaseName, final MetaDataContexts reloadMetaDataContexts, final Collection<String> needReloadTables) {
        ShardingSphereDatabase database = reloadMetaDataContexts.getMetaData().getDatabase(databaseName);
        GenericSchemaBuilderMaterial material = new GenericSchemaBuilderMaterial(database.getResourceMetaData().getStorageUnits(),
                database.getRuleMetaData().getRules(), reloadMetaDataContexts.getMetaData().getProps(),
                new DatabaseTypeRegistry(database.getProtocolType()).getDefaultSchemaName(databaseName));
        try {
            Map<String, ShardingSphereSchema> schemas = GenericSchemaBuilder.build(needReloadTables, database.getProtocolType(), material);
            Map<String, Collection<ShardingSphereTable>> result = new HashMap<>(schemas.size(), 1F);
            for (Entry<String, ShardingSphereSchema> entry : schemas.entrySet()) {
                Collection<ShardingSphereTable> tables = GenericSchemaManager.getToBeAddedTables(entry.getValue(), database.getSchema(entry.getKey()));
                table.persist(databaseName, entry.getKey(), tables);
                result.put(entry.getKey(), tables);
            }
            return result;
        } catch (final SQLException ex) {
            throw new LoadTableMetaDataFailedException(databaseName, needReloadTables, ex);
        }
    }
}
