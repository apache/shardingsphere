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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.reservation.WorkerIDReservationNodePath;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.exception.ClusterRepositoryPersistException;

import java.util.Optional;

/**
 * Reservation persist service.
 */
@RequiredArgsConstructor
public final class ReservationPersistService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Reserve worker ID.
     *
     * @param preselectedWorkerId preselected worker ID
     * @param instanceId instance ID
     * @return worker ID
     */
    public Optional<Integer> reserveWorkerId(final Integer preselectedWorkerId, final String instanceId) {
        try {
            return repository.persistExclusiveEphemeral(
                    NodePathGenerator.toPath(new WorkerIDReservationNodePath(preselectedWorkerId)), instanceId) ? Optional.of(preselectedWorkerId) : Optional.empty();
        } catch (final ClusterRepositoryPersistException ignore) {
            return Optional.empty();
        }
    }
}
