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

package org.apache.shardingsphere.mode.persist.service;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.node.ComputeNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Optional;

/**
 * State persist service.
 */
@RequiredArgsConstructor
public final class StatePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Update cluster state.
     *
     * @param state cluster state
     */
    public void updateClusterState(final ClusterState state) {
        repository.persist(ComputeNode.getClusterStateNodePath(), state.name());
    }
    
    /**
     * Load cluster state.
     *
     * @return cluster state
     */
    public Optional<ClusterState> loadClusterState() {
        String value = repository.query(ComputeNode.getClusterStateNodePath());
        return Strings.isNullOrEmpty(value) ? Optional.empty() : Optional.of(ClusterState.valueOf(value));
    }
}
