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
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Storage node status subscriber.
 */
public final class StorageNodeStatusSubscriber {
    
    private final ClusterPersistRepository repository;
    
    public StorageNodeStatusSubscriber(final ClusterPersistRepository repository) {
        this.repository = repository;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Update data source disabled state.
     *
     * @param event data source disabled event
     */
    @Subscribe
    public void update(final DataSourceDisabledEvent event) {
        if (event.isDisabled()) {
            repository.persist(StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE, new ClusterSchema(event.getSchemaName(), event.getDataSourceName())), "");
        } else {
            repository.delete(StorageStatusNode.getStatusPath(StorageNodeStatus.DISABLE, new ClusterSchema(event.getSchemaName(), event.getDataSourceName())));
        }
    }
    
    /**
     * Update primary data source state.
     *
     * @param event primary data source event
     */
    @Subscribe
    public void update(final PrimaryDataSourceChangedEvent event) {
        repository.persist(StorageStatusNode.getStatusPath(StorageNodeStatus.PRIMARY, new ClusterSchema(event.getSchemaName(), event.getGroupName())), event.getDataSourceName());
    }
}
