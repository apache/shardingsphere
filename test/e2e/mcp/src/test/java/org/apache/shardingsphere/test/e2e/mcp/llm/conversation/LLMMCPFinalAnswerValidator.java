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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

class LLMMCPFinalAnswerValidator {
    
    LLME2EAssertionReport validateSafely(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer, final List<MCPInteractionTraceRecord> interactionTrace) {
        try {
            return validate(scenario, actualAnswer, interactionTrace);
        } catch (final IllegalArgumentException ex) {
            return LLME2EAssertionReport.failure("unexpected_query_result", ex.getMessage());
        }
    }
    
    private LLME2EAssertionReport validate(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer, final List<MCPInteractionTraceRecord> interactionTrace) {
        LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        if (!LLMMCPInteractionCoverage.hasRequiredInteractionCoverage(scenario.getRequiredToolNames(), interactionTrace)) {
            return LLME2EAssertionReport.failure("missing_required_tool_coverage", "Tool trace does not contain the required tools.");
        }
        if (!expectedAnswer.getDatabase().equals(actualAnswer.getDatabase())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer database does not match expected database.");
        }
        if (!expectedAnswer.getSchema().equals(actualAnswer.getSchema())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer schema does not match expected schema.");
        }
        if (!expectedAnswer.getTable().equals(actualAnswer.getTable())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer table does not match expected table.");
        }
        if (!isExpectedQuery(expectedAnswer, actualAnswer)) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer query does not match expected query.");
        }
        int actualTotalOrders = getActualTotalOrders(interactionTrace);
        if (actualTotalOrders != actualAnswer.getTotalOrders() || expectedAnswer.getTotalOrders() != actualAnswer.getTotalOrders()) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer totalOrders does not match the database_gateway_execute_query result.");
        }
        if (!createComparableInteractionSequence(interactionTrace).equals(collapseConsecutiveActionNames(actualAnswer.getInteractionSequence()))) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer interactionSequence does not match the observed interaction trace.");
        }
        return LLME2EAssertionReport.isSuccess("LLM MCP interaction passed.");
    }
    
    private List<String> createComparableInteractionSequence(final List<MCPInteractionTraceRecord> interactionTrace) {
        List<String> result = new LinkedList<>();
        for (MCPInteractionTraceRecord each : interactionTrace) {
            result.add(each.getTargetName());
        }
        return collapseConsecutiveActionNames(result);
    }
    
    private List<String> collapseConsecutiveActionNames(final List<String> actionNames) {
        List<String> result = new LinkedList<>();
        for (String each : actionNames) {
            if (result.isEmpty() || !result.getLast().equals(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isExpectedQuery(final LLMStructuredAnswer expectedAnswer, final LLMStructuredAnswer actualAnswer) {
        String expectedQuery = normalizeComparableQuery(expectedAnswer.getQuery());
        String actualQuery = normalizeComparableQuery(actualAnswer.getQuery());
        if (expectedQuery.equals(actualQuery)) {
            return true;
        }
        String schemaName = expectedAnswer.getSchema().trim();
        if (schemaName.isEmpty()) {
            return false;
        }
        String tableName = expectedAnswer.getTable().trim();
        String qualifiedQuery = expectedQuery.replace(" FROM " + tableName.toUpperCase(Locale.ENGLISH),
                " FROM " + schemaName.toUpperCase(Locale.ENGLISH) + "." + tableName.toUpperCase(Locale.ENGLISH));
        return qualifiedQuery.equals(actualQuery);
    }
    
    private String normalizeComparableQuery(final String query) {
        return query.replaceAll("\\s+", " ").trim().toUpperCase(Locale.ENGLISH);
    }
    
    private int getActualTotalOrders(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!"database_gateway_execute_query".equals(each.getTargetName())) {
                continue;
            }
            Object resultKind = each.getStructuredContent().get("result_kind");
            if (!"result_set".equals(Objects.toString(resultKind, ""))) {
                break;
            }
            List<List<Object>> rows = LLMMCPJsonValues.castToRows(each.getStructuredContent().get("rows"));
            if (!rows.isEmpty() && !rows.get(0).isEmpty()) {
                return getActualTotalOrders(rows.get(0).get(0));
            }
        }
        throw new IllegalArgumentException("The database_gateway_execute_query trace does not contain a numeric result set.");
    }
    
    private int getActualTotalOrders(final Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(Objects.toString(value, "").trim());
        } catch (final NumberFormatException ex) {
            throw new IllegalArgumentException("The database_gateway_execute_query trace does not contain a numeric result set.", ex);
        }
    }
}
