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

    private static final Set<String> BANNED_PUBLIC_FIELDS = Set.of(
            "target_tool", "target_resource", "required_arguments", "action_kind", "suggested_next_tool", "suggested_next_tools", "recommended_next_tool",
            "recommended_recovery", "suggested_next_action", "approved_by_user", "requires_user_approval", "approval_required");

    private static final Map<String, Set<String>> NEXT_ACTION_REQUIRED_FIELDS = Map.of(
            "resource_read", Set.of("order", "type", "title", "resource_uri"),
            "tool_call", Set.of("order", "type", "title", "tool_name", "arguments"),
            "completion", Set.of("order", "type", "title", "reference_type", "reference", "argument_name", "context_arguments"),
            "ask_user", Set.of("order", "type", "title", "question"),
            "terminal", Set.of("order", "type", "title"));

    private MCPModelContractAssertions() {
    }

    /**
     * Assert that no legacy public fields are present recursively.
     *
     * @param value model-facing payload value
     */
    public static void assertNoBannedPublicFields(final Object value) {
        if (value instanceof Map) {
            assertNoBannedPublicFieldMap((Map<?, ?>) value);
        } else if (value instanceof Collection) {
            for (Object each : (Collection<?>) value) {
                assertNoBannedPublicFields(each);
            }
        }
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

    private static void assertNoBannedPublicFieldMap(final Map<?, ?> value) {
        for (Object each : value.keySet()) {
            assertFalse(BANNED_PUBLIC_FIELDS.contains(String.valueOf(each)), () -> "Legacy model-facing field returned: " + each);
        }
        for (Object each : value.values()) {
            assertNoBannedPublicFields(each);
        }
    }

    private static void assertCanonicalNextActionListMap(final Map<?, ?> value) {
        if (value.containsKey("next_actions") && !isNextActionsSchema(value.get("next_actions"))) {
            assertNextActions(value.get("next_actions"));
        }
        for (Object each : value.values()) {
            assertCanonicalNextActionLists(each);
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
    }
}
