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

import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSecretReferenceUtilsTest {
    
    @Test
    void assertCreateAlgorithmProperties() {
        Map<String, Object> rawProperties = Map.of(
                "aes-key-value", Map.of("secret_ref", "placeholder://secret-value-1", "label", "user label"),
                "digest-algorithm-name", "SHA-256");
        Map<String, String> actual = WorkflowSecretReferenceUtils.createAlgorithmProperties(rawProperties, "primary");
        assertThat(actual, is(Map.of("aes-key-value", "secret_reference:primary.aes-key-value", "digest-algorithm-name", "SHA-256")));
    }
    
    @Test
    void assertCreateSecretReferences() {
        Map<String, Object> rawProperties = Map.of(
                "aes-key-value", Map.of("secret_ref", " placeholder://secret-value-1 ", "label", "user label"),
                "digest-algorithm-name", "SHA-256");
        Map<String, SecretReferenceValue> actual = WorkflowSecretReferenceUtils.createSecretReferences(rawProperties);
        assertThat(actual.size(), is(1));
        assertFalse(actual.get("aes-key-value").isMalformed());
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
        assertFalse(String.valueOf(actual).contains("user label"));
    }
    
    @Test
    void assertCreateMalformedSecretReferences() {
        Map<String, SecretReferenceValue> actual = WorkflowSecretReferenceUtils.createSecretReferences(Map.of("aes-key-value", Map.of("label", "user label")));
        assertTrue(actual.get("aes-key-value").isMalformed());
    }
    
    @Test
    void assertCreateSafeSummaries() {
        WorkflowPropertySource propertySource = new WorkflowPropertySource() {
            
            @Override
            public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
                return Map.of();
            }
            
            @Override
            public Map<String, Map<String, SecretReferenceValue>> getSecretReferences() {
                return Map.of("primary", Map.of("aes-key-value", SecretReferenceValue.create()));
            }
        };
        List<Map<String, Object>> actual = WorkflowSecretReferenceUtils.createSafeSummaries(propertySource);
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().get("label"), is("secret_placeholder:primary.aes-key-value"));
        assertFalse(String.valueOf(actual).contains("placeholder://secret-value-1"));
        assertFalse(String.valueOf(actual).contains("user label"));
    }
    
    @Test
    void assertReplacePlaceholdersWithManualPlaceholders() {
        assertThat(WorkflowSecretReferenceUtils.replacePlaceholdersWithManualPlaceholders(
                "PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value')", createSecretReferencePropertySource()),
                is("PROPERTIES('aes-key-value'='<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>')"));
    }
    
    @Test
    void assertMatchesManualPlaceholderProperties() {
        assertTrue(WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(
                Map.of("aes-key-value", "secret_reference:primary.aes-key-value", "digest-algorithm-name", "SHA-256"),
                Map.of("aes-key-value", "manually-filled-secret", "digest-algorithm-name", "SHA-256"),
                createSecretReferencePropertySource(), "primary"));
    }
    
    @Test
    void assertMatchesManualPlaceholderPropertiesRejectsUnresolvedPlaceholder() {
        assertFalse(WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(
                Map.of("aes-key-value", "secret_reference:primary.aes-key-value"),
                Map.of("aes-key-value", "secret_reference:primary.aes-key-value"),
                createSecretReferencePropertySource(), "primary"));
    }
    
    @Test
    void assertMatchesManualPlaceholderPropertiesRejectsNeutralPlaceholder() {
        assertFalse(WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(
                Map.of("aes-key-value", "secret_reference:primary.aes-key-value"),
                Map.of("aes-key-value", "<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>"),
                createSecretReferencePropertySource(), "primary"));
    }
    
    @Test
    void assertMatchesManualPlaceholderPropertiesRejectsMissingValue() {
        assertFalse(WorkflowSecretReferenceUtils.matchesManualPlaceholderProperties(
                Map.of("aes-key-value", "secret_reference:primary.aes-key-value"),
                Map.of(),
                createSecretReferencePropertySource(), "primary"));
    }
    
    private WorkflowPropertySource createSecretReferencePropertySource() {
        return new WorkflowPropertySource() {
            
            @Override
            public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
                return Map.of();
            }
            
            @Override
            public Map<String, Map<String, SecretReferenceValue>> getSecretReferences() {
                return Map.of("primary", Map.of("aes-key-value", SecretReferenceValue.create()));
            }
        };
    }
}
