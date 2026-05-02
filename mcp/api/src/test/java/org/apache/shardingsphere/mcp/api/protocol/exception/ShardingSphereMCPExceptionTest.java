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

package org.apache.shardingsphere.mcp.api.protocol.exception;

import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShardingSphereMCPExceptionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExceptionCases")
    void assertException(final String name, final ShardingSphereMCPException actual, final String expectedMessage) {
        assertException(actual, expectedMessage);
        assertNull(actual.getCause());
    }
    
    private void assertException(final ShardingSphereMCPException actual, final String expectedMessage) {
        assertThat(actual, isA(ShardingSphereExternalException.class));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExceptionWithCauseCases")
    void assertExceptionWithCause(final String name, final ShardingSphereMCPException actual, final String expectedMessage, final Exception expectedCause) {
        assertException(actual, expectedMessage);
        assertThat(actual.getCause(), is(expectedCause));
    }
    
    private static Stream<Arguments> assertExceptionCases() {
        return Stream.of(
                Arguments.of("invalid request", new MCPInvalidRequestException("foo_invalid_request"), "foo_invalid_request"),
                Arguments.of("not found", new MCPNotFoundException("foo_not_found"), "foo_not_found"),
                Arguments.of("query failed", new MCPQueryFailedException("foo_query_failed"), "foo_query_failed"),
                Arguments.of("unavailable", new MCPUnavailableException("foo_unavailable"), "foo_unavailable"),
                Arguments.of("unsupported", new MCPUnsupportedException("foo_unsupported"), "foo_unsupported"),
                Arguments.of("unsupported resource uri", new UnsupportedResourceUriException(), "Unsupported resource URI."),
                Arguments.of("unsupported tool", new UnsupportedToolException(), "Unsupported tool."));
    }
    
    private static Stream<Arguments> assertExceptionWithCauseCases() {
        Exception invalidRequestCause = new IllegalArgumentException("foo_invalid_request_cause");
        Exception queryCause = new SQLException("foo_query_cause");
        Exception timeoutCause = new SQLException("foo_timeout_cause");
        Exception transactionStateCause = new IllegalStateException("foo_transaction_state_cause");
        Exception unsupportedCause = new UnsupportedOperationException("foo_unsupported_cause");
        return Stream.of(
                Arguments.of("invalid request with cause", new MCPInvalidRequestException("foo_invalid_request", invalidRequestCause), "foo_invalid_request", invalidRequestCause),
                Arguments.of("query failed with cause", new MCPQueryFailedException("foo_query_failed", queryCause), "foo_query_failed", queryCause),
                Arguments.of("timeout", new MCPTimeoutException("foo_timeout", timeoutCause), "foo_timeout", timeoutCause),
                Arguments.of("transaction state", new MCPTransactionStateException("foo_transaction_state", transactionStateCause), "foo_transaction_state", transactionStateCause),
                Arguments.of("unsupported with cause", new MCPUnsupportedException("foo_unsupported", unsupportedCause), "foo_unsupported", unsupportedCause));
    }
}
