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

import org.apache.shardingsphere.mcp.support.protocol.MCPModelFacingPayloadContract;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;

import java.util.Collection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Assertions for model-facing MCP contracts.
 */
public final class MCPModelContractAssertions {
    
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
        if (value.containsKey(MCPPayloadFieldNames.NEXT_ACTIONS) && !isNextActionsSchema(value.get(MCPPayloadFieldNames.NEXT_ACTIONS))) {
            assertNextActions(value.get(MCPPayloadFieldNames.NEXT_ACTIONS));
        }
        for (Object each : value.values()) {
            assertCanonicalNextActionLists(each);
        }
    }
    
    private static void assertNoRemovedModelFacingFields(final Map<?, ?> value) {
        for (Object each : value.keySet()) {
            assertFalse(MCPModelFacingPayloadContract.isRemovedFieldName(String.valueOf(each)), () -> "Removed model-facing field returned: " + each);
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
        Collection<String> requiredFields = MCPModelFacingPayloadContract.getNextActionRequiredFields(type);
        assertFalse(requiredFields.isEmpty(), () -> "Unknown next_actions type: " + type);
        for (String each : requiredFields) {
            assertTrue(action.containsKey(each), () -> String.format("next_actions `%s` must contain `%s`.", type, each));
        }
        for (Object each : action.keySet()) {
            assertTrue(MCPModelFacingPayloadContract.getNextActionAllowedFields(type).contains(String.valueOf(each)),
                    () -> String.format("next_actions `%s` contains unsupported field `%s`.", type, each));
        }
    }
}
