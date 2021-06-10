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

package org.apache.shardingsphere.governance.core.registry.process.node;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Process node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProcessNode {
    
    private static final String EXECUTION_NODE_NAME = "executionnodes";
    
    /**
     * Get execution nodes path.
     *
     * @return execution nodes path
     */
    public static String getExecutionNodesPath() {
        return Joiner.on("/").join("", EXECUTION_NODE_NAME);
    }
    
    /**
     * Get execution path.
     *
     * @param executionId execution id
     * @return execution path
     */
    public static String getExecutionPath(final String executionId) {
        return Joiner.on("/").join("", EXECUTION_NODE_NAME, executionId);
    }
}
