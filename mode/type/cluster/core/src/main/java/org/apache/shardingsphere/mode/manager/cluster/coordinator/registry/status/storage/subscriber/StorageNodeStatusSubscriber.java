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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.subscriber;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageNode;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Storage node status subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
public final class StorageNodeStatusSubscriber {
    
    private final ClusterPersistRepository repository;
    
    public StorageNodeStatusSubscriber(final ClusterPersistRepository repository, final EventBusContext eventBusContext) {
        this.repository = repository;
        eventBusContext.register(this);
    }
    
    /**
     * Update data source disabled state.
     *
     * @param event data source disabled event
     */
    @Subscribe
    public void update(final DataSourceDisabledEvent event) {
        repository.persist(StorageNode.getStatusPath(new QualifiedDatabase(event.getDatabaseName(), event.getGroupName(), event.getDataSourceName())),
                YamlEngine.marshal(event.getStorageNodeDataSource()));
    }
    
    /**
     * Update primary data source state.
     *
     * @param event primary data source event
     */
    @Subscribe
    public void update(final PrimaryDataSourceChangedEvent event) {
        repository.persist(StorageNode.getStatusPath(event.getQualifiedDatabase()), YamlEngine.marshal(new StorageNodeDataSource(StorageNodeRole.PRIMARY, StorageNodeStatus.ENABLED)));
    }
}
