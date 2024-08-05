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
import org.apache.shardingsphere.infra.util.eventbus.EventSubscriber;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.mode.event.dispatch.assisted.CreateDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.event.dispatch.assisted.DropDatabaseListenerAssistedEvent;
import org.apache.shardingsphere.mode.lock.GlobalLockContext;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.listener.DataChangedEventListenerManager;
import org.apache.shardingsphere.mode.manager.cluster.listener.MetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.lock.GlobalLockPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Listener assisted subscriber.
 */
@RequiredArgsConstructor
public final class ListenerAssistedSubscriber implements EventSubscriber {
    
    private final ContextManager contextManager;
    
    private final DataChangedEventListenerManager listenerManager;
    
    public ListenerAssistedSubscriber(final ContextManager contextManager) {
        this.contextManager = contextManager;
        listenerManager = new DataChangedEventListenerManager((ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository());
    }
    
    /**
     * Renew to persist meta data.
     *
     * @param event database added event
     */
    @Subscribe
    public synchronized void renew(final CreateDatabaseListenerAssistedEvent event) {
        listenerManager.addListener(DatabaseMetaDataNode.getDatabaseNamePath(event.getDatabaseName()),
                new MetaDataChangedListener(contextManager.getComputeNodeInstanceContext().getEventBusContext()));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().addDatabase(event.getDatabaseName());
        contextManager.getPersistServiceFacade().getListenerAssistedPersistService().deleteDatabaseNameListenerAssisted(event.getDatabaseName());
        refreshShardingSphereStatisticsData();
    }
    
    /**
     * Renew to delete database.
     *
     * @param event database delete event
     */
    @Subscribe
    public synchronized void renew(final DropDatabaseListenerAssistedEvent event) {
        listenerManager.removeListener(DatabaseMetaDataNode.getDatabaseNamePath(event.getDatabaseName()));
        contextManager.getMetaDataContextManager().getSchemaMetaDataManager().dropDatabase(event.getDatabaseName());
        contextManager.getPersistServiceFacade().getListenerAssistedPersistService().deleteDatabaseNameListenerAssisted(event.getDatabaseName());
        refreshShardingSphereStatisticsData();
    }
    
    private void refreshShardingSphereStatisticsData() {
        if (contextManager.getComputeNodeInstanceContext().isCluster() && InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new ShardingSphereStatisticsRefreshEngine(contextManager,
                    new GlobalLockContext(new GlobalLockPersistService((ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository()))).asyncRefresh();
        }
    }
}
