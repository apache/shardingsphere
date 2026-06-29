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

package org.apache.shardingsphere.mcp.feature.broadcast.descriptor;

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidationUtils;
import org.apache.shardingsphere.mcp.support.descriptor.MCPToolDescriptorValidator;

/**
 * Broadcast tool descriptor validator.
 */
public final class BroadcastToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return BroadcastFeatureDefinition.PLAN_TOOL_NAME.equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(toolDescriptor);
        MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanMetaFields(toolDescriptor);
    }
}
