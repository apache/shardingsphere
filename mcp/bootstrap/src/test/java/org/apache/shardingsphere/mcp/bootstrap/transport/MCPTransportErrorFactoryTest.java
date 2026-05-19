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
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPTransportErrorFactoryTest {
    
    @Test
    void assertCreateInvalidParamsError() {
        McpError actual = MCPTransportErrorFactory.createInvalidParamsError(new MCPInvalidRequestException("foo_message"));
        assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
        assertThat(actual.getJsonRpcError().message(), is("foo_message"));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
        assertThat(actualData.get("message"), is("foo_message"));
    }
    
    @Test
    void assertGetProtocolErrorCodeWithUnsupportedResourceUri() {
        assertThat(MCPTransportErrorFactory.getProtocolErrorCode(new UnsupportedResourceUriException("shardingsphere://foo")), is(McpSchema.ErrorCodes.RESOURCE_NOT_FOUND));
    }
    
    @Test
    void assertGetProtocolErrorCodeWithInvalidRequest() {
        assertThat(MCPTransportErrorFactory.getProtocolErrorCode(new MCPInvalidRequestException("foo_message")), is(McpSchema.ErrorCodes.INVALID_PARAMS));
    }
    
    @Test
    void assertGetProtocolErrorCodeWithUnsupportedOperation() {
        assertThat(MCPTransportErrorFactory.getProtocolErrorCode(new MCPUnsupportedException("foo_message")), is(McpSchema.ErrorCodes.INVALID_PARAMS));
    }
    
    @Test
    void assertGetProtocolErrorCodeWithUnexpectedError() {
        assertThat(MCPTransportErrorFactory.getProtocolErrorCode(new IllegalStateException("foo_message")), is(McpSchema.ErrorCodes.INTERNAL_ERROR));
    }
}
