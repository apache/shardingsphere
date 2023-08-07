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

import org.apache.shardingsphere.infra.state.datasource.DataSourceState;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.yaml.YamlStorageNodeDataSourceSwapper;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSource;
import org.apache.shardingsphere.mode.event.storage.StorageNodeRole;
import org.apache.shardingsphere.mode.event.storage.DataSourceDisabledEvent;
import org.apache.shardingsphere.mode.event.storage.StorageNodeDataSourceDeletedEvent;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StorageNodeStatusSubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    private final EventBusContext eventBusContext = new EventBusContext();
    
    @Test
    void assertUpdateDataSourceDisabledState() {
        String databaseName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED);
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(databaseName, groupName, dataSourceName, storageNodeDataSource);
        new StorageNodeStatusSubscriber(repository, eventBusContext).update(dataSourceDisabledEvent);
        verify(repository).persist(StorageNode.getStorageNodeDataSourcePath(new QualifiedDatabase(databaseName, groupName, dataSourceName)),
                YamlEngine.marshal(new YamlStorageNodeDataSourceSwapper().swapToYamlConfiguration(new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.DISABLED))));
    }
    
    @Test
    void assertUpdateDataSourceEnabledState() {
        String databaseName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, DataSourceState.ENABLED);
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(databaseName, groupName, dataSourceName, storageNodeDataSource);
        new StorageNodeStatusSubscriber(repository, eventBusContext).update(dataSourceDisabledEvent);
        verify(repository).persist(StorageNode.getStorageNodeDataSourcePath(new QualifiedDatabase(databaseName, groupName, dataSourceName)),
                YamlEngine.marshal(new YamlStorageNodeDataSourceSwapper().swapToYamlConfiguration(storageNodeDataSource)));
    }
    
    @Test
    void assertDeleteStorageNodeDataSourceDataSourceState() {
        String databaseName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        StorageNodeDataSourceDeletedEvent event = new StorageNodeDataSourceDeletedEvent(new QualifiedDatabase(databaseName, groupName, dataSourceName));
        new StorageNodeStatusSubscriber(repository, eventBusContext).delete(event);
        verify(repository).delete(StorageNode.getStorageNodeDataSourcePath(new QualifiedDatabase(databaseName, groupName, dataSourceName)));
    }
}
