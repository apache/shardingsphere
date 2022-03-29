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
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StorageStatusNode {
    
    private static final String STORAGE_NODES = "storage_nodes";
    
    private static final String ATTRIBUTES_NODE = "attributes";
    
    /**
     * Get storage node root path.
     *
     * @return root path of storage node
     */
    public static String getRootPath() {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODES, ATTRIBUTES_NODE);
    }
    
    /**
     * Get storage node data source path.
     *
     * @param dataSourcePath data source path
     * @return data source path of storage node
     */
    public static String getStorageNodesDataSourcePath(final String dataSourcePath) {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODES, ATTRIBUTES_NODE, dataSourcePath);
    }
    
    /**
     * Get storage node status path.
     *
     * @param schema cluster schema
     * @return status path of storage node
     */
    public static String getStatusPath(final QualifiedSchema schema) {
        return String.join("/", "", StatusNode.ROOT_NODE, STORAGE_NODES, ATTRIBUTES_NODE, schema.toString());
    }
    
    /**
     * Extract qualified schema.
     *
     * @param storageNodePath storage node path
     * @return extracted qualified schema
     */
    public static Optional<QualifiedSchema> extractQualifiedSchema(final String storageNodePath) {
        Pattern pattern = Pattern.compile(getRootPath() + "/(\\S+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(storageNodePath);
        return matcher.find() ? Optional.of(new QualifiedSchema(matcher.group(1))) : Optional.empty();
    }
}
