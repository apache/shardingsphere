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

package org.apache.shardingsphere.test.e2e.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.tool.MCPToolCatalog;
import org.apache.shardingsphere.mcp.tool.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolInputDefinition;
import org.apache.shardingsphere.mcp.tool.MCPToolValueDefinition;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * LLM MCP conversation runner.
 */
final class LLMMCPConversationRunner {
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    
    private final int maxTurns;
    
    private final LLMChatClient llmChatClient;
    
    private final MCPToolClient mcpToolClient;
    
    private final MCPToolCatalog toolCatalog = new MCPToolCatalog();
    
    LLMMCPConversationRunner(final int maxTurns, final LLMChatClient llmChatClient, final MCPToolClient mcpToolClient) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpToolClient = mcpToolClient;
    }
    
    LLME2EArtifactBundle run(final LLME2EScenario scenario) {
        final List<LLMChatMessage> messages = new LinkedList<>();
        final List<String> rawModelOutputs = new LinkedList<>();
        final List<MCPToolTraceRecord> toolTrace = new LinkedList<>();
        final List<String> mcpRuntimeLogLines = new LinkedList<>();
        String finalAnswerJson = "";
        messages.add(LLMChatMessage.system(scenario.systemPrompt()));
        messages.add(LLMChatMessage.user(scenario.userPrompt()));
        try {
            llmChatClient.waitUntilReady();
            mcpToolClient.open();
            boolean finalAnswerRequested = false;
            int finalAnswerAttempts = 0;
            for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
                if (!finalAnswerRequested && hasRequiredToolCoverage(scenario.requiredToolNames(), toolTrace)) {
                    messages.add(LLMChatMessage.user(createFinalAnswerInstruction(scenario)));
                    finalAnswerRequested = true;
                }
                final String toolChoice = finalAnswerRequested ? "none" : toolTrace.isEmpty() ? "required" : "auto";
                final LLMChatCompletion completion = llmChatClient.complete(messages,
                        finalAnswerRequested ? List.of() : createToolDefinitions(scenario.allowedToolNames()),
                        toolChoice, finalAnswerRequested);
                rawModelOutputs.add(completion.rawResponse());
                if (!completion.toolCalls().isEmpty()) {
                    messages.add(LLMChatMessage.assistant(completion.content(), completion.toolCalls()));
                    for (LLMToolCall each : completion.toolCalls()) {
                        if (!scenario.allowedToolNames().contains(each.name())) {
                            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("unexpected_tool_requested", "Model requested an unsupported tool."));
                        }
                        final Map<String, Object> arguments;
                        try {
                            arguments = parseToolArguments(each.argumentsJson());
                        } catch (final IllegalArgumentException ex) {
                            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("invalid_tool_arguments", "Model returned invalid tool arguments JSON."));
                        }
                        if ("execute_query".equals(each.name()) && !isReadOnlyQuery(arguments)) {
                            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                                    LLME2EAssertionReport.failure("unsafe_sql_attempted", "Model attempted a non-read-only SQL statement."));
                        }
                        MCPToolResponse toolResponse = mcpToolClient.call(each.name(), arguments);
                        mcpRuntimeLogLines.add("tool=" + each.name() + " args=" + JsonUtils.toJsonString(arguments));
                        mcpRuntimeLogLines.add("response=" + JsonUtils.toJsonString(toolResponse.structuredContent()));
                        toolTrace.add(new MCPToolTraceRecord(toolTrace.size() + 1, each.name(), arguments, toolResponse.structuredContent()));
                        messages.add(LLMChatMessage.tool(each.id(), JsonUtils.toJsonString(toolResponse.structuredContent())));
                    }
                    continue;
                }
                if (!finalAnswerRequested) {
                    messages.add(LLMChatMessage.user("You must call the required MCP tools before answering. Do not guess. Use the tools now."));
                    continue;
                }
                finalAnswerJson = completion.content();
                finalAnswerAttempts++;
                try {
                    final LLMStructuredAnswer actualAnswer = LLMStructuredAnswer.fromJson(finalAnswerJson);
                    final LLME2EAssertionReport assertionReport = validateFinalAnswer(scenario, actualAnswer, toolTrace);
                    return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                            assertionReport);
                } catch (final IllegalArgumentException ex) {
                    if (1 >= finalAnswerAttempts) {
                        messages.add(LLMChatMessage.user("Return valid JSON only. Do not include markdown or explanation."));
                        continue;
                    }
                    return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                            LLME2EAssertionReport.failure("invalid_final_json", "Model did not return a valid final JSON payload."));
                }
            }
            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("missing_required_tool_coverage", "Conversation exhausted turns before reaching the required tool coverage."));
        } catch (final IOException ex) {
            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("model_service_unavailable", "Model service request failed: " + ex.getMessage()));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure("model_service_unavailable", "Conversation was interrupted."));
        } catch (final IllegalStateException ex) {
            final String failureType = ex.getMessage().startsWith("MCP") || ex.getMessage().startsWith("Failed to initialize MCP")
                    ? "mcp_runtime_unavailable"
                    : "model_service_unavailable";
            return createArtifactBundle(scenario, rawModelOutputs, toolTrace, mcpRuntimeLogLines, finalAnswerJson,
                    LLME2EAssertionReport.failure(failureType, ex.getMessage()));
        } finally {
            closeToolClient();
        }
    }
    
    private LLME2EArtifactBundle createArtifactBundle(final LLME2EScenario scenario, final List<String> rawModelOutputs, final List<MCPToolTraceRecord> toolTrace,
                                                      final List<String> mcpRuntimeLogLines, final String finalAnswerJson, final LLME2EAssertionReport assertionReport) {
        return new LLME2EArtifactBundle(scenario.scenarioId(), scenario.systemPrompt(), scenario.userPrompt(), finalAnswerJson,
                List.copyOf(rawModelOutputs), List.copyOf(toolTrace), List.copyOf(mcpRuntimeLogLines), assertionReport);
    }
    
    private void closeToolClient() {
        try {
            mcpToolClient.close();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (final IOException ignored) {
            // Ignore tool-client close failures in test cleanup.
        }
    }
    
    private boolean hasRequiredToolCoverage(final Collection<String> requiredToolNames, final Collection<MCPToolTraceRecord> toolTrace) {
        Set<String> result = new LinkedHashSet<>();
        for (MCPToolTraceRecord each : toolTrace) {
            result.add(each.toolName());
        }
        return result.containsAll(requiredToolNames);
    }
    
    private boolean isReadOnlyQuery(final Map<String, Object> arguments) {
        String sql = Objects.toString(arguments.get("sql"), "").trim();
        String normalizedSql = sql.toUpperCase(Locale.ENGLISH);
        return normalizedSql.startsWith("SELECT") && (6 == normalizedSql.length() || Character.isWhitespace(normalizedSql.charAt(6))) && !normalizedSql.contains(";");
    }
    
    private String createFinalAnswerInstruction(final LLME2EScenario scenario) {
        final LLMStructuredAnswer expectedAnswer = scenario.expectedAnswer();
        final String prompt = "Return JSON only with keys database, schema, table, query, totalOrders, toolSequence. "
                + "The target table is `%s`, the final toolSequence must match the observed tool trace exactly, "
                + "and the required tools are `%s`.";
        return String.format(Locale.ENGLISH,
                prompt,
                expectedAnswer.table(), String.join(", ", scenario.requiredToolNames()));
    }
    
    private LLME2EAssertionReport validateFinalAnswer(final LLME2EScenario scenario, final LLMStructuredAnswer actualAnswer,
                                                      final List<MCPToolTraceRecord> toolTrace) {
        final LLMStructuredAnswer expectedAnswer = scenario.expectedAnswer();
        if (!hasRequiredToolCoverage(scenario.requiredToolNames(), toolTrace)) {
            return LLME2EAssertionReport.failure("missing_required_tool_coverage", "Tool trace does not contain the required tools.");
        }
        if (!expectedAnswer.database().equals(actualAnswer.database())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer database does not match expected database.");
        }
        if (!expectedAnswer.schema().equals(actualAnswer.schema())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer schema does not match expected schema.");
        }
        if (!expectedAnswer.table().equals(actualAnswer.table())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer table does not match expected table.");
        }
        if (!expectedAnswer.getNormalizedQuery().equals(actualAnswer.getNormalizedQuery())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer query does not match expected query.");
        }
        int actualTotalOrders = getActualTotalOrders(toolTrace);
        if (actualTotalOrders != actualAnswer.totalOrders() || expectedAnswer.totalOrders() != actualAnswer.totalOrders()) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer totalOrders does not match the execute_query result.");
        }
        List<String> actualToolSequence = new LinkedList<>();
        for (MCPToolTraceRecord each : toolTrace) {
            actualToolSequence.add(each.toolName());
        }
        if (!actualToolSequence.equals(actualAnswer.toolSequence())) {
            return LLME2EAssertionReport.failure("unexpected_query_result", "Final answer toolSequence does not match the observed tool trace.");
        }
        return LLME2EAssertionReport.success("LLM MCP smoke passed.");
    }
    
    private int getActualTotalOrders(final List<MCPToolTraceRecord> toolTrace) {
        for (int index = toolTrace.size() - 1; index >= 0; index--) {
            final MCPToolTraceRecord each = toolTrace.get(index);
            if (!"execute_query".equals(each.toolName())) {
                continue;
            }
            final Object resultKind = each.structuredContent().get("result_kind");
            if (!"result_set".equals(Objects.toString(resultKind, ""))) {
                break;
            }
            final List<List<Object>> rows = castToRows(each.structuredContent().get("rows"));
            if (!rows.isEmpty() && !rows.get(0).isEmpty()) {
                final Object value = rows.get(0).get(0);
                if (value instanceof Number) {
                    return ((Number) value).intValue();
                }
                return Integer.parseInt(Objects.toString(value, "").trim());
            }
        }
        throw new IllegalArgumentException("The execute_query trace does not contain a numeric result set.");
    }
    
    private List<List<Object>> castToRows(final Object value) {
        return JsonUtils.fromJsonString(JsonUtils.toJsonString(value), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> parseToolArguments(final String argumentsJson) {
        try {
            return OBJECT_MAPPER.readValue(argumentsJson, new TypeReference<>() {
            });
        } catch (final JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid tool arguments JSON.", ex);
        }
    }
    
    private List<Map<String, Object>> createToolDefinitions(final Collection<String> allowedToolNames) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (String each : allowedToolNames) {
            MCPToolDescriptor toolDescriptor = toolCatalog.findToolDescriptor(each).orElseThrow(() -> new IllegalArgumentException("Unsupported tool descriptor: " + each));
            result.add(Map.of("type", "function", "function", Map.of(
                    "name", toolDescriptor.getName(),
                    "description", toolDescriptor.getDescription(),
                    "parameters", createParameterSchema(toolDescriptor.getInputDefinition()))));
        }
        return result;
    }
    
    private Map<String, Object> createParameterSchema(final MCPToolInputDefinition inputDefinition) {
        Map<String, Object> properties = new LinkedHashMap<>(inputDefinition.getFields().size(), 1F);
        List<String> required = new LinkedList<>();
        for (MCPToolFieldDefinition each : inputDefinition.getFields()) {
            properties.put(each.getName(), createValueSchema(each.getValueDefinition()));
            if (each.isRequired()) {
                required.add(each.getName());
            }
        }
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("additionalProperties", inputDefinition.isAdditionalPropertiesAllowed());
        if (!required.isEmpty()) {
            result.put("required", required);
        }
        return result;
    }
    
    private Map<String, Object> createValueSchema(final MCPToolValueDefinition valueDefinition) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", switch (valueDefinition.getType()) {
            case STRING -> "string";
            case INTEGER -> "integer";
            case ARRAY -> "array";
        });
        result.put("description", valueDefinition.getDescription());
        if (MCPToolValueDefinition.Type.ARRAY == valueDefinition.getType()) {
            result.put("items", createValueSchema(valueDefinition.getItemDefinition()));
        }
        return result;
    }
}
