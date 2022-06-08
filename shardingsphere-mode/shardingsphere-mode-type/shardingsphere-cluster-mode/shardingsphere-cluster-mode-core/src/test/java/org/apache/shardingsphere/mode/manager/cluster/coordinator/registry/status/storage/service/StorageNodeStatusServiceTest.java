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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.service;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class StorageNodeStatusServiceTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    @Test
    public void assertLoadDisabledDataSources() {
        List<String> disabledDataSources = Arrays.asList("replica_query_db.readwrite_ds.replica_ds_0", "other_schema.other_ds.other_ds0");
        when(repository.getChildrenKeys(anyString())).thenReturn(disabledDataSources);
        new StorageNodeStatusService(repository).loadStorageNodes();
    }
}
