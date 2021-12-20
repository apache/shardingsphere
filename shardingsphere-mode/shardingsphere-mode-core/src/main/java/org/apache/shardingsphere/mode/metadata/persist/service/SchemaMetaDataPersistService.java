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
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
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
     * @param schemaName schema name to be persisted
     * @param schema schema to be persisted
     */
    public void persist(final String schemaName, final ShardingSphereSchema schema) {
        if (null != schema) {
            Optional<ShardingSphereSchema> originalSchema = load(schemaName);
            if (originalSchema.isPresent()) {
                originalSchema.get().getTables().entrySet().stream().filter(entry -> !schema.getTables().keySet().contains(entry.getKey()))
                        .forEach(entry -> repository.delete(SchemaMetaDataNode.getTableMetaDataPath(schemaName, entry.getKey())));
            }
            schema.getTables().entrySet().forEach(entry -> repository.persist(SchemaMetaDataNode.getTableMetaDataPath(schemaName, entry.getKey()), 
                    YamlEngine.marshal(new TableMetaDataYamlSwapper().swapToYamlConfiguration(entry.getValue()))));
        }
    }
    
    /**
     * Persist schema tables.
     * 
     * @param schemaName schema name
     */
    public void persist(final String schemaName) {
        repository.persist(SchemaMetaDataNode.getMetaDataTablesPath(schemaName), "");
    }
    
    /**
     * Delete schema.
     *
     * @param schemaName schema name to be deleted
     */
    public void delete(final String schemaName) {
        repository.delete(SchemaMetaDataNode.getSchemaNamePath(schemaName));
    }
    
    /**
     * Load schema.
     *
     * @param schemaName schema name to be loaded
     * @return Loaded schema
     */
    public Optional<ShardingSphereSchema> load(final String schemaName) {
        Collection<String> tables = repository.getChildrenKeys(SchemaMetaDataNode.getMetaDataTablesPath(schemaName));
        if (tables.isEmpty()) {
            return Optional.empty();
        }
        ShardingSphereSchema schema = new ShardingSphereSchema();
        tables.forEach(each -> {
            String content = repository.get(SchemaMetaDataNode.getTableMetaDataPath(schemaName, each));
            TableMetaData tableMetaData = new TableMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(content, YamlTableMetaData.class));
            schema.getTables().put(each, tableMetaData);
        });
        return Optional.of(schema);
    }
    
    /**
     * Load all schema names.
     *
     * @return all schema names
     */
    public Collection<String> loadAllNames() {
        return repository.getChildrenKeys(SchemaMetaDataNode.getMetaDataNodePath());
    }
}
