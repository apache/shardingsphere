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

package org.apache.shardingsphere.mcp.workflow;

import org.apache.shardingsphere.mcp.api.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.database.MCPDatabaseContext;

/**
 * Workflow-aware MCP feature context.
 */
public interface MCPWorkflowContext extends MCPDatabaseContext {
    
    /**
     * Get workflow session context.
     *
     * @return workflow session context
     */
    WorkflowSessionContext getWorkflowSessionContext();
    
    /**
     * Get required workflow-aware request context.
     *
     * @param requestContext feature context
     * @return workflow-aware request context
     * @throws IllegalStateException workflow-aware context is unavailable
     */
    static MCPWorkflowContext getRequired(final MCPFeatureContext requestContext) {
        if (requestContext instanceof MCPWorkflowContext) {
            return (MCPWorkflowContext) requestContext;
        }
        throw new IllegalStateException("Workflow-aware request context is required.");
    }
}
