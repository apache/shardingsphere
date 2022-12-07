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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AddSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.AlterSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.DropIndexEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.DropSchemaEvent;
import org.apache.shardingsphere.infra.metadata.database.schema.event.SchemaAlteredEvent;
import org.apache.shardingsphere.mode.metadata.persist.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Schema meta data registry subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class SchemaMetaDataRegistrySubscriber {
    
    private final DatabaseMetaDataPersistService persistService;
    
    public SchemaMetaDataRegistrySubscriber(final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        persistService = new DatabaseMetaDataPersistService(repository);
        eventBusContext.register(this);
    }
    
    /**
     * Update when meta data altered.
     *
     * @param event schema altered event
     */
    @Subscribe
    public void update(final SchemaAlteredEvent event) {
        String databaseName = event.getDatabaseName();
        String schemaName = event.getSchemaName();
        Map<String, ShardingSphereTable> tables = event.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = event.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        persistService.getTableMetaDataPersistService().persist(databaseName, schemaName, tables);
        persistService.getViewMetaDataPersistService().persist(databaseName, schemaName, views);
        event.getDroppedTables().forEach(each -> persistService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        event.getDroppedViews().forEach(each -> persistService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    /**
     * Add schema.
     *
     * @param event schema add event
     */
    @Subscribe
    public void addSchema(final AddSchemaEvent event) {
        persistService.addSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Alter schema.
     *
     * @param event schema alter event
     */
    @Subscribe
    public void alterSchema(final AlterSchemaEvent event) {
        persistService.persist(event.getDatabaseName(), event.getRenameSchemaName(), event.getSchema());
        persistService.getViewMetaDataPersistService().persist(event.getDatabaseName(), event.getRenameSchemaName(), event.getSchema().getViews());
        persistService.dropSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Drop schema.
     *
     * @param event schema drop event
     */
    @Subscribe
    public void dropSchema(final DropSchemaEvent event) {
        event.getSchemaNames().forEach(each -> persistService.dropSchema(event.getDatabaseName(), each));
    }
    
    /**
     * Drop index.
     * @param event drop index event
     */
    @Subscribe
    public void dropIndex(final DropIndexEvent event) {
        event.getSchemaAlteredEvents().forEach(this::update);
    }
}
