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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class LLMMCPToolCallNormalizer {
    
    static NormalizedToolCall normalize(final LLME2EScenario scenario, final String rawToolName, final Map<String, Object> rawArguments, final List<String> turnToolNames,
                                        final List<MCPInteractionTraceRecord> interactionTrace) {
        final String toolName = normalizeToolName(rawToolName, rawArguments, turnToolNames);
        return new NormalizedToolCall(toolName, normalizeToolArguments(scenario, toolName, normalizeToolNameArguments(rawToolName, toolName, rawArguments), interactionTrace));
    }
    
    private static String normalizeToolName(final String toolName, final Map<String, Object> rawArguments, final List<String> turnToolNames) {
        return shouldRouteReadOnlySqlToOfferedQueryTool(toolName, rawArguments, turnToolNames)
                ? "database_gateway_execute_query"
                : toolName;
    }
    
    private static boolean shouldRouteReadOnlySqlToOfferedQueryTool(final String toolName, final Map<String, Object> rawArguments, final List<String> turnToolNames) {
        return !turnToolNames.contains(toolName)
                && turnToolNames.equals(List.of("database_gateway_execute_query"))
                && isReadOnlySql(Objects.toString(rawArguments.get("sql"), ""));
    }
    
    private static boolean isReadOnlySql(final String sql) {
        return sql.trim().toUpperCase(Locale.ENGLISH).startsWith("SELECT ");
    }
    
    private static Map<String, Object> normalizeToolNameArguments(final String rawToolName, final String toolName, final Map<String, Object> rawArguments) {
        if (rawToolName.equals(toolName) || !"database_gateway_execute_query".equals(toolName)) {
            return rawArguments;
        }
        final Map<String, Object> result = new LinkedHashMap<>(rawArguments);
        result.remove("execution_mode");
        return result;
    }
    
    private static Map<String, Object> normalizeToolArguments(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args,
                                                              final List<MCPInteractionTraceRecord> interactionTrace) {
        Map<String, Object> result = normalizeResourceUriArgument(scenario, toolName, args);
        result = normalizeSearchMetadataScopeArgument(scenario, toolName, result);
        result = normalizeExpectedQuerySchemaArgument(scenario, toolName, result);
        result = normalizeInitialPlanningPlanIdArgument(toolName, result, interactionTrace);
        result = normalizeWorkflowPlanIdArgument(toolName, result, interactionTrace);
        return normalizeCompletionArguments(toolName, result, interactionTrace);
    }
    
    private static Map<String, Object> normalizeResourceUriArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        final String resourceUriArgument = Objects.toString(args.get("uri"), "").trim();
        if (!MCPInteractionActionNames.READ_RESOURCE.equals(toolName) || resourceUriArgument.isEmpty() || resourceUriArgument.startsWith("shardingsphere://")) {
            return args;
        }
        final String resourceUri = LLMMCPScenarioInference.findExpectedResourceUri(scenario);
        if (resourceUri.isEmpty()) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args);
        result.put("uri", resourceUri);
        return result;
    }
    
    private static Map<String, Object> normalizeSearchMetadataScopeArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        if (!"database_gateway_search_metadata".equals(toolName) || !hasExplicitExpectedScopeInstruction(scenario) || !shouldUseExpectedSearchScope(scenario.getExpectedAnswer(), args)) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args);
        putIfBlank(result, "database", scenario.getExpectedAnswer().getDatabase());
        putIfBlank(result, "schema", scenario.getExpectedAnswer().getSchema());
        return result;
    }
    
    private static boolean hasExplicitExpectedScopeInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        return scenario.getUserPrompt().contains(String.format(Locale.ENGLISH, "Use logical database `%s` and schema `%s`", expectedAnswer.getDatabase(), expectedAnswer.getSchema()));
    }
    
    private static void putIfBlank(final Map<String, Object> values, final String key, final String value) {
        if (Objects.toString(values.get(key), "").trim().isEmpty()) {
            values.put(key, value);
        }
    }
    
    private static boolean shouldUseExpectedSearchScope(final LLMStructuredAnswer expectedAnswer, final Map<String, Object> args) {
        final String query = Objects.toString(args.get("query"), "").trim();
        if (expectedAnswer.getDatabase().isEmpty() || expectedAnswer.getSchema().isEmpty() || !expectedAnswer.getTable().equalsIgnoreCase(query)) {
            return false;
        }
        return Objects.toString(args.get("database"), "").trim().isEmpty() || Objects.toString(args.get("schema"), "").trim().isEmpty();
    }
    
    private static Map<String, Object> normalizeExpectedQuerySchemaArgument(final LLME2EScenario scenario, final String toolName, final Map<String, Object> args) {
        if (!"database_gateway_execute_query".equals(toolName) || !Objects.toString(args.get("schema"), "").trim().isEmpty()) {
            return args;
        }
        final LLMStructuredAnswer expectedAnswer = scenario.getExpectedAnswer();
        if (expectedAnswer.getSchema().isEmpty()
                || !expectedAnswer.getDatabase().equals(Objects.toString(args.get("database"), "").trim())
                || !LLMMCPScenarioInference.normalizeComparableQuery(expectedAnswer, expectedAnswer.getQuery()).equals(
                        LLMMCPScenarioInference.normalizeComparableQuery(expectedAnswer, Objects.toString(args.get("sql"), "")))) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args.size() + 1, 1F);
        result.putAll(args);
        result.put("schema", expectedAnswer.getSchema());
        return result;
    }
    
    private static Map<String, Object> normalizeInitialPlanningPlanIdArgument(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!toolName.startsWith(LLMMCPScenarioInference.PLANNING_TOOL_NAME_PREFIX) || !args.containsKey("plan_id") || !LLMMCPScenarioInference.findLatestPlanId(interactionTrace).isEmpty()) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args);
        result.remove("plan_id");
        return result;
    }
    
    private static Map<String, Object> normalizeWorkflowPlanIdArgument(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!("database_gateway_apply_workflow".equals(toolName) || "database_gateway_validate_workflow".equals(toolName)) || !isPlanIdPlaceholder(args.get("plan_id"))) {
            return args;
        }
        final String latestPlanId = LLMMCPScenarioInference.findLatestPlanId(interactionTrace);
        if (latestPlanId.isEmpty()) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args);
        result.put("plan_id", latestPlanId);
        return result;
    }
    
    private static boolean isPlanIdPlaceholder(final Object value) {
        final String planId = Objects.toString(value, "").trim();
        return "plan_id".equals(planId) || "{plan_id}".equals(planId) || "<plan_id>".equals(planId) || planId.matches("\\d+");
    }
    
    private static Map<String, Object> normalizeCompletionArguments(final String toolName, final Map<String, Object> args, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (!MCPInteractionActionNames.COMPLETE.equals(toolName) || args.containsKey("reference")) {
            return args;
        }
        final Map<String, Object> latestReference = findLatestCompletionReference(interactionTrace);
        if (latestReference.isEmpty()) {
            return args;
        }
        final Map<String, Object> result = new LinkedHashMap<>(args.size() + 1, 1F);
        result.put("reference", latestReference);
        result.putAll(args);
        return result;
    }
    
    private static Map<String, Object> findLatestCompletionReference(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = interactionTrace.size() - 1; index >= 0; index--) {
            final MCPInteractionTraceRecord each = interactionTrace.get(index);
            if (!each.isValid() || each.getStructuredContent().containsKey("error_code")) {
                continue;
            }
            final Map<String, Object> result = createCompletionReference(each);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return Map.of();
    }
    
    private static Map<String, Object> createCompletionReference(final MCPInteractionTraceRecord traceRecord) {
        if (MCPInteractionActionNames.GET_PROMPT.equals(traceRecord.getTargetName())) {
            final String promptName = Objects.toString(traceRecord.getArguments().get("name"), "").trim();
            return promptName.isEmpty() ? Map.of() : Map.of("type", "ref/prompt", "name", promptName);
        }
        if (MCPInteractionActionNames.READ_RESOURCE.equals(traceRecord.getTargetName())) {
            final String resourceUri = Objects.toString(traceRecord.getArguments().get("uri"), "").trim();
            return resourceUri.isEmpty() ? Map.of() : Map.of("type", "ref/resource", "uri", resourceUri);
        }
        return Map.of();
    }
    
    record NormalizedToolCall(String name, Map<String, Object> arguments) {
    }
}
