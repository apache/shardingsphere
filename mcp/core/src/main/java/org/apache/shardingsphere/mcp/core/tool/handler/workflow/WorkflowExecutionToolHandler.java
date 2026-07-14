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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

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
        String executionMode = toolArguments.getStringArgument(WorkflowFieldNames.EXECUTION_MODE);
        if (executionMode.isEmpty()) {
            throw new MCPExecutionModeRequiredException(WorkflowToolDescriptors.APPLY_TOOL_NAME, List.of(WorkflowLifecycle.EXECUTION_MODE_PREVIEW,
                    WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE, WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY),
                    createPreviewSuggestedArguments(toolCall.getArguments()));
        }
        MCPDatabaseHandlerContext databaseContext = workflowContext.getDatabaseContext();
        WorkflowContextSnapshot snapshot = WorkflowSessionSnapshotResolver.getRequired(workflowContext.getWorkflowSessionContext(), toolCall.getSessionId(),
                toolArguments.getStringArgument(WorkflowFieldNames.PLAN_ID));
        WorkflowKind workflowKind = WorkflowSessionSnapshotResolver.getRequiredWorkflowKind(snapshot);
        WorkflowRuntimeDefinition workflowRuntimeDefinition = workflowRuntimeDefinitionRegistry.getRequired(workflowKind);
        return new MCPMapResponse(executionService.apply(workflowContext.getWorkflowSessionContext(), databaseContext.getMetadataQueryFacade(), databaseContext.getQueryFacade(),
                databaseContext.getExecutionFacade(), workflowRuntimeDefinition.getApplySynchronizationHandler(), workflowRuntimeDefinition.getApplyArtifactValidator(), toolCall.getSessionId(),
                snapshot, toolArguments.getStringCollectionArgument(WorkflowFieldNames.APPROVED_STEPS), executionMode));
    }
    
    private static Map<String, Object> createPreviewSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(WorkflowFieldNames.EXECUTION_MODE);
        result.put(WorkflowFieldNames.EXECUTION_MODE, WorkflowLifecycle.EXECUTION_MODE_PREVIEW);
        return result;
    }
}
