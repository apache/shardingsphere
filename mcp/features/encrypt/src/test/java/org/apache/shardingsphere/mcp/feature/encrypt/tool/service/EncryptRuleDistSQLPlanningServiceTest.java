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

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.tool.model.workflow.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleDistSQLPlanningServiceTest {
    
    private final EncryptRuleDistSQLPlanningService service = new EncryptRuleDistSQLPlanningService();
    
    @Test
    void assertPlanEncryptRuleWithCreate() {
        EncryptWorkflowRequest request = createRequest("create", true, true);
        RuleArtifact actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of());
        assertThat(actual.getOperationType(), is("create"));
        assertTrue(actual.getSql().startsWith("CREATE ENCRYPT RULE orders"));
        assertTrue(actual.getSql().contains("NAME=phone"));
        assertTrue(actual.getSql().contains("ASSISTED_QUERY=phone_assisted_query"));
        assertTrue(actual.getSql().contains("LIKE_QUERY=phone_like_query"));
    }
    
    @Test
    void assertPlanEncryptRuleWithExistingRules() {
        EncryptWorkflowRequest request = createRequest("alter", true, false);
        RuleArtifact actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of(
                Map.of("logic_column", "phone", "cipher_column", "old_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old"),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")));
        assertThat(actual.getOperationType(), is("alter"));
        assertTrue(actual.getSql().startsWith("ALTER ENCRYPT RULE orders"));
        assertTrue(actual.getSql().contains("NAME=email"));
        assertTrue(actual.getSql().contains("NAME=phone"));
        assertTrue(actual.getSql().contains("CIPHER=phone_cipher"));
    }
    
    @Test
    void assertPlanEncryptDropRuleWithoutRemainingColumns() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        RuleArtifact actual = service.planEncryptDropRule(request, List.of(Map.of("logic_column", "phone", "cipher_column", "phone_cipher")));
        assertThat(actual.getOperationType(), is("drop"));
        assertThat(actual.getSql(), is("DROP ENCRYPT RULE orders"));
    }
    
    @Test
    void assertPlanEncryptDropRuleWithRemainingColumns() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        RuleArtifact actual = service.planEncryptDropRule(request, List.of(
                Map.of("logic_column", "phone", "cipher_column", "phone_cipher"),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")));
        assertThat(actual.getOperationType(), is("drop"));
        assertTrue(actual.getSql().startsWith("ALTER ENCRYPT RULE orders"));
        assertTrue(actual.getSql().contains("NAME=email"));
        assertTrue(actual.getSql().contains("CIPHER=email_cipher"));
    }
    
    private EncryptWorkflowRequest createRequest(final String operationType, final boolean equalityFilter, final boolean likeQuery) {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setOperationType(operationType);
        result.setTable("orders");
        result.setColumn("phone");
        result.setAlgorithmType("AES");
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "secret");
        result.getOptions().setRequiresEqualityFilter(equalityFilter);
        result.getOptions().setRequiresLikeQuery(likeQuery);
        result.getOptions().setAssistedQueryAlgorithmType("MD5");
        result.getOptions().getAssistedQueryAlgorithmProperties().put("salt", "salt");
        result.getOptions().setLikeQueryAlgorithmType("FPE");
        result.getOptions().getLikeQueryAlgorithmProperties().put("salt", "salt");
        return result;
    }
    
    private DerivedColumnPlan createDerivedColumnPlan() {
        DerivedColumnPlan result = new DerivedColumnPlan();
        result.setCipherColumnName("phone_cipher");
        result.setCipherColumnRequired(true);
        result.setAssistedQueryColumnName("phone_assisted_query");
        result.setAssistedQueryColumnRequired(true);
        result.setLikeQueryColumnName("phone_like_query");
        result.setLikeQueryColumnRequired(true);
        return result;
    }
}
