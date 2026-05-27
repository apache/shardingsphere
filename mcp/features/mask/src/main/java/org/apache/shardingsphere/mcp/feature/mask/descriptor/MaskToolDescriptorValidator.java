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

package org.apache.shardingsphere.mcp.feature.mask.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;

import java.util.List;

/**
 * Mask tool descriptor validator.
 */
public final class MaskToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return MaskFeatureDefinition.PLAN_TOOL_NAME.equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor,
                List.of("response_mode", WorkflowFieldNames.PLAN_ID, "workflow_kind", "status", "missing_required_inputs",
                        MCPPayloadFieldNames.RESOURCES_TO_READ, MCPPayloadFieldNames.NEXT_ACTIONS));
    }
}
