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

package org.apache.shardingsphere.governance.core.registry.service.schema;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataCreatedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDroppedEvent;
import org.apache.shardingsphere.governance.core.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.schema.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Schema registry service.
 */
public final class SchemaRegistryService {
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node;

    public SchemaRegistryService(final RegistryCenterRepository repository) {
        this.repository = repository;
        node = new RegistryCenterNode();
        ShardingSphereEventBus.getInstance().register(this);
    }

    /**
     * Persist schema.
     *
     * @param schemaName schema name to be persisted
     * @param schema schema to be persisted
     */
    public void persist(final String schemaName, final ShardingSphereSchema schema) {
        repository.persist(node.getMetadataSchemaPath(schemaName), YamlEngine.marshal(new SchemaYamlSwapper().swapToYamlConfiguration(schema)));
    }
    
    /**
     * Delete schema.
     *
     * @param schemaName schema name to be deleted
     */
    public void delete(final String schemaName) {
        repository.delete(node.getSchemaNamePath(schemaName));
    }
    
    /**
     * Load schema.
     *
     * @param schemaName schema name to be loaded
     * @return Loaded schema
     */
    public Optional<ShardingSphereSchema> load(final String schemaName) {
        String path = repository.get(node.getMetadataSchemaPath(schemaName));
        return Strings.isNullOrEmpty(path) ? Optional.empty() : Optional.of(new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(path, YamlSchema.class)));
    }
    
    /**
     * Load all schema names.
     *
     * @return all schema names
     */
    public Collection<String> loadAllNames() {
        String schemaNames = repository.get(node.getMetadataNodePath());
        return Strings.isNullOrEmpty(schemaNames) ? new LinkedList<>() : node.splitSchemaName(schemaNames);
    }
    
    /**
     * Update when meta data created.
     *
     * @param event meta data created event
     */
    @Subscribe
    public void update(final MetaDataCreatedEvent event) {
        String schemaNames = repository.get(node.getMetadataNodePath());
        Collection<String> schemas = Strings.isNullOrEmpty(schemaNames) ? new LinkedHashSet<>() : new LinkedHashSet<>(Splitter.on(",").splitToList(schemaNames));
        if (!schemas.contains(event.getSchemaName())) {
            schemas.add(event.getSchemaName());
            repository.persist(node.getMetadataNodePath(), Joiner.on(",").join(schemas));
        }
    }
    
    /**
     * Update when meta data altered.
     *
     * @param event schema altered event
     */
    @Subscribe
    public void update(final SchemaAlteredEvent event) {
        persist(event.getSchemaName(), event.getSchema());
    }
    
    /**
     * Update when meta data dropped.
     *
     * @param event meta data dropped event
     */
    @Subscribe
    public void update(final MetaDataDroppedEvent event) {
        String schemaNames = repository.get(node.getMetadataNodePath());
        Collection<String> schemas = Strings.isNullOrEmpty(schemaNames) ? new LinkedHashSet<>() : new LinkedHashSet<>(Splitter.on(",").splitToList(schemaNames));
        if (schemas.contains(event.getSchemaName())) {
            schemas.remove(event.getSchemaName());
            repository.persist(node.getMetadataNodePath(), Joiner.on(",").join(schemas));
        }
    }
}
