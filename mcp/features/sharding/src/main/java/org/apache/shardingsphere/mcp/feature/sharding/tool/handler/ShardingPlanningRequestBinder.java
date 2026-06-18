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

package org.apache.shardingsphere.mcp.feature.sharding.tool.handler;

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingDefaultStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGenerateStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGeneratorWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingRuleComponentCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingTableReferenceRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingTableRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Sharding planning request binder.
 */
public final class ShardingPlanningRequestBinder {
    
    /**
     * Bind sharding table rule workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding table rule workflow request
     */
    public ShardingTableRuleWorkflowRequest bindTableRule(final Map<String, Object> arguments) {
        return new ShardingTableRuleWorkflowRequest(bind(arguments, this::bindTableRuleArguments, this::applyTableRuleEvidence));
    }
    
    /**
     * Bind sharding table reference rule workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding table reference rule workflow request
     */
    public ShardingTableReferenceRuleWorkflowRequest bindTableReferenceRule(final Map<String, Object> arguments) {
        return new ShardingTableReferenceRuleWorkflowRequest(bind(arguments, this::bindTableReferenceRuleArguments, this::applyTableReferenceRuleEvidence));
    }
    
    /**
     * Bind sharding default strategy workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding default strategy workflow request
     */
    public ShardingDefaultStrategyWorkflowRequest bindDefaultStrategy(final Map<String, Object> arguments) {
        return new ShardingDefaultStrategyWorkflowRequest(bind(arguments, this::bindDefaultStrategyArguments, this::applyDefaultStrategyEvidence));
    }
    
    /**
     * Bind sharding key generator workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding key generator workflow request
     */
    public ShardingKeyGeneratorWorkflowRequest bindKeyGenerator(final Map<String, Object> arguments) {
        return new ShardingKeyGeneratorWorkflowRequest(bind(arguments, this::bindKeyGeneratorArguments, this::applyKeyGeneratorEvidence));
    }
    
    /**
     * Bind sharding key generate strategy workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding key generate strategy workflow request
     */
    public ShardingKeyGenerateStrategyWorkflowRequest bindKeyGenerateStrategy(final Map<String, Object> arguments) {
        return new ShardingKeyGenerateStrategyWorkflowRequest(bind(arguments, this::bindKeyGenerateStrategyArguments, this::applyKeyGenerateStrategyEvidence));
    }
    
    /**
     * Bind sharding rule component cleanup workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding rule component cleanup workflow request
     */
    public ShardingRuleComponentCleanupWorkflowRequest bindRuleComponentCleanup(final Map<String, Object> arguments) {
        return new ShardingRuleComponentCleanupWorkflowRequest(bind(arguments, this::bindRuleComponentCleanupArguments, this::applyRuleComponentCleanupEvidence));
    }
    
