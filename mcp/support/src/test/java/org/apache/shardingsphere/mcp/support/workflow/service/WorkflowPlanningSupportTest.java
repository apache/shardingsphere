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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.support.database.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkflowPlanningSupportTest {
    
    private final WorkflowPlanningSupport planningSupport = new WorkflowPlanningSupport();
    
    @Test
    void assertEnsurePlanningContextRejectsMissingDatabase() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        boolean actual = planningSupport.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("clarifying"));
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertEnsurePlanningContextRejectsEmptyDelimitedDatabase() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("``");
        boolean actual = planningSupport.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("clarifying"));
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please provide logical database first.")));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.DATABASE_REQUIRED));
    }
    
    @Test
    void assertEnsurePlanningContextRejectsUnsupportedIdentifier() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setTable("orders\ndrop");
        request.setColumn("phone");
        boolean actual = planningSupport.ensurePlanningContext(mock(MCPMetadataQueryFacade.class), request, clarifiedIntent, snapshot);
        assertFalse(actual);
        assertThat(snapshot.getStatus(), is("failed"));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.UNSUPPORTED_IDENTIFIER));
    }
    
    @Test
    void assertEnsurePlanningContextNormalizesDelimitedIdentifiers() {
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("`logic_db`");
        request.setSchema("`public`");
        request.setTable("`orders-detail`");
        request.setColumn("\"Phone Number\"");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders-detail")).thenReturn(Optional.of(createTableMetadata("orders-detail", "Phone Number")));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders-detail", "Phone Number")).thenReturn(Optional.of(createColumnMetadata("orders-detail", "Phone Number")));
        boolean actual = planningSupport.ensurePlanningContext(metadataQueryFacade, request, clarifiedIntent, snapshot);
        assertTrue(actual);
        assertThat(request.getDatabase(), is("logic_db"));
        assertThat(request.getSchema(), is("public"));
        assertThat(request.getTable(), is("orders-detail"));
        assertThat(request.getColumn(), is("Phone Number"));
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
        when(metadataQueryFacade.queryTable("logic_db", "public", "orders")).thenReturn(Optional.of(createTableMetadata("orders", "phone")));
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(createColumnMetadata("orders", "phone")));
        boolean actual = planningSupport.ensurePlanningContext(metadataQueryFacade, request, clarifiedIntent, snapshot);
        assertTrue(actual);
        assertThat(request.getSchema(), is("public"));
        assertThat(clarifiedIntent.getInferredValues().get("schema"), is("public"));
        assertTrue(clarifiedIntent.getClarificationMessages().isEmpty());
        assertTrue(snapshot.getIssues().isEmpty());
    }
    
    @Test
    void assertApplyResolvedIntent() {
        WorkflowRequest request = new WorkflowRequest();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("alter");
        clarifiedIntent.setFieldSemantics("phone");
        planningSupport.applyResolvedIntent(request, clarifiedIntent);
        assertThat(request.getOperationType(), is("alter"));
        assertThat(request.getFieldSemantics(), is("phone"));
    }
    
    @Test
    void assertPrepareSnapshot() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.DATABASE_REQUIRED, "error", "intaking", "message", "action", true, Map.of()));
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType("create");
        WorkflowRequest actual = planningSupport.prepareSnapshot(snapshot, WorkflowKind.valueOf("encrypt.rule"), request, null, clarifiedIntent, "summary", List.of("step-1"), List.of("rules"));
        assertThat(actual, is(request));
        assertThat(snapshot.getRequest(), is(request));
        assertThat(snapshot.getWorkflowKind(), is(WorkflowKind.valueOf("encrypt.rule")));
        assertThat(snapshot.getClarifiedIntent(), is(clarifiedIntent));
        assertThat(snapshot.getInteractionPlan().getSummary(), is("summary"));
        assertTrue(snapshot.getIssues().isEmpty());
    }
    
    @Test
    void assertPrepareSnapshotInfersManualOnlyExecutionMode() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setPlanId("plan-1");
        WorkflowRequest request = new WorkflowRequest();
        request.setExecutionMode("");
        request.setNaturalLanguageIntent("export reviewable artifacts for manual execution outside MCP");
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        planningSupport.prepareSnapshot(snapshot, WorkflowKind.valueOf("mask.rule"), request, null, clarifiedIntent, "summary", List.of("step-1"), List.of("rules"));
        assertThat(snapshot.getInteractionPlan().getExecutionMode(), is("manual-only"));
        assertThat(snapshot.getRequest().getExecutionMode(), is("manual-only"));
        assertThat(clarifiedIntent.getInferredValues().get("execution_mode"), is("manual-only"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getEnsureLifecycleStateCases")
    void assertEnsureLifecycleState(final String name, final String operationType, final boolean ruleExists, final boolean expectedResult, final String expectedIssueCode) {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        boolean actual = planningSupport.ensureLifecycleState("Encrypt rule", clarifiedIntent, ruleExists, snapshot);
        assertThat(actual, is(expectedResult));
        assertThat(snapshot.getIssues().isEmpty(), is(null == expectedIssueCode));
        if (null != expectedIssueCode) {
            assertThat(snapshot.getIssues().get(0).getCode(), is(expectedIssueCode));
        }
    }
    
    @Test
    void assertHasBlockingAlgorithmIssuesAddsFallbackQuestion() {
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getIssues().add(new WorkflowIssue(WorkflowIssueCode.ALGORITHM_NOT_FOUND, "error", "selecting-algorithm", "message", "action", false, Map.of()));
        boolean actual = planningSupport.hasBlockingAlgorithmIssues(clarifiedIntent, snapshot, "Please use an algorithm visible in the current Proxy.");
        assertTrue(actual);
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please use an algorithm visible in the current Proxy.")));
    }
    
    @Test
    void assertCollectPropertyRequirementsAppliesDefaultsAndReportsMissingValues() {
        WorkflowRequest request = new WorkflowRequest();
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        List<AlgorithmPropertyRequirement> propertyRequirements = List.of(
                new AlgorithmPropertyRequirement("primary", "mask-char", false, false, "mask char", "*"),
                new AlgorithmPropertyRequirement("primary", "from-x", true, false, "from x", ""));
        boolean actual = planningSupport.collectPropertyRequirements(request, clarifiedIntent, snapshot, propertyRequirements);
        assertFalse(actual);
        assertThat(request.getPrimaryAlgorithmProperties().get("mask-char"), is("*"));
        assertThat(clarifiedIntent.getClarificationMessages(), is(List.of("Please provide property `from-x`.")));
        assertThat(snapshot.getPropertyRequirements().size(), is(2));
        assertThat(snapshot.getIssues().get(0).getCode(), is(WorkflowIssueCode.REQUIRED_PROPERTY_MISSING));
    }
    
    private static Stream<Arguments> getEnsureLifecycleStateCases() {
        return Stream.of(
                Arguments.of("create when rule missing", "create", false, true, null),
                Arguments.of("create when rule exists", "create", true, false, WorkflowIssueCode.RULE_STATE_MISMATCH),
                Arguments.of("alter when rule exists", "alter", true, true, null),
                Arguments.of("alter when rule missing", "alter", false, false, WorkflowIssueCode.RULE_STATE_MISMATCH),
                Arguments.of("drop when rule exists", "drop", true, true, null),
                Arguments.of("drop when rule missing", "drop", false, false, WorkflowIssueCode.DROP_TARGET_RULE_NOT_FOUND));
    }
    
    private MCPDatabaseMetadata createDatabaseMetadata() {
        return new MCPDatabaseMetadata("logic_db", "MySQL", "8.0", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(createTableMetadata("orders", "phone")), List.of(), List.of())));
    }
    
    private MCPTableMetadata createTableMetadata(final String tableName, final String columnName) {
        return new MCPTableMetadata("logic_db", "public", tableName, List.of(createColumnMetadata(tableName, columnName)), List.of());
    }
    
    private MCPColumnMetadata createColumnMetadata(final String tableName, final String columnName) {
        return new MCPColumnMetadata("logic_db", "public", tableName, "", columnName);
    }
}
