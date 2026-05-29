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

package org.apache.shardingsphere.mcp.support.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPRuntimeProtectionPolicyTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetMaxToolCallsPerSessionCases")
    void assertGetMaxToolCallsPerSession(final String name, final String configuredValue, final int expectedMaxToolCallsPerSession) {
        String previous = System.getProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        try {
            if (null == configuredValue) {
                System.clearProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
            } else {
                System.setProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, configuredValue);
            }
            assertThat(MCPRuntimeProtectionPolicy.getMaxToolCallsPerSession(), is(expectedMaxToolCallsPerSession));
        } finally {
            restoreProperty(previous);
        }
    }
    
    @Test
    void assertCreateToolCallLimitPayload() {
        String previous = System.getProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        try {
            System.setProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, "9");
            Map<String, Object> actual = MCPRuntimeProtectionPolicy.createToolCallLimitPayload();
            assertThat(actual.get("scope"), is("session"));
            assertThat(actual.get("property"), is(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY));
            assertThat(actual.get("max_calls"), is(9));
            assertThat(actual.get("recovery"), is("Close and recreate the MCP session after the quota is exhausted."));
        } finally {
            restoreProperty(previous);
        }
    }
    
    @Test
    void assertCreateRuntimeProtectionPayload() {
        Map<String, Object> actual = MCPRuntimeProtectionPolicy.createRuntimeProtectionPayload();
        Map<?, ?> actualToolCallLimit = (Map<?, ?>) actual.get("tool_call_limit");
        assertThat(actualToolCallLimit.get("scope"), is("session"));
        Map<?, ?> actualSQLExecutionLimits = (Map<?, ?>) actual.get("sql_execution_limits");
        Map<?, ?> actualMaxRows = (Map<?, ?>) actualSQLExecutionLimits.get("max_rows");
        assertThat(actualMaxRows.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_MAX_ROWS));
        assertThat(actualMaxRows.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_ROWS_LIMIT));
        assertThat(actualMaxRows.get("applied_field"), is("applied_max_rows"));
        assertThat(actualMaxRows.get("truncation_field"), is("truncated"));
        Map<?, ?> actualTimeout = (Map<?, ?>) actualSQLExecutionLimits.get("timeout_ms");
        assertThat(actualTimeout.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("applied_field"), is("applied_timeout_ms"));
        assertThat(actualTimeout.get("zero_means"), is("server_default"));
    }
    
    private static Stream<Arguments> assertGetMaxToolCallsPerSessionCases() {
        return Stream.of(
                Arguments.of("configured value", "8", 8),
                Arguments.of("non-positive value falls back to default", "0", MCPRuntimeProtectionPolicy.DEFAULT_MAX_TOOL_CALLS_PER_SESSION),
                Arguments.of("missing value falls back to default", null, MCPRuntimeProtectionPolicy.DEFAULT_MAX_TOOL_CALLS_PER_SESSION));
    }
    
    private void restoreProperty(final String previous) {
        if (null == previous) {
            System.clearProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        } else {
            System.setProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, previous);
        }
    }
}
