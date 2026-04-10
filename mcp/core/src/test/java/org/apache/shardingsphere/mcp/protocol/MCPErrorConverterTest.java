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

import org.apache.shardingsphere.mcp.protocol.error.MCPError;
import org.apache.shardingsphere.mcp.protocol.error.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.error.MCPErrorConverter;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLTimeoutException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPErrorConverterTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertConvertCases")
    void assertConvert(final String name, final Throwable cause, final MCPErrorCode expectedErrorCode, final String expectedMessage) {
        MCPError actual = MCPErrorConverter.convert(cause);
        assertThat(actual.getCode(), is(expectedErrorCode));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    static Stream<Arguments> assertConvertCases() {
        return Stream.of(
                Arguments.of("protocol exception", new DatabaseCapabilityNotFoundException(), MCPErrorCode.NOT_FOUND, "Database capability does not exist."),
                Arguments.of("sql syntax exception", new SQLSyntaxErrorException("Bad SQL."), MCPErrorCode.INVALID_REQUEST, "Bad SQL."),
                Arguments.of("sql timeout exception", new SQLTimeoutException("Timed out."), MCPErrorCode.TIMEOUT, "Timed out."),
                Arguments.of("sql unsupported feature exception", new SQLFeatureNotSupportedException("Unsupported feature."), MCPErrorCode.UNSUPPORTED, "Unsupported feature."),
                Arguments.of("unsupported operation exception", new UnsupportedOperationException("Unsupported operation."), MCPErrorCode.UNSUPPORTED, "Unsupported operation."),
                Arguments.of("sql exception", new SQLException("Query failed."), MCPErrorCode.QUERY_FAILED, "Query failed."),
                Arguments.of("illegal argument exception", new IllegalArgumentException("Illegal argument."), MCPErrorCode.INVALID_REQUEST, "Illegal argument."),
                Arguments.of("illegal state exception", new IllegalStateException(" Transaction already active. "), MCPErrorCode.TRANSACTION_STATE_ERROR, "Transaction already active."),
                Arguments.of("unknown exception", new RuntimeException(), MCPErrorCode.UNAVAILABLE, "Service is temporarily unavailable."));
    }
}
