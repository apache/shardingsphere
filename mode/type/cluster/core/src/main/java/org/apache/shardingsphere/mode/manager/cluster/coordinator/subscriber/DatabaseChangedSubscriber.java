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
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.data.event.TableDataChangedEvent;

/**
 * Database changed subscriber.
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class DatabaseChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew to persist ShardingSphere database data.
     *
     * @param event database data added event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDataAddedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().addShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to delete ShardingSphere data database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDataDeletedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().dropShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to added ShardingSphere data schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataAddedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().addShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete ShardingSphere data schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataDeletedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().dropShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew ShardingSphere data of the table.
     *
     * @param event table data changed event
     */
    @Subscribe
    public synchronized void renew(final TableDataChangedEvent event) {
        if (null != event.getAddedTable()) {
            contextManager.getManagerServiceFacade().getDatabaseManagerService().addShardingSphereTableData(event.getDatabaseName(), event.getSchemaName(), event.getAddedTable());
        }
        if (null != event.getDeletedTable()) {
            contextManager.getManagerServiceFacade().getDatabaseManagerService().dropShardingSphereTableData(event.getDatabaseName(), event.getSchemaName(), event.getDeletedTable());
        }
    }
    
    /**
     * Renew ShardingSphere data of row.
     *
     * @param event ShardingSphere row data added event
     */
    @Subscribe
    public synchronized void renew(final ShardingSphereRowDataChangedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().alterShardingSphereRowData(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), event.getYamlRowData());
    }
    
    /**
     * Renew ShardingSphere data of row.
     *
     * @param event ShardingSphere row data deleted event
     */
    @Subscribe
    public synchronized void renew(final ShardingSphereRowDataDeletedEvent event) {
        contextManager.getManagerServiceFacade().getDatabaseManagerService().deleteShardingSphereRowData(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), event.getUniqueKey());
    }
}
