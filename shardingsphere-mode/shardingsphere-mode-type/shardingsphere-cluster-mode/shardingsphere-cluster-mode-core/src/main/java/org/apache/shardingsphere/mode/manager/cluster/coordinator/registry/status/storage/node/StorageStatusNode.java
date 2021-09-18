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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.StatusNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageStatusNode {
    
    private static final String STORAGE_NODE = "storage_nodes";
    
    /**
     * Get storage node root path.
     *
     * @return root path of storage node
     */
    public static String getRootPath() {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODE);
    }
    
    /**
     * Get storage node status path.
     *
     * @param status storage node status
     * @return status path of storage node
     */
    public static String getStatusPath(final StorageNodeStatus status) {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODE, status.name().toLowerCase());
    }
    
    /**
     * Get storage node status path.
     *
     * @param status storage node status
     * @param schema cluster schema
     * @return status path of storage node
     */
    public static String getStatusPath(final StorageNodeStatus status, final ClusterSchema schema) {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODE, status.name().toLowerCase(), schema.toString());
    }
    
    /**
     * Extract cluster schema.
     *
     * @param status storage node status
     * @param storageNodePath storage node path
     * @return extracted cluster schema
     */
    public static Optional<ClusterSchema> extractClusterSchema(final StorageNodeStatus status, final String storageNodePath) {
        Pattern pattern = Pattern.compile(getRootPath() + "/" + status.name().toLowerCase() + "/(\\S+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(storageNodePath);
        return matcher.find() ? Optional.of(new ClusterSchema(matcher.group(1))) : Optional.empty();
    }
}
