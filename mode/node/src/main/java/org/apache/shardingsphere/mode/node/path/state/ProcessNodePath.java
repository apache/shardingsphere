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

package org.apache.shardingsphere.mode.node.path.state;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Process node path.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessNodePath {
    
    private static final String ROOT_NODE = "/execution_nodes";
    
    /**
     * Get process ID root path.
     *
     * @param processId process ID
     * @return process ID root path
     */
    public static String getRootPath(final String processId) {
        return String.join("/", ROOT_NODE, processId);
    }
    
    /**
     * Get instance process list.
     *
     * @param processId process ID
     * @param instanceId instance ID
     * @return instance process list
     */
    public static String getInstanceProcessList(final String processId, final String instanceId) {
        return String.join("/", getRootPath(processId), instanceId);
    }
}
