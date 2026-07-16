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

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;

class ShardingWorkflowPlanningServiceTest {
    
    private final ShardingWorkflowPlanningService planningService = new ShardingWorkflowPlanningService();
    
    @Test
    void assertPlanTableRuleClarifiesMissingDatabase() {
        WorkflowContextSnapshot actual = planningService.planTableRule(
                new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), new ShardingWorkflowRequest());
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_CLARIFYING));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(actual.getRequest().getOperationType(), is("create"));
        assertThat(actual.getResourceUriTemplates(), is(List.of(ShardingFeatureDefinition.STORAGE_UNITS_RESOURCE_URI,
                ShardingFeatureDefinition.SINGLE_TABLES_RESOURCE_URI, ShardingFeatureDefinition.SINGLE_TABLE_RESOURCE_URI)));
    }
    
    @Test
    void assertPlanTableRuleRejectsUnsupportedOperation() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setOperationType("replace");
        WorkflowContextSnapshot actual = planningService.planTableRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
        assertThat(actual.getClarifiedIntent().getOperationType(), is(""));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertRuleDistSQLOnlyPayloadDoesNotExpose(actual, "replace");
    }
    
    @Test
    void assertPlanTableReferenceRuleClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planTableReferenceRule(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.table.reference"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide table reference rule name.")));
    }
    
    @Test
    void assertPlanDefaultStrategyClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planDefaultStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.default.strategy"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide DATABASE or TABLE default strategy type.")));
    }
    
    @Test
    void assertPlanKeyGeneratorClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planKeyGenerator(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.key.generator"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide key generator name.")));
    }
    
    @Test
    void assertPlanKeyGenerateStrategyClarifiesMissingInputs() {
        ShardingWorkflowRequest request = createDatabaseRequest();
        WorkflowContextSnapshot actual = planningService.planKeyGenerateStrategy(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getWorkflowKind().getValue(), is("sharding.key.generate.strategy"));
        assertThat(actual.getClarifiedIntent().getClarificationMessages(), is(List.of("Please provide key generate strategy name.")));
    }
    
    @Test
    void assertPlanComponentCleanupRejectsNonDropOperation() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setComponentType("algorithm");
        request.setComponentName("inline_algorithm");
        request.setOperationType("create");
        WorkflowContextSnapshot actual = planningService.planComponentCleanup(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getInteractionPlan().getCurrentStep(), is(WorkflowLifecycle.STEP_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertThat(actual.getIssues().getFirst().getDetails(), is(Map.of("operation_type", "create")));
    }
    
    private ShardingWorkflowRequest createDatabaseRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        return result;
    }
    
    private void assertRuleDistSQLOnlyPayloadDoesNotExpose(final WorkflowContextSnapshot snapshot, final String term) {
        Map<String, Object> actualPayload = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest());
        assertFalse(String.valueOf(actualPayload).toLowerCase(Locale.ENGLISH).contains(term));
    }
}
