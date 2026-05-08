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

package org.apache.shardingsphere.mcp.core.protocol.response;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPErrorResponseTest {
    
    @Test
    void assertToPayload() {
        Map<String, Object> actual = new MCPErrorResponse("invalid_request", "foo_message", Map.of(), "request-1").toPayload();
        assertThat(actual, is(Map.of("response_mode", "recovery", "request_id", "request-1", "error_code", "invalid_request", "message", "foo_message")));
    }
    
    @Test
    void assertToPayloadWithRecovery() {
        Map<String, Object> actual = new MCPErrorResponse("invalid_request", "foo_message", Map.of("recoverable", true), "request-1").toPayload();
        assertThat(actual, is(Map.of("response_mode", "recovery", "request_id", "request-1", "error_code", "invalid_request", "message", "foo_message", "recovery",
                Map.of("recoverable", true, "request_id", "request-1"))));
    }
    
    @Test
    void assertGetErrorCode() {
        assertThat(new MCPErrorResponse("invalid_request", "foo_message").getErrorCode(), is("invalid_request"));
    }
}
