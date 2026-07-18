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

package org.apache.shardingsphere.mcp.core.resource.handler.workflow;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowPlanHandlerTest {
    
    @Test
    void assertHandle() {
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of());
        sessionManager.createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(sessionManager, new MCPDatabaseCapabilityProvider(Map.of()), MCPTransportType.HTTP);
        runtimeContext.getWorkflowSessionContext("session-1").save(createSnapshot());
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(runtimeContext, sessionManager.getRequiredSessionIdentity("session-1"));
        Map<String, Object> actual = new WorkflowPlanHandler().handle(requestContext, new MCPUriVariables(Map.of("plan_id", "plan-1"))).toPayload();
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(actual.get("workflow_kind"), is("mask.rule"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("next_actions")).get(0)).get("tool_name"), is("database_gateway_apply_workflow"));
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setWorkflowKind(WorkflowKind.valueOf("mask.rule"));
        result.setStatus(WorkflowLifecycle.STATUS_PLANNED);
        result.setRequest(request);
        result.setClarifiedIntent(new ClarifiedIntent());
        result.setInteractionPlan(InteractionPlan.create("plan-1", request, "Mask workflow plan.", List.of("Review"), List.of("rules")));
        return result;
    }
}
