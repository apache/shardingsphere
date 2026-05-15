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

package org.apache.shardingsphere.mcp.support.workflow.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;

import java.util.List;
import java.util.Set;

/**
 * Workflow tool descriptor validator.
 */
public final class WorkflowToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    private static final Set<String> SUPPORTED_TOOLS = Set.of(WorkflowToolDescriptors.APPLY_TOOL_NAME, WorkflowToolDescriptors.VALIDATE_TOOL_NAME);
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return SUPPORTED_TOOLS.contains(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        if (WorkflowToolDescriptors.APPLY_TOOL_NAME.equals(toolDescriptor.getName())) {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor,
                    List.of("response_mode", "plan_id", "status", "execution_mode", "next_actions", "requires_user_approval", "manual_artifact_summary"));
        } else {
            MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor, List.of("response_mode", "plan_id", "status", "overall_status", "issues", "next_actions"));
        }
    }
}
