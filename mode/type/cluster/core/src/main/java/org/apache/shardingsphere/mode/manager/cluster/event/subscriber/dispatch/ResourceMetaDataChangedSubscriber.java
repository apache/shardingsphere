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
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaAddedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.CreateOrAlterTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.table.DropTableEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.CreateOrAlterViewEvent;
import org.apache.shardingsphere.mode.event.dispatch.metadata.schema.view.DropViewEvent;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Map;

/**
 * Resource meta data changed subscriber.
 */
@RequiredArgsConstructor
@SuppressWarnings("unused")
public final class ResourceMetaDataChangedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    /**
     * Renew to added schema.
     *
     * @param event schema added event
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().addSchema(event.getDatabaseName(), event.getSchemaName());
        refreshShardingSphereStatisticsData();
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().dropSchema(event.getDatabaseName(), event.getSchemaName());
        refreshShardingSphereStatisticsData();
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
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(),
                tables.values().iterator().next(), null);
        refreshShardingSphereStatisticsData();
    }
    
    /**
     * Renew table.
     *
     * @param event drop table event
     */
    @Subscribe
    public synchronized void renew(final DropTableEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), event.getTableName(), null);
        refreshShardingSphereStatisticsData();
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
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(),
                null, views.values().iterator().next());
        refreshShardingSphereStatisticsData();
    }
    
    /**
     * Renew view.
     *
     * @param event drop view event
     */
    @Subscribe
    public synchronized void renew(final DropViewEvent event) {
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().alterSchema(event.getDatabaseName(), event.getSchemaName(), null, event.getViewName());
        refreshShardingSphereStatisticsData();
    }
    
    private void refreshShardingSphereStatisticsData() {
        if (contextManager.getComputeNodeInstanceContext().isCluster() && InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new ShardingSphereStatisticsRefreshEngine(contextManager,
                    new GlobalLockContext(new GlobalLockPersistService((ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository()))).asyncRefresh();
        }
    }
}
