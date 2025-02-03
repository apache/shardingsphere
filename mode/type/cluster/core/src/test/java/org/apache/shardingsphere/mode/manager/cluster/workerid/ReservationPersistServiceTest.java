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

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationPersistServiceTest {
    
    @Mock
    private ClusterPersistRepository repository;
    
    private ReservationPersistService reservationPersistService;
    
    @BeforeEach
    void setUp() {
        reservationPersistService = new ReservationPersistService(repository);
    }
    
    @Test
    void assertReserveExistedWorkerId() {
        when(repository.persistExclusiveEphemeral("/reservation/worker_id/1", "foo_id")).thenReturn(true);
        Optional<Integer> actual = reservationPersistService.reserveWorkerId(1, "foo_id");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(1));
    }
    
    @Test
    void assertReserveNotExistedWorkerId() {
        assertFalse(reservationPersistService.reserveWorkerId(1, "foo_id").isPresent());
    }
    
    @Test
    void assertReserveWorkerIdWithClusterRepositoryPersistException() {
        when(repository.persistExclusiveEphemeral("/reservation/worker_id/1", "foo_id")).thenThrow(ClusterRepositoryPersistException.class);
        assertFalse(reservationPersistService.reserveWorkerId(1, "foo_id").isPresent());
    }
}
