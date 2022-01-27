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

import org.apache.shardingsphere.infra.instance.definition.InstanceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node.
 */
public final class ComputeNode {
    
    private static final String ROOT_NODE = "nodes";
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    private static final String ONLINE_NODE = "online";
    
    private static final String ATTRIBUTES_NODE = "attributes";
    
    private static final String LABELS_NODE = "labels";
    
    private static final String STATUS_NODE = "status";
    
    private static final String WORKER_ID = "worker_id";
    
    /**
     * Get online compute node path.
     * 
     * @param instanceType instance type
     * @return path of online compute node
     */
    public static String getOnlineNodePath(final InstanceType instanceType) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ONLINE_NODE, instanceType.name().toLowerCase());
    }
    
    /**
     * Get online compute node instance path.
     *
     * @param instanceId instance id
     * @param instanceType instance type
     * @return path of online compute node instance
     */
    public static String getOnlineInstanceNodePath(final String instanceId, final InstanceType instanceType) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ONLINE_NODE, instanceType.name().toLowerCase(), instanceId);
    }
    
    /**
     * Get compute node instance labels path.
     *
     * @param instanceId instance id
     * @return path of compute node instance labels
     */
    public static String getInstanceLabelsNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ATTRIBUTES_NODE, instanceId, LABELS_NODE);
    }
    
    /**
     * Get attributes node path.
     * 
     * @return attributes node path
     */
    public static String getAttributesNodePath() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ATTRIBUTES_NODE);
    }
    
    /**
     * Get instance worker id node path.
     *
     * @param instanceId instance id
     * @return worker id path
     */
    public static String getInstanceWorkerIdNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ATTRIBUTES_NODE, instanceId, WORKER_ID);
    }
    
    /**
     * Get instance id by status path.
     * 
     * @param attributesPath attributes path
     * @return instance id
     */
    public static String getInstanceIdByAttributes(final String attributesPath) {
        Pattern pattern = Pattern.compile(getAttributesNodePath() + "/([\\S]+)" + "(/status|/worker_id|/labels)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(attributesPath);
        return matcher.find() ? matcher.group(1) : "";
    }
    
    /**
     * Get instance status node path.
     * 
     * @param instanceId instance id
     * @return instance status node path
     */
    public static String getInstanceStatusNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ATTRIBUTES_NODE, instanceId, STATUS_NODE);
    }
}
