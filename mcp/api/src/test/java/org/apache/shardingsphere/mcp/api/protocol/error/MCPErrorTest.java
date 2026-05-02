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

package org.apache.shardingsphere.mcp.api.protocol.error;

import org.apache.shardingsphere.mcp.api.protocol.error.MCPError.MCPErrorCode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPErrorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetDefaultMessageCases")
    void assertGetDefaultMessage(final String name, final MCPErrorCode errorCode, final String expectedDefaultMessage) {
        assertThat(errorCode.getDefaultMessage(), is(expectedDefaultMessage));
    }
    
    private static Stream<Arguments> assertGetDefaultMessageCases() {
        return Stream.of(
                Arguments.of("invalid request", MCPErrorCode.INVALID_REQUEST, "Invalid request."),
                Arguments.of("not found", MCPErrorCode.NOT_FOUND, "MCP operation not found."),
                Arguments.of("unsupported", MCPErrorCode.UNSUPPORTED, "Unsupported MCP operation."),
                Arguments.of("conflict", MCPErrorCode.CONFLICT, "MCP operation conflict."),
                Arguments.of("timeout", MCPErrorCode.TIMEOUT, "MCP operation timeout."),
                Arguments.of("transaction state error", MCPErrorCode.TRANSACTION_STATE_ERROR, "MCP transaction operation failed."),
                Arguments.of("query failed", MCPErrorCode.QUERY_FAILED, "MCP query failed."),
                Arguments.of("unavailable", MCPErrorCode.UNAVAILABLE, "Service is temporarily unavailable."));
    }
}
