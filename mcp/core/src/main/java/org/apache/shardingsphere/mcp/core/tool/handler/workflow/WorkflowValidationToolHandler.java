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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.core.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.core.workflow.WorkflowRuntimeDefinitionRegistry;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Map;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

/**
 * Generic workflow validation tool handler.
 */
@RequiredArgsConstructor
public final class WorkflowValidationToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final WorkflowRuntimeDefinitionRegistry workflowRuntimeDefinitionRegistry;
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return WorkflowToolDescriptors.VALIDATE_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        WorkflowSessionContext workflowSessionContext = requestContext.getWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = workflowSessionContext.getRequired(toolArguments.getStringArgument(WorkflowFieldNames.PLAN_ID));
        return new MCPMapPayload(workflowRuntimeDefinitionRegistry.getRequired(snapshot).getValidationHandler().validate(workflowSessionContext,
                requestContext.getMetadataQueryFacade(), requestContext.getQueryFacade(), requestContext.getExecutionFacade(), requestContext.getSessionId(), snapshot));
    }
    
}
