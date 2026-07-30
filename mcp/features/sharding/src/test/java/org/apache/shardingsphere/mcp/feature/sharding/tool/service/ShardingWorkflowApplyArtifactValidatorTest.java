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

import org.apache.shardingsphere.infra.algorithm.keygen.spi.KeyGenerateAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.spi.ShardingAuditAlgorithm;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mockStatic;

class ShardingWorkflowApplyArtifactValidatorTest {
    
    @Test
    void assertValidateUnavailableShardingAlgorithm() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setAlgorithmType("INLINE");
        request.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShardingAlgorithm.class, "INLINE",
                    WorkflowAlgorithmUtils.createProperties(request.getPrimaryAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShardingWorkflowApplyArtifactValidator().validate(snapshot,
                    List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("message"), is("Generated sharding DistSQL references sharding algorithm `INLINE`, "
                    + "but it cannot be loaded or initialized by ShardingAlgorithm SPI."));
        }
    }
    
    @Test
    void assertValidateUnavailableKeyGenerator() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGeneratorType("SNOWFLAKE");
        request.putKeyGeneratorProperties(Map.of("worker-id", "1"));
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, "SNOWFLAKE",
                    WorkflowAlgorithmUtils.createProperties(request.getKeyGeneratorProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE SHARDING KEY GENERATOR `snowflake_generator`(TYPE(NAME='snowflake'))")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateUnavailableAuditor() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setStrategyType("none");
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShardingAuditAlgorithm.class, "DML_SHARDING_CONDITIONS",
                    WorkflowAlgorithmUtils.createProperties(Map.of()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("message"), is("Generated sharding DistSQL references auditor algorithm `DML_SHARDING_CONDITIONS`, "
                    + "but it cannot be loaded or initialized by ShardingAuditAlgorithm SPI."));
        }
    }
    
    @Test
    void assertValidateUnavailableDefaultStrategyAlgorithm() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.DEFAULT_STRATEGY_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(ShardingAlgorithm.class, "INLINE",
                    WorkflowAlgorithmUtils.createProperties(request.getPrimaryAlgorithmProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE DEFAULT SHARDING TABLE STRATEGY(TYPE='standard')")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateUnavailableInlineKeyGenerator() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.setKeyGeneratorType("SNOWFLAKE");
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.KEY_GENERATE_STRATEGY_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> mockedStatic = mockStatic(TypedSPILoader.class)) {
            mockedStatic.when(() -> TypedSPILoader.checkService(KeyGenerateAlgorithm.class, "SNOWFLAKE",
                    WorkflowAlgorithmUtils.createProperties(request.getKeyGeneratorProperties()))).thenThrow(new IllegalArgumentException("unavailable"));
            List<Map<String, Object>> actual = new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE SHARDING KEY GENERATE STRATEGY `order_id_strategy`(...)")));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        }
    }
    
    @Test
    void assertValidateWithoutShardingRequest() {
        assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                new WorkflowContextSnapshot(), List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)"))).size(), is(0));
    }
    
    @Test
    void assertValidateIgnoresDropArtifact() {
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, createTableRuleRequest());
        assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                snapshot, List.of(createArtifact("DROP SHARDING TABLE RULE `t_order`"))).size(), is(0));
    }
    
    @Test
    void assertValidateAvailableAlgorithms() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setStorageUnits("ds_0");
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorType("SNOWFLAKE");
        request.getAuditorNames().add("DML_SHARDING_CONDITIONS");
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)"))).size(), is(0));
        }
    }
    
    @Test
    void assertValidateNamedKeyGenerator() {
        ShardingWorkflowRequest request = createTableRuleRequest();
        request.setKeyGenerateColumn("order_id");
        request.setKeyGeneratorName("snowflake_generator");
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        try (MockedStatic<TypedSPILoader> ignored = mockStatic(TypedSPILoader.class)) {
            assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                    snapshot, List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)"))).size(), is(0));
        }
    }
    
    @Test
    void assertValidateTableReferenceRule() {
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_REFERENCE_WORKFLOW_KIND, new ShardingWorkflowRequest());
        assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                snapshot, List.of(createArtifact("CREATE SHARDING TABLE REFERENCE RULE `ref_rule`(...)"))).size(), is(0));
    }
    
    @Test
    void assertValidateEmptyAlgorithmTypes() {
        ShardingWorkflowRequest request = new ShardingWorkflowRequest();
        request.getAuditorNames().add("");
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.TABLE_RULE_WORKFLOW_KIND, request);
        assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                snapshot, List.of(createArtifact("CREATE SHARDING TABLE RULE `t_order`(...)"))).size(), is(0));
    }
    
    @Test
    void assertValidateEmptyKeyGeneratorType() {
        WorkflowContextSnapshot snapshot = createSnapshot(ShardingFeatureDefinition.KEY_GENERATOR_WORKFLOW_KIND, new ShardingWorkflowRequest());
        assertThat(new ShardingWorkflowApplyArtifactValidator().validate(
                snapshot, List.of(createArtifact("CREATE SHARDING KEY GENERATOR `generator`(TYPE(NAME=''))"))).size(), is(0));
    }
    
    private WorkflowContextSnapshot createSnapshot(final WorkflowKind workflowKind, final ShardingWorkflowRequest request) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setWorkflowKind(workflowKind);
        result.setRequest(request);
        return result;
    }
    
    private ShardingWorkflowRequest createTableRuleRequest() {
        ShardingWorkflowRequest result = new ShardingWorkflowRequest();
        result.setAlgorithmType("INLINE");
        result.putAlgorithmProperties(Map.of("algorithm-expression", "t_order_${order_id % 2}"));
        return result;
    }
    
    private ExecutableWorkflowArtifact createArtifact(final String sql) {
        return new ExecutableWorkflowArtifact(sql, sql);
    }
}
