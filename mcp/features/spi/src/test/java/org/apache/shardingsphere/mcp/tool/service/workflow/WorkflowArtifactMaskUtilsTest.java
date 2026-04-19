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
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowFeatureData;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowArtifactMaskUtilsTest {
    
    @Test
    void assertCreateMaskedRuleArtifactMapMasksSecretPropertiesAcrossRoles() {
        WorkflowRequest request = new WorkflowRequest();
        request.getPrimaryAlgorithmProperties().put("aes-key-value", "primary-secret");
        WorkflowFeatureData featureData = new WorkflowFeatureData() {
            
            @Override
            public Map<String, String> getAlgorithmProperties(final String algorithmRole) {
                if ("assisted_query".equals(algorithmRole)) {
                    return Map.of("salt", "assist-secret");
                }
                if ("like_query".equals(algorithmRole)) {
                    return Map.of("token", "like-secret");
                }
                return Map.of();
            }
            
            @Override
            public WorkflowFeatureData copy() {
                return this;
            }
        };
        RuleArtifact ruleArtifact = new RuleArtifact("create", "SQL primary-secret 'assist-secret' like-secret");
        Map<String, Object> actualRuleArtifact = WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(ruleArtifact, WorkflowPropertySources.compose(request, featureData), List.of(
                new AlgorithmPropertyRequirement("primary", "aes-key-value", true, true, "primary", ""),
                new AlgorithmPropertyRequirement("assisted_query", "salt", true, true, "assist", ""),
                new AlgorithmPropertyRequirement("like_query", "token", true, true, "like", "")));
        assertThat(actualRuleArtifact.get("operation_type"), is("create"));
        assertThat(actualRuleArtifact.get("sql"), is("SQL ****** '******' ******"));
    }
    
    @Test
    void assertMaskSensitiveSqlKeepsSqlWhenRequestIsNull() {
        String actualSql = WorkflowArtifactMaskUtils.maskSensitiveSql("SELECT plain_text", null, List.of());
        assertThat(actualSql, is("SELECT plain_text"));
    }
}
