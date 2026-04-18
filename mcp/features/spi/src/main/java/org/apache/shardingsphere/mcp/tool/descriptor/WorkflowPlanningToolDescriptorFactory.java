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

import java.util.LinkedList;
import java.util.List;

/**
 * Workflow planning tool descriptor factory.
 */
public final class WorkflowPlanningToolDescriptorFactory {
    
    private static final List<MCPToolFieldDefinition> COMMON_FIELDS = List.of(
            new MCPToolFieldDefinition("plan_id", new MCPToolValueDefinition(Type.STRING, "Optional existing workflow plan identifier.", null), false),
            new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Logical database name.", null), false),
            new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional logical schema name.", null), false),
            new MCPToolFieldDefinition("table", new MCPToolValueDefinition(Type.STRING, "Target logical table name.", null), false),
            new MCPToolFieldDefinition("column", new MCPToolValueDefinition(Type.STRING, "Target logical column name.", null), false),
            new MCPToolFieldDefinition("operation_type", new MCPToolValueDefinition(Type.STRING, "Lifecycle operation: create, alter or drop.", null), false),
            new MCPToolFieldDefinition("natural_language_intent", new MCPToolValueDefinition(Type.STRING, "Natural-language request used for intent clarification.", null), false),
            new MCPToolFieldDefinition("structured_intent_evidence", new MCPToolValueDefinition(Type.OBJECT, "Structured intent evidence extracted by the caller.", null), false),
            new MCPToolFieldDefinition("delivery_mode", new MCPToolValueDefinition(Type.STRING, "Delivery mode: all-at-once or step-by-step.", null), false),
            new MCPToolFieldDefinition("execution_mode", new MCPToolValueDefinition(Type.STRING, "Execution mode: auto-execute, review-then-execute or manual-only.", null), false));
    
    private WorkflowPlanningToolDescriptorFactory() {
    }
    
    /**
     * Create workflow planning descriptor.
     *
     * @param toolName tool name
     * @param featureFields feature-specific fields
     * @return MCP tool descriptor
     */
    public static MCPToolDescriptor create(final String toolName, final List<MCPToolFieldDefinition> featureFields) {
        List<MCPToolFieldDefinition> result = new LinkedList<>(COMMON_FIELDS);
        result.addAll(featureFields);
        return new MCPToolDescriptor(toolName, List.copyOf(result));
    }
}
