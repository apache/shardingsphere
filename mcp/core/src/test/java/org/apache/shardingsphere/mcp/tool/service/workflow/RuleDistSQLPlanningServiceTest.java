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

import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuleDistSQLPlanningServiceTest {
    
    @Test
    void assertPlanEncryptRuleBuildsExpectedSql() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("status");
        request.setOperationType("alter");
        request.setAlgorithmType("AES");
        request.setAssistedQueryAlgorithmType("CRC32");
        request.setLikeQueryAlgorithmType("MD5");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        ClarifiedIntent intent = new ClarifiedIntent();
        intent.setRequiresEqualityFilter(true);
        intent.setRequiresLikeQuery(true);
        DerivedColumnPlan derivedColumnPlan = new DerivedColumnPlan();
        derivedColumnPlan.setCipherColumnName("status_cipher");
        derivedColumnPlan.setAssistedQueryColumnRequired(true);
        derivedColumnPlan.setAssistedQueryColumnName("status_assisted_query");
        derivedColumnPlan.setLikeQueryColumnRequired(true);
        derivedColumnPlan.setLikeQueryColumnName("status_like_query");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planEncryptRule(request, intent, derivedColumnPlan);
        String expected = "ALTER ENCRYPT RULE orders (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator()
                + "(NAME=status, CIPHER=status_cipher, ASSISTED_QUERY_COLUMN=status_assisted_query, LIKE_QUERY_COLUMN=status_like_query, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456'))), ASSISTED_QUERY_ALGORITHM(TYPE(NAME='crc32')), "
                + "LIKE_QUERY_ALGORITHM(TYPE(NAME='md5')))))";
        assertThat(actual.getSql(), is(expected));
    }
    
    @Test
    void assertPlanEncryptRuleKeepsSiblingColumnsWhenTableRuleAlreadyExists() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("status");
        request.setOperationType("create");
        request.setAlgorithmType("AES");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        ClarifiedIntent intent = new ClarifiedIntent();
        DerivedColumnPlan derivedColumnPlan = new DerivedColumnPlan();
        derivedColumnPlan.setCipherColumnName("status_cipher");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planEncryptRule(request, intent, derivedColumnPlan, List.of(Map.of(
                "table", "orders",
                "logic_column", "order_id",
                "cipher_column", "order_id_cipher",
                "encryptor_type", "AES",
                "encryptor_props", Map.of("aes-key-value", "origin-secret"))));
        assertThat(actual.getSql(), containsString("ALTER ENCRYPT RULE orders"));
        assertThat(actual.getSql(), containsString("NAME=order_id"));
        assertThat(actual.getSql(), containsString("NAME=status"));
    }
    
    @Test
    void assertPlanEncryptRuleKeepsSiblingAlgorithmPropertiesWhenProxyReturnsJsonProps() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("amount");
        request.setOperationType("create");
        request.setAlgorithmType("AES");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "second-secret");
        request.getPrimaryAlgorithmProperties().put("digest-algorithm-name", "SHA-1");
        ClarifiedIntent intent = new ClarifiedIntent();
        DerivedColumnPlan derivedColumnPlan = new DerivedColumnPlan();
        derivedColumnPlan.setCipherColumnName("amount_cipher");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planEncryptRule(request, intent, derivedColumnPlan, List.of(Map.of(
                "table", "orders",
                "logic_column", "status",
                "cipher_column", "status_cipher",
                "encryptor_type", "AES",
                "encryptor_props", "{\"aes-key-value\":\"first-secret\",\"digest-algorithm-name\":\"SHA-1\"}")));
        assertThat(actual.getSql(), containsString("NAME=status"));
        assertThat(actual.getSql(), containsString("'aes-key-value'='first-secret'"));
        assertThat(actual.getSql(), containsString("'digest-algorithm-name'='SHA-1'"));
    }
    
    @Test
    void assertPlanMaskRuleBuildsExpectedSql() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("phone");
        request.setOperationType("create");
        request.setAlgorithmType("KEEP_FIRST_N_LAST_M");
        request.getPrimaryAlgorithmProperties().put("first-n", "1");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planMaskRule(request);
        String expected = "CREATE MASK RULE orders (" + System.lineSeparator() + "COLUMNS(" + System.lineSeparator()
                + "(NAME=phone, TYPE(NAME='keep_first_n_last_m', PROPERTIES('first-n'='1')))" + System.lineSeparator() + "))";
        assertThat(actual.getSql(), is(expected));
    }
    
    @Test
    void assertPlanMaskRuleKeepsSiblingColumnsWhenTableRuleAlreadyExists() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("status");
        request.setOperationType("create");
        request.setAlgorithmType("KEEP_FIRST_N_LAST_M");
        request.getPrimaryAlgorithmProperties().put("first-n", "1");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planMaskRule(request, List.of(Map.of(
                "table", "orders",
                "column", "amount",
                "algorithm_type", "MD5",
                "algorithm_props", Map.of())));
        assertThat(actual.getSql(), containsString("ALTER MASK RULE orders"));
        assertThat(actual.getSql(), containsString("NAME=amount"));
        assertThat(actual.getSql(), containsString("NAME=status"));
    }
    
    @Test
    void assertPlanMaskDropRuleBuildsExpectedSql() {
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planMaskDropRule("orders");
        assertThat(actual.getSql(), is("DROP MASK RULE orders"));
    }
    
    @Test
    void assertPlanMaskDropRuleKeepsSiblingColumns() {
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("orders");
        request.setColumn("status");
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        RuleArtifact actual = service.planMaskDropRule(request, List.of(
                Map.of("table", "orders", "column", "status", "algorithm_type", "MD5", "algorithm_props", Map.of()),
                Map.of("table", "orders", "column", "amount", "algorithm_type", "KEEP_FIRST_N_LAST_M", "algorithm_props", Map.of("first-n", "1"))));
        assertThat(actual.getSql(), containsString("ALTER MASK RULE orders"));
        assertThat(actual.getSql(), containsString("NAME=amount"));
        assertFalse(actual.getSql().contains("NAME=status"));
    }
    
    @Test
    void assertPlanMaskRuleRejectsUnsafeIdentifier() {
        RuleDistSQLPlanningService service = new RuleDistSQLPlanningService();
        WorkflowRequest request = new WorkflowRequest();
        request.setTable("bad table");
        request.setColumn("phone");
        request.setOperationType("create");
        request.setAlgorithmType("MASK_TYPE");
        Exception actual = assertThrows(RuntimeException.class, () -> service.planMaskRule(request));
        assertThat(actual.getMessage(), is("table `bad table` contains unsupported characters. Only unquoted SQL identifiers are supported in V1."));
    }
}
