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

package org.apache.shardingsphere.mcp.tool.descriptor;

import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;

import java.util.List;

/**
 * Workflow execution tool descriptor factory.
 */
public final class WorkflowExecutionToolDescriptorFactory {
    
    private WorkflowExecutionToolDescriptorFactory() {
    }
    
    /**
     * Create workflow execution descriptor.
     *
     * @param toolName tool name
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor create(final String toolName) {
        return new MCPToolDescriptor(toolName, List.of(
                new MCPToolFieldDefinition("plan_id", new MCPToolValueDefinition(Type.STRING, "Workflow plan identifier.", null), true),
                new MCPToolFieldDefinition("execution_mode", new MCPToolValueDefinition(Type.STRING, "Optional execution mode override.", null), false),
                new MCPToolFieldDefinition("approved_steps",
                        new MCPToolValueDefinition(Type.ARRAY, "Approved execution steps.", new MCPToolValueDefinition(Type.STRING, "Step name.", null)), false)));
    }
}
