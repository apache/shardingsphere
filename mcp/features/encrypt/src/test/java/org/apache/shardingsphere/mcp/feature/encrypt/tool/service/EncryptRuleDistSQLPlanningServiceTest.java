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
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EncryptRuleDistSQLPlanningServiceTest {
    
    private final EncryptRuleDistSQLPlanningService service = new EncryptRuleDistSQLPlanningService();
    
    @Test
    void assertPlanEncryptRuleWithCreate() {
        EncryptWorkflowRequest request = createRequest("create", true, true);
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("create"));
        assertTrue(actual.getFirst().getSql().startsWith("CREATE ENCRYPT RULE `orders`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=`phone`"));
        assertTrue(actual.getFirst().getSql().contains("CIPHER=`phone_cipher`"));
        assertTrue(actual.getFirst().getSql().contains("ASSISTED_QUERY_COLUMN=`phone_assisted_query`"));
        assertTrue(actual.getFirst().getSql().contains("LIKE_QUERY_COLUMN=`phone_like_query`"));
    }
    
    @Test
    void assertPlanEncryptRuleEscapesAlgorithmLiterals() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setAlgorithmType("AES'X");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "s'1");
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertTrue(actual.getFirst().getSql().contains("TYPE(NAME='aes''x', PROPERTIES('aes-key-value'='s''1'))"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsSpecialCharacterIdentifiers() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setTable("order detail");
        request.setColumn("Phone Number");
        request.getOptions().setCipherColumnName("Phone Number Cipher");
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertTrue(actual.getFirst().getSql().startsWith("CREATE ENCRYPT RULE `order detail`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=`Phone Number`"));
        assertTrue(actual.getFirst().getSql().contains("CIPHER=`Phone Number Cipher`"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsReservedIdentifiers() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setTable("key");
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertTrue(actual.getFirst().getSql().startsWith("CREATE ENCRYPT RULE `key`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=`phone`"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsReservedColumnIdentifier() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setTable("t_user");
        request.setColumn("name");
        request.getOptions().setCipherColumnName("name_cipher");
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertTrue(actual.getFirst().getSql().startsWith("CREATE ENCRYPT RULE `t_user`"));
        assertTrue(actual.getFirst().getSql().contains("NAME=`name`"));
        assertTrue(actual.getFirst().getSql().contains("CIPHER=`name_cipher`"));
        assertTrue(actual.getFirst().getSql().contains("TYPE(NAME='aes'"));
    }
    
    @Test
    void assertPlanEncryptRuleFormatsReservedQueryColumnIdentifiers() {
        EncryptWorkflowRequest request = createRequest("create", true, true);
        request.getOptions().setCipherColumnName("cipher");
        request.getOptions().setAssistedQueryColumnName("order");
        request.getOptions().setLikeQueryColumnName("type");
        List<RuleArtifact> actual = service.planEncryptRule(request);
        assertTrue(actual.getFirst().getSql().contains("CIPHER=`cipher`"));
        assertTrue(actual.getFirst().getSql().contains("ASSISTED_QUERY_COLUMN=`order`"));
        assertTrue(actual.getFirst().getSql().contains("LIKE_QUERY_COLUMN=`type`"));
    }
    
    @Test
    void assertPlanEncryptRuleRejectsLineTerminatorColumn() {
        EncryptWorkflowRequest request = createRequest("create", false, false);
        request.setColumn("phone\ndrop");
        MCPInvalidRequestException actualException = assertThrows(MCPInvalidRequestException.class, () -> service.planEncryptRule(request));
        assertThat(actualException.getMessage(), is("column `phone\ndrop` contains unsupported characters that cannot be rendered as a reviewable SQL identifier."));
    }
    
    @Test
    void assertPlanEncryptDropRuleWithoutRemainingColumns() {
        EncryptWorkflowRequest request = createRequest("drop", false, false);
        request.getOptions().setCipherColumnName("");
        List<RuleArtifact> actual = service.planEncryptDropRule(request);
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().getOperationType(), is("drop"));
        assertThat(actual.getFirst().getSql(), is("DROP ENCRYPT RULE `orders`"));
    }
    
    private EncryptWorkflowRequest createRequest(final String operationType, final boolean equalityFilter, final boolean likeQuery) {
        EncryptWorkflowRequest result = new EncryptWorkflowRequest();
        result.setOperationType(operationType);
        result.setTable("orders");
        result.setColumn("phone");
        result.setAlgorithmType("AES");
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "secret");
        result.getOptions().setCipherColumnName("phone_cipher");
        result.getOptions().setRequiresEqualityFilter(equalityFilter);
        result.getOptions().setRequiresLikeQuery(likeQuery);
        result.getOptions().setAssistedQueryColumnName("phone_assisted_query");
        result.getOptions().setAssistedQueryAlgorithmType("MD5");
        result.getOptions().getAssistedQueryAlgorithmProperties().put("salt", "salt");
        result.getOptions().setLikeQueryColumnName("phone_like_query");
        result.getOptions().setLikeQueryAlgorithmType("FPE");
        result.getOptions().getLikeQueryAlgorithmProperties().put("salt", "salt");
        return result;
    }
}
