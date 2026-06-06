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

package org.apache.shardingsphere.mcp.feature.shadow;

import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.resource.handler.ShadowResourceHandler;
import org.apache.shardingsphere.mcp.feature.shadow.tool.handler.PlanDefaultShadowAlgorithmToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.tool.handler.PlanShadowAlgorithmCleanupToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.tool.handler.PlanShadowRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * Shadow MCP handler provider.
 */
public final class ShadowMCPHandlerProvider implements MCPHandlerProvider, MCPWorkflowDefinitionProvider {
    
    @Override
    public Collection<MCPResourceHandler<?>> getResourceHandlers() {
        return List.of(
                ShadowResourceHandler.rules(),
                ShadowResourceHandler.rule(),
                ShadowResourceHandler.tableRules(),
                ShadowResourceHandler.tableRule(),
                ShadowResourceHandler.algorithms(),
                ShadowResourceHandler.defaultAlgorithm(),
                ShadowResourceHandler.ruleCount(),
                ShadowResourceHandler.algorithmPlugins());
    }
    
    @Override
    public Collection<MCPToolHandler<?>> getToolHandlers() {
        return List.of(new PlanShadowRuleToolHandler(), new PlanDefaultShadowAlgorithmToolHandler(), new PlanShadowAlgorithmCleanupToolHandler());
    }
    
    @Override
    public Collection<WorkflowRuntimeDefinition> getWorkflowDefinitions() {
        final ShadowWorkflowValidationService validationService = new ShadowWorkflowValidationService();
        return List.of(
                new WorkflowRuntimeDefinition(ShadowFeatureDefinition.RULE_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShadowFeatureDefinition.DEFAULT_ALGORITHM_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShadowFeatureDefinition.ALGORITHM_CLEANUP_WORKFLOW_KIND, validationService));
    }
}
