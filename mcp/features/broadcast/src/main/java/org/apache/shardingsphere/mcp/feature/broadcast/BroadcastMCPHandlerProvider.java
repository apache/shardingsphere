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

package org.apache.shardingsphere.mcp.feature.broadcast;

import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.resource.handler.BroadcastRuleCountHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.resource.handler.BroadcastRulesHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.resource.handler.BroadcastTableRuleHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.handler.PlanBroadcastRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * Broadcast MCP handler provider.
 */
public final class BroadcastMCPHandlerProvider implements MCPHandlerProvider, MCPWorkflowDefinitionProvider {
    
    @Override
    public Collection<MCPResourceHandler<?>> getResourceHandlers() {
        return List.of(new BroadcastRulesHandler(), new BroadcastTableRuleHandler(), new BroadcastRuleCountHandler());
    }
    
    @Override
    public Collection<MCPToolHandler<?>> getToolHandlers() {
        return List.of(new PlanBroadcastRuleToolHandler());
    }
    
    @Override
    public Collection<WorkflowRuntimeDefinition> getWorkflowDefinitions() {
        return List.of(new WorkflowRuntimeDefinition(BroadcastFeatureDefinition.WORKFLOW_KIND, new BroadcastWorkflowValidationService()));
    }
}
