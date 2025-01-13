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

package org.apache.shardingsphere.mode.persist.service.unified;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.state.ClusterState;
import org.apache.shardingsphere.mode.node.path.metadata.StatesNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * State persist service.
 */
@RequiredArgsConstructor
public final class StatePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Update cluster state.
     *
     * @param state to be updated cluster state
     */
    public void update(final ClusterState state) {
        repository.persist(StatesNodePath.getClusterStatePath(), state.name());
    }
    
    /**
     * Load cluster state.
     *
     * @return loaded cluster state
     */
    public ClusterState load() {
        String value = repository.query(StatesNodePath.getClusterStatePath());
        return Strings.isNullOrEmpty(value) ? ClusterState.OK : ClusterState.valueOf(value);
    }
}
