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

package org.apache.shardingsphere.test.e2e.mcp.support.assertion;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertions for model-facing MCP contracts.
 */
public final class MCPModelContractAssertions {
    
    private static final Set<String> REMOVED_MODEL_FACING_FIELDS = Set.of(
            "target_tool", "target_resource", "required_arguments", "action_kind", "suggested_next_tool", "suggested_next_tools", "recommended_next_tool",
            "recommended_recovery", "suggested_next_action", "approved_by_user", "requires_user_approval", "approval_required", "user_overrides");
    
    private static final Map<String, Set<String>> NEXT_ACTION_REQUIRED_FIELDS = Map.of(
            "resource_read", Set.of("order", "type", "title", "resource_uri"),
            "tool_call", Set.of("order", "type", "title", "tool_name", "arguments"),
            "completion", Set.of("order", "type", "title", "ref", "argument"),
            "ask_user", Set.of("order", "type", "title", "question"),
            "terminal", Set.of("order", "type", "title"));
    
    private static final Map<String, Set<String>> NEXT_ACTION_ALLOWED_FIELDS = Map.of(
            "resource_read", Set.of("order", "type", "title", "resource_uri", "reason", "depends_on"),
            "tool_call", Set.of("order", "type", "title", "tool_name", "arguments", "reason", "depends_on"),
            "completion", Set.of("order", "type", "title", "ref", "argument", "context", "missing_context_arguments", "resume_ref", "resume_arguments", "reason", "depends_on"),
            "ask_user", Set.of("order", "type", "title", "question", "required_inputs", "reason", "depends_on"),
            "terminal", Set.of("order", "type", "title", "reason", "depends_on"));
    
    private MCPModelContractAssertions() {
    }
    
    /**
     * Assert that all concrete next_actions lists use the canonical action shape.
     *
     * @param value model-facing payload value
     */
    public static void assertCanonicalNextActionLists(final Object value) {
        if (value instanceof Map) {
            assertCanonicalNextActionListMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                assertCanonicalNextActionLists(each);
            }
        }
    }
    
    private static void assertCanonicalNextActionListMap(final Map<?, ?> value) {
        assertNoRemovedModelFacingFields(value);
        if (value.containsKey("next_actions") && !isNextActionsSchema(value.get("next_actions"))) {
            assertNextActions(value.get("next_actions"));
        }
        for (Object each : value.values()) {
            assertCanonicalNextActionLists(each);
        }
    }
    
    private static void assertNoRemovedModelFacingFields(final Map<?, ?> value) {
        for (Object each : value.keySet()) {
            assertFalse(REMOVED_MODEL_FACING_FIELDS.contains(String.valueOf(each)), () -> "Removed model-facing field returned: " + each);
        }
    }
    
    private static boolean isNextActionsSchema(final Object value) {
        return value instanceof Map && "array".equals(((Map<?, ?>) value).get("type")) && ((Map<?, ?>) value).containsKey("items");
    }
    
    private static void assertNextActions(final Object value) {
        assertTrue(value instanceof Collection, () -> "next_actions must be an array: " + value);
        for (Object each : (Collection<?>) value) {
            assertTrue(each instanceof Map, () -> "next_actions item must be an object: " + each);
            assertNextAction((Map<?, ?>) each);
        }
    }
    
    private static void assertNextAction(final Map<?, ?> action) {
        String type = String.valueOf(action.get("type"));
        assertTrue(NEXT_ACTION_REQUIRED_FIELDS.containsKey(type), () -> "Unknown next_actions type: " + type);
        for (String each : NEXT_ACTION_REQUIRED_FIELDS.get(type)) {
            assertTrue(action.containsKey(each), () -> String.format("next_actions `%s` must contain `%s`.", type, each));
        }
        for (Object each : action.keySet()) {
            assertTrue(NEXT_ACTION_ALLOWED_FIELDS.get(type).contains(String.valueOf(each)), () -> String.format("next_actions `%s` contains unsupported field `%s`.", type, each));
        }
    }
}
