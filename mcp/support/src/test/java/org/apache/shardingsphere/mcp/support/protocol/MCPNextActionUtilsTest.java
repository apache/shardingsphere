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
    }
    
    @Test
    void assertCallTool() {
        Map<String, Object> actual = MCPNextActionUtils.callTool("database_gateway_search_metadata", "Search metadata.", Map.of("query", "orders"));
        assertThat(actual.get("order"), is(1));
        assertThat(actual.get("type"), is("tool_call"));
        assertThat(actual.get("title"), is("Call database_gateway_search_metadata"));
        assertThat(actual.get("tool_name"), is("database_gateway_search_metadata"));
        assertThat(actual.get("arguments"), is(Map.of("query", "orders")));
    }
    
    @Test
    void assertRetryTool() {
        Map<String, Object> actual = MCPNextActionUtils.retryTool("database_gateway_execute_update", "Retry in preview mode.", Map.of("execution_mode", "preview"));
        assertThat(actual.get("type"), is("tool_call"));
        assertThat(actual.get("title"), is("Retry database_gateway_execute_update"));
        assertThat(actual.get("tool_name"), is("database_gateway_execute_update"));
        assertThat(actual.get("arguments"), is(Map.of("execution_mode", "preview")));
    }
    
    @Test
    void assertRetryToolWithoutToolName() {
        assertThrows(IllegalArgumentException.class, () -> MCPNextActionUtils.retryTool("", "Retry after choosing a tool.", Map.of("execution_mode", "preview")));
    }
    
    @Test
    void assertCompleteArgument() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument(MCPCompletionAction.builder()
                .referenceType("ref/prompt").reference("inspect_metadata").argumentName("schema").argumentPrefix("pub")
                .contextArguments(Map.of("database", "logic_db")).missingContextArguments(List.of("table"))
                .resumeTargetType("tool").resumeTarget("database_gateway_search_metadata").resumeArguments(Map.of("query", "orders"))
                .reason("Complete schema.").build());
        assertThat(actual.get("type"), is("completion"));
        assertThat(actual.get("title"), is("Complete schema"));
        assertThat(actual.get("ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(actual.get("argument"), is(Map.of("name", "schema", "value", "pub")));
        assertThat(actual.get("context"), is(Map.of("arguments", Map.of("database", "logic_db"))));
        assertThat(actual.get("missing_context_arguments"), is(List.of("table")));
        assertThat(actual.get("resume_ref"), is(Map.of("type", "tool", "name", "database_gateway_search_metadata")));
        assertThat(actual.get("resume_arguments"), is(Map.of("query", "orders")));
    }
    
    @Test
    void assertCompleteArgumentNormalizesReferenceTypes() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument(MCPCompletionAction.builder()
                .referenceType("prompt").reference("inspect_metadata").argumentName("schema").argumentPrefix("pub")
                .contextArguments(Map.of("database", "logic_db")).missingContextArguments(List.of("table"))
                .resumeTargetType("resource").resumeTarget("shardingsphere://databases/{database}/schemas/{schema}").resumeArguments(Map.of("database", "logic_db"))
                .reason("Complete schema.").build());
        assertThat(actual.get("ref"), is(Map.of("type", "ref/prompt", "name", "inspect_metadata")));
        assertThat(actual.get("resume_ref"), is(Map.of("type", "ref/resource", "uri", "shardingsphere://databases/{database}/schemas/{schema}")));
    }
    
    @Test
    void assertCompleteArgumentWithoutResumeTarget() {
        Map<String, Object> actual = MCPNextActionUtils.completeArgument(MCPCompletionAction.builder()
                .referenceType("ref/resource").reference("shardingsphere://databases/{database}").argumentName("database").reason("Complete database.").build());
        assertThat(actual.get("type"), is("completion"));
        assertThat(actual.get("missing_context_arguments"), is(List.of()));
        assertFalse(actual.containsKey("context"));
        assertFalse(actual.containsKey("resume_ref"));
        assertFalse(actual.containsKey("resume_arguments"));
    }
    
    @Test
    void assertAskUser() {
        Map<String, Object> actual = MCPNextActionUtils.askUser("Choose execution mode.", List.of("execution_mode"));
        assertThat(actual.get("type"), is("ask_user"));
        assertThat(actual.get("title"), is("Ask user"));
        assertThat(actual.get("question"), is("Choose execution mode."));
        assertThat(actual.get("required_inputs"), is(List.of("execution_mode")));
        assertFalse(actual.containsKey("reason"));
    }
    
    @Test
    void assertStop() {
        Map<String, Object> actual = MCPNextActionUtils.stop("Done.");
        assertThat(actual.get("type"), is("terminal"));
        assertThat(actual.get("title"), is("Stop"));
        assertThat(actual.get("reason"), is("Done."));
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
    void assertOrderedCollectionCopiesActions() {
        Map<String, Object> action = Map.of("type", "terminal");
        List<Map<String, Object>> actual = MCPNextActionUtils.ordered(List.of(action, Map.of("type", "tool_call")));
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
}
