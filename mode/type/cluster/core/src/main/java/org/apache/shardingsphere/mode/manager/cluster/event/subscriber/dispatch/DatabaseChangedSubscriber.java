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

package org.apache.shardingsphere.mode.manager.cluster.event.subscriber.dispatch;

import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.DatabaseDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.SchemaDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataChangedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.ShardingSphereRowDataDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.data.TableDataChangedEvent;

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
        contextManager.getMetaDataContextManager().getDatabaseManager().addShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to delete ShardingSphere data database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DatabaseDataDeletedEvent event) {
        contextManager.getMetaDataContextManager().getDatabaseManager().dropShardingSphereDatabaseData(event.getDatabaseName());
    }
    
    /**
     * Renew to added ShardingSphere data schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataAddedEvent event) {
        contextManager.getMetaDataContextManager().getDatabaseManager().addShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew to delete ShardingSphere data schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDataDeletedEvent event) {
        contextManager.getMetaDataContextManager().getDatabaseManager().dropShardingSphereSchemaData(event.getDatabaseName(), event.getSchemaName());
    }
    
    /**
     * Renew ShardingSphere data of the table.
     *
     * @param event table data changed event
     */
    @Subscribe
    public synchronized void renew(final TableDataChangedEvent event) {
        if (null != event.getAddedTable()) {
            contextManager.getMetaDataContextManager().getDatabaseManager().addShardingSphereTableData(event.getDatabaseName(), event.getSchemaName(), event.getAddedTable());
        }
        if (null != event.getDeletedTable()) {
            contextManager.getMetaDataContextManager().getDatabaseManager().dropShardingSphereTableData(event.getDatabaseName(), event.getSchemaName(), event.getDeletedTable());
        }
    }
    
    /**
     * Renew ShardingSphere data of row.
     *
     * @param event ShardingSphere row data added event
     */
    @Subscribe
    public synchronized void renew(final ShardingSphereRowDataChangedEvent event) {
        contextManager.getMetaDataContextManager().getDatabaseManager().alterShardingSphereRowData(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), event.getYamlRowData());
    }
    
    /**
     * Renew ShardingSphere data of row.
     *
     * @param event ShardingSphere row data deleted event
     */
    @Subscribe
    public synchronized void renew(final ShardingSphereRowDataDeletedEvent event) {
        contextManager.getMetaDataContextManager().getDatabaseManager().deleteShardingSphereRowData(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), event.getUniqueKey());
    }
}
