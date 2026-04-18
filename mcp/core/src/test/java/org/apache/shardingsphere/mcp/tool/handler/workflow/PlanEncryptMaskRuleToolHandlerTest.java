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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.service.workflow.AlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PlanEncryptMaskRuleToolHandlerTest {
    
    @Test
    void assertHandleMasksSensitivePropertyPreviewAndDistSql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("review");
        interactionPlan.getSteps().add("step-1");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        snapshot.setRequest(request);
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES secret key.", ""));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t_order (COLUMNS((NAME=phone, ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))))))"));
        WorkflowPlanningService planningService = mock(WorkflowPlanningService.class);
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        when(planningService.plan(org.mockito.ArgumentMatchers.same(requestContext), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(snapshot);
        PlanEncryptMaskRuleToolHandler handler = new PlanEncryptMaskRuleToolHandler(planningService, new AlgorithmPropertyTemplateService());
        Map<String, Object> actual = handler.handle(requestContext, "session-1", Map.of("database", "logic_db")).toPayload();
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(String.valueOf(((Map<?, ?>) ((Map<?, ?>) actual.get("masked_property_preview")).get("primary")).get("aes-key-value")), is("******"));
        assertThat(String.valueOf(((Map<?, ?>) ((List<?>) actual.get("distsql_artifacts")).get(0)).get("sql")), containsString("******"));
    }
    
    @Test
    void assertGetToolDescriptor() {
        assertThat(new PlanEncryptMaskRuleToolHandler().getToolDescriptor().getName(), is("plan_encrypt_mask_rule"));
    }
    
    @Test
    void assertHandlePrefersFeatureTypeAndStructuredIntentEvidence() {
        WorkflowPlanningService planningService = mock(WorkflowPlanningService.class);
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-2");
        snapshot.setStatus("clarifying");
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        when(planningService.plan(org.mockito.ArgumentMatchers.same(requestContext), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.argThat(request -> "encrypt".equals(request.getFeatureType())
                        && Boolean.TRUE.equals(request.getRequiresEqualityFilter())
                        && Boolean.FALSE.equals(request.getRequiresLikeQuery())
                        && "phone".equals(request.getFieldSemantics())
                        && "给手机号加密".equals(request.getRawUserRequest()))))
                .thenReturn(snapshot);
        PlanEncryptMaskRuleToolHandler handler = new PlanEncryptMaskRuleToolHandler(planningService, new AlgorithmPropertyTemplateService());
        Map<String, Object> actual = handler.handle(requestContext, "session-1", Map.of(
                "feature_type", "encrypt",
                "raw_user_request", "给手机号加密",
                "structured_intent_evidence", Map.of(
                        "requires_equality_filter", true,
                        "requires_like_query", false,
                        "field_semantics", "phone")))
                .toPayload();
        assertThat(actual.get("plan_id"), is("plan-2"));
    }
    
    @Test
    void assertHandleAcceptsLegacyIntentTypeArgument() {
        WorkflowPlanningService planningService = mock(WorkflowPlanningService.class);
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-3");
        snapshot.setStatus("planned");
        MCPRequestContext requestContext = mock(MCPRequestContext.class);
        when(planningService.plan(org.mockito.ArgumentMatchers.same(requestContext), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.argThat(request -> "mask".equals(request.getFeatureType()) && "drop".equals(request.getOperationType()))))
                .thenReturn(snapshot);
        PlanEncryptMaskRuleToolHandler handler = new PlanEncryptMaskRuleToolHandler(planningService, new AlgorithmPropertyTemplateService());
        Map<String, Object> actual = handler.handle(requestContext, "session-1", Map.of("intent_type", "mask", "operation_type", "drop")).toPayload();
        assertThat(actual.get("plan_id"), is("plan-3"));
    }
}
