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

import java.util.Map;
import java.util.function.Consumer;

/**
 * Tool handler for readwrite-splitting rule workflow planning.
 */
public final class PlanReadwriteSplittingRuleToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ReadwriteSplittingRuleWorkflowPlanningService planningService;
    
    public PlanReadwriteSplittingRuleToolHandler() {
        planningService = new ReadwriteSplittingRuleWorkflowPlanningService();
    }
    
    PlanReadwriteSplittingRuleToolHandler(final ReadwriteSplittingRuleWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
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
                this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyUserOverrides);
        WorkflowContextSnapshot snapshot = planningService.plan(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ReadwriteSplittingRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request::setWriteStorageUnit);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request::setReadStorageUnits);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD, request::setTransactionalReadQueryStrategy);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, request::setLoadBalancerType);
        request.putLoadBalancerProperties(workflowPlanningArguments.getMapArgument(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_PROPERTIES_FIELD));
    }
    
    private void applyStructuredIntentEvidence(final ReadwriteSplittingRuleWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request::setWriteStorageUnit);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request::setReadStorageUnits);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD, request::setTransactionalReadQueryStrategy);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, request::setLoadBalancerType);
    }
    
    private void applyUserOverrides(final ReadwriteSplittingRuleWorkflowRequest request, final Map<String, Object> userOverrides) {
        applyStructuredIntentEvidence(request, userOverrides);
    }
    
    private void applyStringField(final Map<String, Object> values, final String fieldName, final Consumer<String> consumer) {
        Object value = values.get(fieldName);
        if (null != value) {
            consumer.accept(String.valueOf(value));
        }
    }
    
    private void applyStringArgument(final WorkflowPlanningArguments workflowPlanningArguments, final String fieldName, final Consumer<String> consumer) {
        final String value = workflowPlanningArguments.getStringArgument(fieldName);
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }
}
