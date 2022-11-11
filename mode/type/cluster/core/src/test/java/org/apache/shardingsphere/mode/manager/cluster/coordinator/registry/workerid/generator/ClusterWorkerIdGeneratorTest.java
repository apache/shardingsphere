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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.workerid.generator;

import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ClusterWorkerIdGeneratorTest {
    
    @Test
    public void assertGenerateWithExistedWorkerId() {
        Properties props = new Properties();
        props.setProperty(WorkerIdGenerator.WORKER_ID_KEY, "1");
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_id");
        RegistryCenter registryCenter = mock(RegistryCenter.class, RETURNS_DEEP_STUBS);
        when(registryCenter.getComputeNodeStatusService().loadInstanceWorkerId("foo_id")).thenReturn(Optional.of(10));
        assertThat(new ClusterWorkerIdGenerator(registryCenter, instanceMetaData).generate(props), is(10));
    }
    
    @Test
    public void assertGenerateWithoutExistedWorkerId() {
        InstanceMetaData instanceMetaData = mock(InstanceMetaData.class);
        when(instanceMetaData.getId()).thenReturn("foo_id");
        RegistryCenter registryCenter = mock(RegistryCenter.class, RETURNS_DEEP_STUBS);
        ClusterPersistRepository repository = mock(ClusterPersistRepository.class);
        Mockito.doAnswer((Answer<Object>) invocation -> "foo_id").when(repository).persistEphemeral("/worker_id/0", "foo_id");
        when(registryCenter.getRepository()).thenReturn(repository);
        when(registryCenter.getComputeNodeStatusService().loadInstanceWorkerId("foo_id")).thenReturn(Optional.empty());
        when(registryCenter.getComputeNodeStatusService().getAssignedWorkerIds()).thenReturn(Collections.singleton(1));
        assertThat(new ClusterWorkerIdGenerator(registryCenter, instanceMetaData).generate(new Properties()), is(0));
    }
}
