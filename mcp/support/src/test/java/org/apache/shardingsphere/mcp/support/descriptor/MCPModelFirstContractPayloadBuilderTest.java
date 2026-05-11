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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPModelFirstContractPayloadBuilderTest {
    
    private final MCPModelFirstContractPayloadBuilder builder = new MCPModelFirstContractPayloadBuilder(MCPDescriptorCatalogLoader.load());
    
    @Test
    void assertCreateModelFirstSummary() {
        Map<String, Object> actual = builder.createModelFirstSummary();
        assertThat(actual.get("safe_first_resource"), is("shardingsphere://capabilities"));
        assertThat(castToMap(castToMap(actual.get("sql_tool_selection")).get("side_effecting")).get("execute_requires"), is("approved_by_user=true"));
        Map<?, ?> actualWorkflowRule = castToMap(actual.get("workflow_rule"));
        assertTrue(actualWorkflowRule.containsKey("planning_tools"));
        assertThat(castToMap(actualWorkflowRule.get("preview_tool")).get("execution_mode"), is("preview"));
        assertThat(actualWorkflowRule.get("validate_tool"), is("validate_workflow"));
    }
    
    @Test
    void assertCreateModelContract() {
        Map<String, Object> actual = builder.createModelContract();
        assertThat(actual.get("public_surface_source"), is("shardingsphere://capabilities"));
        assertThat(actual.get("metadata_first_resource"), is("shardingsphere://databases"));
        assertThat(actual.get("side_effect_rule"), is("Preview before side effects and continue only after explicit user approval with approved_by_user=true."));
    }
    
    @Test
    void assertCreateNextActionContract() {
        List<Map<String, Object>> actual = builder.createNextActionContract();
        assertThat(actual.size(), is(5));
        Map<?, ?> actualToolCall = findByType(actual, "tool_call");
        assertTrue(((Collection<?>) actualToolCall.get("required_fields")).contains("tool_name"));
        assertTrue(((Collection<?>) actualToolCall.get("required_fields")).contains("requires_user_approval"));
        assertTrue(((Collection<?>) findByType(actual, "completion").get("optional_fields")).contains("resume_arguments"));
    }
    
    @Test
    void assertCreateCommonFlows() {
        List<Map<String, Object>> actual = builder.createCommonFlows();
        Map<?, ?> actualSideEffectingSql = findByKey(actual, "flow_id", "side_effecting_sql");
        assertThat(actualSideEffectingSql.get("referenced_tools"), is(List.of("execute_update")));
        assertTrue(((Collection<?>) actualSideEffectingSql.get("steps")).contains("call_tool execute_update execution_mode=preview"));
    }
    
    @Test
    void assertCreateSecurityHints() {
        Map<String, Object> actual = builder.createSecurityHints();
        assertThat(actual.get("remote_access"), is("Prefer loopback access unless the operator explicitly configures remote exposure."));
        assertThat(actual.get("http_access_token"), is("HTTP transport may require an Authorization bearer token; capabilities never exposes secrets."));
    }
    
    private Map<?, ?> findByType(final List<Map<String, Object>> values, final String type) {
        return findByKey(values, "type", type);
    }
    
    private Map<?, ?> findByKey(final List<Map<String, Object>> values, final String key, final String value) {
        return values.stream().filter(each -> value.equals(each.get(key))).findFirst().orElseThrow();
    }
    
    private Map<?, ?> castToMap(final Object value) {
        return (Map<?, ?>) value;
    }
}
