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

import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowArtifactMaskUtilsTest {
    
    private final WorkflowRequest request = createRequest();
    
    @Test
    void assertCreateMaskedRuleArtifactMapMasksSecretProperties() {
        RuleArtifact ruleArtifact = new RuleArtifact("create", "SQL primary-secret 'assist-secret' like-secret");
        Map<String, Object> actualRuleArtifact = WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(ruleArtifact, request, List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", ""),
                new AlgorithmPropertyRequirement("primary", "salt", true, true, "assist", ""),
                new AlgorithmPropertyRequirement("primary", "token", true, true, "like", "")));
        assertThat(actualRuleArtifact.get("operation_type"), is("create"));
        assertThat(actualRuleArtifact.get("sql"), is("SQL ****** '******' ******"));
        Map<?, ?> actualRedaction = (Map<?, ?>) actualRuleArtifact.get("redaction");
        assertTrue((Boolean) actualRedaction.get("applied"));
        assertThat(actualRedaction.get("redacted_count"), is(3));
        assertThat(actualRedaction.get("redacted_properties"), is(List.of("primary.aes-key-value", "primary.salt", "primary.token")));
        assertThat(actualRedaction.get("categories"), is(List.of("aes-key-value", "salt", "token")));
        assertFalse(actualRedaction.containsKey("secret_reference_summary"));
    }
    
    @Test
    void assertMaskPropertyMapMasksSecretKeys() {
        Map<String, String> actual = WorkflowArtifactMaskUtils.maskPropertyMap(Map.of("aes-key-value", "123456", "replace-char", "*", "token", "abc"),
                List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", "")));
        assertThat(actual, is(Map.of("aes-key-value", "******", "replace-char", "*", "token", "******")));
    }
    
    @Test
    void assertMaskSecretReferencePlaceholder() {
        WorkflowRequest actualRequest = createSecretReferenceRequest();
        assertThat(WorkflowArtifactMaskUtils.maskSensitiveSql("SQL secret_reference:primary.aes-key-value", actualRequest, List.of()), is("SQL <SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"));
        assertThat(WorkflowArtifactMaskUtils.maskPropertyMap(Map.of("aes-key-value", "secret_reference:primary.aes-key-value"), List.of()), is(Map.of("aes-key-value", "******")));
        assertThat(WorkflowArtifactMaskUtils.maskPropertyMap(Map.of("replace-char", "?"), List.of(), actualRequest, "primary"), is(Map.of("replace-char", "******")));
        Map<?, ?> actualSummary = WorkflowArtifactMaskUtils.createSecretReferenceSummary(actualRequest);
        assertTrue((Boolean) actualSummary.get("required"));
        assertThat(actualSummary.get("reference_count"), is(2));
        assertThat(actualSummary.get("value_handling"), is("manual_execution"));
        assertFalse(String.valueOf(actualSummary).contains("placeholder://"));
    }
    
    private WorkflowRequest createRequest() {
        WorkflowRequest result = new WorkflowRequest();
        result.getPrimaryAlgorithmProperties().putAll(Map.of("aes-key-value", "primary-secret", "salt", "assist-secret", "token", "like-secret"));
        return result;
    }
    
    private WorkflowRequest createSecretReferenceRequest() {
        WorkflowRequest result = new WorkflowRequest();
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "secret_reference:primary.aes-key-value");
        result.getPrimaryAlgorithmSecretReferences().putAll(Map.of(
                "aes-key-value", SecretReferenceValue.create(),
                "replace-char", SecretReferenceValue.create()));
        return result;
    }
}
