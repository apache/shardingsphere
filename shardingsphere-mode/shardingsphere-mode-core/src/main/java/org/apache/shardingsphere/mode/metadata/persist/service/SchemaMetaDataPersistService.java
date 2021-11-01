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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.infra.yaml.schema.swapper.SchemaYamlSwapper;

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
        String content = null == schema ? "" : YamlEngine.marshal(new SchemaYamlSwapper().swapToYamlConfiguration(schema));
        repository.persist(SchemaMetaDataNode.getMetaDataSchemaPath(schemaName), content);
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
        String path = repository.get(SchemaMetaDataNode.getMetaDataSchemaPath(schemaName));
        return Strings.isNullOrEmpty(path) ? Optional.empty() : Optional.of(new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(path, YamlSchema.class)));
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
