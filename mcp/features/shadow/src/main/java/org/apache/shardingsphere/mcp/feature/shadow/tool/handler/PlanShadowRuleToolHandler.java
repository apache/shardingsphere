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

package org.apache.shardingsphere.mcp.feature.shadow.tool.handler;

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for shadow rule workflow planning.
 */
public final class PlanShadowRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final ShadowWorkflowPlanningService planningService = new ShadowWorkflowPlanningService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        ShadowRuleWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ShadowRuleWorkflowRequest::new, arguments, this::bindFeatureArguments);
        WorkflowContextSnapshot snapshot = planningService.planRule(requestContext.getWorkflowSessionContext(), requestContext.getQueryFacade(), request);
        return new MCPMapPayload(WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ShadowRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.RULE_FIELD, request::setRuleName);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD, request::setSourceStorageUnit);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD, request::setShadowStorageUnit);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.TABLE_FIELD, request::setTableName);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, request::setAlgorithmType);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument(ShadowFeatureDefinition.ALGORITHM_PROPERTIES_FIELD));
    }
    
}
