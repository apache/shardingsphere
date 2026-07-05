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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPModelFacingToolResponseFormatterTest {
    
    @Test
    void assertFormat() {
        Map<String, Object> actual = format(Map.of("resources", List.of(Map.of(
                "uri", "shardingsphere://databases",
                "name", "logical-databases",
                "title", "Logical Databases",
                "description", "Long model-facing description.",
                "mimeType", "application/json",
                "_meta", Map.of("org.apache.shardingsphere/resource-kind", "list")))));
        Map<String, Object> expected = Map.of("resources", List.of(Map.of(
                "uri", "shardingsphere://databases",
                "name", "logical-databases",
                "title", "Logical Databases",
                "mimeType", "application/json")));
        assertThat(actual, is(expected));
    }
    
    @Test
    void assertFormatWithCompactItems() {
        Map<String, Object> actual = format(Map.of("items", List.of(
                Map.of("database", "logic_db", "schema", "public", "ignored", "value"),
                Map.of("table", "orders"),
                Map.of("view", "order_view"),
                Map.of("column", "status"),
                Map.of("name", "order_id"),
                Map.of("resource", "extra"))));
        assertThat(actual, is(Map.of("items", List.of(
                Map.of("database", "logic_db", "schema", "public"),
                Map.of("table", "orders"),
                Map.of("view", "order_view"),
                Map.of("column", "status"),
                Map.of("name", "order_id")))));
    }
    
    @Test
    void assertFormatWithPrompts() {
        Map<String, Object> actual = format(Map.of("prompts", List.of(Map.of("name", "inspect_metadata"), Map.of("description", "ignored"))));
        assertThat(actual, is(Map.of("prompts", List.of("inspect_metadata"))));
    }
    
    @Test
    void assertFormatWithPromptMessages() {
        Map<String, Object> actual = format(Map.of("description", "Inspect metadata.", "messages", List.of(Map.of("role", "user"), Map.of("role", "assistant"))));
        assertThat(actual, is(Map.of("description", "Inspect metadata.", "message_count", 2)));
    }
    
    @Test
    void assertFormatWithArtifactSummaries() {
        Map<String, Object> actual = format(Map.of(
                "manual_artifacts", List.of(Map.of(
                        "ddl_artifacts", List.of("a", "b"),
                        "index_plan", List.of("c"),
                        "distsql_artifacts", List.of("d", "e", "f"))),
                "exported_artifacts", List.of(Map.of("ddl_artifacts", List.of("g")))));
        assertThat(actual, is(Map.of(
                "manual_artifacts", List.of(Map.of("ddl_artifact_count", 2, "index_plan_count", 1, "distsql_artifact_count", 3)),
                "exported_artifacts", List.of(Map.of("ddl_artifact_count", 1)))));
    }
    
    @Test
    void assertFormatWithModelCriticalFields() {
        Map<String, Object> actual = format(Map.of(
                "resources_to_read", List.of(Map.of("uri", "shardingsphere://capabilities")),
                "resource", Map.of("uri", "shardingsphere://databases"),
                "parent_resource", Map.of("uri", "shardingsphere://databases"),
                "next_resources", List.of(Map.of("uri", "shardingsphere://databases/logic_db/schemas")),
                "manual_artifact_summary", "Review DistSQL.",
                "manual_follow_up", "Validate runtime state.",
                "empty_state", Map.of("state", "no_match"),
                "recovery_guidance", "Read metadata before retrying.",
                "remediation", "Fix the mismatch.",
                "ignored", "value"));
        assertThat(actual, is(Map.of(
                "resources_to_read", List.of(Map.of("uri", "shardingsphere://capabilities")),
                "resource", Map.of("uri", "shardingsphere://databases"),
                "parent_resource", Map.of("uri", "shardingsphere://databases"),
                "next_resources", List.of(Map.of("uri", "shardingsphere://databases/logic_db/schemas")),
                "manual_artifact_summary", "Review DistSQL.",
                "manual_follow_up", "Validate runtime state.",
                "empty_state", Map.of("state", "no_match"),
                "recovery_guidance", "Read metadata before retrying.",
                "remediation", "Fix the mismatch.")));
    }
    
    @Test
    void assertFormatWithRecoveryAndNextActions() {
        Map<String, Object> actual = format(Map.of(
                "next_actions", List.of(
                        Map.of("type", "tool_call", "tool_name", "database_gateway_execute_update", "title", "Execute", "reason", "approved",
                                "arguments", Map.of("execution_mode", "execute")),
                        Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases")),
                "recovery", Map.of(
                        "recovery_category", "missing_context",
                        "model_action", "retry",
                        "suggested_arguments", Map.of("database", "logic_db"),
                        "next_actions", List.of(
                                Map.of("type", "tool_call", "tool_name", "database_gateway_execute_update", "arguments", Map.of("execution_mode", "execute")),
                                Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases")),
                        "resources_to_read", List.of(Map.of("uri", "shardingsphere://databases")),
                        "recovery_guidance", "Read metadata.",
                        "remediation", "Fix the request.",
                        "ignored", "value")));
        assertThat(actual, is(Map.of(
                "next_actions", List.of(
                        Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases")),
                "recovery", Map.of(
                        "recovery_category", "missing_context",
                        "model_action", "retry",
                        "suggested_arguments", Map.of("database", "logic_db"),
                        "resources_to_read", List.of(Map.of("uri", "shardingsphere://databases")),
                        "recovery_guidance", "Read metadata.",
                        "remediation", "Fix the request.",
                        "next_actions", List.of(Map.of("type", "resource_read", "resource_uri", "shardingsphere://databases"))))));
    }
    
    @Test
    void assertFormatWithOriginalPayloadFallback() {
        assertThat(format(Map.of("unknown", "value")), is(Map.of("unknown", "value")));
    }
    
    private Map<String, Object> format(final Map<String, Object> value) {
        return JsonUtils.fromJsonString(LLMMCPModelFacingToolResponseFormatter.format(value), new TypeReference<>() {
        });
    }
}
