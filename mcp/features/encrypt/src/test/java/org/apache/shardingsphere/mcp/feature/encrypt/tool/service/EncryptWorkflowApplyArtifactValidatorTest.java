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

import org.apache.shardingsphere.encrypt.spi.EncryptAlgorithm;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowAlgorithmUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle.ExecutableWorkflowArtifact;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

class EncryptWorkflowApplyArtifactValidatorTest {
    
    @Test
    void assertValidateInvalidGeneratedEncryptRule() {
        String sql = "CREATE ENCRYPT RULE t_user (COLUMNS((NAME=name, CIPHER=name_cipher, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME=AES, PROPERTIES('aes-key-value'='123456'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowApplyArtifactValidator().validate(new WorkflowContextSnapshot(),
                List.of(new ExecutableWorkflowArtifact(sql, sql.replace("123456", "******"))));
        assertThat(actual.size(), is(3));
        assertThat(actual.getFirst().get("code"), is(WorkflowIssueCode.SQL_EXECUTABILITY_FAILED));
        assertThat(actual.getFirst().get("message"), is("Generated encrypt DistSQL uses reserved logical column identifier `name` without DistSQL quoting."));
        assertFalse(String.valueOf(actual).contains("123456"));
    }
    
    @Test
    void assertValidateAllowsNamePrefixColumn() {
        String sql = "CREATE ENCRYPT RULE t_user (COLUMNS((NAME=name_cipher, CIPHER=name_cipher_value, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='123456', 'digest-algorithm-name'='SHA-1'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowApplyArtifactValidator().validate(new WorkflowContextSnapshot(),
                List.of(createArtifact(sql, sql.replace("123456", "******"))));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertValidateRejectsUnavailableEncryptAlgorithm() {
        EncryptWorkflowRequest request = new EncryptWorkflowRequest();
        request.setAlgorithmType("AES");
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "raw-secret");
        request.getPrimaryAlgorithmProperties().put("digest-algorithm-name", "SHA-1");
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setRequest(request);
        String sql = "CREATE ENCRYPT RULE `t_user` (COLUMNS((NAME=`phone`, CIPHER=`phone_cipher`, "
                + "ENCRYPT_ALGORITHM(TYPE(NAME='aes', PROPERTIES('aes-key-value'='******', 'digest-algorithm-name'='SHA-1'))))))";
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class, CALLS_REAL_METHODS)) {
            typedSPILoader.when(() -> TypedSPILoader.checkService(
                    EncryptAlgorithm.class, "AES", WorkflowAlgorithmUtils.createProperties(request.getPrimaryAlgorithmProperties())))
                    .thenThrow(new IllegalArgumentException("raw-secret"));
            List<Map<String, Object>> actual = new EncryptWorkflowApplyArtifactValidator().validate(snapshot, List.of(createArtifact(sql, sql)));
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().get("message"), is("Generated encrypt DistSQL references encrypt algorithm `AES`, "
                    + "but it cannot be loaded or initialized by EncryptAlgorithm SPI."));
            assertFalse(String.valueOf(actual).contains("raw-secret"));
        }
    }
    
    @Test
    void assertValidateIgnoresNonEncryptRuleArtifacts() {
        String sql = "CREATE MASK RULE orders (COLUMNS((NAME=name, "
                + "MASK_ALGORITHM(TYPE(NAME=AES, PROPERTIES('description'='CREATE ENCRYPT RULE', 'aes-key-value'='123456'))))))";
        List<Map<String, Object>> actual = new EncryptWorkflowApplyArtifactValidator().validate(new WorkflowContextSnapshot(), List.of(createArtifact(sql, sql)));
        assertTrue(actual.isEmpty());
    }
    
    private ExecutableWorkflowArtifact createArtifact(final String sql, final String displaySql) {
        return new ExecutableWorkflowArtifact(sql, displaySql);
    }
}
