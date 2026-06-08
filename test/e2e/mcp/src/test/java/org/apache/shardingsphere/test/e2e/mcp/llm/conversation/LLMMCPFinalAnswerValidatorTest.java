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

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMMCPFinalAnswerValidatorTest {
    
    @Test
    void assertValidateSafely() {
        LLME2EAssertionReport actual =
                new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of("database_gateway_execute_query")), createActualAnswer(2, List.of("database_gateway_execute_query")),
                        List.of(createQueryTrace(1, List.of(List.of(2)))));
        assertTrue(actual.isSuccess());
        assertThat(actual.getMessage(), is("LLM MCP interaction passed."));
    }
    
    @Test
    void assertValidateSafelyWithMissingRequiredCoverage() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of("mcp_read_resource")), createActualAnswer(2, List.of("database_gateway_execute_query")),
                List.of(createQueryTrace(1, List.of(List.of(2)))));
        assertThat(actual.getFailureType(), is("missing_required_tool_coverage"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("mismatchedAnswerProvider")
    void assertValidateSafelyWithMismatchedAnswerFields(final String name, final LLMStructuredAnswer actualAnswer, final String expectedMessage) {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), actualAnswer, List.of(createQueryTrace(1, List.of(List.of(2)))));
        assertThat(actual.getFailureType(), is("unexpected_query_result"));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    @Test
    void assertValidateSafelyWithSchemaQualifiedQuery() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), new LLMStructuredAnswer(
                "logic_db", "public", "orders", "SELECT COUNT(*) FROM public.orders", 2, List.of("database_gateway_execute_query")), List.of(createQueryTrace(1, List.of(List.of(2)))));
        assertTrue(actual.isSuccess());
    }
    
    @Test
    void assertValidateSafelyWithStringTotalOrders() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), createActualAnswer(2, List.of("database_gateway_execute_query")),
                List.of(createQueryTrace(1, List.of(List.of("2")))));
        assertTrue(actual.isSuccess());
    }
    
    @Test
    void assertValidateSafelyWithNonResultSet() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), createActualAnswer(2, List.of("database_gateway_execute_query")), List.of(
                new MCPInteractionTraceRecord(1, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, "database_gateway_execute_query", Map.of(), Map.of("result_kind", "update_count"), true,
                        0L)));
        assertThat(actual.getFailureType(), is("unexpected_query_result"));
        assertThat(actual.getMessage(), is("The database_gateway_execute_query trace does not contain a numeric result set."));
    }
    
    @Test
    void assertValidateSafelyWithNonNumericTotalOrders() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), createActualAnswer(2, List.of("database_gateway_execute_query")),
                List.of(createQueryTrace(1, List.of(List.of("bad")))));
        assertThat(actual.getFailureType(), is("unexpected_query_result"));
        assertThat(actual.getMessage(), is("The database_gateway_execute_query trace does not contain a numeric result set."));
    }
    
    @Test
    void assertValidateSafelyWithMismatchedInteractionSequence() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), createActualAnswer(2, List.of("mcp_read_resource")),
                List.of(createQueryTrace(1, List.of(List.of(2)))));
        assertThat(actual.getFailureType(), is("unexpected_query_result"));
        assertThat(actual.getMessage(), is("Final answer interactionSequence does not match the observed interaction trace."));
    }
    
    @Test
    void assertValidateSafelyWithConsecutiveInteractionCollapse() {
        LLME2EAssertionReport actual = new LLMMCPFinalAnswerValidator().validateSafely(createScenario(List.of()), createActualAnswer(2, List.of("database_gateway_execute_query")),
                List.of(createQueryTrace(1, List.of(List.of(2))), createQueryTrace(2, List.of(List.of(2)))));
        assertTrue(actual.isSuccess());
    }
    
    private static Stream<Object[]> mismatchedAnswerProvider() {
        return Stream.of(
                new Object[]{"database", new LLMStructuredAnswer("other_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2, List.of("database_gateway_execute_query")),
                        "Final answer database does not match expected database."},
                new Object[]{"schema", new LLMStructuredAnswer("logic_db", "other_schema", "orders", "SELECT COUNT(*) FROM orders", 2, List.of("database_gateway_execute_query")),
                        "Final answer schema does not match expected schema."},
                new Object[]{"table", new LLMStructuredAnswer("logic_db", "public", "users", "SELECT COUNT(*) FROM orders", 2, List.of("database_gateway_execute_query")),
                        "Final answer table does not match expected table."},
                new Object[]{"query", new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM users", 2, List.of("database_gateway_execute_query")),
                        "Final answer query does not match expected query."},
                new Object[]{"totalOrders", new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 3, List.of("database_gateway_execute_query")),
                        "Final answer totalOrders does not match the database_gateway_execute_query result."});
    }
    
    private LLME2EScenario createScenario(final List<String> requiredToolNames) {
        return new LLME2EScenario("scenario", "", "", createExpectedAnswer(), List.of(), requiredToolNames);
    }
    
    private LLMStructuredAnswer createExpectedAnswer() {
        return new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", 2, List.of("database_gateway_execute_query"));
    }
    
    private LLMStructuredAnswer createActualAnswer(final int totalOrders, final List<String> interactionSequence) {
        return new LLMStructuredAnswer("logic_db", "public", "orders", "SELECT COUNT(*) FROM orders", totalOrders, interactionSequence);
    }
    
    private MCPInteractionTraceRecord createQueryTrace(final int sequence, final List<List<Object>> rows) {
        return new MCPInteractionTraceRecord(sequence, "tool_call", MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN, "database_gateway_execute_query", Map.of(),
                Map.of("result_kind", "result_set", "rows", rows), true, 0L);
    }
}
