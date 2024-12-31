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

package org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.type;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.node.StatesNode;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.handler.DataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.event.dispatch.listener.type.DatabaseMetaDataChangedListener;
import org.apache.shardingsphere.mode.metadata.refresher.ShardingSphereStatisticsRefreshEngine;
import org.apache.shardingsphere.mode.persist.service.unified.ListenerAssistedType;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Arrays;
import java.util.Collection;

/**
 * Listener assisted changed handler.
 */
public final class ListenerAssistedChangedHandler implements DataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return StatesNode.getListenerAssistedNodePath();
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        StatesNode.getDatabaseNameByListenerAssistedNodePath(event.getKey()).ifPresent(optional -> handle(contextManager, optional, ListenerAssistedType.valueOf(event.getValue())));
    }
    
    private static void handle(final ContextManager contextManager, final String databaseName, final ListenerAssistedType listenerAssistedType) {
        ClusterPersistRepository repository = (ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository();
        if (ListenerAssistedType.CREATE_DATABASE == listenerAssistedType) {
            repository.watch(DatabaseMetaDataNode.getDatabaseNamePath(databaseName), new DatabaseMetaDataChangedListener(contextManager.getComputeNodeInstanceContext().getEventBusContext()));
            contextManager.getMetaDataContextManager().getSchemaMetaDataManager().addDatabase(databaseName);
        } else if (ListenerAssistedType.DROP_DATABASE == listenerAssistedType) {
            repository.removeDataListener(DatabaseMetaDataNode.getDatabaseNamePath(databaseName));
            contextManager.getMetaDataContextManager().getSchemaMetaDataManager().dropDatabase(databaseName);
        }
        contextManager.getPersistServiceFacade().getListenerAssistedPersistService().deleteDatabaseNameListenerAssisted(databaseName);
        if (InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new ShardingSphereStatisticsRefreshEngine(contextManager).asyncRefresh();
        }
    }
}
