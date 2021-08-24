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

package org.apache.shardingsphere.governance.core.registry.state.subscriber;

import org.apache.shardingsphere.governance.core.registry.state.ResourceState;
import org.apache.shardingsphere.governance.core.registry.state.node.StatesNode;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class DataSourceStatusRegistrySubscriberTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    public void assertUpdateDataSourceDisabledState() {
        assertUpdateDataSourceState(true, ResourceState.DISABLED.toString());
    }
    
    @Test
    public void assertUpdateDataSourceEnabledState() {
        assertUpdateDataSourceState(false, "");
    }
    
    private void assertUpdateDataSourceState(final boolean isDisabled, final String value) {
        String schemaName = "replica_query_db";
        String dataSourceName = "replica_ds_0";
        DataSourceDisabledEvent dataSourceDisabledEvent = new DataSourceDisabledEvent(schemaName, dataSourceName, isDisabled);
        new DataSourceStatusRegistrySubscriber(repository).update(dataSourceDisabledEvent);
        verify(repository).persist(StatesNode.getDataSourcePath(schemaName, dataSourceName), value);
    }
    
    @Test
    public void assertUpdatePrimaryDataSourceState() {
        String schemaName = "replica_query_db";
        String groupName = "group1";
        String dataSourceName = "replica_ds_0";
        PrimaryDataSourceEvent primaryDataSourceEvent = new PrimaryDataSourceEvent(schemaName, groupName, dataSourceName);
        new DataSourceStatusRegistrySubscriber(repository).update(primaryDataSourceEvent);
        verify(repository).persist(StatesNode.getPrimaryDataSourcePath(schemaName, groupName), dataSourceName);
    }
}
