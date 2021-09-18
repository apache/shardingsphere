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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StatusNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.ComputeNodeStatus;

/**
 * Compute status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputeStatusNode {
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    /**
     * Get compute node root path.
     *
     * @return root path of compute node
     */
    public static String getRootPath() {
        return String.join("/", "", StatusNode.ROOT_NODE, COMPUTE_NODE);
    }
    
    /**
     * Get compute node status path.
     *
     * @param status status of compute node
     * @return status path of compute node
     */
    public static String getStatusPath(final ComputeNodeStatus status) {
        return String.join("/", "", StatusNode.ROOT_NODE, COMPUTE_NODE, status.name().toLowerCase());
    }
    
    /**
     * Get compute node status path.
     *
     * @param status status of compute node
     * @param instanceId instance id
     * @return status path of compute node
     */
    public static String getStatusPath(final ComputeNodeStatus status, final String instanceId) {
        return String.join("/", "", StatusNode.ROOT_NODE, COMPUTE_NODE, status.name().toLowerCase(), instanceId);
    }
}
