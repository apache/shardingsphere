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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.model.TableMetaData;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlTableMetaData;
import org.apache.shardingsphere.infra.yaml.schema.swapper.TableMetaDataYamlSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Schema meta data registry service.
 */
@RequiredArgsConstructor
public final class SchemaMetaDataPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Persist schema.
     *
     * @param databaseName database name to be persisted
     * @param schemaName schema name to be persisted
     * @param schema schema to be persisted
     */
    public void persist(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        if (null == schema) {
            return;
        }
        Optional<ShardingSphereSchema> originalSchema = load(databaseName, schemaName);
        if (originalSchema.isPresent()) {
            compareAndPersist(databaseName, schemaName, schema, originalSchema.get());
            return;
        }
        persistTables(databaseName, schemaName, schema.getTables());
    }
    
    /**
     * Persist table meta data.
     *
     * @param schemaName schema name
     * @param tableMetaData table meta data
     */
    public void persist(final String schemaName, final TableMetaData tableMetaData) {
        repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(schemaName, schemaName, tableMetaData.getName().toLowerCase()),
                YamlEngine.marshal(new TableMetaDataYamlSwapper().swapToYamlConfiguration(tableMetaData)));
    }
    
    /**
     * Persist table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableMetaData table meta data
     */
    public void persist(final String databaseName, final String schemaName, final TableMetaData tableMetaData) {
        repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, tableMetaData.getName().toLowerCase()),
                YamlEngine.marshal(new TableMetaDataYamlSwapper().swapToYamlConfiguration(tableMetaData)));
    }
    
    /**
     * Persist schema tables.
     *
     * @param databaseName database name
     */
    public void persist(final String databaseName) {
        repository.persist(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, databaseName), "");
    }
    
    private void compareAndPersist(final String databaseName, final String schemaName, final ShardingSphereSchema schema, final ShardingSphereSchema originalSchema) {
        Map<String, TableMetaData> cachedLocalTables = new LinkedHashMap<>(schema.getTables());
        for (Map.Entry<String, TableMetaData> entry : originalSchema.getTables().entrySet()) {
            String onlineTableName = entry.getKey();
            TableMetaData localTableMetaData = cachedLocalTables.remove(onlineTableName);
            if (null == localTableMetaData) {
                delete(databaseName, schemaName, onlineTableName);
                continue;
            }
            if (!localTableMetaData.equals(entry.getValue())) {
                persist(databaseName, schemaName, localTableMetaData);
            }
        }
        if (!cachedLocalTables.isEmpty()) {
            persistTables(databaseName, schemaName, cachedLocalTables);
        }
    }
    
    private void persistTables(final String databaseName, final String schemaName, final Map<String, TableMetaData> tables) {
        tables.forEach((key, value) -> repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, key),
                YamlEngine.marshal(new TableMetaDataYamlSwapper().swapToYamlConfiguration(value))));
    }
    
    /**
     * Delete database.
     *
     * @param databaseName database name to be deleted
     */
    public void delete(final String databaseName) {
        repository.delete(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Delete table meta data.
     * 
     * @param schemaName schema name
     * @param tableName table name
     */
    public void delete(final String schemaName, final String tableName) {
        repository.delete(DatabaseMetaDataNode.getTableMetaDataPath(schemaName, schemaName, tableName));
    }
    
    /**
     * Delete table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public void delete(final String databaseName, final String schemaName, final String tableName) {
        repository.delete(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, tableName));
    }
    
    /**
     * Load schema.
     *
     * @param databaseName database name to be loaded
     * @param schemaName schema name to be loaded
     * @return Loaded schema
     */
    public Optional<ShardingSphereSchema> load(final String databaseName, final String schemaName) {
        Collection<String> tables = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName));
        if (tables.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereSchema schema = new ShardingSphereSchema();
        tables.forEach(each -> {
            String content = repository.get(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, each));
            TableMetaData tableMetaData = new TableMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(content, YamlTableMetaData.class));
            schema.getTables().put(each, tableMetaData);
        });
        return Optional.of(schema);
    }
    
    /**
     * Load all database names.
     *
     * @return all database names
     */
    public Collection<String> loadAllDatabaseNames() {
        return repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataNodePath());
    }
}
