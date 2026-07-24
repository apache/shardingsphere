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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation;

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMMCPInteractionCoverageTest {
    
    @Test
    void assertHasRequiredInteractionCoverage() {
        assertTrue(LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(List.of("database_gateway_execute_query"), List.of(
                createTrace("database_gateway_execute_query", true, Map.of("result_kind", "result_set")))));
    }
    
    @Test
    void assertFindMissingRequiredInteractionNames() {
        assertThat(LLMMCPInteractionCoverage.findMissingRequiredInteractionNames(List.of("database_gateway_execute_query", "mcp_read_resource"), List.of(
                createTrace("database_gateway_execute_query", true, Map.of()),
                createTrace("mcp_read_resource", false, Map.of()),
                createTrace("mcp_list_resources", true, Map.of("error_code", "failed")))), is(List.of("mcp_read_resource")));
    }
    
    @Test
    void assertHasRequiredInteractionCoverageWithErrorPayload() {
        assertFalse(LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(List.of("database_gateway_execute_query"), List.of(
                createTrace("database_gateway_execute_query", true, Map.of("error_code", "failed")))));
    }
    
    @Test
    void assertHasRequiredInteractionCoverageWithHarnessOrigin() {
        assertTrue(LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(List.of("database_gateway_execute_query"), List.of(
                new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN,
                        "database_gateway_execute_query", Map.of(), Map.of("result_kind", "result_set"), true, 0L))));
    }
    
    private MCPInteractionTraceRecord createTrace(final String targetName, final boolean valid, final Map<String, Object> structuredContent) {
        return new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, targetName, Map.of(), structuredContent, valid, 0L);
    }
}
