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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.infra.util.json.JsonUtils;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Run one LLM conversation without harness corrections.
 */
public final class LLMConversationRunner {
    
    static final String READ_RESOURCE_TOOL_NAME = "mcp_read_resource";
    
    private static final String TOOL_CALL_KIND = "tool_call";
    
    private static final String RESOURCE_READ_KIND = "resource_read";
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating a live ShardingSphere MCP server. Use the available MCP functions to inspect current state and complete the user's task.
            Choose tools from their advertised names, descriptions, and input schemas. Pass resource URIs only to mcp_read_resource, never as SQL.
            Never guess or stop before retrieving the requested evidence. When target metadata is explicitly marked as already verified, use the requested
            planning response as evidence instead of repeating metadata discovery. Preview side effects without executing them. Use function calls for
            every tool invocation rather than printing a tool-call object as the answer. When you have enough evidence, answer the user's question concisely.
            """;
    
    private final int maxTurns;
    
    private final LLMChatModelClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    private final String modelName;
    
    private final LLMMCPToolDefinitionFactory toolDefinitionFactory = new LLMMCPToolDefinitionFactory();
    
    private final LLMMCPSafetyValidator safetyValidator = new LLMMCPSafetyValidator();
    
    public LLMConversationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                 final String modelName) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpInteractionClient = mcpInteractionClient;
        this.modelName = modelName;
    }
    
    /**
     * Run one scenario. The supplied MCP interaction client is opened and closed for this invocation.
     *
     * @param scenario scenario
     * @return conversation result
     */
    public Result run(final Scenario scenario) {
        ConversationArtifacts artifacts = new ConversationArtifacts();
        try {
            mcpInteractionClient.open();
            List<Map<String, Object>> advertisedTools = mcpInteractionClient.listTools();
            List<Map<String, Object>> toolDefinitions = toolDefinitionFactory.createFromRemote(advertisedTools, scenario.allowedToolNames());
            artifacts.setToolDefinitions(toolDefinitions);
            return runTurns(scenario, artifacts, toolDefinitions);
        } catch (final IOException ex) {
            return artifacts.createResult(scenario, modelName,
                    LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage()));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return artifacts.createResult(scenario, modelName,
                    LLME2EAssertionReport.failure("conversation_interrupted", "Conversation was interrupted."));
        } catch (final IllegalStateException ex) {
            return artifacts.createResult(scenario, modelName,
                    LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage()));
        } finally {
            closeInteractionClient();
        }
    }
    
    private void closeInteractionClient() {
        try {
            mcpInteractionClient.close();
        } catch (final InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } catch (final IOException ignored) {
        }
    }
    
    private Result runTurns(final Scenario scenario, final ConversationArtifacts artifacts,
                            final List<Map<String, Object>> toolDefinitions) throws InterruptedException {
        List<LLMChatMessage> messages = new LinkedList<>();
        messages.add(LLMChatMessage.system(SYSTEM_PROMPT));
        messages.add(LLMChatMessage.user(scenario.question()));
        Set<String> availableToolNames = toolDefinitions.stream()
                .map(each -> LLMMCPJsonValues.castToMap(each.get("function")))
                .map(each -> String.valueOf(each.get("name")))
                .collect(Collectors.toSet());
        for (int turnIndex = 0; turnIndex < maxTurns; turnIndex++) {
            LLMChatCompletion completion;
            try {
                completion = llmChatClient.complete(messages, toolDefinitions, "auto", false);
            } catch (final IOException | IllegalStateException ex) {
                return artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure("model_service_unavailable", ex.getMessage()));
            }
            artifacts.addRawModelOutput(completion.getRawResponse());
            if (completion.getToolCalls().isEmpty()) {
                return createFinalResult(scenario, completion.getContent(), artifacts);
            }
            messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
            Optional<Result> failure = executeToolCalls(scenario, turnIndex + 1, completion.getToolCalls(), messages, artifacts, availableToolNames);
            if (failure.isPresent()) {
                return failure.get();
            }
        }
        return artifacts.createResult(scenario, modelName,
                LLME2EAssertionReport.failure("turn_limit_exhausted", "Model did not produce a final answer within the configured turn limit."));
    }
    
    private Optional<Result> executeToolCalls(final Scenario scenario, final int modelTurn, final List<LLMToolCall> toolCalls, final List<LLMChatMessage> messages,
                                              final ConversationArtifacts artifacts, final Set<String> availableToolNames) throws InterruptedException {
        for (LLMToolCall each : toolCalls) {
            if (!availableToolNames.contains(each.getName())) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), modelTurn, TOOL_CALL_KIND, each.getName(),
                        Map.of("rawArgumentsJson", each.getArgumentsJson()), "unexpected_tool_requested"));
                return Optional.of(artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure("unexpected_tool_requested", "Model requested a tool that was not advertised for this scenario.")));
            }
            Map<String, Object> arguments;
            try {
                arguments = LLMMCPJsonValues.parseToolArguments(each.getArgumentsJson());
            } catch (final IllegalArgumentException ex) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), modelTurn, TOOL_CALL_KIND, each.getName(),
                        Map.of("rawArgumentsJson", each.getArgumentsJson()), "invalid_tool_arguments"));
                return Optional.of(artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure("invalid_tool_arguments", "Model returned invalid tool arguments JSON.")));
            }
            Optional<LLMMCPSafetyValidator.ValidationFailure> validationFailure = safetyValidator.validate(each.getName(), arguments);
            if (validationFailure.isPresent()) {
                LLMMCPSafetyValidator.ValidationFailure failure = validationFailure.get();
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(
                        artifacts.nextSequence(), modelTurn, getActionKind(each.getName()), each.getName(), arguments, failure.failureType()));
                return Optional.of(artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure(failure.failureType(), failure.message())));
            }
            long startTime = System.currentTimeMillis();
            Map<String, Object> response;
            try {
                response = executeAction(each.getName(), arguments);
            } catch (final IllegalArgumentException ex) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(
                        artifacts.nextSequence(), modelTurn, TOOL_CALL_KIND, each.getName(), arguments, "invalid_tool_arguments"));
                return Optional.of(artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure("invalid_tool_arguments", ex.getMessage())));
            } catch (final IllegalStateException ex) {
                return Optional.of(artifacts.createResult(scenario, modelName,
                        LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage())));
            }
            long latencyMillis = System.currentTimeMillis() - startTime;
            artifacts.addTrace(new MCPInteractionTraceRecord(
                    artifacts.nextSequence(), modelTurn, getActionKind(each.getName()), MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN,
                    each.getName(), getTraceArguments(each.getName(), arguments), response, true, latencyMillis));
            messages.add(LLMChatMessage.tool(each.getId(), JsonUtils.toJsonString(response)));
        }
        return Optional.empty();
    }
    
    private String getActionKind(final String toolName) {
        return READ_RESOURCE_TOOL_NAME.equals(toolName) ? RESOURCE_READ_KIND : TOOL_CALL_KIND;
    }
    
    private Map<String, Object> executeAction(final String actionName, final Map<String, Object> arguments) throws InterruptedException {
        try {
            return READ_RESOURCE_TOOL_NAME.equals(actionName)
                    ? mcpInteractionClient.readResource(getRequiredResourceUri(arguments))
                    : mcpInteractionClient.call(actionName, arguments);
        } catch (final IOException | IllegalStateException ex) {
            throw new IllegalStateException(String.format("MCP action `%s` failed: %s", actionName, ex.getMessage()), ex);
        }
    }
    
    private String getRequiredResourceUri(final Map<String, Object> arguments) {
        Object value = arguments.get("uri");
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) {
            throw new IllegalArgumentException("Resource URI is required.");
        }
        return ((String) value).trim();
    }
    
    private Map<String, Object> getTraceArguments(final String toolName, final Map<String, Object> arguments) {
        return READ_RESOURCE_TOOL_NAME.equals(toolName)
                ? Map.of("uri", Objects.toString(arguments.get("uri"), "").trim())
                : arguments;
    }
    
    private Result createFinalResult(final Scenario scenario, final String actualAnswer, final ConversationArtifacts artifacts) {
        artifacts.setActualAnswer(actualAnswer.trim());
        if (artifacts.getTrace().stream().noneMatch(MCPInteractionTraceRecord::isValid)) {
            return artifacts.createResult(scenario, modelName,
                    LLME2EAssertionReport.failure("missing_mcp_evidence", "Model returned an answer without retrieving MCP evidence."));
        }
        LLME2EAssertionReport assertionReport;
        try {
            assertionReport = scenario.evaluator().apply(artifacts.getActualAnswer(), artifacts.getTrace());
        } catch (final IllegalArgumentException | IllegalStateException | ClassCastException ex) {
            assertionReport = LLME2EAssertionReport.failure("invalid_scenario_evidence", ex.getMessage());
        }
        return artifacts.createResult(scenario, modelName, assertionReport);
    }
    
    /**
     * LLM scenario.
     *
     * @param id scenario ID
     * @param question question
     * @param allowedToolNames tools exposed only to this scenario
     * @param evaluator scenario evidence evaluator
     */
    public record Scenario(String id, String question, Set<String> allowedToolNames,
                           BiFunction<String, List<MCPInteractionTraceRecord>, LLME2EAssertionReport> evaluator) {
    }
    
    /**
     * Autonomous conversation result.
     *
     * @param scenario scenario
     * @param systemPrompt system prompt
     * @param modelProvider model provider
     * @param modelName model name
     * @param actualAnswer actual answer
     * @param evidence model and MCP evidence
     * @param assertionReport assertion report
     */
    public record Result(Scenario scenario, String systemPrompt, String modelProvider, String modelName, String actualAnswer,
                         Evidence evidence, LLME2EAssertionReport assertionReport) {
    }
    
    /**
     * Model and MCP evidence captured for one conversation.
     *
     * @param rawModelOutputs raw model outputs
     * @param toolDefinitions tool definitions derived from MCP discovery
     * @param interactionTrace interaction trace
     */
    public record Evidence(List<String> rawModelOutputs, List<Map<String, Object>> toolDefinitions,
                           List<MCPInteractionTraceRecord> interactionTrace) {
    }
    
    private static final class ConversationArtifacts {
        
        private String actualAnswer = "";
        
        private final List<String> rawModelOutputs = new LinkedList<>();
        
        private List<Map<String, Object>> toolDefinitions = List.of();
        
        private final List<MCPInteractionTraceRecord> trace = new LinkedList<>();
        
        private int nextSequence() {
            return trace.size() + 1;
        }
        
        private void setActualAnswer(final String actualAnswer) {
            this.actualAnswer = actualAnswer;
        }
        
        private String getActualAnswer() {
            return actualAnswer;
        }
        
        private void addRawModelOutput(final String rawModelOutput) {
            rawModelOutputs.add(rawModelOutput);
        }
        
        private void setToolDefinitions(final List<Map<String, Object>> toolDefinitions) {
            this.toolDefinitions = toolDefinitions;
        }
        
        private void addTrace(final MCPInteractionTraceRecord traceRecord) {
            trace.add(traceRecord);
        }
        
        private List<MCPInteractionTraceRecord> getTrace() {
            return trace;
        }
        
        private Result createResult(final Scenario scenario, final String modelName, final LLME2EAssertionReport assertionReport) {
            return new Result(scenario, SYSTEM_PROMPT, LLME2EConfiguration.MODEL_PROVIDER, modelName, actualAnswer,
                    new Evidence(rawModelOutputs, toolDefinitions, trace), assertionReport);
        }
    }
}
