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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ReadwriteSplittingWorkflowPlanningServicesTest {
    
    @Test
    void assertPlanCreateRule() {
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), createRuleRequest("create"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("readwrite.rule"));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(),
                is("CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC')"));
        assertFalse(actual.getDdlArtifacts().iterator().hasNext());
        assertFalse(actual.getIndexPlans().iterator().hasNext());
    }
    
    @Test
    void assertPlanCreateRuleWithLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("WEIGHT");
        request.putLoadBalancerProperties(Map.of("read_ds_0", "2"));
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("WEIGHT"));
        assertThat(actual.getPropertyRequirements().getFirst().getPropertyKey(), is("read_ds_0"));
        String expectedSQL = "CREATE READWRITE_SPLITTING RULE `readwrite_ds` (WRITE_STORAGE_UNIT=`write_ds`, READ_STORAGE_UNITS(`read_ds_0`), "
                + "TRANSACTIONAL_READ_QUERY_STRATEGY='DYNAMIC', TYPE(NAME='weight', PROPERTIES('read_ds_0'='2')))";
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is(expectedSQL));
    }
    
    @Test
    void assertPlanCreateRuleClarifiesMissingLoadBalancerProperties() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("WEIGHT");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
        assertThat(actual.getPropertyRequirements().getFirst().getPropertyKey(), is("read_ds_0"));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanCreateRuleWithLoadBalancerRecommendation() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setLoadBalancerType("RANDOM");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getAlgorithmCandidates().getFirst().getAlgorithmType(), is("RANDOM"));
        assertTrue(actual.getPropertyRequirements().isEmpty());
    }
    
    @Test
    void assertPlanAlterRule() {
        WorkflowContextSnapshot actual =
                createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), createRuleRequest("alter"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getOperationType(), is("alter"));
    }
    
    @Test
    void assertPlanDropRule() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setOperationType("drop");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("DROP READWRITE_SPLITTING RULE `readwrite_ds`"));
    }
    
    @Test
    void assertPlanClarifiesMissingStorageUnits() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setWriteStorageUnit("");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
    }
    
    @Test
    void assertPlanCreateFailsWhenRuleExists() {
        WorkflowContextSnapshot actual =
                createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of(Map.of("name", "readwrite_ds"))), createRuleRequest("create"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertPlanDropFailsWhenRuleDoesNotExist() {
        ReadwriteSplittingRuleWorkflowRequest request = new ReadwriteSplittingRuleWorkflowRequest();
        request.setDatabase("logic_db");
        request.setRuleName("readwrite_ds");
        request.setOperationType("drop");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mockRuleQueryFacade(List.of()), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    @Test
    void assertPlanFailsForUnsupportedIdentifier() {
        ReadwriteSplittingRuleWorkflowRequest request = createRuleRequest("create");
        request.setRuleName("bad`rule");
        WorkflowContextSnapshot actual = createRuleService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertPlanStatus() {
        WorkflowContextSnapshot actual =
                createStatusService().plan(new TestWorkflowSessionContext(), mockStatusQueryFacade(List.of(createStatusRow("ENABLED"))), createStatusRequest("disable"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("readwrite.status"));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("ALTER READWRITE_SPLITTING RULE `readwrite_ds` DISABLE `read_ds_0` FROM `logic_db`"));
    }
    
    @Test
    void assertPlanStatusInfersTargetStatus() {
        ReadwriteSplittingStatusWorkflowRequest request = createStatusRequest("");
        request.setNaturalLanguageIntent("disable read storage unit");
        WorkflowContextSnapshot actual = createStatusService().plan(
                new TestWorkflowSessionContext(), mockStatusQueryFacade(List.of(createStatusRow("ENABLED"))), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(((ReadwriteSplittingStatusWorkflowRequest) actual.getRequest()).getTargetStatus(), is("disable"));
        assertThat(actual.getRequest().getOperationType(), is(""));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("ALTER READWRITE_SPLITTING RULE `readwrite_ds` DISABLE `read_ds_0` FROM `logic_db`"));
    }
    
    @Test
    void assertPlanStatusClarifiesMissingStatus() {
        ReadwriteSplittingStatusWorkflowRequest request = createStatusRequest("");
        WorkflowContextSnapshot actual = createStatusService().plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
    }
    
    @Test
    void assertPlanStatusFailsWhenTargetMissing() {
        WorkflowContextSnapshot actual = createStatusService().plan(new TestWorkflowSessionContext(), mockStatusQueryFacade(List.of()), createStatusRequest("enable"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    private ReadwriteSplittingRuleWorkflowPlanningService createRuleService() {
        return new ReadwriteSplittingRuleWorkflowPlanningService();
    }
    
    private ReadwriteSplittingStatusWorkflowPlanningService createStatusService() {
        return new ReadwriteSplittingStatusWorkflowPlanningService();
    }
    
    private MCPFeatureQueryFacade mockRuleQueryFacade(final List<Map<String, Object>> rules) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.query(eq("logic_db"), eq(""), any())).thenReturn(rules);
        return result;
    }
    
    private MCPFeatureQueryFacade mockStatusQueryFacade(final List<Map<String, Object>> statuses) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "read_ds_0", "read_ds_0")).thenReturn(true);
        when(result.query(eq("logic_db"), eq(""), any())).thenReturn(statuses);
        return result;
    }
    
    private ReadwriteSplittingRuleWorkflowRequest createRuleRequest(final String operationType) {
        ReadwriteSplittingRuleWorkflowRequest result = new ReadwriteSplittingRuleWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("readwrite_ds");
        result.setOperationType(operationType);
        result.setWriteStorageUnit("write_ds");
        result.setReadStorageUnits("read_ds_0");
        result.setTransactionalReadQueryStrategy("DYNAMIC");
        return result;
    }
    
    private ReadwriteSplittingStatusWorkflowRequest createStatusRequest(final String targetStatus) {
        ReadwriteSplittingStatusWorkflowRequest result = new ReadwriteSplittingStatusWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("readwrite_ds");
        result.setStorageUnit("read_ds_0");
        result.setTargetStatus(targetStatus);
        return result;
    }
    
    private Map<String, Object> createStatusRow(final String status) {
        return Map.of("name", "readwrite_ds", "storage_unit", "read_ds_0", "status", status);
    }
}
