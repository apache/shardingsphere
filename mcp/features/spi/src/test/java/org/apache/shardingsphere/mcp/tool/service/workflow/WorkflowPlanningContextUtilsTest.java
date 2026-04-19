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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmCandidate;
import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowFeatureData;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowPlanningContextUtilsTest {
    
    @Test
    void assertGetOrCreateSnapshotCreatesNewSnapshot() {
        WorkflowContextSnapshot actualSnapshot = WorkflowPlanningContextUtils.getOrCreateSnapshot(new WorkflowContextStore(), "session-1", "");
        assertThat(actualSnapshot.getSessionId(), is("session-1"));
        assertThat(actualSnapshot.getStatus(), is("clarifying"));
        assertTrue(actualSnapshot.getPlanId().startsWith("plan-"));
    }
    
    @Test
    void assertGetOrCreateSnapshotReturnsStoredSnapshot() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        WorkflowContextSnapshot actualSnapshot = WorkflowPlanningContextUtils.getOrCreateSnapshot(contextStore, "session-1", "plan-1");
        assertThat(actualSnapshot.getPlanId(), is("plan-1"));
        assertThat(actualSnapshot.getStatus(), is("planned"));
    }
    
    @Test
    void assertClearPlanningStateRemovesTransientArtifacts() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getIssues().add(new WorkflowIssue("code", "error", "stage", "message", "action", true, Collections.emptyMap()));
        snapshot.getAlgorithmCandidates().add(new AlgorithmCandidate("primary", "AES", "heuristic", true, true, false, 90, "reason", ""));
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "key", true, true, "desc", ""));
        snapshot.getDdlArtifacts().add(new DDLArtifact("ddl", "ALTER TABLE t ADD c VARCHAR(32)", 1));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        snapshot.getIndexPlans().add(new IndexPlan("idx", "c", "reason", "CREATE INDEX idx ON t(c)"));
        WorkflowFeatureData featureData = new WorkflowFeatureData() {
            
            @Override
            public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
                return Map.of();
            }
            
            @Override
            public WorkflowFeatureData copy() {
                return this;
            }
        };
        snapshot.setFeatureData(featureData);
        snapshot.setValidationReport(new ValidationReport());
        WorkflowPlanningContextUtils.clearPlanningState(snapshot);
        assertTrue(snapshot.getIssues().isEmpty());
        assertTrue(snapshot.getAlgorithmCandidates().isEmpty());
        assertTrue(snapshot.getPropertyRequirements().isEmpty());
        assertTrue(snapshot.getDdlArtifacts().isEmpty());
        assertTrue(snapshot.getRuleArtifacts().isEmpty());
        assertTrue(snapshot.getIndexPlans().isEmpty());
        assertThat(snapshot.getFeatureData(), is(featureData));
        assertNull(snapshot.getValidationReport());
    }
    
    @Test
    void assertPersistSnapshotUpdatesCurrentStepAndStatus() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("intaking");
        snapshot.setInteractionPlan(interactionPlan);
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowPlanningContextUtils.persistSnapshot(contextStore, snapshot, "review", "planned");
        WorkflowContextSnapshot actualSnapshot = contextStore.getRequired("plan-1");
        assertThat(actualSnapshot.getStatus(), is("planned"));
        assertThat(actualSnapshot.getInteractionPlan().getCurrentStep(), is("review"));
    }
    
    @Test
    void assertEnsurePlanningContextRejectsMissingDatabase() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        boolean actual = WorkflowPlanningContextUtils.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("clarifying"));
        assertThat(clarifiedIntent.getPendingQuestions(), is(List.of("请先提供 logical database。")));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertEnsurePlanningContextRejectsUnsupportedIdentifier() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders-detail");
        request.setColumn("phone");
        boolean actual = WorkflowPlanningContextUtils.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("failed"));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertEnsurePlanningContextResolvesSchemaAndValidatesMetadata() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders");
        request.setColumn("phone");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryDatabase("logic_db")).thenReturn(Optional.of(createDatabaseMetadata()));
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata()));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata()));
        boolean actual = WorkflowPlanningContextUtils.ensurePlanningContext(metadataQueryFacade, request, clarifiedIntent, snapshot);
        assertTrue(actual);
        assertThat(request.getSchema(), is("public"));
        assertTrue(clarifiedIntent.getPendingQuestions().isEmpty());
        assertTrue(snapshot.getIssues().isEmpty());
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata()), List.of())));
    }
    
    private MCPTableMetadata createTableMetadata() {
        return new MCPTableMetadata("logic_db", "public", "orders", List.of(createColumnMetadata()), List.of());
    }
    
    private MCPColumnMetadata createColumnMetadata() {
        return new MCPColumnMetadata("logic_db", "public", "orders", "", "phone");
    }
}
