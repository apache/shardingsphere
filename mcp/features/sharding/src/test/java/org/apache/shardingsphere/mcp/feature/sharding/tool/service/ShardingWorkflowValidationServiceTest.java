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

import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.TestWorkflowSessionContext;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.InteractionPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.AdditionalAnswers;
import org.mockito.MockedConstruction;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.withSettings;
import static org.mockito.Mockito.when;

class ShardingWorkflowValidationServiceTest {
    
    @Test
    void assertValidateRejectsDifferentSession() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(createSnapshot("plan-1", "session-1", "executed", "create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest()));
        Map<String, Object> actual = createService(mock(ShardingInspectionService.class)).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                mock(MCPFeatureQueryFacade.class), mock(MCPFeatureExecutionFacade.class), "session-2", workflowSessionContext.getRequired("plan-1"));
        assertThat(actual.get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("issues")).get(0)).get("code"), is(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH));
    }
    
    @Test
    void assertValidateTableRuleHappyPath() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRow("ds_0.t_order_0,ds_1.t_order_1", Map.of(
                "algorithm-expression", "t_order_${order_id % 2}"), "")));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        MCPMetadataQueryFacade metadataQueryFacade = mock(MCPMetadataQueryFacade.class);
        MCPFeatureExecutionFacade executionFacade = mock(MCPFeatureExecutionFacade.class);
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        assertThat(getValidationSection(actual, "rule").get("status"), is("passed"));
        verify(queryFacade).isSameIdentifier("logic_db", IdentifierScope.COLUMN, "order_id", "order_id");
        verifyNoInteractions(metadataQueryFacade);
        verifyNoInteractions(executionFacade);
    }
    
    @Test
    void assertValidateComplexTableRule() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setStrategyType("complex");
        request.setShardingColumns("order_id,user_id");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "table", "t_order",
                "actual_data_nodes", "ds_0.t_order_0,ds_1.t_order_1",
                "table_strategy_type", "COMPLEX",
                "table_sharding_column", "order_id,user_id",
                "table_sharding_algorithm_type", "INLINE",
                "table_sharding_algorithm_props", Map.of("algorithm-expression", "t_order_${order_id % 2}"),
                "key_generate_column", "")));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        verify(queryFacade).isSameIdentifier("logic_db", IdentifierScope.COLUMN, "user_id", "user_id");
    }
    
    @Test
    void assertValidateTableRuleMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(getValidationSection(actual, "rule").get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentFields() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRow("ds_0.t_order_0", Map.of(
                "algorithm-expression", "t_order_${order_id % 2}"), "")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentNamedKeyGenerator() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRowWithKeyGenerator()));
        when(inspectionService.queryTableRulesUsedKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator"))).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleWithNamedKeyGenerator() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRowWithKeyGenerator()));
        when(inspectionService.queryTableRulesUsedKeyGenerator(any(), eq("logic_db"), eq("snowflake_generator")))
                .thenReturn(List.of(Map.of("type", "table", "name", "t_order")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleWithInlineKeyGeneratorAndAuditor() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        request.setAllowHintDisable("true");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRowWithKeyGenerator());
        row.put("key_generator_type", "SNOWFLAKE");
        row.put("key_generator_props", Map.of("worker-id", "1"));
        row.put("auditor_types", "DML_SHARDING_CONDITIONS");
        row.put("allow_hint_disable", "true");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleDrop() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of());
        assertThat(validateState("drop", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                createTableRuleRequest(), inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleDropWhenRuleRemains() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", Map.of("algorithm-expression", "t_order_${order_id % 2}"), "")));
        assertThat(validateState("drop", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                createTableRuleRequest(), inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateAutomaticTableRule() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDataNodes("");
        request.setStorageUnits("ds_0,ds_1");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "table", "t_order", "actual_data_sources", "ds_0,ds_1", "table_strategy_type", "STANDARD",
                "table_sharding_column", "order_id", "table_sharding_algorithm_type", "INLINE",
                "table_sharding_algorithm_props", Map.of("algorithm-expression", "t_order_${order_id % 2}"), "key_generate_column", "")));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "ds_0", "ds_0")).thenReturn(true);
        when(queryFacade.isSameIdentifier("logic_db", IdentifierScope.TABLE, "ds_1", "ds_1")).thenReturn(true);
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, queryFacade).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleWithoutStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setStrategyType("none");
        request.setAlgorithmType("");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "table", "t_order", "actual_data_nodes", "ds_0.t_order_0,ds_1.t_order_1", "key_generate_column", "")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "table", "t_order", "actual_data_nodes", "ds_0.t_order_0,ds_1.t_order_1", "table_strategy_type", "HINT",
                "table_sharding_algorithm_type", "INLINE", "table_sharding_algorithm_props", request.getPrimaryAlgorithmProperties(), "key_generate_column", "")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentName() {
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", Map.of("algorithm-expression", "t_order_${order_id % 2}"), ""));
        row.put("table", "t_other");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                createTableRuleRequest(), inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentAlgorithmType() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), ""));
        row.put("table_sharding_algorithm_type", "MOD");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentAlgorithmProperties() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", Map.of("algorithm-expression", "t_order_${user_id % 2}"), "")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateHintTableRule() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setStrategyType("hint");
        request.setAlgorithmType("HINT_INLINE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "table", "t_order", "actual_data_nodes", "ds_0.t_order_0,ds_1.t_order_1", "table_strategy_type", "HINT",
                "table_sharding_algorithm_type", "HINT_INLINE", "table_sharding_algorithm_props", request.getPrimaryAlgorithmProperties(), "key_generate_column", "")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentKeyGenerateColumn() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorType("SNOWFLAKE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), "other_id")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentInlineKeyGenerator() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRowWithKeyGenerator());
        row.put("key_generator_type", "SNOWFLAKE");
        row.put("key_generator_props", Map.of("worker-id", "2"));
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentInlineKeyGeneratorType() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorType("SNOWFLAKE");
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRowWithKeyGenerator());
        row.put("key_generator_type", "UUID");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleWithDefaultAuditorOption() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), ""));
        row.put("auditor_types", "DML_SHARDING_CONDITIONS, , ");
        row.put("allow_hint_disable", "false");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentAuditors() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.getAuditorNames().addAll(List.of("DML_SHARDING_CONDITIONS", "DML_SHARDING_CONDITIONS_2"));
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), ""));
        row.put("auditor_types", "DML_SHARDING_CONDITIONS");
        row.put("allow_hint_disable", "false");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentAllowHintDisable() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        request.setAllowHintDisable("true");
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), ""));
        row.put("auditor_types", "DML_SHARDING_CONDITIONS");
        row.put("allow_hint_disable", "false");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableRuleRejectsDifferentAuditorName() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        Map<String, Object> row = new LinkedHashMap<>(createTableRuleRow(
                "ds_0.t_order_0,ds_1.t_order_1", request.getPrimaryAlgorithmProperties(), ""));
        row.put("auditor_types", "OTHER_AUDITOR");
        row.put("allow_hint_disable", "false");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableRule(any(), any(), any())).thenReturn(List.of(row));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyDrop() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateDefaultStrategyDropWhenStrategyRemains() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of("name", "DATABASE", "type", "STANDARD")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateCleanupWhenComponentRemains() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "drop",
                ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND, createCleanupRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryAlgorithms(any(), any())).thenReturn(List.of(Map.of("name", "unused_algorithm")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("cleanupComponentCases")
    void assertValidateCleanupComponentTypes(final String name, final String componentType, final String componentName) {
        ShardingWorkflowRequest request = createCleanupRequest();
        request.setComponentType(componentType);
        request.setComponentName(componentName);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        assertThat(validateState("drop", ShardingFeatureDefinition.COMPONENT_CLEANUP_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateTableReferenceRuleMismatch() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, createReferenceRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of());
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
        assertThat(getValidationSection(actual, "rule").get("status"), is("failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("mismatches")).getFirst()).get("code"), is(WorkflowIssueCode.RULE_STATE_MISMATCH));
    }
    
    @Test
    void assertValidateTableReferenceRuleRejectsDifferentTables() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, createReferenceRuleRequest());
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "ref_rule", "sharding_table_reference", "t_order,t_user")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableReferenceRuleRejectsDifferentName() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "other_ref", "sharding_table_reference", "t_order,t_order_item")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND,
                createReferenceRuleRequest(), inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateTableReferenceRuleRejectsMissingTable() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "ref_rule", "sharding_table_reference", "t_order")));
        assertThat(validateState("create", ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND,
                createReferenceRuleRequest(), inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDifferentProperties() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "STANDARD", "sharding_column", "order_id", "sharding_algorithm_type", "INLINE",
                "sharding_algorithm_props", Map.of("algorithm-expression", "t_order_${user_id % 2}"))));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategy() {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "STANDARD", "sharding_column", "order_id", "sharding_algorithm_type", "INLINE",
                "sharding_algorithm_props", Map.of("algorithm-expression", "t_order_${order_id % 2}"))));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDuplicateRows() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        Map<String, Object> row = Map.of(
                "name", "DATABASE", "type", "STANDARD", "sharding_column", "order_id", "sharding_algorithm_type", "INLINE",
                "sharding_algorithm_props", request.getPrimaryAlgorithmProperties());
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(row, row));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsIncompleteState() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of("name", "DATABASE", "type", "")));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDifferentName() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of("name", "TABLE", "type", "STANDARD")));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDifferentType() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of("name", "DATABASE", "type", "HINT")));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateHintDefaultStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("hint");
        request.setAlgorithmType("HINT_INLINE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "HINT", "sharding_algorithm_type", "HINT_INLINE",
                "sharding_algorithm_props", request.getPrimaryAlgorithmProperties())));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDifferentAlgorithmType() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "STANDARD", "sharding_column", "order_id", "sharding_algorithm_type", "MOD",
                "sharding_algorithm_props", request.getPrimaryAlgorithmProperties())));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateDefaultStrategyWithoutSharding() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("none");
        request.setAlgorithmType("");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of("name", "DATABASE", "type", "NONE")));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateDefaultStrategyRejectsDifferentColumn() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "STANDARD", "sharding_column", "user_id", "sharding_algorithm_type", "INLINE",
                "sharding_algorithm_props", request.getPrimaryAlgorithmProperties())));
        assertThat(validateState("create", ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateComplexDefaultStrategy() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setDefaultStrategyType("DATABASE");
        request.setStrategyType("complex");
        request.setShardingColumns("order_id,user_id");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryDefaultStrategy(any(), any())).thenReturn(List.of(Map.of(
                "name", "DATABASE", "type", "COMPLEX", "sharding_column", "order_id,user_id", "sharding_algorithm_type", "INLINE",
                "sharding_algorithm_props", Map.of("algorithm-expression", "t_order_${order_id % 2}"))));
        MCPFeatureQueryFacade queryFacade = createQueryFacade();
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
        verify(queryFacade).isSameIdentifier("logic_db", IdentifierScope.COLUMN, "user_id", "user_id");
    }
    
    @Test
    void assertValidateKeyGeneratorRejectsDifferentProperties() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setKeyGeneratorName("snowflake_generator");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerator(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "snowflake_generator", "type", "SNOWFLAKE", "props", Map.of("worker-id", "2"))));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGeneratorRejectsDifferentName() {
        ShardingWorkflowRequest request = createKeyGeneratorRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerator(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "other_generator", "type", "SNOWFLAKE", "props", Map.of("worker-id", "1"))));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGeneratorRejectsMissingState() {
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerator(any(), any(), any())).thenReturn(List.of());
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                createKeyGeneratorRequest(), inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGeneratorRejectsDifferentType() {
        ShardingWorkflowRequest request = createKeyGeneratorRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerator(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "snowflake_generator", "type", "UUID", "props", Map.of("worker-id", "1"))));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerator() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setKeyGeneratorName("snowflake_generator");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerator(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "snowflake_generator", "type", "SNOWFLAKE", "props", Map.of("worker-id", "1"))));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategyRejectsDifferentColumn() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setKeyGenerateStrategyName("order_id_strategy");
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_order", "column", "user_id", "generator_name", "snowflake_generator")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategyRejectsMissingState() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of());
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategyRejectsDifferentName() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "other_strategy", "type", "COLUMN", "table", "t_order", "column", "order_id", "generator_name", "snowflake_generator")));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategyRejectsDifferentType() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "SEQUENCE", "sequence", "order_seq", "generator_name", "snowflake_generator")));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategyRejectsDifferentTable() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_other", "column", "order_id", "generator_name", "snowflake_generator")));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateInlineKeyGenerateStrategy() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        request.setKeyGeneratorName("");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_order", "column", "order_id",
                "generator_type", "SNOWFLAKE", "generator_props", Map.of("worker-id", "1"))));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("validated"));
    }
    
    @Test
    void assertValidateInlineKeyGenerateStrategyRejectsDifferentProperties() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        request.setKeyGeneratorName("");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_order", "column", "order_id",
                "generator_type", "SNOWFLAKE", "generator_props", Map.of("worker-id", "2"))));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateInlineKeyGenerateStrategyRejectsDifferentType() {
        ShardingWorkflowRequest request = createColumnKeyGenerateStrategyRequest();
        request.setKeyGeneratorName("");
        request.setKeyGeneratorType("SNOWFLAKE");
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_order", "column", "order_id",
                "generator_type", "UUID", "generator_props", Map.of())));
        assertThat(validateState("create", ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND,
                request, inspectionService, createQueryFacade()).get("status"), is("failed"));
    }
    
    @Test
    void assertValidateKeyGenerateStrategy() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setDatabase("logic_db");
        request.setKeyGenerateStrategyName("order_id_strategy");
        request.setTable("t_order");
        request.setColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_id_strategy", "type", "COLUMN", "table", "t_order", "column", "order_id", "generator_name", "snowflake_generator")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateSequenceKeyGenerateStrategy() {
        ShardingWorkflowRequest request = createSequenceKeyGenerateStrategyRequest();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_sequence_strategy", "type", "SEQUENCE", "sequence", "order_seq", "generator_name", "snowflake_generator")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("validated"));
    }
    
    @Test
    void assertValidateSequenceKeyGenerateStrategyRejectsDifferentSequence() {
        ShardingWorkflowRequest request = createSequenceKeyGenerateStrategyRequest();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, request);
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        workflowSessionContext.save(snapshot);
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryKeyGenerateStrategy(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "order_sequence_strategy", "type", "SEQUENCE", "sequence", "other_seq", "generator_name", "snowflake_generator")));
        Map<String, Object> actual = createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class),
                createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
        assertThat(actual.get("status"), is("failed"));
    }
    
    @Test
    void assertSynchronize() {
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", "create",
                ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, createReferenceRuleRequest());
        ShardingInspectionService inspectionService = mock(ShardingInspectionService.class);
        when(inspectionService.queryTableReferenceRule(any(), any(), any())).thenReturn(List.of(Map.of(
                "name", "ref_rule", "sharding_table_reference", "t_order,t_order_item")));
        createService(inspectionService).synchronize(snapshot, mock(MCPMetadataQueryFacade.class), createQueryFacade(), mock(MCPFeatureExecutionFacade.class), "session-1");
        verify(inspectionService).queryTableReferenceRule(any(), any(), any());
    }
    
    private ShardingWorkflowValidationService createService(final ShardingInspectionService inspectionService) {
        try (
                MockedConstruction<ShardingInspectionService> ignored = mockConstruction(
                        ShardingInspectionService.class, withSettings().defaultAnswer(AdditionalAnswers.delegatesTo(inspectionService)))) {
            return new ShardingWorkflowValidationService();
        }
    }
    
    private Map<String, Object> validateState(final String operationType, final WorkflowKind workflowKind, final ShardingWorkflowRequest request,
                                              final ShardingInspectionService inspectionService, final MCPFeatureQueryFacade queryFacade) {
        WorkflowSessionContext workflowSessionContext = new TestWorkflowSessionContext();
        WorkflowContextSnapshot snapshot = createSnapshot("plan-1", "session-1", "executed", operationType, workflowKind, request);
        workflowSessionContext.save(snapshot);
        return createService(inspectionService).validate(workflowSessionContext, mock(MCPMetadataQueryFacade.class), queryFacade,
                mock(MCPFeatureExecutionFacade.class), "session-1", snapshot);
    }
    
    private MCPFeatureQueryFacade createQueryFacade() {
        MCPFeatureQueryFacade result = mock(MCPFeatureQueryFacade.class);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order", "t_order")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "t_order_item", "t_order_item")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "order_id", "order_id")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.COLUMN, "user_id", "user_id")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "DATABASE", "DATABASE")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "ref_rule", "ref_rule")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "snowflake_generator", "snowflake_generator")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "order_id_strategy", "order_id_strategy")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "order_sequence_strategy", "order_sequence_strategy")).thenReturn(true);
        when(result.isSameIdentifier("logic_db", IdentifierScope.TABLE, "unused_algorithm", "unused_algorithm")).thenReturn(true);
        return result;
    }
    
    private WorkflowContextSnapshot createSnapshot(final String planId, final String sessionId, final String status, final String operationType,
                                                   final WorkflowKind workflowKind, final ShardingWorkflowRequest request) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId(planId);
        result.setSessionId(sessionId);
        result.setStatus(status);
        result.setWorkflowKind(workflowKind);
        result.setRequest(request);
        InteractionPlan interactionPlan = new InteractionPlan();
        interactionPlan.setCurrentStep("executed");
        result.setInteractionPlan(interactionPlan);
        ClarifiedIntent clarifiedIntent = new ClarifiedIntent();
        clarifiedIntent.setOperationType(operationType);
        result.setClarifiedIntent(clarifiedIntent);
        return result;
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setTable("t_order");
        result.setColumn("order_id");
        result.setDataNodes("ds_0.t_order_0,ds_1.t_order_1");
        result.setStrategyType("standard");
        result.setAlgorithmType("INLINE");
        result.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        return result;
    }
    
    private ShardingWorkflowRequest createReferenceRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setRuleName("ref_rule");
        result.getReferenceTables().addAll(List.of("t_order", "t_order_item"));
        return result;
    }
    
    private ShardingWorkflowRequest createSequenceKeyGenerateStrategyRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setKeyGenerateStrategyName("order_sequence_strategy");
        result.setSequenceName("order_seq");
        result.setKeyGeneratorName("snowflake_generator");
        return result;
    }
    
    private ShardingWorkflowRequest createKeyGeneratorRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setKeyGeneratorName("snowflake_generator");
        result.setKeyGeneratorType("SNOWFLAKE");
        result.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        return result;
    }
    
    private ShardingWorkflowRequest createColumnKeyGenerateStrategyRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setKeyGenerateStrategyName("order_id_strategy");
        result.setTable("t_order");
        result.setColumn("order_id");
        result.setKeyGeneratorName("snowflake_generator");
        return result;
    }
    
    private Map<String, Object> createTableRuleRow(final String dataNodes, final Map<String, String> algorithmProperties, final String keyGenerateColumn) {
        return Map.of(
                "table", "t_order",
                "actual_data_nodes", dataNodes,
                "table_strategy_type", "STANDARD",
                "table_sharding_column", "order_id",
                "table_sharding_algorithm_type", "INLINE",
                "table_sharding_algorithm_props", algorithmProperties,
                "key_generate_column", keyGenerateColumn);
    }
    
    private Map<String, Object> createTableRuleRowWithKeyGenerator() {
        return createTableRuleRow("ds_0.t_order_0,ds_1.t_order_1", Map.of("algorithm-expression", "t_order_${order_id % 2}"), "order_id");
    }
    
    private ShardingWorkflowRequest createCleanupRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setDatabase("logic_db");
        result.setComponentType("algorithm");
        result.setComponentName("unused_algorithm");
        return result;
    }
    
    private static Stream<Arguments> cleanupComponentCases() {
        return Stream.of(
                Arguments.of("algorithm", "algorithm", "unused_algorithm"),
                Arguments.of("key generator", "key-generator", "unused_generator"),
                Arguments.of("auditor", "auditor", "unused_auditor"));
    }
    
    private Map<?, ?> getValidationSection(final Map<String, Object> payload, final String layer) {
        return ((List<?>) payload.get("sections")).stream().map(each -> (Map<?, ?>) each).filter(each -> layer.equals(each.get("layer"))).findFirst().orElse(Map.of());
    }
}
