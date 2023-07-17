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
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.event.schema.table.AlterTableEvent;
import org.apache.shardingsphere.mode.event.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.schema.view.AlterViewEvent;
import org.apache.shardingsphere.mode.event.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;

import java.util.Map;

/**
 * TODO Rename ResourceMetaDataChangedSubscriber when metadata structure adjustment completed. #25485
 * Resource meta data changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class NewResourceMetaDataChangedSubscriber {
    
    private final ContextManager contextManager;
    
    public NewResourceMetaDataChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Renew to persist meta data.
     *
     * @param event database added event
     */
    @Subscribe
    public synchronized void renew(final DatabaseAddedEvent event) {
        contextManager.getResourceMetaDataContextManager().addDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDeletedEvent event) {
        contextManager.getResourceMetaDataContextManager().dropDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.getResourceMetaDataContextManager().addSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.getResourceMetaDataContextManager().dropSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew table.
     *
     * @param event alter table event
     */
    @Subscribe
    public synchronized void renew(final AlterTableEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        Map<String, ShardingSphereTable> tables = contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService()
                .getTableMetaDataPersistService().load(event.getDatabaseName(), event.getSchemaName(), event.getTableName());
        contextManager.getResourceMetaDataContextManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), tables.values().iterator().next(), null);
    }
    
    /**
     * Renew table.
     *
     * @param event drop table event
     */
    @Subscribe
    public synchronized void renew(final DropTableEvent event) {
        contextManager.getResourceMetaDataContextManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), null);
    }
    
    /**
     * Renew view.
     *
     * @param event alter view event
     */
    @Subscribe
    public synchronized void renew(final AlterViewEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getMetaDataContexts().getPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey()))) {
            return;
        }
        Map<String, ShardingSphereView> views = contextManager.getMetaDataContexts().getPersistService().getDatabaseMetaDataService()
                .getViewMetaDataPersistService().load(event.getDatabaseName(), event.getSchemaName(), event.getViewName());
        contextManager.getResourceMetaDataContextManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, views.values().iterator().next());
    }
    
    /**
     * Renew view.
     *
     * @param event drop view event
     */
    @Subscribe
    public synchronized void renew(final DropViewEvent event) {
        contextManager.getResourceMetaDataContextManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getViewName());
    }
}
