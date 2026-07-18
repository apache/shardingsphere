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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic workflow execution tool handler.
 */
public final class WorkflowExecutionToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final WorkflowExecutionService executionService;
    
    private final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry;
    
    public WorkflowExecutionToolHandler(final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) {
        executionService = new WorkflowExecutionService();
        this.workflowRuntimeDefinitionRegistry = workflowRuntimeDefinitionRegistry;
    }
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return WorkflowToolDescriptors.APPLY_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        String executionMode = toolArguments.getStringArgument(WorkflowFieldNames.EXECUTION_MODE);
        if (executionMode.isEmpty()) {
            throw new MCPExecutionModeRequiredException(WorkflowToolDescriptors.APPLY_TOOL_NAME, List.of(WorkflowLifecycle.EXECUTION_MODE_PREVIEW,
                    WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE, WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY),
                    createPreviewSuggestedArguments(arguments));
        }
        WorkflowSessionContext workflowSessionContext = requestContext.getWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = workflowSessionContext.getRequired(toolArguments.getStringArgument(WorkflowFieldNames.PLAN_ID));
        WorkflowRuntimeDefinition workflowRuntimeDefinition = workflowRuntimeDefinitionRegistry.getRequired(snapshot);
        return new MCPMapPayload(executionService.apply(workflowSessionContext, requestContext.getMetadataQueryFacade(), requestContext.getQueryFacade(),
                requestContext.getExecutionFacade(), workflowRuntimeDefinition.getApplySynchronizationHandler(), workflowRuntimeDefinition.getApplyArtifactValidator(),
                requestContext.getSessionIdentity().getSessionId(),
                snapshot, toolArguments.getStringCollectionArgument(WorkflowFieldNames.APPROVED_STEPS), executionMode));
    }
    
    private static Map<String, Object> createPreviewSuggestedArguments(final Map<String, Object> arguments) {
        Map<String, Object> result = new LinkedHashMap<>(arguments);
        result.remove(WorkflowFieldNames.EXECUTION_MODE);
        result.put(WorkflowFieldNames.EXECUTION_MODE, WorkflowLifecycle.EXECUTION_MODE_PREVIEW);
        return result;
    }
}
