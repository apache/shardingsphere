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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.type;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.table.TableCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.table.TableDroppedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.view.ViewCreatedOrAlteredEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.event.metadata.schema.view.ViewDroppedEvent;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.subscriber.DispatchEventSubscriber;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;

/**
 * Meta data changed subscriber.
 */
@RequiredArgsConstructor
public final class MetaDataChangedSubscriber implements DispatchEventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().addSchema(event.getDatabaseName(), event.getSchemaName());
        refreshStatisticsData();
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().dropSchema(event.getDatabaseName(), event.getSchemaName());
        refreshStatisticsData();
    }
    
    /**
     * Renew table.
     *
     * @param event create or alter table event
     */
    @Subscribe
    public synchronized void renew(final TableCreatedOrAlteredEvent event) {
        Preconditions.checkArgument(event.getActiveVersion().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey())),
                "Invalid active version: %s of key: %s", event.getActiveVersion(), event.getActiveVersionKey());
        ShardingSphereTable table = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getTable()
                .load(event.getDatabaseName(), event.getSchemaName(), event.getTableName());
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), table, null);
        refreshStatisticsData();
    }
    
    /**
     * Renew table.
     *
     * @param event drop table event
     */
    @Subscribe
    public synchronized void renew(final TableDroppedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), null);
        refreshStatisticsData();
    }
    
    /**
     * Renew view.
     *
     * @param event create or alter view event
     */
    @Subscribe
    public synchronized void renew(final ViewCreatedOrAlteredEvent event) {
        Preconditions.checkArgument(event.getActiveVersion().equals(
                contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().getActiveVersionByFullPath(event.getActiveVersionKey())),
                "Invalid active version: %s of key: %s", event.getActiveVersion(), event.getActiveVersionKey());
        ShardingSphereView view = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataFacade().getView()
                .load(event.getDatabaseName(), event.getSchemaName(), event.getViewName());
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, view);
        refreshStatisticsData();
    }
    
    /**
     * Renew view.
     *
     * @param event drop view event
     */
    @Subscribe
    public synchronized void renew(final ViewDroppedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getViewName());
        refreshStatisticsData();
    }
    
    private void refreshStatisticsData() {
        if (contextManager.getComputeNodeInstanceContext().getModeConfiguration().isCluster()
                && InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new ShardingSphereStatisticsRefreshEngine(contextManager).asyncRefresh();
        }
    }
}
