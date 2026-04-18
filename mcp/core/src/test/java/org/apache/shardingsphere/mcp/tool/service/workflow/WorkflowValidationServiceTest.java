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

import org.apache.shardingsphere.mcp.context.MCPRequestContext;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPTableMetadata;
import org.apache.shardingsphere.mcp.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.tool.handler.execute.MCPSQLExecutionFacade;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowValidationServiceTest {
    
    @Test
    void assertValidateDetectsEncryptMappingAndAlgorithmMismatches() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createEncryptSnapshot());
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            WorkflowProxyQueryService proxyQueryService = mock(WorkflowProxyQueryService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of(Map.of(
                    "logic_column", "status",
                    "cipher_column", "status_cipher",
                    "assisted_query_column", "status_assisted_query_1",
                    "encryptor_type", "AES",
                    "assisted_query_type", "MD5")));
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(proxyQueryService.queryInformationSchemaColumnNames(requestContext, "logic_db", "public", "orders", Set.of("status_cipher", "status_assisted_query")))
                    .thenReturn(Set.of("status_cipher", "status_assisted_query"));
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, proxyQueryService);
            try (
                    MockedConstruction<MCPSQLExecutionFacade> facades = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                List<?> actualMismatches = (List<?>) actualResponse.get("mismatches");
                assertThat(actualResponse.get("status"), is("failed"));
                assertThat(actualResponse.get("overall_status"), is("failed"));
                assertThat(actualMismatches.size(), is(2));
                Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
                assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_STATE_MISMATCH));
                assertThat(contextStore.getRequired("plan-1").getStatus(), is("failed"));
                verify(facades.constructed().get(0), times(2)).execute(any());
            }
        }
    }
    
    @Test
    void assertValidatePassesWhenMaskDropAlreadyRemoved() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createMaskSnapshot("drop"));
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, mock(WorkflowProxyQueryService.class));
            try (
                    MockedConstruction<MCPSQLExecutionFacade> ignoredFacade = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                assertThat(actualResponse.get("status"), is("validated"));
                assertThat(actualResponse.get("overall_status"), is("passed"));
                assertThat(((List<?>) actualResponse.get("issues")).size(), is(0));
                Map<?, ?> actualRuleValidation = (Map<?, ?>) actualResponse.get("rule_validation");
                assertThat(actualRuleValidation.get("status"), is("passed"));
                assertThat(contextStore.getRequired("plan-1").getStatus(), is("validated"));
            }
        }
    }
    
    @Test
    void assertValidateSkipsDdlAndPassesWhenEncryptDropAlreadyRemoved() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createEncryptDropSnapshot());
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, mock(WorkflowProxyQueryService.class));
            try (
                    MockedConstruction<MCPSQLExecutionFacade> facades = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                assertThat(actualResponse.get("status"), is("validated"));
                assertThat(actualResponse.get("overall_status"), is("passed"));
                assertThat(((Map<?, ?>) actualResponse.get("ddl_validation")).get("status"), is("skipped"));
                assertThat(((Map<?, ?>) actualResponse.get("rule_validation")).get("status"), is("passed"));
                verify(facades.constructed().get(0), times(1)).execute(any());
            }
        }
    }
    
    @Test
    void assertValidateReportsSqlExecutabilityFailure() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createMaskSnapshot("create"));
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of(Map.of("column", "status", "algorithm_type", "MASK_TYPE")));
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, mock(WorkflowProxyQueryService.class));
            try (
                    MockedConstruction<MCPSQLExecutionFacade> ignoredFacade = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenThrow(new IllegalStateException("sql failed")))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
                assertThat(actualResponse.get("status"), is("failed"));
                assertThat(actualResponse.get("overall_status"), is("failed"));
                assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
                Map<?, ?> actualSqlValidation = (Map<?, ?>) actualResponse.get("sql_executability_validation");
                assertThat(actualSqlValidation.get("status"), is("failed"));
            }
        }
    }
    
    @Test
    void assertValidateFailsWhenLogicalMetadataIsMissing() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("other_status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createMaskSnapshot("create"));
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of(Map.of("column", "status", "algorithm_type", "MASK_TYPE")));
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, mock(WorkflowProxyQueryService.class));
            try (
                    MockedConstruction<MCPSQLExecutionFacade> ignoredFacade = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
                assertThat(actualResponse.get("status"), is("failed"));
                assertThat(actualResponse.get("overall_status"), is("failed"));
                assertThat(actualIssue.get("code"), is(WorkflowIssueCode.LOGICAL_METADATA_MISMATCH));
                Map<?, ?> actualLogicalValidation = (Map<?, ?>) actualResponse.get("logical_metadata_validation");
                assertThat(actualLogicalValidation.get("status"), is("failed"));
            }
        }
    }
    
    @Test
    void assertValidateSupportsMaskRuleFieldAliases() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createMaskSnapshot("create"));
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders"))
                    .thenReturn(List.of(Map.of("logic_column", "status", "mask_algorithm", "MASK_TYPE", "props", Map.of("replace-char", "*"))));
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, mock(WorkflowProxyQueryService.class));
            try (
                    MockedConstruction<MCPSQLExecutionFacade> ignoredFacade = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                assertThat(actualResponse.get("status"), is("validated"));
                assertThat(((Map<?, ?>) actualResponse.get("rule_validation")).get("status"), is("passed"));
            }
        }
    }
    
    @Test
    void assertValidateDetectsMissingPhysicalDerivedColumns() {
        try (MCPRequestContext requestContext = createRequestContextWithColumn("status")) {
            WorkflowContextStore contextStore = new WorkflowContextStore();
            contextStore.save(createEncryptSnapshot());
            RuleInspectionService ruleInspectionService = mock(RuleInspectionService.class);
            WorkflowProxyQueryService proxyQueryService = mock(WorkflowProxyQueryService.class);
            when(ruleInspectionService.queryEncryptRules(requestContext, "logic_db", "orders")).thenReturn(List.of(Map.of(
                    "logic_column", "status",
                    "cipher_column", "status_cipher",
                    "assisted_query_column", "status_assisted_query",
                    "encryptor_type", "AES",
                    "assisted_query_type", "CRC32")));
            when(ruleInspectionService.queryMaskRules(requestContext, "logic_db", "orders")).thenReturn(List.of());
            when(proxyQueryService.queryInformationSchemaColumnNames(requestContext, "logic_db", "public", "orders", Set.of("status_cipher", "status_assisted_query")))
                    .thenReturn(Set.of("status_cipher"));
            WorkflowValidationService validationService = new WorkflowValidationService(contextStore, ruleInspectionService, proxyQueryService);
            try (
                    MockedConstruction<MCPSQLExecutionFacade> ignoredFacade = mockConstruction(MCPSQLExecutionFacade.class,
                            (mock, context) -> when(mock.execute(any())).thenReturn(null))) {
                Map<String, Object> actualResponse = validationService.validate(requestContext, "session-1", "plan-1");
                Map<?, ?> actualDdlValidation = (Map<?, ?>) actualResponse.get("ddl_validation");
                assertThat(actualResponse.get("status"), is("failed"));
                assertThat(actualDdlValidation.get("status"), is("failed"));
                assertThat(((List<?>) actualResponse.get("mismatches")).size(), is(1));
            }
        }
    }
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(createMaskSnapshot("create"));
        WorkflowValidationService validationService = new WorkflowValidationService(contextStore, mock(RuleInspectionService.class), mock(WorkflowProxyQueryService.class));
        Map<String, Object> actualResponse = validationService.validate(mock(MCPRequestContext.class), "session-2", "plan-1");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateRejectsInvalidLifecycleStatus() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createMaskSnapshot("create");
        snapshot.setStatus("planned");
        contextStore.save(snapshot);
        WorkflowValidationService validationService = new WorkflowValidationService(contextStore, mock(RuleInspectionService.class), mock(WorkflowProxyQueryService.class));
        Map<String, Object> actualResponse = validationService.validate(mock(MCPRequestContext.class), "session-1", "plan-1");
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actualResponse.get("issues")).get(0);
        assertThat(actualResponse.get("status"), is("failed"));
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
    }
    
    private WorkflowContextSnapshot createEncryptSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus("executed");
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("status");
        request.setAlgorithmType("AES");
        request.setAssistedQueryAlgorithmType("CRC32");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setFeatureType("encrypt");
        clarifiedIntent.setOperationType("create");
        clarifiedIntent.setRequiresEqualityFilter(true);
        clarifiedIntent.setRequiresLikeQuery(false);
        result.setClarifiedIntent(clarifiedIntent);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        DerivedColumnPlan derivedColumnPlan = new DerivedColumnPlan();
        derivedColumnPlan.setLogicalColumn("status");
        derivedColumnPlan.setCipherColumnRequired(true);
        derivedColumnPlan.setCipherColumnName("status_cipher");
        derivedColumnPlan.setAssistedQueryColumnRequired(true);
        derivedColumnPlan.setAssistedQueryColumnName("status_assisted_query");
        result.setDerivedColumnPlan(derivedColumnPlan);
        return result;
    }
    
    private WorkflowContextSnapshot createMaskSnapshot(final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setSessionId("session-1");
        result.setStatus("executed");
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("status");
        request.setAlgorithmType("MASK_TYPE");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setFeatureType("mask");
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        return result;
    }
    
    private MCPRequestContext createRequestContextWithColumn(final String columnName) {
        MCPTableMetadata tableMetadata = new MCPTableMetadata("logic_db", "public", "orders",
                List.of(new MCPColumnMetadata("logic_db", "public", "orders", "", columnName)), List.of());
        MCPSchemaMetadata schemaMetadata = new MCPSchemaMetadata("logic_db", "public", List.of(tableMetadata), List.of());
        return ResourceTestDataFactory.createRequestContext(List.of(new MCPDatabaseMetadata("logic_db", "MySQL", "", List.of(schemaMetadata))));
    }
    
    private WorkflowContextSnapshot createEncryptDropSnapshot() {
        WorkflowContextSnapshot result = createEncryptSnapshot();
        result.getClarifiedIntent().setOperationType("drop");
        result.setDerivedColumnPlan(null);
        return result;
    }
}
