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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;

import java.util.Map;

/**
 * Generic workflow execution tool handler.
 */
@RequiredArgsConstructor
public final class WorkflowExecutionToolHandler implements ToolHandler {
    
    private final String toolName;
    
    private final WorkflowExecutionService executionService;
    
    public WorkflowExecutionToolHandler(final String toolName) {
        this(toolName, new WorkflowExecutionService());
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return WorkflowToolDescriptors.createExecution(toolName);
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        return new MCPMapResponse(executionService.apply(requestContext.getWorkflowContextStore(), requestContext.getExecutionFacade(), sessionId, toolArguments.getStringArgument("plan_id"),
                toolArguments.getStringCollectionArgument("approved_steps"), toolArguments.getStringArgument("execution_mode")));
    }
}
