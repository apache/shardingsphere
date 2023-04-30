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

package org.apache.shardingsphere.metadata.persist.node;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Process node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessNode {
    
    private static final String EXECUTION_NODES = "execution_nodes";
    
    /**
     * Get process id path.
     *
     * @param processId process id
     * @return execution path
     */
    public static String getProcessIdPath(final String processId) {
        return String.join("/", "", EXECUTION_NODES, processId);
    }
    
    /**
     * Get process list instance path.
     *
     * @param processId process id
     * @param instancePath instance path
     * @return execution path
     */
    public static String getProcessListInstancePath(final String processId, final String instancePath) {
        return String.join("/", "", EXECUTION_NODES, processId, instancePath);
    }
}
