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

package org.apache.shardingsphere.mode.node.path.metadata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Compute node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputeNodePath {
    
    private static final String ROOT_NODE = "/nodes/compute_nodes";
    
    private static final String ONLINE_NODE = "online";
    
    private static final String SHOW_PROCESS_LIST_TRIGGER_NODE = "show_process_list_trigger";
    
    private static final String KILL_PROCESS_TRIGGER_NODE = "kill_process_trigger";
    
    private static final String STATUS_NODE = "status";
    
    private static final String WORKER_ID_NODE = "worker_id";
    
    private static final String LABELS_NODE = "labels";
    
    private static final String INSTANCE_ID_PATTERN = "([\\S]+)";
    
    /**
     * Get compute node root path.
     *
     * @return compute node root path
     */
    public static String getRootPath() {
        return ROOT_NODE;
    }
    
    /**
     * Get online root path.
     *
     * @return online root path
     */
    public static String getOnlineRootPath() {
        return String.join("/", getRootPath(), ONLINE_NODE);
    }
    
    /**
     * Get online path.
     *
     * @param instanceType instance type
     * @return online path
     */
    public static String getOnlinePath(final InstanceType instanceType) {
        return String.join("/", getOnlineRootPath(), instanceType.name().toLowerCase());
    }
    
    /**
     * Get online path.
     *
     * @param instanceId instance ID
     * @param instanceType instance type
     * @return online path
     */
    public static String getOnlinePath(final String instanceId, final InstanceType instanceType) {
        return String.join("/", getOnlinePath(instanceType), instanceId);
    }
    
    /**
     * Get show process list trigger root path.
     *
     * @return show process list trigger root path
     */
    public static String getShowProcessListTriggerRootPath() {
        return String.join("/", ROOT_NODE, SHOW_PROCESS_LIST_TRIGGER_NODE);
    }
    
    /**
     * Get show process list trigger path.
     *
     * @param instanceId instance ID
     * @param taskId show process list task ID
     * @return show process list trigger path
     */
    public static String getShowProcessListTriggerPath(final String instanceId, final String taskId) {
        return String.join("/", getShowProcessListTriggerRootPath(), String.join(":", instanceId, taskId));
    }
    
    /**
     * Get kill process trigger root path.
     *
     * @return kill process trigger root path
     */
    public static String getKillProcessTriggerRootPath() {
        return String.join("/", ROOT_NODE, KILL_PROCESS_TRIGGER_NODE);
    }
    
    /**
     * Get kill process trigger path.
     *
     * @param instanceId instance ID
     * @param processId process ID
     * @return kill process trigger path
     */
    public static String getKillProcessTriggerPath(final String instanceId, final String processId) {
        return String.join("/", getKillProcessTriggerRootPath(), String.join(":", instanceId, processId));
    }
    
    /**
     * Get state path.
     *
     * @param instanceId instance ID
     * @return state path
     */
    public static String getStatePath(final String instanceId) {
        return String.join("/", ROOT_NODE, STATUS_NODE, instanceId);
    }
    
    /**
     * Get worker ID root path.
     *
     * @return worker ID root path
     */
    public static String getWorkerIdRootPath() {
        return String.join("/", ROOT_NODE, WORKER_ID_NODE);
    }
    
    /**
     * Get worker ID path.
     *
     * @param instanceId instance ID
     * @return worker ID path
     */
    public static String getWorkerIdPath(final String instanceId) {
        return String.join("/", getWorkerIdRootPath(), instanceId);
    }
    
    /**
     * Get labels path.
     *
     * @param instanceId instance ID
     * @return labels path
     */
    public static String getLabelsPath(final String instanceId) {
        return String.join("/", ROOT_NODE, LABELS_NODE, instanceId);
    }
    
    /**
     * Find instance id by compute node path.
     *
     * @param computeNodePath compute node path
     * @return found instance ID
     */
    public static Optional<String> findInstanceId(final String computeNodePath) {
        Pattern pattern = Pattern.compile(getRootPath() + "(/status|/worker_id|/labels)" + "/" + INSTANCE_ID_PATTERN + "$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(computeNodePath);
        return matcher.find() ? Optional.of(matcher.group(2)) : Optional.empty();
    }
}
