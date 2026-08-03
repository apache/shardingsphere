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

import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    public ShardingWorkflowRequest bindTableRule(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindTableRuleArguments);
    }
    
    /**
     * Bind sharding table reference rule workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding table reference rule workflow request
     */
    public ShardingWorkflowRequest bindTableReferenceRule(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindTableReferenceRuleArguments);
    }
    
    /**
     * Bind sharding default strategy workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding default strategy workflow request
     */
    public ShardingWorkflowRequest bindDefaultStrategy(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindDefaultStrategyArguments);
    }
    
    /**
     * Bind sharding key generator workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding key generator workflow request
     */
    public ShardingWorkflowRequest bindKeyGenerator(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindKeyGeneratorArguments);
    }
    
    /**
     * Bind sharding key generate strategy workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding key generate strategy workflow request
     */
    public ShardingWorkflowRequest bindKeyGenerateStrategy(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindKeyGenerateStrategyArguments);
    }
    
    /**
     * Bind sharding rule component cleanup workflow request.
     *
     * @param arguments raw MCP arguments
     * @return sharding rule component cleanup workflow request
     */
    public ShardingWorkflowRequest bindRuleComponentCleanup(final Map<String, Object> arguments) {
        return WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, arguments, this::bindRuleComponentCleanupArguments);
    }
    
    private void bindTableRuleArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("rule", request::setRuleName);
        workflowPlanningArguments.applyStringArgument("data_nodes", request::setDataNodes);
        workflowPlanningArguments.applyStringArgument("storage_units", request::setStorageUnits);
        workflowPlanningArguments.applyStringArgument("strategy_type", request::setStrategyType);
        workflowPlanningArguments.applyStringArgument("sharding_columns", request::setShardingColumns);
        workflowPlanningArguments.applyStringArgument("algorithm_type", request::setAlgorithmType);
        workflowPlanningArguments.applyStringArgument("key_generate_column", request::setKeyGenerateColumn);
        workflowPlanningArguments.applyStringArgument("key_generator", request::setKeyGeneratorName);
        workflowPlanningArguments.applyStringArgument("key_generator_type", request::setKeyGeneratorType);
        workflowPlanningArguments.applyStringArgument("allow_hint_disable", request::setAllowHintDisable);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
        replaceValues(request.getAuditorNames(), workflowPlanningArguments.getStringArgument("auditors"));
    }
    
    private void bindTableReferenceRuleArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("rule", request::setRuleName);
        replaceValues(request.getReferenceTables(), workflowPlanningArguments.getStringArgument("reference_tables"));
    }
    
    private void bindDefaultStrategyArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("default_strategy_type", request::setDefaultStrategyType);
        workflowPlanningArguments.applyStringArgument("strategy_type", request::setStrategyType);
        workflowPlanningArguments.applyStringArgument("sharding_columns", request::setShardingColumns);
        workflowPlanningArguments.applyStringArgument("algorithm_type", request::setAlgorithmType);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
    }
    
    private void bindKeyGeneratorArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("key_generator", request::setKeyGeneratorName);
        workflowPlanningArguments.applyStringArgument("key_generator_type", request::setKeyGeneratorType);
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
    }
    
    private void bindKeyGenerateStrategyArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("key_generate_strategy", request::setKeyGenerateStrategyName);
        workflowPlanningArguments.applyStringArgument("sequence", request::setSequenceName);
        workflowPlanningArguments.applyStringArgument("key_generator", request::setKeyGeneratorName);
        workflowPlanningArguments.applyStringArgument("key_generator_type", request::setKeyGeneratorType);
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
    }
    
    private void bindRuleComponentCleanupArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument("component_type", request::setComponentType);
        workflowPlanningArguments.applyStringArgument("component_name", request::setComponentName);
    }
    
    private void replaceValues(final Collection<String> target, final Object value) {
        List<String> values = createStringList(value);
        if (!values.isEmpty()) {
            target.clear();
            target.addAll(values);
        }
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
    
}
