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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.workflow.spi.MCPWorkflowValidationHandler;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.workflow.MCPWorkflowContext;

import java.util.Map;

/**
 * Generic workflow validation tool handler.
 */
public final class WorkflowValidationToolHandler implements ToolHandler {
    
    private final String toolName;
    
    private final MCPWorkflowValidationHandler workflowValidationHandler;
    
    public WorkflowValidationToolHandler(final String toolName, final MCPWorkflowValidationHandler workflowValidationHandler) {
        this.toolName = toolName;
        this.workflowValidationHandler = workflowValidationHandler;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return WorkflowToolDescriptors.createValidation(toolName);
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        MCPWorkflowContext workflowContext = MCPWorkflowContext.getRequired(requestContext);
        return new MCPMapResponse(workflowValidationHandler.validate(workflowContext.getWorkflowSessionContext(), workflowContext.getMetadataQueryFacade(),
                workflowContext.getQueryFacade(), workflowContext.getExecutionFacade(), sessionId, toolArguments.getStringArgument("plan_id")));
    }
}
