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

package org.apache.shardingsphere.mcp.core.workflow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Objects;

/**
 * Workflow session snapshot resolver.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowSessionSnapshotResolver {
    
    /**
     * Get a workflow snapshot owned by the current MCP session.
     *
     * @param workflowSessionContext workflow session context
     * @param sessionId session identifier
     * @param planId plan identifier
     * @return workflow snapshot
     * @throws MCPWorkflowStateException when the plan is unavailable in the current MCP session
     */
    public static WorkflowContextSnapshot getRequired(final WorkflowSessionContext workflowSessionContext, final String sessionId, final String planId) {
        WorkflowContextSnapshot result = workflowSessionContext.getRequired(planId);
        if (!Objects.equals(sessionId, result.getSessionId())) {
            throw new MCPWorkflowStateException(
                    String.format("Unknown or unavailable plan_id `%s`. Call the planning tool again in the current MCP session.", planId), planId);
        }
        return result;
    }
}
