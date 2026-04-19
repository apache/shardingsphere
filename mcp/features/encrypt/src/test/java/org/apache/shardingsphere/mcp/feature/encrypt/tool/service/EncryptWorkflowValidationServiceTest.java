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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.feature.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.metadata.model.MCPColumnMetadata;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.InteractionPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.response.SQLExecutionResponse;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowContextStore;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EncryptWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(createSnapshot("plan-1", "session-1", "executed", "create"));
        EncryptWorkflowValidationService service = new EncryptWorkflowValidationService(contextStore, mock(EncryptRuleInspectionService.class));
        Map<String, Object> actual = service.validate(mock(MCPFeatureContext.class), "session-2", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateHappyPath() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.getRequest().setAlgorithmType("AES");
        snapshot.getRequest().setAssistedQueryAlgorithmType("MD5");
        snapshot.getRequest().setLikeQueryAlgorithmType("FPE");
        snapshot.getClarifiedIntent().setRequiresEqualityFilter(true);
        snapshot.getClarifiedIntent().setRequiresLikeQuery(true);
        snapshot.setDerivedColumnPlan(createDerivedColumnPlan(true, true));
        WorkflowContextStore contextStore = new WorkflowContextStore();
        contextStore.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "assisted_query_column", "phone_assisted_query",
                "like_query_column", "phone_like_query",
                "encryptor_type", "AES",
                "assisted_query_type", "MD5",
                "like_query_type", "FPE")));
        EncryptWorkflowValidationService service = new EncryptWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryInformationSchemaColumnNames("logic_db", "public", "orders", Set.of("phone_cipher", "phone_assisted_query", "phone_like_query")))
                .thenReturn(Set.of("phone_cipher", "phone_assisted_query", "phone_like_query"));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, queryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("validated"));
        assertThat(actual.get("overall_status"), is("passed"));
        verify(executionFacade, times(3)).execute(any());
    }
    
    @Test
    void assertValidateDropWorkflowAfterRuleRemoval() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop");
        contextStore.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowValidationService service = new EncryptWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("validated"));
        assertThat(((Map<?, ?>) actual.get("ddl_validation")).get("status"), is("skipped"));
        assertThat(((Map<?, ?>) actual.get("rule_validation")).get("status"), is("passed"));
    }
    
    @Test
    void assertValidateWhenRuleMissing() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setDerivedColumnPlan(createDerivedColumnPlan(false, false));
        contextStore.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of());
        EncryptWorkflowValidationService service = new EncryptWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenReturn(mock(SQLExecutionResponse.class));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, mock(MCPFeatureQueryFacade.class), executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(actual.get("overall_status"), is("failed"));
    }
    
    @Test
    void assertValidateWhenSqlExecutionFails() {
        WorkflowContextStore contextStore = new WorkflowContextStore();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create");
        snapshot.setDerivedColumnPlan(createDerivedColumnPlan(false, false));
        snapshot.getRequest().setAlgorithmType("AES");
        contextStore.save(snapshot);
        EncryptRuleInspectionService ruleInspectionService = mock(EncryptRuleInspectionService.class);
        when(ruleInspectionService.queryEncryptRules(any(), any(), any())).thenReturn(List.of(Map.of(
                "logic_column", "phone",
                "cipher_column", "phone_cipher",
                "encryptor_type", "AES")));
        EncryptWorkflowValidationService service = new EncryptWorkflowValidationService(contextStore, ruleInspectionService);
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        when(metadataQueryFacade.queryTableColumn("logic_db", "public", "orders", "phone")).thenReturn(Optional.of(new MCPColumnMetadata("logic_db", "public", "orders", "", "phone")));
        MCPFeatureQueryFacade queryFacade = mock(MCPFeatureQueryFacade.class);
        when(queryFacade.queryInformationSchemaColumnNames("logic_db", "public", "orders", Set.of("phone_cipher"))).thenReturn(Set.of("phone_cipher"));
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        when(executionFacade.execute(any())).thenThrow(new IllegalStateException("sql failed"));
        Map<String, Object> actual = service.validate(createRequestContext(metadataQueryFacade, queryFacade, executionFacade), "session-1", "plan-1");
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) actual.get("sql_executability_validation")).get("status"), is("failed"));
    }
    
    private MCPFeatureContext createRequestContext(final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                                   final MCPFeatureExecutionFacade executionFacade) {
        MCPFeatureContext result = mock(MCPFeatureContext.class);
        when(result.getMetadataQueryFacade()).thenReturn(metadataQueryFacade);
        when(result.getQueryFacade()).thenReturn(queryFacade);
        when(result.getExecutionFacade()).thenReturn(executionFacade);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        WorkflowRequest request = new WorkflowRequest();
        request.setDatabase("logic_db");
        request.setSchema("public");
        request.setTable("orders");
        request.setColumn("phone");
        result.setRequest(request);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        clarifiedIntent.setRequiresEqualityFilter(false);
        clarifiedIntent.setRequiresLikeQuery(false);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private DerivedColumnPlan createDerivedColumnPlan(final boolean assistedQuery, final boolean likeQuery) {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnRequired(true);
        result.setCipherColumnName("phone_cipher");
        result.setAssistedQueryColumnRequired(assistedQuery);
        result.setAssistedQueryColumnName("phone_assisted_query");
        result.setLikeQueryColumnRequired(likeQuery);
        result.setLikeQueryColumnName("phone_like_query");
        return result;
    }
}
