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
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.SecretReferenceValue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowArtifactBundleTest {
    
    @Test
    void assertToExecutableArtifacts() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot).toExecutableArtifacts(role -> Map.of(), List.of());
        assertThat(actual, is(List.of(new WorkflowArtifactBundle.ExecutableWorkflowArtifact("CREATE ENCRYPT RULE t", "CREATE ENCRYPT RULE t"))));
    }
    
    @Test
    void assertToExecutableArtifactsWithMaskedDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toExecutableArtifacts(createPropertySource(), List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", "")));
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        assertThat(actual.getFirst().displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertToRuleExecutableArtifactsKeepsSecretReferenceSqlSeparateFromDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toExecutableArtifacts(createSecretReferencePropertySource(), List.of());
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value'))"));
        assertThat(actual.getFirst().displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>'))"));
    }
    
    private WorkflowPropertySource createPropertySource() {
        return algorithmRole -> "primary".equals(algorithmRole) ? Map.of("aes-key-value", "primary-secret") : Map.of();
    }
    
    private WorkflowPropertySource createSecretReferencePropertySource() {
        return new WorkflowPropertySource() {
            
            @Override
            public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
                return "primary".equals(algorithmRole) ? Map.of("aes-key-value", "secret_reference:primary.aes-key-value") : Map.of();
            }
            
            @Override
            public Map<String, Map<String, SecretReferenceValue>> getSecretReferences() {
                return Map.of("primary", Map.of("aes-key-value", SecretReferenceValue.create()));
            }
        };
    }
}
