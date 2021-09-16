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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.ComputeNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;

import java.util.Optional;

/**
 * Status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatusNode {
    
    private static final String ROOT_NODE = "status";
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    private static final String STORAGE_NODE = "storage_nodes";
    
    private static final String PRIVILEGE_NODE = "privilegenode";
    
    /**
     * Get compute node path.
     *
     * @param status compute node status
     * @return compute node path
     */
    public static String getComputeNodePath(final ComputeNodeStatus status) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, status.name().toLowerCase());
    }
    
    /**
     * Get compute node path.
     *
     * @param status compute node status
     * @param instanceId instance id
     * @return compute node path
     */
    public static String getComputeNodePath(final ComputeNodeStatus status, final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, status.name().toLowerCase(), instanceId);
    }
    
    /**
     * Get storage node path.
     *
     * @param status storage node status
     * @return storage node path
     */
    public static String getStorageNodePath(final StorageNodeStatus status) {
        return String.join("/", "", ROOT_NODE, STORAGE_NODE, status.name().toLowerCase());
    }
    
    /**
     * Get storage node path.
     *
     * @param status storage node status
     * @param schema cluster schema
     * @return storage node path
     */
    public static String getStorageNodePath(final StorageNodeStatus status, final ClusterSchema schema) {
        return String.join("/", "", ROOT_NODE, STORAGE_NODE, status.name().toLowerCase(), schema.toString());
    }
    
    /**
     * Find cluster schema.
     *
     * @param status storage node status
     * @param storageNodeFullPath storage node full path
     * @return found cluster schema
     */
    public static Optional<ClusterSchema> findClusterSchema(final StorageNodeStatus status, final String storageNodeFullPath) {
        String prefix = String.join("/", "", ROOT_NODE, STORAGE_NODE, status.name().toLowerCase());
        return storageNodeFullPath.startsWith(prefix) ? Optional.of(new ClusterSchema(storageNodeFullPath.substring(storageNodeFullPath.lastIndexOf("/") + 1))) : Optional.empty();
    }
    
    /**
     * Get privilege node path.
     *
     * @return privilege node path
     */
    public static String getPrivilegeNodePath() {
        return String.join("/", "", ROOT_NODE, PRIVILEGE_NODE);
    }
}
