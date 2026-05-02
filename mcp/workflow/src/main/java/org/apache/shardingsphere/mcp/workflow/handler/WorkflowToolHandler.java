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

package org.apache.shardingsphere.mcp.workflow.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolContribution;
import org.apache.shardingsphere.mcp.workflow.MCPWorkflowContext;

/**
 * Handler for MCP tools that require workflow capabilities.
 */
public interface WorkflowToolHandler extends MCPToolContribution {
    
    /**
     * Handle one workflow-aware tool call.
     *
     * @param workflowContext workflow context
     * @param toolCall tool call
     * @return tool response
     */
    MCPResponse handle(MCPWorkflowContext workflowContext, MCPToolCall toolCall);
}
