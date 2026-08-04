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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowArtifactBundleTest {
    
    @Test
    void assertToExecutableArtifacts() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot).toExecutableArtifacts(new WorkflowRequest(), List.of());
        assertThat(actual, is(List.of(new WorkflowArtifactBundle.ExecutableWorkflowArtifact("CREATE ENCRYPT RULE t", "CREATE ENCRYPT RULE t"))));
    }
    
    @Test
    void assertToExecutableArtifactsWithMaskedDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toExecutableArtifacts(createRequest(), List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", "")));
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        assertThat(actual.getFirst().displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertToRuleExecutableArtifactsKeepsSecretReferenceSqlSeparateFromDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toExecutableArtifacts(createSecretReferenceRequest(), List.of());
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='secret_reference:primary.aes-key-value'))"));
        assertThat(actual.getFirst().displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='<SECRET_VALUE_PRIMARY_AES_KEY_VALUE>'))"));
    }
    
    private WorkflowRequest createRequest() {
        WorkflowRequest result = new WorkflowRequest();
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "primary-secret");
        return result;
    }
    
    private WorkflowRequest createSecretReferenceRequest() {
        WorkflowRequest result = new WorkflowRequest();
        result.getPrimaryAlgorithmProperties().put("aes-key-value", "secret_reference:primary.aes-key-value");
        result.getPrimaryAlgorithmSecretReferences().put("aes-key-value", SecretReferenceValue.create());
        return result;
    }
}
