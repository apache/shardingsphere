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

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.DerivedColumnPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleDistSQLPlanningServiceTest {
    
    private final EncryptRuleDistSQLPlanningService service = new EncryptRuleDistSQLPlanningService();
    
    @Test
    void assertPlanEncryptRuleWithCreate() {
        EncryptWorkflowRequest request = createRequest("create", true, true);
        List<RuleArtifact> actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of(), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getOperationType(), is("create"));
        assertTrue(actual.get(0).getSql().startsWith("CREATE ENCRYPT RULE `orders`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`phone`"));
        assertTrue(actual.get(0).getSql().contains("ASSISTED_QUERY_COLUMN=`phone_assisted_query`"));
        assertTrue(actual.get(0).getSql().contains("LIKE_QUERY_COLUMN=`phone_like_query`"));
    }
    
    @Test
    void assertPlanEncryptRuleWithExistingRules() {
        EncryptWorkflowRequest request = createRequest("alter", true, false);
        List<RuleArtifact> actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of(
                Map.of("logic_column", "phone", "cipher_column", "old_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old"),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getOperationType(), is("alter"));
        assertTrue(actual.get(0).getSql().startsWith("ALTER ENCRYPT RULE `orders`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`email`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`phone`"));
        assertTrue(actual.get(0).getSql().contains("CIPHER=`phone_cipher`"));
    }
    
    @Test
    void assertPlanEncryptRuleEscapesAlgorithmLiterals() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setAlgorithmType("AES'X");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "s'1");
        List<RuleArtifact> actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of(), "MySQL");
        assertTrue(actual.get(0).getSql().contains("TYPE(NAME='aes''x', PROPERTIES('aes-key-value'='s''1'))"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsSpecialCharacterIdentifiers() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setTable("order detail");
        request.setColumn("Phone Number");
        DerivedColumnPlan derivedColumnPlan = createDerivedColumnPlan();
        derivedColumnPlan.setCipherColumnName("Phone Number Cipher");
        List<RuleArtifact> actual = service.planEncryptRule(request, derivedColumnPlan, List.of(), "MySQL");
        assertTrue(actual.get(0).getSql().startsWith("CREATE ENCRYPT RULE `order detail`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`Phone Number`"));
        assertTrue(actual.get(0).getSql().contains("CIPHER=`Phone Number Cipher`"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsReservedIdentifiers() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setTable("key");
        List<RuleArtifact> actual = service.planEncryptRule(request, createDerivedColumnPlan(), List.of(), "MySQL");
        assertTrue(actual.get(0).getSql().startsWith("CREATE ENCRYPT RULE `key`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`phone`"));
    }
    
    @Test
    void assertPlanEncryptRuleRejectsLineTerminatorColumn() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setColumn("phone\ndrop");
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class, () -> service.planEncryptRule(request, createDerivedColumnPlan(), List.of(), "MySQL"));
        assertThat(actualException.getMessage(), is("column `phone\ndrop` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertPlanEncryptDropRuleWithoutRemainingColumns() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        List<RuleArtifact> actual = service.planEncryptDropRule(request, List.of(Map.of("logic_column", "phone", "cipher_column", "phone_cipher")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getOperationType(), is("drop"));
        assertThat(actual.get(0).getSql(), is("DROP ENCRYPT RULE `orders`"));
    }
    
    @Test
    void assertPlanEncryptDropRuleWithRemainingColumns() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        List<RuleArtifact> actual = service.planEncryptDropRule(request, List.of(
                Map.of("logic_column", "phone", "cipher_column", "phone_cipher"),
                Map.of("logic_column", "email", "cipher_column", "email_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getOperationType(), is("drop"));
        assertTrue(actual.get(0).getSql().startsWith("ALTER ENCRYPT RULE `orders`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`email`"));
        assertTrue(actual.get(0).getSql().contains("CIPHER=`email_cipher`"));
    }
    
    @Test
    void assertPlanEncryptDropRulePreservesCaseSensitiveSiblingColumn() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        request.setColumn("Phone");
        List<RuleArtifact> actual = service.planEncryptDropRule(request, List.of(
                Map.of("logic_column", "Phone", "cipher_column", "Phone_cipher"),
                Map.of("logic_column", "phone", "cipher_column", "phone_cipher", "encryptor_type", "AES", "encryptor_props", "aes-key-value=old")), "PostgreSQL");
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).getSql().startsWith("ALTER ENCRYPT RULE `orders`"));
        assertTrue(actual.get(0).getSql().contains("NAME=`phone`"));
        assertTrue(actual.get(0).getSql().contains("CIPHER=`phone_cipher`"));
    }
    
    @Test
    void assertPlanEncryptDropRuleMatchesCaseInsensitiveColumn() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        request.setColumn("Phone");
        List<RuleArtifact> actual = service.planEncryptDropRule(request, List.of(Map.of("logic_column", "phone", "cipher_column", "phone_cipher")), "MySQL");
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).getOperationType(), is("drop"));
        assertThat(actual.get(0).getSql(), is("DROP ENCRYPT RULE `orders`"));
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
