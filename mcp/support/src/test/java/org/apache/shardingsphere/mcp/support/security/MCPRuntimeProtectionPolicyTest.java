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
import java.util.function.IntSupplier;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            restoreProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, previous);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetMaxToolCallsPerSessionWithInvalidValueCases")
    void assertGetMaxToolCallsPerSessionWithInvalidValue(final String name, final String configuredValue) {
        assertInvalidProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, configuredValue, MCPRuntimeProtectionPolicy::getMaxToolCallsPerSession);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetMaxCompletionRequestsPerMinuteCases")
    void assertGetMaxCompletionRequestsPerMinute(final String name, final String configuredValue, final int expectedMaxRequests) {
        String previous = System.getProperty(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY);
        try {
            if (null == configuredValue) {
                System.clearProperty(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY);
            } else {
                System.setProperty(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY, configuredValue);
            }
            assertThat(MCPRuntimeProtectionPolicy.getMaxCompletionRequestsPerMinute(), is(expectedMaxRequests));
        } finally {
            restoreProperty(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY, previous);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetMaxCompletionRequestsPerMinuteWithInvalidValueCases")
    void assertGetMaxCompletionRequestsPerMinuteWithInvalidValue(final String name, final String configuredValue) {
        assertInvalidProperty(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY, configuredValue,
                MCPRuntimeProtectionPolicy::getMaxCompletionRequestsPerMinute);
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
            restoreProperty(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, previous);
        }
    }
    
    @Test
    void assertCreateRuntimeProtectionPayload() {
        Map<String, Object> actual = MCPRuntimeProtectionPolicy.createRuntimeProtectionPayload();
        Map<?, ?> actualToolCallLimit = (Map<?, ?>) actual.get("tool_call_limit");
        assertThat(actualToolCallLimit.get("scope"), is("session"));
        Map<?, ?> actualCompletionRateLimit = (Map<?, ?>) actual.get("completion_rate_limit");
        assertThat(actualCompletionRateLimit.get("scope"), is("session"));
        assertThat(actualCompletionRateLimit.get("window_seconds"), is(60));
        assertThat(actualCompletionRateLimit.get("max_requests"), is(MCPRuntimeProtectionPolicy.getMaxCompletionRequestsPerMinute()));
        assertThat(actualCompletionRateLimit.get("property"), is(MCPRuntimeProtectionPolicy.MAX_COMPLETION_REQUESTS_PER_MINUTE_PROPERTY));
        Map<?, ?> actualSQLExecutionLimits = (Map<?, ?>) actual.get("sql_execution_limits");
        Map<?, ?> actualMaxRows = (Map<?, ?>) actualSQLExecutionLimits.get("max_rows");
        assertThat(actualMaxRows.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_MAX_ROWS));
        assertThat(actualMaxRows.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_ROWS_LIMIT));
        assertThat(actualMaxRows.get("applied_field"), is("applied_max_rows"));
        assertThat(actualMaxRows.get("truncation_field"), is("truncated"));
        assertThat(actualMaxRows.get("recovery"), is(
                "For read-only queries, retry with a narrower SELECT, stronger WHERE clause, or smaller projection. "
                        + "If rows came from a side-effecting statement, do not replay that statement automatically; use a separate read-only query when more data is needed."));
        Map<?, ?> actualTimeout = (Map<?, ?>) actualSQLExecutionLimits.get("timeout_ms");
        assertThat(actualTimeout.get("default_value"), is(MCPRuntimeProtectionPolicy.DEFAULT_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("maximum_value"), is(MCPRuntimeProtectionPolicy.MAX_TIMEOUT_MILLISECONDS));
        assertThat(actualTimeout.get("applied_field"), is("applied_timeout_ms"));
        assertThat(actualTimeout.get("zero_means"), is("server_default"));
    }
    
    private static Stream<Arguments> assertGetMaxToolCallsPerSessionCases() {
        return Stream.of(
                Arguments.of("configured value", "8", 8),
                Arguments.of("missing value falls back to default", null, MCPRuntimeProtectionPolicy.DEFAULT_MAX_TOOL_CALLS_PER_SESSION));
    }
    
    private static Stream<Arguments> assertGetMaxToolCallsPerSessionWithInvalidValueCases() {
        return Stream.of(Arguments.of("malformed value", "foo"), Arguments.of("non-positive value", "0"));
    }
    
    private static Stream<Arguments> assertGetMaxCompletionRequestsPerMinuteCases() {
        return Stream.of(
                Arguments.of("configured value", "120", 120),
                Arguments.of("missing value falls back to default", null, MCPRuntimeProtectionPolicy.DEFAULT_MAX_COMPLETION_REQUESTS_PER_MINUTE));
    }
    
    private static Stream<Arguments> assertGetMaxCompletionRequestsPerMinuteWithInvalidValueCases() {
        return Stream.of(Arguments.of("malformed value", "foo"), Arguments.of("non-positive value", "0"));
    }
    
    private void assertInvalidProperty(final String propertyName, final String configuredValue, final IntSupplier action) {
        String previous = System.getProperty(propertyName);
        try {
            System.setProperty(propertyName, configuredValue);
            IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, action::getAsInt);
            assertThat(actual.getMessage(), is(String.format("System property `%s` must be a positive integer, but was `%s`.", propertyName, configuredValue)));
        } finally {
            restoreProperty(propertyName, previous);
        }
    }
    
    private void restoreProperty(final String propertyName, final String previous) {
        if (null == previous) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, previous);
        }
    }
}
