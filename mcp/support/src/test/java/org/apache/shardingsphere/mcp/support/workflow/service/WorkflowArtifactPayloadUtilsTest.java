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
import org.apache.shardingsphere.mcp.support.workflow.model.DDLArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.IndexPlan;
import org.apache.shardingsphere.mcp.support.workflow.model.RuleArtifact;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFeatureData;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowArtifactPayloadUtilsTest {
    
    @Test
    void assertCreateArtifactPayload() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "123456");
        snapshot.setRequest(request);
        snapshot.getPropertyRequirements().add(new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "AES key.", ""));
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE t ADD COLUMN c_cipher VARCHAR(32)", 1));
        snapshot.getIndexPlans().add(new IndexPlan("idx_t_c_cipher", "c_cipher", "lookup", "CREATE INDEX idx_t_c_cipher ON t(c_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='123456'))"));
        Map<String, Object> actual = WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, snapshot.getRequest());
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS)).size(), is(1));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN)).size(), is(1));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).size(), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).getFirst()).get("sql"),
                is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
        Map<?, ?> actualRedaction = (Map<?, ?>) ((Map<?, ?>) ((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).getFirst()).get("redaction");
        assertThat(actualRedaction.get("marker"), is("******"));
        assertThat(actualRedaction.get("redacted_count"), is(1));
        assertThat(actualRedaction.get("redacted_properties"), is(List.of("primary.aes-key-value")));
    }
    
    @Test
    void assertCreateRuleArtifactPayload() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        WorkflowRequest request = new WorkflowRequest();
        snapshot.setRequest(request);
        snapshot.getDdlArtifacts().add(new DDLArtifact("add-column", "ALTER TABLE t ADD COLUMN c_cipher VARCHAR(32)", 1));
        snapshot.getIndexPlans().add(new IndexPlan("idx_t_c_cipher", "c_cipher", "lookup", "CREATE INDEX idx_t_c_cipher ON t(c_cipher)"));
        snapshot.getRuleArtifacts().add(new RuleArtifact("create", "CREATE ENCRYPT RULE t"));
        Map<String, Object> actual = WorkflowArtifactPayloadUtils.createRuleArtifactPayload(snapshot, snapshot.getRequest());
        assertFalse(actual.containsKey(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS));
        assertFalse(actual.containsKey(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).size(), is(1));
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot));
    }
    
    @Test
    void assertRuleWorkflowWithFeatureStateIsRuleDistSQLOnly() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        snapshot.setFeatureData(new TestWorkflowFeatureData());
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot));
    }
    
    @Test
    void assertRuleWorkflowWithoutPhysicalArtifactsIsRuleDistSQLOnly() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("encrypt.rule"));
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot));
    }
    
    @Test
    void assertBroadcastWorkflowIsRuleDistSQLOnly() {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf("broadcast.rule"));
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot));
    }
    
    @Test
    void assertReadwriteWorkflowsAreRuleDistSQLOnly() {
        WorkflowContextSnapshot ruleSnapshot = new WorkflowContextSnapshot();
        ruleSnapshot.setWorkflowKind(WorkflowKind.valueOf("readwrite.rule"));
        WorkflowContextSnapshot statusSnapshot = new WorkflowContextSnapshot();
        statusSnapshot.setWorkflowKind(WorkflowKind.valueOf("readwrite.status"));
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(ruleSnapshot));
        assertTrue(WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(statusSnapshot));
    }
    
    @Test
    void assertShardingWorkflowsAreRuleDistSQLOnly() {
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.table.rule"));
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.table.reference"));
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.default.strategy"));
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.key.generator"));
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.key.generate.strategy"));
        assertTrue(isRuleDistSQLOnlyWorkflow("sharding.component.cleanup"));
    }
    
    private boolean isRuleDistSQLOnlyWorkflow(final String workflowKind) {
        WorkflowContextSnapshot snapshot = new WorkflowContextSnapshot();
        snapshot.setWorkflowKind(WorkflowKind.valueOf(workflowKind));
        return WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot);
    }
    
    private static final class TestWorkflowFeatureData implements WorkflowFeatureData {
        
        @Override
        public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
            return Map.of();
        }
        
        @Override
        public WorkflowFeatureData copy() {
            return new TestWorkflowFeatureData();
        }
    }
}
