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

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShadowRuleWorkflowRequestTest {
    
    @Test
    void assertMerge() {
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
    void assertMergeFromBaseRequest() {
        WorkflowRequest previous = new WorkflowRequest();
        previous.setDatabase("foo_db");
        ShadowRuleWorkflowRequest current = new ShadowRuleWorkflowRequest();
        current.setTableName("foo_table");
        ShadowRuleWorkflowRequest actual = ShadowRuleWorkflowRequest.merge(previous, current);
        assertThat(actual.getDatabase(), is("foo_db"));
        assertThat(actual.getTable(), is("foo_table"));
    }
    
    @Test
    void assertGetAlgorithmProperties() {
        ShadowRuleWorkflowRequest request = new ShadowRuleWorkflowRequest();
        request.putAlgorithmProperties(Map.of("foo_key", "foo_value"));
        assertTrue(request.getAlgorithmProperties("assisted").isEmpty());
    }
}
