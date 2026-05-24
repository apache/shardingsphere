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

package org.apache.shardingsphere.mcp.core.tool.handler.workflow;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPWorkflowStateException;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowSessionSnapshotResolver;
import org.apache.shardingsphere.mcp.support.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic workflow execution tool handler.
 */
public final class WorkflowExecutionToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final WorkflowExecutionService executionService;
    
    private final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry;
    
    public WorkflowExecutionToolHandler(final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) {
        executionService = new WorkflowExecutionService();
        this.workflowRuntimeDefinitionRegistry = workflowRuntimeDefinitionRegistry;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return WorkflowToolDescriptors.APPLY_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        String executionMode = toolArguments.getStringArgument("execution_mode");
        if (executionMode.isEmpty()) {
            throw new MCPExecutionModeRequiredException("database_gateway_apply_workflow", List.of("preview", "review-then-execute", "manual-only"),
                    createPreviewSuggestedArguments(toolCall.getArguments()));
        }
        MCPDatabaseHandlerContext databaseContext = workflowContext.getDatabaseContext();
        WorkflowContextSnapshot snapshot = WorkflowSessionSnapshotResolver.getRequired(workflowContext.getWorkflowSessionContext(), toolCall.getSessionId(),
                toolArguments.getStringArgument("plan_id"));
        WorkflowKind workflowKind = getRequiredWorkflowKind(snapshot);
        return new MCPMapResponse(executionService.apply(workflowContext.getWorkflowSessionContext(), databaseContext.getMetadataQueryFacade(), databaseContext.getQueryFacade(),
                databaseContext.getExecutionFacade(), workflowRuntimeDefinitionRegistry.getRequired(workflowKind).getApplySynchronizationHandler(), toolCall.getSessionId(), snapshot,
                toolArguments.getStringCollectionArgument("approved_steps"), executionMode));
    }
    
    private WorkflowKind getRequiredWorkflowKind(final WorkflowContextSnapshot snapshot) {
        if (null != snapshot.getWorkflowKind()) {
            return snapshot.getWorkflowKind();
        }
        throw new MCPWorkflowStateException(String.format("Workflow kind is required for plan_id `%s`.", snapshot.getPlanId()), snapshot.getPlanId());
    }
    
    private static Map<String, Object> createPreviewSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove("execution_mode");
        result.put("execution_mode", "preview");
        return result;
    }
}
