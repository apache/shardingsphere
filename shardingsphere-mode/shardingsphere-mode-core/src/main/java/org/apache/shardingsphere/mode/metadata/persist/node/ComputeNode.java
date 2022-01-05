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

package org.apache.shardingsphere.mode.metadata.persist.node;

/**
 * Compute node.
 */
public final class ComputeNode {
    
    private static final String ROOT_NODE = "nodes";
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    private static final String ONLINE_NODE = "online";
    
    private static final String ATTRIBUTES_NODE = "attributes";
    
    private static final String LABEL_NODE = "label";
    
    private static final String STATUS_NODE = "status";
    
    /**
     * Get online compute node path.
     * 
     * @return path of online compute node
     */
    public static String getOnlineNodePath() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ONLINE_NODE);
    }
    
    /**
     * Get online compute node instance path.
     *
     * @param instanceId instance id
     * @return path of online compute node instance
     */
    public static String getOnlineInstanceNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ONLINE_NODE, instanceId);
    }
    
    /**
     * Get online compute node instance label path.
     *
     * @param instanceId instance id
     * @return path of compute node instance label
     */
    public static String getInstanceLabelNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ATTRIBUTES_NODE, instanceId, LABEL_NODE);
    }
}
