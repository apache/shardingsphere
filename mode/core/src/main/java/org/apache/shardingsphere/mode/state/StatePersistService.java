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

package org.apache.shardingsphere.mode.state;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.state.StateNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

/**
 * State persist service.
 */
@RequiredArgsConstructor
public final class StatePersistService {
    
    private final PersistRepository repository;
    
    /**
     * Update state.
     *
     * @param state to be updated state
     */
    public void update(final ShardingSphereState state) {
        repository.persist(NodePathGenerator.toPath(new StateNodePath()), state.name());
    }
    
    /**
     * Load state.
     *
     * @return loaded state
     */
    public ShardingSphereState load() {
        return ShardingSphereState.valueFrom(repository.query(NodePathGenerator.toPath(new StateNodePath())));
    }
}
