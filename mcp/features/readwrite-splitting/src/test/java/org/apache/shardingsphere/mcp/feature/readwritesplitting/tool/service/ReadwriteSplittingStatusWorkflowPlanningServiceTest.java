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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReadwriteSplittingStatusWorkflowPlanningServiceTest {
    
    private final ReadwriteSplittingStatusWorkflowPlanningService planningService = new ReadwriteSplittingStatusWorkflowPlanningService();
    
    @Test
    void assertPlan() {
        WorkflowContextSnapshot actual = planningService.plan(
                new TestWorkflowSessionContext(), createQueryFacade(List.of(createStatusRow("ENABLED"))), createRequest("disable"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_PLANNED));
        assertThat(actual.getWorkflowKind().getValue(), is("readwrite.status"));
        assertThat(actual.getRuleArtifacts().getFirst().getSql(), is("ALTER READWRITE_SPLITTING RULE `readwrite_ds` DISABLE `read_ds_0` FROM `logic_db`"));
    }
    
    @Test
    void assertPlanDoesNotInferTargetStatusFromNaturalLanguage() {
        ReadwriteSplittingStatusWorkflowRequest request = createRequest("");
        request.setNaturalLanguageIntent("opaque request text");
        WorkflowContextSnapshot actual = planningService.plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), request);
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(((ReadwriteSplittingStatusWorkflowRequest) actual.getRequest()).getTargetStatus(), is(""));
        assertThat(actual.getRequest().getOperationType(), is(""));
        assertTrue(actual.getRuleArtifacts().isEmpty());
    }
    
    @Test
    void assertPlanClarifiesMissingStatus() {
        WorkflowContextSnapshot actual = planningService.plan(new TestWorkflowSessionContext(), mock(MCPFeatureQueryFacade.class), createRequest(""));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_CLARIFYING));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.RULE_INPUT_REQUIRED));
    }
    
    @Test
    void assertPlanFailsWhenTargetMissing() {
        WorkflowContextSnapshot actual = planningService.plan(new TestWorkflowSessionContext(), createQueryFacade(List.of()), createRequest("enable"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    @Test
    void assertPlanFailsInStandaloneMode() {
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.query("logic_db", "SHOW COMPUTE NODE INFO")).thenReturn(List.of(Map.of("mode_type", "Standalone")));
        WorkflowContextSnapshot actual = planningService.plan(new TestWorkflowSessionContext(), queryFacade, createRequest("disable"));
        assertThat(actual.getStatus(), is(WorkflowLifecycle.STATUS_FAILED));
        assertThat(actual.getIssues().getFirst().getCode(), is(WorkflowIssueCode.CLUSTER_MODE_REQUIRED));
        assertThat(actual.getIssues().getFirst().getStage(), is(WorkflowLifecycle.STEP_DISCOVERING));
        assertFalse(actual.getIssues().getFirst().isRetryable());
        assertThat(actual.getIssues().getFirst().getDetails(), is(Map.of("required_mode", "Cluster", "actual_mode", "Standalone")));
        assertTrue(actual.getRuleArtifacts().isEmpty());
        verify(queryFacade, never()).query("logic_db", "SHOW STATUS FROM READWRITE_SPLITTING RULE readwrite_ds FROM logic_db");
    }
    
    private MCPFeatureQueryFacade createQueryFacade(final List<Map<String, Object>> statuses) {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "readwrite_ds", "readwrite_ds")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "read_ds_0", "read_ds_0")).thenReturn(true);
        when(result.query("logic_db", "SHOW COMPUTE NODE INFO")).thenReturn(List.of(Map.of("mode_type", "Cluster")));
        when(result.query("logic_db", "SHOW STATUS FROM READWRITE_SPLITTING RULE readwrite_ds FROM logic_db")).thenReturn(statuses);
        return result;
    }
    
    private ReadwriteSplittingStatusWorkflowRequest createRequest(final String targetStatus) {
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
