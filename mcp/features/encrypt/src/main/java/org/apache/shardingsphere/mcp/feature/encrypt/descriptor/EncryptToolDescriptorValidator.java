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

package org.apache.shardingsphere.mcp.feature.encrypt.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;

import java.util.List;

/**
 * Encrypt tool descriptor validator.
 */
public final class EncryptToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return EncryptFeatureDefinition.PLAN_TOOL_NAME.equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredOutputFields(toolDescriptor,
                List.of("response_mode", "plan_id", "workflow_kind", "status", "missing_required_inputs", "resources_to_read", "next_actions"));
    }
}
