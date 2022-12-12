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

package org.apache.shardingsphere.mode.process.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Process node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessNode {
    
    private static final String EXECUTION_NODES = "execution_nodes";
    
    /**
     * Get process list id path.
     *
     * @param processListId process list id
     * @return execution path
     */
    public static String getProcessListIdPath(final String processListId) {
        return String.join("/", "", EXECUTION_NODES, processListId);
    }
    
    /**
     * Get process list instance path.
     *
     * @param processListId process list id
     * @param instancePath instance path
     * @return execution path
     */
    public static String getProcessListInstancePath(final String processListId, final String instancePath) {
        return String.join("/", "", EXECUTION_NODES, processListId, instancePath);
    }
}
