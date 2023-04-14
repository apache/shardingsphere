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

package org.apache.shardingsphere.infra.state.cluster;

import lombok.Getter;

/**
 * Cluster state context.
 */
public final class ClusterStateContext {
    
    @Getter
    private ClusterState currentState = ClusterState.OK;
    
    /**
     * Switch state.
     * 
     * @param state state
     * @throws IllegalStateException illegal state exception
     */
    public void switchState(final ClusterState state) {
        if (currentState == state) {
            return;
        }
        if (ClusterState.OK != currentState && ClusterState.OK != state) {
            throw new IllegalStateException("Cluster is locked");
        }
        currentState = state;
    }
}
