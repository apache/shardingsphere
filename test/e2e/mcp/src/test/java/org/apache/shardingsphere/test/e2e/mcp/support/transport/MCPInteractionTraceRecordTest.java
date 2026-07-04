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

package org.apache.shardingsphere.test.e2e.mcp.support.transport;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPInteractionTraceRecordTest {
    
    @Test
    void assertCreateResourceRead() {
        MCPInteractionTraceRecord actual = MCPInteractionTraceRecord.createResourceRead(1, "shardingsphere://databases", Map.of("items", 1), 12L);
        assertThat(actual.getSequence(), is(1));
        assertThat(actual.getActionKind(), is(MCPInteractionActionNames.RESOURCE_READ_KIND));
        assertThat(actual.getActionOrigin(), is(MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN));
        assertThat(actual.getTargetName(), is(MCPInteractionActionNames.READ_RESOURCE));
        assertThat(actual.getArguments(), is(Map.of("uri", "shardingsphere://databases")));
        assertThat(actual.getStructuredContent(), is(Map.of("items", 1)));
        assertTrue(actual.isValid());
        assertThat(actual.getLatencyMillis(), is(12L));
    }
    
    @Test
    void assertCreateCompletion() {
        Map<String, Object> arguments = Map.of("ref", Map.of("type", "ref/prompt", "name", "inspect_metadata"), "argument", Map.of("name", "schema"));
        MCPInteractionTraceRecord actual = MCPInteractionTraceRecord.createCompletion(2, arguments, Map.of("completion", "public"), 7L);
        assertThat(actual.getSequence(), is(2));
        assertThat(actual.getActionKind(), is(MCPInteractionActionNames.COMPLETION_KIND));
        assertThat(actual.getActionOrigin(), is(MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN));
        assertThat(actual.getTargetName(), is(MCPInteractionActionNames.COMPLETE));
        assertThat(actual.getArguments(), is(arguments));
        assertThat(actual.getStructuredContent(), is(Map.of("completion", "public")));
        assertTrue(actual.isValid());
        assertThat(actual.getLatencyMillis(), is(7L));
    }
    
    @Test
    void assertCreateInvalidAction() {
        MCPInteractionTraceRecord actual = MCPInteractionTraceRecord.createInvalidAction(3, "tool_call", "database_gateway_execute_update", Map.of("sql", "UPDATE t SET c = 1"), "unsafe_sql");
        assertThat(actual.getSequence(), is(3));
        assertThat(actual.getActionKind(), is("tool_call"));
        assertThat(actual.getActionOrigin(), is(MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN));
        assertThat(actual.getTargetName(), is("database_gateway_execute_update"));
        assertThat(actual.getArguments(), is(Map.of("sql", "UPDATE t SET c = 1")));
        assertThat(actual.getStructuredContent(), is(Map.of("error_code", "unsafe_sql")));
        assertFalse(actual.isValid());
        assertThat(actual.getLatencyMillis(), is(0L));
    }
}
