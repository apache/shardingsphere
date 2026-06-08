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

package org.apache.shardingsphere.mcp.feature.sharding;

import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.resource.handler.ShardingResourceHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingDefaultStrategyToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingKeyGenerateStrategyToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingKeyGeneratorToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingRuleComponentCleanupToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingTableReferenceRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.handler.PlanShardingTableRuleToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowValidationService;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowDefinitionProvider;
import org.apache.shardingsphere.mcp.support.workflow.spi.WorkflowRuntimeDefinition;

import java.util.Collection;
import java.util.List;

/**
 * Sharding MCP handler provider.
 */
public final class ShardingMCPHandlerProvider implements MCPHandlerProvider, MCPWorkflowDefinitionProvider {
    
    @Override
    public Collection<MCPResourceHandler<?>> getResourceHandlers() {
        return List.of(
                ShardingResourceHandler.algorithmPlugins(),
                ShardingResourceHandler.keyGenerateAlgorithmPlugins(),
                ShardingResourceHandler.algorithms(),
                ShardingResourceHandler.tableRules(),
                ShardingResourceHandler.tableRule(),
                ShardingResourceHandler.tableNodes(),
                ShardingResourceHandler.tableNode(),
                ShardingResourceHandler.tableReferenceRules(),
                ShardingResourceHandler.tableReferenceRule(),
                ShardingResourceHandler.defaultStrategy(),
                ShardingResourceHandler.keyGenerators(),
                ShardingResourceHandler.keyGenerator(),
                ShardingResourceHandler.keyGenerateStrategies(),
                ShardingResourceHandler.keyGenerateStrategy(),
                ShardingResourceHandler.auditors(),
                ShardingResourceHandler.unusedAlgorithms(),
                ShardingResourceHandler.unusedKeyGenerators(),
                ShardingResourceHandler.unusedAuditors(),
                ShardingResourceHandler.algorithmUsedTableRules(),
                ShardingResourceHandler.keyGeneratorUsedTableRules(),
                ShardingResourceHandler.auditorUsedTableRules(),
                ShardingResourceHandler.ruleCount());
    }
    
    @Override
    public Collection<MCPToolHandler<?>> getToolHandlers() {
        return List.of(
                new PlanShardingTableRuleToolHandler(),
                new PlanShardingTableReferenceRuleToolHandler(),
                new PlanShardingDefaultStrategyToolHandler(),
                new PlanShardingKeyGeneratorToolHandler(),
                new PlanShardingKeyGenerateStrategyToolHandler(),
                new PlanShardingRuleComponentCleanupToolHandler());
    }
    
    @Override
    public Collection<WorkflowRuntimeDefinition> getWorkflowDefinitions() {
        ShardingWorkflowValidationService validationService = new ShardingWorkflowValidationService();
        return List.of(
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, validationService),
                new WorkflowRuntimeDefinition(ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND, validationService));
    }
}
