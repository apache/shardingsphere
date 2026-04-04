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

import org.apache.shardingsphere.mcp.protocol.MCPError.MCPErrorCode;
import org.apache.shardingsphere.mcp.protocol.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPProtocolErrorConverter;
import org.junit.jupiter.api.Test;

import java.sql.SQLTimeoutException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPProtocolErrorConverterTest {
    
    @Test
    void assertToErrorWithProtocolException() {
        MCPError actual = MCPProtocolErrorConverter.toError(new UnsupportedToolException());
        assertThat(actual.getCode(), is(MCPErrorCode.INVALID_REQUEST));
        assertThat(actual.getMessage(), is("Unsupported tool."));
    }
    
    @Test
    void assertToErrorWithSqlTimeoutException() {
        MCPError actual = MCPProtocolErrorConverter.toError(new SQLTimeoutException("Timed out."));
        assertThat(actual.getCode(), is(MCPErrorCode.TIMEOUT));
        assertThat(actual.getMessage(), is("Timed out."));
    }
    
    @Test
    void assertToPayloadWithNotFoundException() {
        Map<String, Object> actual = new MCPErrorResponse(MCPProtocolErrorConverter.toError(new DatabaseCapabilityNotFoundException())).toPayload();
        assertThat(actual.get("error_code"), is("not_found"));
        assertThat(actual.get("message"), is("Database capability does not exist."));
    }
    
    @Test
    void assertToPayloadWithUnknownException() {
        Map<String, Object> actual = new MCPErrorResponse(MCPProtocolErrorConverter.toError(new RuntimeException())).toPayload();
        assertThat(actual.get("error_code"), is("unavailable"));
        assertThat(actual.get("message"), is("Service is temporarily unavailable."));
    }
}
