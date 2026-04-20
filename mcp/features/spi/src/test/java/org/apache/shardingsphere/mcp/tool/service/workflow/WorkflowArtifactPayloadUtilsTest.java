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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

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
        Map<String, Object> actual = WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, WorkflowPropertySources.compose(snapshot.getRequest(), snapshot.getFeatureData()));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS)).size(), is(1));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN)).size(), is(1));
        assertThat(((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).size(), is(1));
        assertThat(((Map<?, ?>) ((List<?>) actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS)).get(0)).get("sql"),
                is("CREATE ENCRYPT RULE t (PROPERTIES('aes-key-value'='******'))"));
    }
}
