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

package org.apache.shardingsphere.mcp.support.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPNextActionUtilsTest {
    
    @Test
    void assertReadResource() {
        Map<String, Object> actual = MCPNextActionUtils.readResource("shardingsphere://capabilities", "Read capabilities.");
        assertThat(actual.get("order"), is(1));
        assertThat(actual.get("type"), is("resource_read"));
        assertThat(actual.get("title"), is("Read resource"));
        assertThat(actual.get("resource_uri"), is("shardingsphere://capabilities"));
        assertThat(actual.get("reason"), is("Read capabilities."));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertCallTool() {
        Map<String, Object> actual = MCPNextActionUtils.callTool("database_gateway_search_metadata", "Search metadata.", Map.of("query", "orders"));
        assertThat(actual.get("order"), is(1));
        assertThat(actual.get("type"), is("tool_call"));
        assertThat(actual.get("title"), is("Call database_gateway_search_metadata"));
        assertThat(actual.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actual.get("arguments"), is(Map.of("query", "orders")));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertRetryTool() {
        Map<String, Object> actual = MCPNextActionUtils.retryTool("database_gateway_execute_update", "Retry in preview mode.", Map.of("execution_mode", "preview"));
        assertThat(actual.get("type"), is("tool_call"));
        assertThat(actual.get("title"), is("Retry database_gateway_execute_update"));
        assertThat(actual.get("tool_name"), is("database_gateway_execute_update"));
        assertThat(actual.get("arguments"), is(Map.of("execution_mode", "preview")));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertRetryToolWithoutToolName() {
        assertThrows(IllegalArgumentException.class, () -> MCPNextActionUtils.retryTool("", "Retry after choosing a tool.", Map.of("execution_mode", "preview")));
    }
    
    @Test
    void assertCompleteArgument() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument("ref/prompt", "inspect_metadata", "schema", "pub", Map.of("database", "logic_db"),
                List.of("table"), "tool", "database_gateway_search_metadata", Map.of("query", "orders"), "Complete schema.");
        assertThat(actual.get("type"), is("completion"));
        assertThat(actual.get("title"), is("Complete schema"));
        assertThat(actual.get("ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(actual.get("argument"), is(Map.of("name", "schema", "value", "pub")));
        assertThat(actual.get("context"), is(Map.of("arguments", Map.of("database", "logic_db"))));
        assertThat(actual.get("missing_context_arguments"), is(List.of("table")));
        assertThat(actual.get("resume_ref"), is(Map.of("type", "tool", "name", "database_gateway_search_metadata")));
        assertThat(actual.get("resume_arguments"), is(Map.of("query", "orders")));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertCompleteArgumentNormalizesReferenceTypes() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument("prompt", "inspect_metadata", "schema", "pub", Map.of("database", "logic_db"),
                List.of("table"), "resource", "shardingsphere://databases/{database}/schemas/{schema}", Map.of("database", "logic_db"), "Complete schema.");
        assertThat(actual.get("ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(actual.get("resume_ref"), is(Map.of("type", "ref/resource", "uri", "shardingsphere://databases/{database}/schemas/{schema}")));
    }
    
    @Test
    void assertCompleteArgumentWithoutResumeTarget() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument("ref/resource", "shardingsphere://databases/{database}", "database", "", Map.of(), List.of(), "", "", Map.of(),
                "Complete database.");
        assertThat(actual.get("type"), is("completion"));
        assertFalse(actual.containsKey("context"));
        assertFalse(actual.containsKey("resume_ref"));
        assertFalse(actual.containsKey("resume_arguments"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertAskUser() {
        Map<String, Object> actual = MCPNextActionUtils.askUser("Choose execution mode.", List.of("execution_mode"));
        assertThat(actual.get("type"), is("ask_user"));
        assertThat(actual.get("title"), is("Ask user"));
        assertThat(actual.get("question"), is("Choose execution mode."));
        assertThat(actual.get("required_inputs"), is(List.of("execution_mode")));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertStop() {
        Map<String, Object> actual = MCPNextActionUtils.stop("Done.");
        assertThat(actual.get("type"), is("terminal"));
        assertThat(actual.get("title"), is("Stop"));
        assertThat(actual.get("reason"), is("Done."));
        assertFalse(actual.containsKey("requires_user_approval"));
        assertNoRemovedAliases(actual);
    }
    
    @Test
    void assertOrderedCopiesActions() {
        Map<String, Object> action = Map.of("type", "terminal");
        List<Map<String, Object>> actual = MCPNextActionUtils.ordered(action, Map.of("type", "tool_call"));
        assertThat(actual.get(0).get("order"), is(1));
        assertThat(actual.get(1).get("order"), is(2));
        assertFalse(action.containsKey("order"));
    }
    
    @Test
    void assertDependsOnCopiesAction() {
        Map<String, Object> action = Map.of("type", "tool_call");
        Map<String, Object> actual = MCPNextActionUtils.dependsOn(action, 1);
        assertThat(actual.get("depends_on"), is(List.of(1)));
        assertFalse(action.containsKey("depends_on"));
    }
    
    private void assertNoRemovedAliases(final Map<String, Object> action) {
        assertFalse(action.containsKey("target_tool"));
        assertFalse(action.containsKey("target_resource"));
        assertFalse(action.containsKey("required_arguments"));
        assertFalse(action.containsKey("action_kind"));
        assertFalse(action.containsKey("reference_type"));
        assertFalse(action.containsKey("reference"));
        assertFalse(action.containsKey("argument_name"));
        assertFalse(action.containsKey("argument_value"));
        assertFalse(action.containsKey("argument_prefix"));
        assertFalse(action.containsKey("context_arguments"));
        assertFalse(action.containsKey("resume_target_type"));
        assertFalse(action.containsKey("resume_target"));
    }
}
