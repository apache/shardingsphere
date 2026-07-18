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

package org.apache.shardingsphere.mcp.feature.readwritesplitting;

import org.apache.shardingsphere.mcp.spi.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.LoadBalanceAlgorithmPluginsHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.ReadwriteSplittingRuleCountHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.ReadwriteSplittingRuleHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.ReadwriteSplittingRuleStatusHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.ReadwriteSplittingRulesHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.resource.handler.ReadwriteSplittingStatusHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.handler.PlanReadwriteSplittingRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.handler.PlanReadwriteSplittingStatusToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingRuleWorkflowValidationService;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingStatusWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * Readwrite-splitting MCP handler provider.
 */
public final class ReadwriteSplittingMCPHandlerProvider implements MCPHandlerProvider, MCPWorkflowDefinitionProvider {
    
    @Override
    public Collection<MCPResourceHandler<?>> getResourceHandlers() {
        return List.of(new ReadwriteSplittingRulesHandler(), new ReadwriteSplittingRuleHandler(), new ReadwriteSplittingStatusHandler(),
                new ReadwriteSplittingRuleStatusHandler(), new ReadwriteSplittingRuleCountHandler(), new LoadBalanceAlgorithmPluginsHandler());
    }
    
    @Override
    public Collection<MCPToolHandler<?>> getToolHandlers() {
        return List.of(new PlanReadwriteSplittingRuleToolHandler(), new PlanReadwriteSplittingStatusToolHandler());
    }
    
    @Override
    public Collection<WorkflowRuntimeDefinition> getWorkflowDefinitions() {
        return List.of(
                new WorkflowRuntimeDefinition(ReadwriteSplittingFeatureDefinition.RULE_WORKFLOW_KIND, new ReadwriteSplittingRuleWorkflowValidationService()),
                new WorkflowRuntimeDefinition(ReadwriteSplittingFeatureDefinition.STATUS_WORKFLOW_KIND, new ReadwriteSplittingStatusWorkflowValidationService()));
    }
}
