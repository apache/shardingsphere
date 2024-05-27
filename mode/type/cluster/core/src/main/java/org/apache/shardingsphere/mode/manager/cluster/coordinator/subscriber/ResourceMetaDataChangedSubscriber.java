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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.event.schema.table.CreateOrAlterTableEvent;
import org.apache.shardingsphere.mode.event.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.schema.view.CreateOrAlterViewEvent;
import org.apache.shardingsphere.mode.event.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;

import java.util.Map;

/**
 * Resource meta data changed subscriber.
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class ResourceMetaDataChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew to persist meta data.
     *
     * @param event database added event
     */
    @Subscribe
    public synchronized void renew(final DatabaseAddedEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().addDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDeletedEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().dropDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().addSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().dropSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew table.
     *
     * @param event create or alter table event
     */
    @Subscribe
    public synchronized void renew(final CreateOrAlterTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        Map<String, ShardingSphereTable> tables = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService()
                .getTableMetaDataPersistService().load(event.getDatabaseName(), event.getSchemaName(), event.getTableName());
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().alterSchema(event.getDatabaseName(), event.getSchemaName(),
                tables.values().iterator().next(), null);
    }
    
    /**
     * Renew table.
     *
     * @param event drop table event
     */
    @Subscribe
    public synchronized void renew(final DropTableEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), null);
    }
    
    /**
     * Renew view.
     *
     * @param event create or alter view event
     */
    @Subscribe
    public synchronized void renew(final CreateOrAlterViewEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        Map<String, ShardingSphereView> views = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService()
                .getViewMetaDataPersistService().load(event.getDatabaseName(), event.getSchemaName(), event.getViewName());
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().alterSchema(event.getDatabaseName(), event.getSchemaName(),
                null, views.values().iterator().next());
    }
    
    /**
     * Renew view.
     *
     * @param event drop view event
     */
    @Subscribe
    public synchronized void renew(final DropViewEvent event) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getViewName());
    }
}
