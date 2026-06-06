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

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.stream.Stream;

abstract class AbstractShardingPlanningToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ShardingWorkflowPlanningService planningService;
    
    AbstractShardingPlanningToolHandler() {
        planningService = new ShardingWorkflowPlanningService();
    }
    
    AbstractShardingPlanningToolHandler(final ShardingWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        ShardingWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ShardingWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = plan(workflowContext, toolCall, request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder().buildPlanResponse(snapshot));
    }
    
    protected abstract WorkflowContextSnapshot plan(MCPWorkflowHandlerContext workflowContext, MCPToolCall toolCall, ShardingWorkflowRequest request);
    
    protected ShardingWorkflowPlanningService getPlanningService() {
        return planningService;
    }
    
    private void bindFeatureArguments(final ShardingWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, "rule", request::setRuleName);
        applyStringArgument(workflowPlanningArguments, "data_nodes", request::setDataNodes);
        applyStringArgument(workflowPlanningArguments, "storage_units", request::setStorageUnits);
        applyStringArgument(workflowPlanningArguments, "strategy_type", request::setStrategyType);
        applyStringArgument(workflowPlanningArguments, "sharding_columns", request::setShardingColumns);
        applyStringArgument(workflowPlanningArguments, "default_strategy_type", request::setDefaultStrategyType);
        applyStringArgument(workflowPlanningArguments, "algorithm_type", request::setAlgorithmType);
        applyStringArgument(workflowPlanningArguments, "key_generate_column", request::setKeyGenerateColumn);
        applyStringArgument(workflowPlanningArguments, "key_generator", request::setKeyGeneratorName);
        applyStringArgument(workflowPlanningArguments, "key_generator_type", request::setKeyGeneratorType);
        applyStringArgument(workflowPlanningArguments, "sequence", request::setSequenceName);
        applyStringArgument(workflowPlanningArguments, "key_generate_strategy", request::setKeyGenerateStrategyName);
        applyStringArgument(workflowPlanningArguments, "component_type", request::setComponentType);
        applyStringArgument(workflowPlanningArguments, "component_name", request::setComponentName);
        applyStringArgument(workflowPlanningArguments, "allow_hint_disable", request::setAllowHintDisable);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument("algorithm_properties"));
        request.putKeyGeneratorProperties(workflowPlanningArguments.getMapArgument("key_generator_properties"));
        request.getReferenceTables().addAll(createStringList(workflowPlanningArguments.getStringArgument("reference_tables")));
        request.getAuditorNames().addAll(createStringList(workflowPlanningArguments.getStringArgument("auditors")));
    }
    
    private void applyStructuredIntentEvidence(final ShardingWorkflowRequest request, final Map<String, Object> values) {
        applyStringField(values, "table", request::setTable);
        applyStringField(values, "column", request::setColumn);
        applyStringField(values, "rule", request::setRuleName);
        applyStringField(values, "data_nodes", request::setDataNodes);
        applyStringField(values, "storage_units", request::setStorageUnits);
        applyStringField(values, "strategy_type", request::setStrategyType);
        applyStringField(values, "sharding_columns", request::setShardingColumns);
        applyStringField(values, "default_strategy_type", request::setDefaultStrategyType);
        applyStringField(values, "algorithm_type", request::setAlgorithmType);
        applyStringField(values, "key_generate_column", request::setKeyGenerateColumn);
        applyStringField(values, "key_generator", request::setKeyGeneratorName);
        applyStringField(values, "key_generator_type", request::setKeyGeneratorType);
        applyStringField(values, "sequence", request::setSequenceName);
        applyStringField(values, "key_generate_strategy", request::setKeyGenerateStrategyName);
        applyStringField(values, "component_type", request::setComponentType);
        applyStringField(values, "component_name", request::setComponentName);
        applyStringField(values, "allow_hint_disable", request::setAllowHintDisable);
        applyMapField(values, "algorithm_properties", request::putAlgorithmProperties);
        applyMapField(values, "key_generator_properties", request::putKeyGeneratorProperties);
        request.getReferenceTables().addAll(createStringList(values.get("reference_tables")));
        request.getAuditorNames().addAll(createStringList(values.get("auditors")));
    }
    
    private void applyStringArgument(final WorkflowPlanningArguments workflowPlanningArguments, final String fieldName, final Consumer<String> consumer) {
        final String value = workflowPlanningArguments.getStringArgument(fieldName);
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
}
