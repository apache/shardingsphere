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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingRuleWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tool handler for readwrite-splitting rule workflow planning.
 */
public final class PlanReadwriteSplittingRuleToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ReadwriteSplittingRuleWorkflowPlanningService planningService = new ReadwriteSplittingRuleWorkflowPlanningService();
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        ReadwriteSplittingRuleWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ReadwriteSplittingRuleWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.plan(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ReadwriteSplittingRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request::setWriteStorageUnit);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request::setReadStorageUnits);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD, request::setTransactionalReadQueryStrategy);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, request::setLoadBalancerType);
        request.putLoadBalancerProperties(workflowPlanningArguments.getMapArgument(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_PROPERTIES_FIELD));
    }
    
    private void applyStructuredIntentEvidence(final ReadwriteSplittingRuleWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request::setWriteStorageUnit);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request::setReadStorageUnits);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD, request::setTransactionalReadQueryStrategy);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, request::setLoadBalancerType);
        applyMapField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_PROPERTIES_FIELD, request::putLoadBalancerProperties);
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
            String key = Objects.toString(entry.getKey(), "").trim();
            if (!key.isEmpty()) {
                result.put(key, Objects.toString(entry.getValue(), "").trim());
            }
        }
        return result;
    }
}
