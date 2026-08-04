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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingRuleWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for readwrite-splitting rule workflow planning.
 */
public final class PlanReadwriteSplittingRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final ReadwriteSplittingRuleWorkflowPlanningService planningService = new ReadwriteSplittingRuleWorkflowPlanningService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return ReadwriteSplittingFeatureDefinition.PLAN_RULE_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        ReadwriteSplittingRuleWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ReadwriteSplittingRuleWorkflowRequest::new, arguments, this::bindFeatureArguments);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext.getWorkflowSessionContext(), requestContext.getQueryFacade(), request);
        return new MCPMapPayload(WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ReadwriteSplittingRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.WRITE_STORAGE_UNIT_FIELD, request::setWriteStorageUnit);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.READ_STORAGE_UNITS_FIELD, request::setReadStorageUnits);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD, request::setTransactionalReadQueryStrategy);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_TYPE_FIELD, request::setLoadBalancerType);
        request.putLoadBalancerProperties(workflowPlanningArguments.getMapArgument(ReadwriteSplittingFeatureDefinition.LOAD_BALANCER_PROPERTIES_FIELD));
    }
    
}
