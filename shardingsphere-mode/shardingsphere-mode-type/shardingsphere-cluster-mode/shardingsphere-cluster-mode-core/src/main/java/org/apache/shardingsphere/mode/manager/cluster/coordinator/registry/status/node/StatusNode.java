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

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.schema.ClusterSchema;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Status node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatusNode {
    
    private static final String ROOT_NODE = "status";
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    private static final String STORAGE_NODE = "storage_nodes";
    
    private static final String PRIMARY_NODE = "primarynodes";
    
    private static final String PRIVILEGE_NODE = "privilegenode";
    
    /**
     * Get compute node path.
     *
     * @param instanceId instance id
     * @return compute node path
     */
    public static String getComputeNodePath(final String instanceId) {
        return Joiner.on("/").join("", ROOT_NODE, COMPUTE_NODE, instanceId);
    }
    
    /**
     * Get storage nodes path.
     *
     * @return storage nodes path
     */
    public static String getStorageNodePath() {
        return Joiner.on("/").join("", ROOT_NODE, STORAGE_NODE);
    }
    
    /**
     * Get primary nodes path.
     *
     * @return primary nodes path
     */
    public static String getPrimaryNodesPath() {
        return Joiner.on("/").join("", ROOT_NODE, PRIMARY_NODE);
    }
    
    /**
     * Get schema path.
     * 
     * @param schemaName schema name
     * @return schema path
     */
    public static String getSchemaPath(final String schemaName) {
        return Joiner.on("/").join("", ROOT_NODE);
    }
    
    /**
     * Get data source path.
     * 
     * @param schemaName schema name
     * @param dataSourceName data source name
     * @return data source path
     */
    public static String getDataSourcePath(final String schemaName, final String dataSourceName) {
        return Joiner.on("/").join("", ROOT_NODE, STORAGE_NODE, schemaName, dataSourceName);
    }
    
    /**
     * Get primary data source path.
     *
     * @param schemaName schema name
     * @param groupName group name
     * @return data source path
     */
    public static String getPrimaryDataSourcePath(final String schemaName, final String groupName) {
        return Joiner.on("/").join("", ROOT_NODE, PRIMARY_NODE, schemaName, groupName);
    }
    
    /**
     * Get cluster schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return cluster schema
     */
    public static Optional<ClusterSchema> getClusterSchema(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getStorageNodePath() + "/" + "(\\w+)/(\\S+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(new ClusterSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
    }
    
    /**
     * Get primary nodes cluster schema.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return primary nodes cluster schema
     */
    public static Optional<ClusterSchema> getPrimaryNodesClusterSchema(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getPrimaryNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find() ? Optional.of(new ClusterSchema(matcher.group(1), matcher.group(2))) : Optional.empty();
    }
    
    /**
     * Is primary data source path.
     *
     * @param dataSourceNodeFullPath data source node full path
     * @return is primary data source path
     */
    public static boolean isPrimaryDataSourcePath(final String dataSourceNodeFullPath) {
        Pattern pattern = Pattern.compile(getPrimaryNodesPath() + "/" + "(\\w+)/(\\w+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(dataSourceNodeFullPath);
        return matcher.find();
    }
    
    /**
     * Get proxy nodes path.
     *
     * @return proxy nodes path
     */
    public static String getComputeNodesPath() {
        return Joiner.on("/").join("", ROOT_NODE, COMPUTE_NODE);
    }
    
    /**
     * Get privilege node path.
     *
     * @return privilege node path
     */
    public static String getPrivilegeNodePath() {
        return Joiner.on("/").join("", ROOT_NODE, PRIVILEGE_NODE);
    }
}
