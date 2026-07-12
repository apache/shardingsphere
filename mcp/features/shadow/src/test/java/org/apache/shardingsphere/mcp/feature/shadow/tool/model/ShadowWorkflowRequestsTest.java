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

package org.apache.shardingsphere.mcp.feature.shadow.tool.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ShadowWorkflowRequestsTest {
    
    @Test
    void assertMergeRuleRequest() {
        ShadowRuleWorkflowRequest previous = new ShadowRuleWorkflowRequest();
        previous.setDatabase("logic_db");
        previous.setRuleName("shadow_rule");
        ShadowRuleWorkflowRequest current = new ShadowRuleWorkflowRequest();
        current.setTableName("t_order");
        current.putAlgorithmProperties(Map.of("operation", "insert"));
        ShadowRuleWorkflowRequest actual = ShadowRuleWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getRuleName(), is("shadow_rule"));
        assertThat(actual.getTableName(), is("t_order"));
        assertThat(actual.getTable(), is("t_order"));
        assertThat(actual.getAlgorithmProperties().get("operation"), is("insert"));
    }
    
    @Test
    void assertMergeDefaultAlgorithmRequest() {
        ShadowDefaultAlgorithmWorkflowRequest previous = new ShadowDefaultAlgorithmWorkflowRequest();
        previous.setAlgorithmType("SQL_HINT");
        ShadowDefaultAlgorithmWorkflowRequest current = new ShadowDefaultAlgorithmWorkflowRequest();
        current.putAlgorithmProperties(Map.of("k", "v"));
        ShadowDefaultAlgorithmWorkflowRequest actual = ShadowDefaultAlgorithmWorkflowRequest.merge(previous, current);
        assertThat(actual.getAlgorithmType(), is("SQL_HINT"));
        assertThat(actual.getAlgorithmProperties().get("k"), is("v"));
    }
    
    @Test
    void assertMergeCleanupRequest() {
        ShadowAlgorithmCleanupWorkflowRequest previous = new ShadowAlgorithmCleanupWorkflowRequest();
        previous.setDatabase("logic_db");
        ShadowAlgorithmCleanupWorkflowRequest current = new ShadowAlgorithmCleanupWorkflowRequest();
        current.setAlgorithmName("shadow_algorithm");
        ShadowAlgorithmCleanupWorkflowRequest actual = ShadowAlgorithmCleanupWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("logic_db"));
        assertThat(actual.getAlgorithmName(), is("shadow_algorithm"));
    }
}
