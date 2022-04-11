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

import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.storage.StorageNodeDataSource;
import org.apache.shardingsphere.infra.storage.StorageNodeRole;
import org.apache.shardingsphere.infra.storage.StorageNodeStatus;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node.StorageStatusNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StorageNodeStatusSubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    public void assertUpdateDataSourceDisabledState() {
        String schemaName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.DISABLED);
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(schemaName, groupName, dataSourceName, storageNodeDataSource);
        new StorageNodeStatusSubscriber(repository).update(dataSourceDisabledEvent);
        verify(repository).persist(StorageStatusNode.getStatusPath(new QualifiedSchema(schemaName, groupName, dataSourceName)), YamlEngine.marshal(storageNodeDataSource));
    }
    
    @Test
    public void assertUpdateDataSourceEnabledState() {
        String schemaName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        StorageNodeDataSource storageNodeDataSource = new StorageNodeDataSource(StorageNodeRole.MEMBER, StorageNodeStatus.ENABLED);
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(schemaName, groupName, dataSourceName, storageNodeDataSource);
        new StorageNodeStatusSubscriber(repository).update(dataSourceDisabledEvent);
        verify(repository).persist(StorageStatusNode.getStatusPath(new QualifiedSchema(schemaName, groupName, dataSourceName)), YamlEngine.marshal(storageNodeDataSource));
    }
    
    @Test
    public void assertUpdatePrimaryDataSourceState() {
        String schemaName = "replica_query_db";
        String groupName = "readwrite_ds";
        String dataSourceName = "replica_ds_0";
        PrimaryDataSourceChangedEvent event = new PrimaryDataSourceChangedEvent(new QualifiedSchema(schemaName, groupName, dataSourceName));
        new StorageNodeStatusSubscriber(repository).update(event);
        verify(repository).persist(StorageStatusNode.getStatusPath(new QualifiedSchema(schemaName, groupName, dataSourceName)),
                YamlEngine.marshal(new StorageNodeDataSource(StorageNodeRole.PRIMARY, StorageNodeStatus.ENABLED)));
    }
}
