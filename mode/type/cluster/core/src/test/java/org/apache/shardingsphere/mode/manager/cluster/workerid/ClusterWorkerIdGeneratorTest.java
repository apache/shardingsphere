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
import org.apache.shardingsphere.mode.manager.cluster.persist.ReservationPersistService;
import org.apache.shardingsphere.mode.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
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
    
    @BeforeEach
    void setUp() {
        workerIdGenerator = new ClusterWorkerIdGenerator(mock(ClusterPersistRepository.class), "foo_id");
    }
    
    @Test
    void assertGenerateWithExistedWorkerId() {
        ComputeNodePersistService computeNodePersistService = mock(ComputeNodePersistService.class);
        when(computeNodePersistService.loadInstanceWorkerId("foo_id")).thenReturn(Optional.of(10));
        setField("computeNodePersistService", computeNodePersistService);
        assertThat(workerIdGenerator.generate(new Properties()), is(10));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithoutExistedWorkerId() {
        ComputeNodePersistService computeNodePersistService = mock(ComputeNodePersistService.class);
        when(computeNodePersistService.getAssignedWorkerIds()).thenReturn(Collections.singleton(0));
        setField("computeNodePersistService", computeNodePersistService);
        ReservationPersistService reservationPersistService = mock(ReservationPersistService.class);
        when(reservationPersistService.reserveWorkerId(1, "foo_id")).thenReturn(Optional.empty(), Optional.of(1));
        setField("reservationPersistService", reservationPersistService);
        assertThat(workerIdGenerator.generate(new Properties()), is(1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertGenerateWithoutExistedWorkerIdFailed() {
        ComputeNodePersistService computeNodePersistService = mock(ComputeNodePersistService.class);
        Collection<Integer> mockedAssignedWorkerIds = mock(Collection.class);
        when(mockedAssignedWorkerIds.size()).thenReturn(Integer.MAX_VALUE);
        when(computeNodePersistService.getAssignedWorkerIds()).thenReturn(mockedAssignedWorkerIds);
        setField("computeNodePersistService", computeNodePersistService);
        assertThrows(WorkerIdAssignedException.class, () -> workerIdGenerator.generate(new Properties()));
    }
    
    @Test
    void assertGenerateWorkerIdWithWarnLog() {
        ComputeNodePersistService computeNodePersistService = mock(ComputeNodePersistService.class);
        when(computeNodePersistService.loadInstanceWorkerId("foo_id")).thenReturn(Optional.of(10));
        setField("computeNodePersistService", computeNodePersistService);
        assertThat(workerIdGenerator.generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "100"))), is(10));
        assertThat(workerIdGenerator.generate(PropertiesBuilder.build(new Property(WorkerIdGenerator.WORKER_ID_KEY, "100"))), is(10));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setField(final String fieldName, final Object fieldValue) {
        Field field = workerIdGenerator.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(workerIdGenerator, fieldValue);
        field.setAccessible(false);
    }
}
