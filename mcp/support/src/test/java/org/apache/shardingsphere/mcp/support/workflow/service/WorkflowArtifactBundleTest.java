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
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowArtifactBundleTest {
    
    @Test
    void assertToExecutableArtifacts() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE t ADD COLUMN c_cipher VARCHAR(32)", 1));
        snapshot.getIndexPlans().add(new IndexPlan("idx_t_c_cipher", "c_cipher", "lookup", "CREATE INDEX idx_t_c_cipher ON t(c_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot).toExecutableArtifacts();
        assertThat(actual.size(), is(3));
        assertThat(actual.getFirst().approvalStep(), is(WorkflowArtifactPayloadUtils.STEP_DDL));
        assertThat(actual.get(1).artifactType(), is(WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_CREATE_INDEX));
        assertTrue(actual.get(2).ruleDistSql());
    }
    
    @Test
    void assertToExecutableArtifactsWithMaskedDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE t ADD COLUMN c_cipher VARCHAR(32)", 1));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toExecutableArtifacts(createPropertySource(), List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", "")));
        assertThat(actual.size(), is(2));
        assertThat(actual.get(1).sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        assertThat(actual.get(1).displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
    }
    
    @Test
    void assertToRuleExecutableArtifacts() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE t ADD COLUMN c_cipher VARCHAR(32)", 1));
        snapshot.getIndexPlans().add(new IndexPlan("idx_t_c_cipher", "c_cipher", "lookup", "CREATE INDEX idx_t_c_cipher ON t(c_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot).toRuleExecutableArtifacts();
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().approvalStep(), is(WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL));
        assertThat(actual.getFirst().artifactType(), is(WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL));
        assertTrue(actual.getFirst().ruleDistSql());
    }
    
    @Test
    void assertToRuleExecutableArtifactsWithMaskedDisplaySql() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> actual = WorkflowArtifactBundle.from(snapshot)
                .toRuleExecutableArtifacts(createPropertySource(), List.of(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", "")));
        assertThat(actual.size(), is(1));
        assertThat(actual.getFirst().sql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='primary-secret'))"));
        assertThat(actual.getFirst().displaySql(), is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
    }
    
    private WorkflowPropertySource createPropertySource() {
        return algorithmRole -> "primary".equals(algorithmRole) ? Map.of("aes-key-value", "primary-secret") : Map.of();
    }
}
