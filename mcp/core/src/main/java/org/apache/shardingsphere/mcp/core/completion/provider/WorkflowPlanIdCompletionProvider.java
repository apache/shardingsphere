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

package org.apache.shardingsphere.mcp.core.completion.provider;

import org.apache.shardingsphere.mcp.support.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.support.completion.MCPCompletionRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Workflow plan id completion provider.
 */
public final class WorkflowPlanIdCompletionProvider implements MCPCompletionProvider<MCPWorkflowHandlerContext> {
    
    private static final Set<String> COMPLETION_ELIGIBLE_WORKFLOW_STATUSES = Set.of(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION,
            WorkflowLifecycle.STATUS_EXECUTED, WorkflowLifecycle.STATUS_FAILED, WorkflowLifecycle.STATUS_PLANNED, WorkflowLifecycle.STATUS_VALIDATED);
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequestContext requestContext) {
        return "plan_id".equals(requestContext.getArgumentName());
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPWorkflowHandlerContext handlerContext, final MCPCompletionRequestContext requestContext) {
        return new MCPCompletionProviderResult(completePlanIds(handlerContext, requestContext.getSessionId()));
    }
    
    private List<MCPCompletionCandidate> completePlanIds(final MCPWorkflowHandlerContext handlerContext, final String sessionId) {
        return handlerContext.getWorkflowSessionContext().list(sessionId).stream().filter(this::isCompletionEligiblePlan)
                .map(each -> new MCPCompletionCandidate(each.getPlanId(), String.format("%s %s", each.getWorkflowKind(), each.getStatus()), "workflow-session", each.getUpdateTime(),
                        "recent-plan-first-for-plan_id"))
                .toList();
    }
    
    private boolean isCompletionEligiblePlan(final WorkflowContextSnapshot snapshot) {
        return COMPLETION_ELIGIBLE_WORKFLOW_STATUSES.contains(Objects.toString(snapshot.getStatus(), ""));
    }
}
