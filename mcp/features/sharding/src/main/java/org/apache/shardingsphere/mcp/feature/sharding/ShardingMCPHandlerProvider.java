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

import org.apache.shardingsphere.mcp.spi.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandler;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.completion.ShardingAlgorithmCompletionHandler;
import org.apache.shardingsphere.mcp.feature.sharding.resource.handler.ShardingAlgorithmResourceHandler;
import org.apache.shardingsphere.mcp.feature.sharding.resource.handler.ShardingGovernanceResourceHandler;
import org.apache.shardingsphere.mcp.feature.sharding.resource.handler.ShardingStrategyResourceHandler;
import org.apache.shardingsphere.mcp.feature.sharding.resource.handler.ShardingTableResourceHandler;
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
                ShardingAlgorithmResourceHandler.algorithmPlugins(),
                ShardingAlgorithmResourceHandler.keyGenerateAlgorithmPlugins(),
                ShardingAlgorithmResourceHandler.algorithms(),
                ShardingTableResourceHandler.tableRules(),
                ShardingTableResourceHandler.tableRule(),
                ShardingTableResourceHandler.tableNodes(),
                ShardingTableResourceHandler.tableNode(),
                ShardingTableResourceHandler.tableReferenceRules(),
                ShardingTableResourceHandler.tableReferenceRule(),
                ShardingStrategyResourceHandler.defaultStrategy(),
                ShardingStrategyResourceHandler.keyGenerators(),
                ShardingStrategyResourceHandler.keyGenerator(),
                ShardingStrategyResourceHandler.keyGenerateStrategies(),
                ShardingStrategyResourceHandler.keyGenerateStrategy(),
                ShardingGovernanceResourceHandler.auditors(),
                ShardingAlgorithmResourceHandler.unusedAlgorithms(),
                ShardingStrategyResourceHandler.unusedKeyGenerators(),
                ShardingGovernanceResourceHandler.unusedAuditors(),
                ShardingAlgorithmResourceHandler.algorithmUsedTableRules(),
                ShardingStrategyResourceHandler.keyGeneratorUsedTableRules(),
                ShardingGovernanceResourceHandler.auditorUsedTableRules(),
                ShardingGovernanceResourceHandler.ruleCount());
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
    public Collection<MCPCompletionHandler<?>> getCompletionHandlers() {
        return List.of(new ShardingAlgorithmCompletionHandler());
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
