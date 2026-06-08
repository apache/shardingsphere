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

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLMMCPScenarioInferenceTest {
    
    @Test
    void assertFindExpectedResourceUriWithPromptMatch() {
        LLME2EScenario scenario = createScenario("Read `shardingsphere://databases/logic_db/schemas/public/tables/orders`.");
        assertThat(LLMMCPScenarioInference.findExpectedResourceUri(scenario), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
    }
    
    @Test
    void assertFindExpectedResourceUriWithPromptFallback() {
        LLME2EScenario scenario = createScenario("Read `shardingsphere://runtime`.");
        assertThat(LLMMCPScenarioInference.findExpectedResourceUri(scenario), is("shardingsphere://runtime"));
    }
    
    @Test
    void assertFindExpectedResourceUriWithExpectedTable() {
        assertThat(LLMMCPScenarioInference.findExpectedResourceUri(createScenario("Inspect the table.")),
                is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
    }
    
    @Test
    void assertFindLatestPlanId() {
        assertThat(LLMMCPScenarioInference.findLatestPlanId(List.of(
                createTrace(1, "plan-a", true, Map.of()),
                createTrace(2, "plan-b", true, Map.of("error_code", "failed")),
                createTrace(3, "plan-c", true, Map.of()))), is("plan-c"));
    }
    
    @Test
    void assertFindLatestPlanIdWithNoValidPlan() {
        assertThat(LLMMCPScenarioInference.findLatestPlanId(List.of(createTrace(1, "plan-a", false, Map.of()))), is(""));
    }
    
    @Test
    void assertNormalizeComparableQuery() {
        assertThat(LLMMCPScenarioInference.normalizeComparableQuery(createAnswer(), " select count(*) from public.orders "),
                is("SELECT COUNT(*) FROM ORDERS"));
    }
    
    private LLME2EScenario createScenario(final String userPrompt) {
        return new LLME2EScenario("scenario", "", userPrompt, createAnswer(), List.of(), List.of());
    }
    
    private LLMStructuredAnswer createAnswer() {
        return new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2, List.of());
    }
    
    private MCPInteractionTraceRecord createTrace(final int sequence, final String planId, final boolean valid, final Map<String, Object> extraContent) {
        Map<String, Object> structuredContent = new LinkedHashMap<>(extraContent);
        structuredContent.put("plan_id", planId);
        return new MCPInteractionTraceRecord(sequence, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, "database_gateway_plan_mask_rule", Map.of(), structuredContent, valid, 0L);
    }
}
