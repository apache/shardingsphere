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

package org.apache.shardingsphere.mcp.feature.mask.tool.handler;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowExecutionService;

import java.util.List;
import java.util.Map;

/**
 * Tool handler for mask workflow execution.
 */
public final class ApplyMaskRuleToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("apply_mask_rule",
            List.of(
                    new MCPToolFieldDefinition("plan_id", new MCPToolValueDefinition(Type.STRING, "Workflow plan identifier.", null), true),
                    new MCPToolFieldDefinition("execution_mode", new MCPToolValueDefinition(Type.STRING, "Optional execution mode override.", null), false),
                    new MCPToolFieldDefinition("approved_steps",
                            new MCPToolValueDefinition(Type.ARRAY, "Approved execution steps.",
                                    new MCPToolValueDefinition(Type.STRING, "Step name.", null)),
                            false)));
    
    private final WorkflowExecutionService executionService;
    
    public ApplyMaskRuleToolHandler() {
        this(new WorkflowExecutionService());
    }
    
    ApplyMaskRuleToolHandler(final WorkflowExecutionService executionService) {
        this.executionService = executionService;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        return new MCPMapResponse(executionService.apply(requestContext, sessionId, toolArguments.getStringArgument("plan_id"),
                toolArguments.getStringCollectionArgument("approved_steps"), toolArguments.getStringArgument("execution_mode")));
    }
}
