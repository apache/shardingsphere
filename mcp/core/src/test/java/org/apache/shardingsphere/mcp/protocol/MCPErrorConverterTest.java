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

package org.apache.shardingsphere.mcp.protocol;

import org.apache.shardingsphere.mcp.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPQueryFailedException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTimeoutException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPTransactionStateException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnavailableException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.protocol.response.MCPErrorResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPErrorConverterTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertConvertCases")
    void assertConvert(final String name, final Throwable cause, final String expectedErrorCode, final String expectedMessage) {
        MCPErrorResponse actual = MCPErrorConverter.convert(cause);
        assertThat(actual.toPayload(), is(Map.of("error_code", expectedErrorCode, "message", expectedMessage)));
    }
    
    static Stream<Arguments> assertConvertCases() {
        return Stream.of(
                Arguments.of("invalid request exception", new MCPInvalidRequestException("Invalid request."), "invalid_request", "Invalid request."),
                Arguments.of("not found exception", new DatabaseCapabilityNotFoundException(), "not_found", "Database capability does not exist."),
                Arguments.of("unsupported exception", new MCPUnsupportedException("Unsupported."), "unsupported", "Unsupported."),
                Arguments.of("timeout exception", new MCPTimeoutException("Timed out.", new SQLTimeoutException("Timed out.")), "timeout", "Timed out."),
                Arguments.of("transaction state exception", new MCPTransactionStateException("Transaction already active.", new IllegalStateException()), "transaction_state_error",
                        "Transaction already active."),
                Arguments.of("query failed exception", new MCPQueryFailedException("Query failed."), "query_failed", "Query failed."),
                Arguments.of("unavailable exception", new MCPUnavailableException("Unavailable."), "unavailable", "Unavailable."),
                Arguments.of("sql syntax exception", new SQLSyntaxErrorException("Bad SQL."), "invalid_request", "Bad SQL."),
                Arguments.of("sql timeout exception", new SQLTimeoutException("Timed out."), "timeout", "Timed out."),
                Arguments.of("sql unsupported feature exception", new SQLFeatureNotSupportedException("Unsupported feature."), "unsupported", "Unsupported feature."),
                Arguments.of("unsupported operation exception", new UnsupportedOperationException("Unsupported operation."), "unsupported", "Unsupported operation."),
                Arguments.of("sql exception", new SQLException("Query failed."), "query_failed", "Query failed."),
                Arguments.of("illegal argument exception", new IllegalArgumentException("Illegal argument."), "invalid_request", "Illegal argument."),
                Arguments.of("illegal state exception", new IllegalStateException(" Transaction already active. "), "transaction_state_error", "Transaction already active."),
                Arguments.of("unknown exception", new RuntimeException(), "unavailable", "Service is temporarily unavailable."));
    }
}
