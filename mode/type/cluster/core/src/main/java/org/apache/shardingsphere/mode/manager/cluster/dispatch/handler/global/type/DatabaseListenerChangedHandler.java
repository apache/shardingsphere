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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.type;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.DatabaseMetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.metadata.refresher.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.metadata.database.TableMetadataNodePath;
import org.apache.shardingsphere.mode.node.path.type.state.DatabaseListenerCoordinatorNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Arrays;
import java.util.Collection;

/**
 * Database listener changed handler.
 */
public final class DatabaseListenerChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public String getSubscribedKey() {
        return NodePathGenerator.toPath(new DatabaseListenerCoordinatorNodePath(null), false);
    }
    
    @Override
    public Collection<Type> getSubscribedTypes() {
        return Arrays.asList(Type.ADDED, Type.UPDATED);
    }
    
    @Override
    public void handle(final ContextManager contextManager, final DataChangedEvent event) {
        NodePathSearcher.find(event.getKey(), DatabaseListenerCoordinatorNodePath.createDatabaseSearchCriteria())
                .ifPresent(optional -> handle(contextManager, optional, ClusterDatabaseListenerCoordinatorType.valueOf(event.getValue())));
    }
    
    private void handle(final ContextManager contextManager, final String databaseName, final ClusterDatabaseListenerCoordinatorType type) {
        ClusterPersistRepository repository = (ClusterPersistRepository) contextManager.getPersistServiceFacade().getRepository();
        if (ClusterDatabaseListenerCoordinatorType.CREATE == type) {
            repository.watch(NodePathGenerator.toPath(new TableMetadataNodePath(databaseName, null, null), true), new DatabaseMetaDataChangedListener(contextManager));
            contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().addDatabase(databaseName);
        } else if (ClusterDatabaseListenerCoordinatorType.DROP == type) {
            repository.removeDataListener(NodePathGenerator.toPath(new TableMetadataNodePath(databaseName, null, null), true));
            contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropDatabase(databaseName);
        }
        new ClusterDatabaseListenerPersistCoordinator(repository).delete(databaseName);
        if (InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new StatisticsRefreshEngine(contextManager).asyncRefresh();
        }
    }
}
