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

import org.apache.shardingsphere.infra.instance.metadata.InstanceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node.
 */
public final class ComputeNode {
    
    private static final String ROOT_NODE = "nodes";
    
    private static final String COMPUTE_NODE = "compute_nodes";
    
    private static final String ONLINE_NODE = "online";
    
    private static final String LABELS_NODE = "labels";
    
    private static final String PROCESS_TRIGGER = "process_trigger";
    
    private static final String PROCESS_KILL = "process_kill";
    
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
     * Get online compute node path.
     *
     * @return path of online compute node
     */
    public static String getOnlineInstanceNodePath() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, ONLINE_NODE);
    }
    
    /**
     * Get process trigger node path.
     * 
     * @return path of process trigger node path
     */
    public static String getProcessTriggerNodePatch() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, PROCESS_TRIGGER);
    }
    
    /**
     * Get process kill node path.
     *
     * @return path of process kill node path
     */
    public static String getProcessKillNodePatch() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, PROCESS_KILL);
    }
    
    /**
     * Get process trigger instance show process list id node path.
     *
     * @param instanceId instance id
     * @param showProcessListId show process list id
     * @return path of process trigger instance node path
     */
    public static String getProcessTriggerInstanceIdNodePath(final String instanceId, final String showProcessListId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, PROCESS_TRIGGER, String.join(":", instanceId, showProcessListId));
    }
    
    /**
     * Get process kill instance id node path.
     *
     * @param instanceId instance id
     * @param processId process id
     * @return path of process kill instance id node path
     */
    public static String getProcessKillInstanceIdNodePath(final String instanceId, final String processId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, PROCESS_KILL, String.join(":", instanceId, processId));
    }
    
    /**
     * Get compute node instance labels path.
     *
     * @param instanceId instance id
     * @return path of compute node instance labels
     */
    public static String getInstanceLabelsNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, LABELS_NODE, instanceId);
    }
    
    /**
     * Get compute node path.
     *
     * @return compute node path
     */
    public static String getComputeNodePath() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE);
    }
    
    /**
     * Get instance worker id node path.
     *
     * @param instanceId instance id
     * @return worker id path
     */
    public static String getInstanceWorkerIdNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, WORKER_ID, instanceId);
    }
    
    /**
     * Get instance worker id root node path.
     *
     * @return worker id root node path
     */
    public static String getInstanceWorkerIdRootNodePath() {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, WORKER_ID);
    }
    
    /**
     * Get instance id by compute node path.
     * 
     * @param computeNodePath compute node path
     * @return instance id
     */
    public static String getInstanceIdByComputeNode(final String computeNodePath) {
        Pattern pattern = Pattern.compile(getComputeNodePath() + "(/status|/worker_id|/labels)" + "/([\\S]+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(computeNodePath);
        return matcher.find() ? matcher.group(2) : "";
    }
    
    /**
     * Get instance status node path.
     * 
     * @param instanceId instance id
     * @return instance status node path
     */
    public static String getInstanceStatusNodePath(final String instanceId) {
        return String.join("/", "", ROOT_NODE, COMPUTE_NODE, STATUS_NODE, instanceId);
    }
}