    private ShardingWorkflowRequest bind(final Map<String, Object> arguments, final FeatureArgumentBinder argumentBinder, final EvidenceBinder evidenceBinder) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, argumentBinder::bind, evidenceBinder::bind, evidenceBinder::bind);
    }
    
    private void bindTableRuleArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "rule", request::setRuleName);
        applyStringArgument(workflowPlanningArguments, "data_nodes", request::setDataNodes);
        applyStringArgument(workflowPlanningArguments, "storage_units", request::setStorageUnits);
        applyStringArgument(workflowPlanningArguments, "strategy_type", request::setStrategyType);
        applyStringArgument(workflowPlanningArguments, "sharding_columns", request::setShardingColumns);
        applyStringArgument(workflowPlanningArguments, "algorithm_type", request::setAlgorithmType);
        applyStringArgument(workflowPlanningArguments, "key_generate_column", request::setKeyGenerateColumn);
        applyStringArgument(workflowPlanningArguments, "key_generator", request::setKeyGeneratorName);
        applyStringArgument(workflowPlanningArguments, "key_generator_type", request::setKeyGeneratorType);
        applyStringArgument(workflowPlanningArguments, "allow_hint_disable", request::setAllowHintDisable);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
        request.getAuditorNames().addAll(createStringList(workflowPlanningArguments.getStringArgument("auditors")));
    }
    
    private void applyTableRuleEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "table", request::setTable);
        applyStringField(values, "column", request::setColumn);
        applyStringField(values, "rule", request::setRuleName);
        applyStringField(values, "data_nodes", request::setDataNodes);
        applyStringField(values, "storage_units", request::setStorageUnits);
        applyStringField(values, "strategy_type", request::setStrategyType);
        applyStringField(values, "sharding_columns", request::setShardingColumns);
        applyStringField(values, "algorithm_type", request::setAlgorithmType);
        applyStringField(values, "key_generate_column", request::setKeyGenerateColumn);
        applyStringField(values, "key_generator", request::setKeyGeneratorName);
        applyStringField(values, "key_generator_type", request::setKeyGeneratorType);
        applyStringField(values, "allow_hint_disable", request::setAllowHintDisable);
        applyMapField(values, "algorithm_properties", request::putAlgorithmProperties);
        applyMapField(values, "key_generator_properties", request::putKeyGeneratorProperties);
        request.getAuditorNames().addAll(createStringList(values.get("auditors")));
    }
    
    private void bindTableReferenceRuleArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "rule", request::setRuleName);
        request.getReferenceTables().addAll(createStringList(workflowPlanningArguments.getStringArgument("reference_tables")));
    }
    
    private void applyTableReferenceRuleEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "rule", request::setRuleName);
        request.getReferenceTables().addAll(createStringList(values.get("reference_tables")));
    }
    
    private void bindDefaultStrategyArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "default_strategy_type", request::setDefaultStrategyType);
        applyStringArgument(workflowPlanningArguments, "strategy_type", request::setStrategyType);
        applyStringArgument(workflowPlanningArguments, "sharding_columns", request::setShardingColumns);
        applyStringArgument(workflowPlanningArguments, "algorithm_type", request::setAlgorithmType);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
    }
    
    private void applyDefaultStrategyEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "column", request::setColumn);
        applyStringField(values, "default_strategy_type", request::setDefaultStrategyType);
        applyStringField(values, "strategy_type", request::setStrategyType);
        applyStringField(values, "sharding_columns", request::setShardingColumns);
        applyStringField(values, "algorithm_type", request::setAlgorithmType);
        applyMapField(values, "algorithm_properties", request::putAlgorithmProperties);
    }
    
    private void bindKeyGeneratorArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "key_generator", request::setKeyGeneratorName);
        applyStringArgument(workflowPlanningArguments, "key_generator_type", request::setKeyGeneratorType);
        applyStringArgument(workflowPlanningArguments, "algorithm_type", request::setAlgorithmType);
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
    }
    
    private void applyKeyGeneratorEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "key_generator", request::setKeyGeneratorName);
        applyStringField(values, "key_generator_type", request::setKeyGeneratorType);
        applyStringField(values, "algorithm_type", request::setAlgorithmType);
        applyMapField(values, "key_generator_properties", request::putKeyGeneratorProperties);
        applyMapField(values, "algorithm_properties", request::putAlgorithmProperties);
    }
    
    private void bindKeyGenerateStrategyArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "key_generate_strategy", request::setKeyGenerateStrategyName);
        applyStringArgument(workflowPlanningArguments, "sequence", request::setSequenceName);
        applyStringArgument(workflowPlanningArguments, "key_generator", request::setKeyGeneratorName);
        applyStringArgument(workflowPlanningArguments, "key_generator_type", request::setKeyGeneratorType);
        applyStringArgument(workflowPlanningArguments, "algorithm_type", request::setAlgorithmType);
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
    }
    
    private void applyKeyGenerateStrategyEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "table", request::setTable);
        applyStringField(values, "column", request::setColumn);
        applyStringField(values, "key_generate_strategy", request::setKeyGenerateStrategyName);
        applyStringField(values, "sequence", request::setSequenceName);
        applyStringField(values, "key_generator", request::setKeyGeneratorName);
        applyStringField(values, "key_generator_type", request::setKeyGeneratorType);
        applyStringField(values, "algorithm_type", request::setAlgorithmType);
        applyMapField(values, "key_generator_properties", request::putKeyGeneratorProperties);
        applyMapField(values, "algorithm_properties", request::putAlgorithmProperties);
    }
    
    private void bindRuleComponentCleanupArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "component_type", request::setComponentType);
        applyStringArgument(workflowPlanningArguments, "component_name", request::setComponentName);
    }
    
    private void applyRuleComponentCleanupEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "component_type", request::setComponentType);
        applyStringField(values, "component_name", request::setComponentName);
    }
    
    private void applyStringArgument(final WorkflowPlanningArguments workflowPlanningArguments, final String fieldName, final Consumer<String> consumer) {
        String value = workflowPlanningArguments.getStringArgument(fieldName);
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }
    
    private void applyStringField(final Map<String, Object> values, final String fieldName, final Consumer<String> consumer) {
        Object value = values.get(fieldName);
        if (null != value) {
            consumer.accept(String.valueOf(value));
        }
    }
    
    private void applyMapField(final Map<String, Object> values, final String fieldName, final Consumer<Map<String, String>> consumer) {
        Object value = values.get(fieldName);
        if (value instanceof Map) {
            consumer.accept(createStringMap((Map<?, ?>) value));
        }
    }
    
    private Map<String, String> createStringMap(final Map<?, ?> values) {
        Map<String, String> result = new LinkedHashMap<>(values.size(), 1F);
        for (Entry<?, ?> entry : values.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
    
    private List<String> createStringList(final Object value) {
        if (value instanceof List) {
            return ((List<?>) value).stream().map(String::valueOf).map(String::trim).filter(each -> !each.isEmpty()).toList();
        }
        if (null == value) {
            return List.of();
        }
        return Stream.of(String.valueOf(value).split(",")).map(String::trim).filter(each -> !each.isEmpty()).toList();
    }
    
    @FunctionalInterface
    private interface FeatureArgumentBinder {
        
        void bind(ShardingWorkflowRequest request, WorkflowPlanningArguments workflowPlanningArguments);
    }
    
    @FunctionalInterface
    private interface EvidenceBinder {
        
        void bind(ShardingWorkflowRequest request, Map<String, Object> values);
    }
}
