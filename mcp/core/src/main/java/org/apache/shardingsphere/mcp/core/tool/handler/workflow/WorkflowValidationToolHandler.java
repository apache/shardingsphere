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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.database.MCPDatabaseHandlerContext;
import org.apache.shardingsphere.mcp.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.workflow.model.WorkflowKind;

/**
 * Generic workflow validation tool handler.
 */
public final class WorkflowValidationToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry;
    
    public WorkflowValidationToolHandler(final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry) {
        this.workflowRuntimeDefinitionRegistry = workflowRuntimeDefinitionRegistry;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return WorkflowToolDescriptors.createValidation();
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        MCPToolArguments toolArguments = new MCPToolArguments(toolCall.getArguments());
        MCPDatabaseHandlerContext databaseContext = workflowContext.getDatabaseContext();
        WorkflowContextSnapshot snapshot = workflowContext.getWorkflowSessionContext().getRequired(toolArguments.getStringArgument("plan_id"));
        WorkflowKind workflowKind = getRequiredWorkflowKind(snapshot);
        return new MCPMapResponse(workflowRuntimeDefinitionRegistry.getRequired(workflowKind).getValidationHandler().validate(workflowContext.getWorkflowSessionContext(),
                databaseContext.getMetadataQueryFacade(), databaseContext.getQueryFacade(), databaseContext.getExecutionFacade(), toolCall.getSessionId(), snapshot));
    }
    
    private WorkflowKind getRequiredWorkflowKind(final WorkflowContextSnapshot snapshot) {
        if (null != snapshot.getWorkflowKind()) {
            return snapshot.getWorkflowKind();
        }
        throw new MCPInvalidRequestException(String.format("Workflow kind is required for plan_id `%s`.", snapshot.getPlanId()));
    }
}
