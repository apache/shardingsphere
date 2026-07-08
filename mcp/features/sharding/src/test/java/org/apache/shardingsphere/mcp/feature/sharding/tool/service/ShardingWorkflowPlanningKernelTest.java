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

package org.apache.shardingsphere.mcp.feature.sharding.tool.service;

import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class ShardingWorkflowPlanningKernelTest {
    
    private final ShardingWorkflowPlanningKernel kernel = new ShardingWorkflowPlanningKernel();
    
    @Test
    void assertPlanTableRuleClarifiesMissingDatabase() {
        WorkflowContextSnapshot actual = kernel.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", new ShardingWorkflowRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_CLARIFYING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(actual.getRequest().getOperationType(), is("create"));
    }
    
    @Test
    void assertPlanComponentCleanupRejectsNonDropOperation() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setComponentType("algorithm");
        request.setComponentName("inline_algorithm");
        request.setOperationType("create");
        WorkflowContextSnapshot actual = kernel.planComponentCleanup(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), "session-1", request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertThat(actual.getIssues().getFirst().getDetails(), is(Map.of("operation_type", "create")));
    }
}
