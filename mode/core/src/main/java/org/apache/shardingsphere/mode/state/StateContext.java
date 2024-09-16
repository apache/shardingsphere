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

import org.apache.shardingsphere.infra.state.cluster.ClusterState;

import java.util.concurrent.atomic.AtomicReference;

/**
 * State context.
 */
public final class StateContext {
    
    private final AtomicReference<ClusterState> clusterState;
    
    public StateContext(final ClusterState clusterState) {
        this.clusterState = new AtomicReference<>(clusterState);
    }
    
    /**
     * Get cluster state.
     *
     * @return cluster state
     */
    public ClusterState getClusterState() {
        return clusterState.get();
    }
    
    /**
     * Switch cluster state.
     *
     * @param state to be switched cluster state
     */
    public void switchClusterState(final ClusterState state) {
        clusterState.set(state);
    }
}
