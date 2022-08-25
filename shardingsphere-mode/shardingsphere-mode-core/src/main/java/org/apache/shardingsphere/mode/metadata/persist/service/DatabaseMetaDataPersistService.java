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
import org.apache.commons.lang.StringUtils;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlShardingSphereTable;
import org.apache.shardingsphere.infra.yaml.schema.swapper.YamlTableSwapper;
import org.apache.shardingsphere.mode.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database meta data registry service.
 */
@RequiredArgsConstructor
public final class DatabaseMetaDataPersistService {
    
    private final PersistRepository repository;
    
    /**
     * Compare and persist meta data.
     *
     * @param databaseName database name to be persisted
     * @param schemaName schema name to be persisted
     * @param schema schema to be persisted
     */
    public void compareAndPersistMetaData(final String databaseName, final String schemaName, final ShardingSphereSchema schema) {
        Optional<ShardingSphereSchema> originalSchema = load(databaseName, schemaName);
        if (originalSchema.isPresent()) {
            compareAndPersist(databaseName, schemaName, schema, originalSchema.get());
            return;
        }
        persistMetaData(databaseName, schemaName, schema.getTables());
    }
    
    /**
     * Persist meta data.
     *
     * @param databaseName database name to be persisted
     * @param schemaName schema name to be persisted
     * @param tables tables to be persisted
     */
    public void persistMetaData(final String databaseName, final String schemaName, final Map<String, ShardingSphereTable> tables) {
        if (tables.isEmpty()) {
            persistSchema(databaseName, schemaName);
            return;
        }
        tables.forEach((key, value) -> repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, key),
                YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(value))));
    }
    
    /**
     * Persist table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param table table meta data
     */
    public void persistTable(final String databaseName, final String schemaName, final ShardingSphereTable table) {
        repository.persist(DatabaseMetaDataNode.getTableMetaDataPath(databaseName, schemaName, table.getName().toLowerCase()),
                YamlEngine.marshal(new YamlTableSwapper().swapToYamlConfiguration(table)));
    }
    
    /**
     * Persist schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void persistSchema(final String databaseName, final String schemaName) {
        repository.persist(DatabaseMetaDataNode.getMetaDataTablesPath(databaseName, schemaName), "");
    }
    
    private void compareAndPersist(final String databaseName, final String schemaName, final ShardingSphereSchema schema, final ShardingSphereSchema originalSchema) {
        Map<String, ShardingSphereTable> cachedLocalTables = new LinkedHashMap<>(schema.getTables());
        for (Entry<String, ShardingSphereTable> entry : originalSchema.getTables().entrySet()) {
            String onlineTableName = entry.getKey();
            ShardingSphereTable localTableMetaData = cachedLocalTables.remove(onlineTableName);
            if (null == localTableMetaData) {
                deleteTable(databaseName, schemaName, onlineTableName);
                continue;
            }
            if (!localTableMetaData.equals(entry.getValue())) {
                persistTable(databaseName, schemaName, localTableMetaData);
            }
        }
        if (!cachedLocalTables.isEmpty()) {
            persistMetaData(databaseName, schemaName, cachedLocalTables);
        }
    }
    
    /**
     * Delete database.
     *
     * @param databaseName database name to be deleted
     */
    public void deleteDatabase(final String databaseName) {
        repository.delete(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
    }
    
    /**
     * Persist database name.
     * 
     * @param databaseName database name
     */
    public void persistDatabase(final String databaseName) {
        repository.persist(DatabaseMetaDataNode.getDatabaseNamePath(databaseName), "");
    }
    
    /**
     * Delete schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    public void deleteSchema(final String databaseName, final String schemaName) {
        repository.delete(DatabaseMetaDataNode.getMetaDataSchemaPath(databaseName, schemaName));
    }
    
    /**
     * Delete table meta data.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @param tableName table name
     */
    public void deleteTable(final String databaseName, final String schemaName, final String tableName) {
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
            if (!StringUtils.isEmpty(content)) {
                ShardingSphereTable table = new YamlTableSwapper().swapToObject(YamlEngine.unmarshal(content, YamlShardingSphereTable.class));
                schema.put(each, table);
            }
        });
        return Optional.of(schema);
    }
    
    /**
     * Load schemas.
     *
     * @param databaseName database name to be loaded
     * @return Loaded schemas
     */
    public Map<String, ShardingSphereSchema> load(final String databaseName) {
        Collection<String> schemas = repository.getChildrenKeys(DatabaseMetaDataNode.getMetaDataSchemasPath(databaseName));
        Map<String, ShardingSphereSchema> result = new ConcurrentHashMap<>(schemas.size(), 1);
        schemas.forEach(each -> load(databaseName, each).ifPresent(optional -> result.put(each.toLowerCase(), optional)));
        return result;
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
