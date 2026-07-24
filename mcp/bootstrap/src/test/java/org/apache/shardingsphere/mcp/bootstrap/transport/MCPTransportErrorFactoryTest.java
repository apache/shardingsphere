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

package org.apache.shardingsphere.mcp.bootstrap.transport;

import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.validator.MCPTransportSecurityException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MCPTransportErrorFactoryTest {
    
    @Test
    void assertCreateErrorWithInvalidRequest() {
        McpError actual = MCPTransportErrorFactory.createError(new MCPInvalidRequestException("foo_message"));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("foo_message"));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
        assertThat(actualData.get("summary"), is("foo_message"));
    }
    
    @Test
    void assertCreateResourceErrorWithUnsupportedResourceUri() {
        McpError actual = MCPTransportErrorFactory.createResourceError(new UnsupportedResourceUriException("shardingsphere://foo"));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.RESOURCE_NOT_FOUND));
        assertThat(actual.getJsonRpcError().message(), is("Unsupported resource URI `shardingsphere://foo`."));
    }
    
    @Test
    void assertCreateErrorWithUnsupportedTool() {
        McpError actual = MCPTransportErrorFactory.createError(new UnsupportedToolException("foo_tool"));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Unsupported tool `foo_tool`."));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
        assertThat(actualData.get("summary"), is("Unsupported tool `foo_tool`."));
    }
    
    @Test
    void assertCreateErrorWithTransportSecurityCategory() {
        McpError actual = MCPTransportErrorFactory.createError(new MCPTransportSecurityException(403,
                "Origin is not allowed by MCP HTTP transport policy.", MCPTransportSecurityException.CATEGORY_ORIGIN_NOT_ALLOWED));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Origin is not allowed by MCP HTTP transport policy."));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
        @SuppressWarnings("unchecked")
        Map<String, Object> actualRecovery = (Map<String, Object>) actualData.get("recovery");
        assertThat(actualRecovery.get("category"), is("origin_not_allowed"));
        assertThat(actualRecovery.get("recovery_category"), is("transport_security"));
        assertFalse(String.valueOf(actualData).contains("session-"));
    }
    
    @Test
    void assertCreateErrorWithUnexpectedError() {
        McpError actual = MCPTransportErrorFactory.createError(new IllegalStateException("foo_message"));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INTERNAL_ERROR));
        assertThat(actual.getJsonRpcError().message(), is("Service is temporarily unavailable."));
        assertFalse(String.valueOf(actual.getJsonRpcError().data()).contains("foo_message"));
    }
    
    @Test
    void assertCreateResourceErrorWithNotFoundError() {
        McpError actual = MCPTransportErrorFactory.createResourceError(new DatabaseCapabilityNotFoundException());
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.RESOURCE_NOT_FOUND));
        assertThat(actual.getJsonRpcError().message(), is("Database capability does not exist."));
    }
    
    @Test
    void assertCreateResourceErrorWithInvalidRequest() {
        McpError actual = MCPTransportErrorFactory.createResourceError(new MCPInvalidRequestException(" "));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("Invalid request."));
    }
}
