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

package org.apache.shardingsphere.mcp.core.completion.handler;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionCandidate;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionHandlerResult;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionRequest;
import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowPlanIdCompletionHandlerTest {
    
    @Test
    void assertGetContextType() {
        assertThat(new WorkflowPlanIdCompletionHandler().getContextType(), is(MCPFeatureRequestContext.class));
    }
    
    @Test
    void assertSupports() {
        assertTrue(new WorkflowPlanIdCompletionHandler().supports(createRequestContext()));
    }
    
    @Test
    void assertComplete() {
        WorkflowSessionContext workflowSessionContext = mock(WorkflowSessionContext.class);
        when(workflowSessionContext.list()).thenReturn(List.of(
                createSnapshot("plan-ready", WorkflowLifecycle.STATUS_PLANNED),
                createSnapshot("plan-previewed", WorkflowLifecycle.STATUS_PREVIEWED),
                createSnapshot("plan-draft", WorkflowLifecycle.STATUS_CLARIFYING)));
        MCPFeatureRequestContext handlerContext = mock(MCPFeatureRequestContext.class);
        when(handlerContext.getSessionIdentity()).thenReturn(new MCPSessionIdentity("session-1", "", "", Map.of()));
        when(handlerContext.getWorkflowSessionContext()).thenReturn(workflowSessionContext);
        MCPCompletionHandlerResult actual = new WorkflowPlanIdCompletionHandler().complete(handlerContext, createRequestContext());
        List<MCPCompletionCandidate> actualCandidates = new ArrayList<>(actual.getCandidates());
        assertThat(actualCandidates.size(), is(2));
        assertThat(actualCandidates.get(0).getValue(), is("plan-ready"));
        assertThat(actualCandidates.get(1).getValue(), is("plan-previewed"));
        assertThat(actualCandidates.get(1).getLabel(), is("encrypt.rule previewed"));
        assertThat(actualCandidates.get(1).getRankingReason(), is("recent-plan-first-for-plan_id"));
    }
    
    private MCPCompletionRequest createRequestContext() {
        return new MCPCompletionRequest(new MCPCompletionTargetDescriptor("prompt", "recover_workflow", List.of("plan_id"), 50, Map.of()), "plan_id", Map.of());
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String status) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        result.setStatus(status);
        return result;
    }
}
