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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.TableMetadataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.ViewMetadataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.DatabaseDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;

/**
 * Resource Metadata changed subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class ResourceMetadataChangedSubscriber {
    
    private final ContextManager contextManager;
    
    public ResourceMetadataChangedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        contextManager.getInstanceContext().getEventBusContext().register(this);
    }
    
    /**
     * Renew to persist Metadata.
     *
     * @param event database added event
     */
    @Subscribe
    public synchronized void renew(final DatabaseAddedEvent event) {
        contextManager.addDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDeletedEvent event) {
        contextManager.dropDatabase(event.getDatabaseName());
    }
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.addSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.dropSchema(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew Metadata of the table.
     *
     * @param event table Metadata changed event
     */
    @Subscribe
    public synchronized void renew(final TableMetadataChangedEvent event) {
        contextManager.alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getChangedTableMetadata(), null);
        contextManager.alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getDeletedTable(), null);
    }
    
    /**
     * Renew Metadata of the view.
     *
     * @param event view Metadata changed event
     */
    @Subscribe
    public synchronized void renew(final ViewMetadataChangedEvent event) {
        contextManager.alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getChangedViewMetadata());
        contextManager.alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getDeletedView());
    }
}
