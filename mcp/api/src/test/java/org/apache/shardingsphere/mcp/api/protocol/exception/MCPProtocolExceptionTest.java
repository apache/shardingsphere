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

import org.apache.shardingsphere.mcp.api.protocol.error.MCPError.MCPErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;

class MCPProtocolExceptionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertProtocolExceptionCases")
    void assertProtocolException(final String name, final MCPProtocolException actual, final MCPErrorCode expectedErrorCode, final String expectedMessage, final Throwable expectedCause) {
        assertThat(actual.getErrorCode(), is(expectedErrorCode));
        assertThat(actual.getMessage(), is(expectedMessage));
        if (null == expectedCause) {
            assertNull(actual.getCause());
        } else {
            assertThat(actual.getCause(), is(expectedCause));
        }
    }
    
    private static Stream<Arguments> assertProtocolExceptionCases() {
        Throwable queryCause = new RuntimeException("foo_query_cause");
        Throwable invalidRequestCause = new RuntimeException("foo_invalid_request_cause");
        Throwable timeoutCause = new RuntimeException("foo_timeout_cause");
        Throwable transactionStateCause = new RuntimeException("foo_transaction_state_cause");
        Throwable unsupportedCause = new RuntimeException("foo_unsupported_cause");
        return Stream.of(
                Arguments.of("invalid request", new MCPInvalidRequestException("foo_invalid_request"), MCPErrorCode.INVALID_REQUEST, "foo_invalid_request", null),
                Arguments.of("invalid request with cause", new MCPInvalidRequestException("foo_invalid_request", invalidRequestCause),
                        MCPErrorCode.INVALID_REQUEST, "foo_invalid_request", invalidRequestCause),
                Arguments.of("not found", new MCPNotFoundException("foo_not_found"), MCPErrorCode.NOT_FOUND, "foo_not_found", null),
                Arguments.of("query failed", new MCPQueryFailedException("foo_query_failed"), MCPErrorCode.QUERY_FAILED, "foo_query_failed", null),
                Arguments.of("query failed with cause", new MCPQueryFailedException("foo_query_failed", queryCause), MCPErrorCode.QUERY_FAILED, "foo_query_failed", queryCause),
                Arguments.of("timeout", new MCPTimeoutException("foo_timeout", timeoutCause), MCPErrorCode.TIMEOUT, "foo_timeout", timeoutCause),
                Arguments.of("transaction state", new MCPTransactionStateException("foo_transaction_state", transactionStateCause),
                        MCPErrorCode.TRANSACTION_STATE_ERROR, "foo_transaction_state", transactionStateCause),
                Arguments.of("unavailable", new MCPUnavailableException("foo_unavailable"), MCPErrorCode.UNAVAILABLE, "foo_unavailable", null),
                Arguments.of("unsupported", new MCPUnsupportedException("foo_unsupported"), MCPErrorCode.UNSUPPORTED, "foo_unsupported", null),
                Arguments.of("unsupported with cause", new MCPUnsupportedException("foo_unsupported", unsupportedCause),
                        MCPErrorCode.UNSUPPORTED, "foo_unsupported", unsupportedCause),
                Arguments.of("unsupported resource uri", new UnsupportedResourceUriException(), MCPErrorCode.INVALID_REQUEST, "Unsupported resource URI.", null),
                Arguments.of("unsupported tool", new UnsupportedToolException(), MCPErrorCode.INVALID_REQUEST, "Unsupported tool.", null));
    }
}
