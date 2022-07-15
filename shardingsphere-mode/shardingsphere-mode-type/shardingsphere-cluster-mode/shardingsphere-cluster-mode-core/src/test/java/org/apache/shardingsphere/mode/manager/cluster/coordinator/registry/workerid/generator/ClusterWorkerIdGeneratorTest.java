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

import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceMetaData;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.RegistryCenter;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ClusterWorkerIdGeneratorTest {

    @Test
    public void assertGenerateWithNullProperties() {
        assertThat(new ClusterWorkerIdGenerator(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext()), mock(InstanceMetaData.class)).generate(null), is(WorkerIdGenerator.DEFAULT_WORKER_ID));
    }

    @Test
    public void assertGenerateWithEmptyProperties() {
        assertThat(new ClusterWorkerIdGenerator(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext()), mock(InstanceMetaData.class)).generate(new Properties()), is(WorkerIdGenerator.DEFAULT_WORKER_ID));
    }

    @Test
    public void assertGenerateWithProperties() {
        Properties props = new Properties();
        props.setProperty(WorkerIdGenerator.WORKER_ID_KEY, "1");
        assertThat(new ClusterWorkerIdGenerator(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext()), mock(InstanceMetaData.class)).generate(props), is(1L));
    }

    @Test(expected = IllegalStateException.class)
    public void assertGenerateWithInvalidProperties() {
        Properties props = new Properties();
        props.setProperty(WorkerIdGenerator.WORKER_ID_KEY, "1024");
        new ClusterWorkerIdGenerator(new RegistryCenter(mock(ClusterPersistRepository.class), new EventBusContext()), mock(InstanceMetaData.class)).generate(props);
    }
}
