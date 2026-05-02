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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.api.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowExecutionService;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.workflow.MCPWorkflowContext;
import org.apache.shardingsphere.mcp.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowKind;

import java.util.Map;

/**
 * Generic workflow execution tool handler.
 */
public final class WorkflowExecutionToolHandler implements ToolHandler {
    
    private final WorkflowExecutionService executionService;
    
    private final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry;
    
    public WorkflowExecutionToolHandler(final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) {
        this(new WorkflowExecutionService(), workflowRuntimeDefinitionRegistry);
    }
    
    WorkflowExecutionToolHandler(final WorkflowExecutionService executionService, final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) {
        this.executionService = executionService;
        this.workflowRuntimeDefinitionRegistry = workflowRuntimeDefinitionRegistry;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return WorkflowToolDescriptors.createExecution();
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        MCPWorkflowContext workflowContext = MCPWorkflowContext.getRequired(requestContext);
        WorkflowContextSnapshot snapshot = workflowContext.getWorkflowSessionContext().getRequired(toolArguments.getStringArgument("plan_id"));
        WorkflowKind workflowKind = getRequiredWorkflowKind(snapshot);
        return new MCPMapResponse(executionService.apply(workflowContext.getWorkflowSessionContext(), workflowContext.getMetadataQueryFacade(), workflowContext.getQueryFacade(),
                workflowContext.getExecutionFacade(), workflowRuntimeDefinitionRegistry.getRequired(workflowKind).getApplySynchronizationHandler(), sessionId, snapshot,
                toolArguments.getStringCollectionArgument("approved_steps"), toolArguments.getStringArgument("execution_mode")));
    }
    
    private WorkflowKind getRequiredWorkflowKind(final WorkflowContextSnapshot snapshot) {
        if (null != snapshot.getWorkflowKind()) {
            return snapshot.getWorkflowKind();
        }
        throw new MCPInvalidRequestException(String.format("Workflow kind is required for plan_id `%s`.", snapshot.getPlanId()));
    }
}
