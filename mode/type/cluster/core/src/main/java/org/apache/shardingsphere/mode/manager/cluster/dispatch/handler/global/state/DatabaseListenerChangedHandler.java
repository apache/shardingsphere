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

package org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.state;

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.handler.global.GlobalDataChangedEventHandler;
import org.apache.shardingsphere.mode.manager.cluster.dispatch.listener.type.DatabaseMetaDataChangedListener;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.manager.cluster.statistics.StatisticsRefreshEngine;
import org.apache.shardingsphere.mode.node.path.NodePath;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.engine.searcher.NodePathSearcher;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.DatabaseMetaDataNodePath;
import org.apache.shardingsphere.mode.node.path.type.global.state.coordinator.database.DatabaseListenerCoordinatorNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Arrays;
import java.util.Collection;

/**
 * Database listener changed handler.
 */
public final class DatabaseListenerChangedHandler implements GlobalDataChangedEventHandler {
    
    @Override
    public NodePath getSubscribedNodePath() {
        return new DatabaseListenerCoordinatorNodePath(null);
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
        String databasePath = NodePathGenerator.toPath(new DatabaseMetaDataNodePath(databaseName));
        switch (type) {
            case CREATE:
                repository.watch(databasePath, new DatabaseMetaDataChangedListener(contextManager));
                contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().addDatabase(databaseName);
                break;
            case DROP:
                repository.removeDataListener(databasePath);
                contextManager.getMetaDataContextManager().getDatabaseMetaDataManager().dropDatabase(databaseName);
                break;
            default:
                break;
        }
        new ClusterDatabaseListenerPersistCoordinator(repository).delete(databaseName);
        if (InstanceType.PROXY == contextManager.getComputeNodeInstanceContext().getInstance().getMetaData().getType()) {
            new StatisticsRefreshEngine(contextManager).asyncRefresh();
        }
    }
}
