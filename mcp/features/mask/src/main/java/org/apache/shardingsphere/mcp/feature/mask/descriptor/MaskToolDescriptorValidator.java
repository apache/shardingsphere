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

import java.util.Collection;
import java.util.List;

/**
 * Mask tool descriptor validator.
 */
public final class MaskToolDescriptorValidator implements MCPToolDescriptorValidator {
    
    private static final Collection<String> REQUIRED_SECRET_WORKFLOW_OUTPUT_FIELDS = List.of("masked_property_preview", "secret_reference_summary");
    
    private static final Collection<String> REQUIRED_META_FIELDS = List.of(
            "org.apache.shardingsphere/workflow-kind", "org.apache.shardingsphere/related-resource-uris", "org.apache.shardingsphere/follow-up-tools");
    
    @Override
    public boolean supports(final MCPToolDescriptor toolDescriptor) {
        return MaskFeatureDefinition.PLAN_TOOL_NAME.equals(toolDescriptor.getName());
    }
    
    @Override
    public void validate(final MCPToolDescriptor toolDescriptor) {
        MCPToolDescriptorValidationUtils.validateRequiredWorkflowPlanOutputFields(toolDescriptor, REQUIRED_SECRET_WORKFLOW_OUTPUT_FIELDS);
        MCPToolDescriptorValidationUtils.validateRequiredMetaFields(toolDescriptor, REQUIRED_META_FIELDS);
    }
}
