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

package org.apache.shardingsphere.mcp.support.security;

import org.junit.jupiter.api.Test;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPClientSafetyPolicyTest {
    
    @Test
    void assertCreateModelFacingPayload() {
        Map<String, Object> actual = MCPClientSafetyPolicy.createModelFacingPayload();
        assertThat(actual.get("identity_scope"), is("mcp_session"));
        assertTrue(String.valueOf(actual.get("transport_scope")).contains("trusted session attribution"));
        assertTrue(String.valueOf(actual.get("external_model_boundary")).contains("never calls external model providers"));
        Map<?, ?> actualRuntimeProtection = (Map<?, ?>) actual.get("runtime_protection");
        Map<?, ?> actualToolCallLimit = (Map<?, ?>) actualRuntimeProtection.get("tool_call_limit");
        assertThat(actualToolCallLimit.get("scope"), is("session"));
        assertThat(actualToolCallLimit.get("property"), is(MCPRuntimeProtectionPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY));
        Map<?, ?> actualSQLExecutionLimits = (Map<?, ?>) actualRuntimeProtection.get("sql_execution_limits");
        assertThat(((Map<?, ?>) actualSQLExecutionLimits.get("max_rows")).get("applied_field"), is("applied_max_rows"));
        assertThat(((Map<?, ?>) actualSQLExecutionLimits.get("timeout_ms")).get("applied_field"), is("applied_timeout_ms"));
    }
}
