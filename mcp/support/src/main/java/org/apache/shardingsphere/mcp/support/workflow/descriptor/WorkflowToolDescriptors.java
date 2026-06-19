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
import org.apache.shardingsphere.mcp.support.descriptor.MCPDescriptorCatalogIndex;

/**
 * Shared workflow tool descriptors.
 */
public final class WorkflowToolDescriptors {
    
    public static final String APPLY_TOOL_NAME = "database_gateway_apply_workflow";
    
    public static final String VALIDATE_TOOL_NAME = "database_gateway_validate_workflow";
    
    private WorkflowToolDescriptors() {
    }
    
    /**
     * Create workflow planning descriptor.
     *
     * @param toolName tool name
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor createPlanning(final String toolName) {
        return MCPDescriptorCatalogIndex.getRequiredToolDescriptor(toolName);
    }
    
    /**
     * Create workflow execution descriptor.
     *
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor createExecution() {
        return MCPDescriptorCatalogIndex.getRequiredToolDescriptor(APPLY_TOOL_NAME);
    }
    
    /**
     * Create workflow validation descriptor.
     *
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor createValidation() {
        return MCPDescriptorCatalogIndex.getRequiredToolDescriptor(VALIDATE_TOOL_NAME);
    }
}
