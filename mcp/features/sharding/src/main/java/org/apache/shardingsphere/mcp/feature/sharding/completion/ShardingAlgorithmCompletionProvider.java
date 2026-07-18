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

package org.apache.shardingsphere.mcp.feature.sharding.completion;

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingInspectionService;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.spi.MCPCompletionProvider;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionProviderResult;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Sharding algorithm completion provider.
 */
public final class ShardingAlgorithmCompletionProvider implements MCPCompletionProvider<MCPFeatureRequestContext> {
    
    private static final String ALGORITHM_TYPE_FIELD = "algorithm_type";
    
    private static final String KEY_GENERATOR_TYPE_FIELD = "key_generator_type";
    
    private static final Set<String> SHARDING_ALGORITHM_PROMPTS = Set.of(
            ShardingFeatureDefinition.PLAN_TABLE_RULE_PROMPT_NAME, ShardingFeatureDefinition.PLAN_DEFAULT_STRATEGY_PROMPT_NAME);
    
    private static final Set<String> KEY_GENERATOR_PROMPTS = Set.of(
            ShardingFeatureDefinition.PLAN_KEY_GENERATOR_PROMPT_NAME, ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_PROMPT_NAME);
    
    private final ShardingInspectionService inspectionService = new ShardingInspectionService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public boolean supports(final MCPCompletionRequest request) {
        return isShardingAlgorithmCompletion(request) || isKeyGeneratorCompletion(request);
    }
    
    private boolean isShardingAlgorithmCompletion(final MCPCompletionRequest request) {
        String reference = request.getDescriptor().getReference();
        return ALGORITHM_TYPE_FIELD.equals(request.getArgumentName())
                && (SHARDING_ALGORITHM_PROMPTS.contains(reference) || ShardingFeatureDefinition.ALGORITHM_PLUGINS_RESOURCE_URI.equals(reference));
    }
    
    private boolean isKeyGeneratorCompletion(final MCPCompletionRequest request) {
        String reference = request.getDescriptor().getReference();
        return KEY_GENERATOR_TYPE_FIELD.equals(request.getArgumentName())
                && (KEY_GENERATOR_PROMPTS.contains(reference) || ShardingFeatureDefinition.KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI.equals(reference));
    }
    
    @Override
    public MCPCompletionProviderResult complete(final MCPFeatureRequestContext handlerContext, final MCPCompletionRequest request) {
        return new MCPCompletionProviderResult(queryPlugins(handlerContext, request).stream()
                .map(each -> createAlgorithmCandidate(each, request.getArgumentName())).filter(each -> !each.getValue().isEmpty()).toList());
    }
    
    private List<Map<String, Object>> queryPlugins(final MCPFeatureRequestContext handlerContext, final MCPCompletionRequest request) {
        return KEY_GENERATOR_TYPE_FIELD.equals(request.getArgumentName())
                ? inspectionService.queryKeyGenerateAlgorithmPlugins(handlerContext.getQueryFacade())
                : inspectionService.queryAlgorithmPlugins(handlerContext.getQueryFacade());
    }
    
    private MCPCompletionCandidate createAlgorithmCandidate(final Map<String, Object> row, final String argumentName) {
        String value = Objects.toString(row.getOrDefault("type", row.getOrDefault("name", "")), "").trim();
        String label = Objects.toString(row.getOrDefault("description", "sharding algorithm"), "sharding algorithm");
        return new MCPCompletionCandidate(value, label, KEY_GENERATOR_TYPE_FIELD.equals(argumentName) ? "sharding-key-generate-algorithm" : "sharding-algorithm");
    }
}
