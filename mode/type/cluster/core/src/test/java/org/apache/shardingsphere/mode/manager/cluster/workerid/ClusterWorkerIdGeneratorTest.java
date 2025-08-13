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

package org.apache.shardingsphere.mode.manager.cluster.workerid;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdAssignedException;
import org.apache.shardingsphere.infra.instance.workerid.WorkerIdGenerator;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.cluster.persist.service.ClusterComputeNodePersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClusterWorkerIdGeneratorTest {
    
    private ClusterWorkerIdGenerator workerIdGenerator;
    
    @Mock
    private ClusterComputeNodePersistService computeNodePersistService;
    
    @Mock
    private ReservationPersistService reservationPersistService;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setUp() {
        workerIdGenerator = new ClusterWorkerIdGenerator(mock(ClusterPersistRepository.class), "foo_id");
        Plugins.getMemberAccessor().set(ClusterWorkerIdGenerator.class.getDeclaredField("computeNodePersistService"), workerIdGenerator, computeNodePersistService);
        Plugins.getMemberAccessor().set(ClusterWorkerIdGenerator.class.getDeclaredField("reservationPersistService"), workerIdGenerator, reservationPersistService);
    }
    
    @Test
    void assertGenerateWithExistedWorkerId() {
        when(computeNodePersistService.loadWorkerId("foo_id")).thenReturn(Optional.of(10));
        assertThat(workerIdGenerator.generate(new Properties()), is(10));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithoutExistedWorkerId() {
        when(computeNodePersistService.getAssignedWorkerIds()).thenReturn(Collections.singleton(0));
        when(reservationPersistService.reserveWorkerId(1, "foo_id")).thenReturn(Optional.empty(), Optional.of(1));
        assertThat(workerIdGenerator.generate(new Properties()), is(1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithoutExistedWorkerIdFailed() {
        Collection<Integer> mockedAssignedWorkerIds = mock(Collection.class);
        when(mockedAssignedWorkerIds.size()).thenReturn(Integer.MAX_VALUE);
        when(computeNodePersistService.getAssignedWorkerIds()).thenReturn(mockedAssignedWorkerIds);
        assertThrows(WorkerIdAssignedException.class, () -> workerIdGenerator.generate(new Properties()));
    }
    
    @Test
    void assertGenerateWorkerIdWithWarnLog() {
        when(computeNodePersistService.loadWorkerId("foo_id")).thenReturn(Optional.of(10));
        assertThat(workerIdGenerator.generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "100"))), is(10));
        assertThat(workerIdGenerator.generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "100"))), is(10));
    }
}
