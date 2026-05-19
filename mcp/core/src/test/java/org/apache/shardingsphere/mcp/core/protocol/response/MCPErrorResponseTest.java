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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPErrorResponseTest {
    
    @Test
    void assertToPayload() {
        Map<String, Object> actual = new MCPErrorResponse("foo_message").toPayload();
        assertNotNull(actual.get("request_id"));
        assertFalse(actual.containsKey("recovery"));
        assertThat(actual.get("response_mode"), is("recovery"));
        assertThat(actual.get("message"), is("foo_message"));
    }
    
    @Test
    void assertToPayloadWithRecovery() {
        Map<String, Object> actual = new MCPErrorResponse("foo_message", Map.of("recoverable", true)).toPayload();
        assertNotNull(actual.get("request_id"));
        assertThat(actual.get("response_mode"), is("recovery"));
        assertThat(actual.get("message"), is("foo_message"));
        Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
        assertTrue((Boolean) actualRecovery.get("recoverable"));
        assertThat(actualRecovery.get("request_id"), is(actual.get("request_id")));
    }
}
