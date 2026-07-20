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

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog.EvaluationCase;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Run one autonomous MCP Builder evaluation case without harness corrections.
 */
public final class AutonomousMCPBuilderEvaluationRunner {
    
    private static final String SYSTEM_PROMPT = """
            You are evaluating a live ShardingSphere MCP server in a read-only task. Use the available MCP functions to inspect current state.
            Choose tools from their advertised names, descriptions, and input schemas. Pass resource URIs only to mcp_read_resource, never as SQL.
            Never guess, request a write, or stop before retrieving the requested value. Use function calls for every tool invocation rather than
            printing a tool-call object as the answer. When you have enough evidence, follow the user's exact answer format and return only the answer
            with no explanation or Markdown.
            """;
    
    private static final List<String> BRIDGE_TOOL_NAMES = List.of(MCPInteractionActionNames.READ_RESOURCE);
    
    private final int maxTurns;
    
    private final LLMChatModelClient llmChatClient;
    
    private final MCPInteractionClient mcpInteractionClient;
    
    private final String modelProvider;
    
    private final String modelName;
    
    private final LLMMCPToolDefinitionFactory toolDefinitionFactory = new LLMMCPToolDefinitionFactory();
    
    private final LLMMCPTraceRecordFactory traceRecordFactory = new LLMMCPTraceRecordFactory();
    
    private final LLMMCPActionExecutor actionExecutor;
    
    public AutonomousMCPBuilderEvaluationRunner(final int maxTurns, final LLMChatModelClient llmChatClient, final MCPInteractionClient mcpInteractionClient,
                                                final String modelProvider, final String modelName) {
        this.maxTurns = maxTurns;
        this.llmChatClient = llmChatClient;
        this.mcpInteractionClient = mcpInteractionClient;
        this.modelProvider = modelProvider;
        this.modelName = modelName;
        actionExecutor = new LLMMCPActionExecutor(mcpInteractionClient);
    }
    
    /**
     * Run one evaluation case. The supplied MCP interaction client is opened and closed for this invocation.
     *
     * @param evaluationCase evaluation case
     * @return evaluation result
     */
    public EvaluationResult run(final EvaluationCase evaluationCase) {
        EvaluationArtifacts artifacts = new EvaluationArtifacts();
        try {
            mcpInteractionClient.open();
            List<Map<String, Object>> advertisedTools = mcpInteractionClient.listTools();
            List<Map<String, Object>> toolDefinitions = toolDefinitionFactory.createReadOnlyFromRemote(advertisedTools, BRIDGE_TOOL_NAMES);
            artifacts.setToolDefinitions(toolDefinitions);
            artifacts.addTrace(traceRecordFactory.createTraceRecord(artifacts.nextSequence(), MCPInteractionActionNames.LIST_TOOLS,
                    MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN, Map.of(), Map.of("tools", advertisedTools), 0L));
            return runTurns(evaluationCase, artifacts, toolDefinitions);
        } catch (final IOException ex) {
            return artifacts.createResult(evaluationCase, modelProvider, modelName, LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage()));
        } catch (final InterruptedException ex) {
            Thread.currentThread().interrupt();
            return artifacts.createResult(evaluationCase, modelProvider, modelName, LLME2EAssertionReport.failure("evaluation_interrupted", "Evaluation was interrupted."));
        } catch (final IllegalStateException ex) {
            return artifacts.createResult(evaluationCase, modelProvider, modelName, LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage()));
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
    
    private EvaluationResult runTurns(final EvaluationCase evaluationCase, final EvaluationArtifacts artifacts,
                                      final List<Map<String, Object>> toolDefinitions) throws InterruptedException {
        List<LLMChatMessage> messages = new LinkedList<>();
        messages.add(LLMChatMessage.system(SYSTEM_PROMPT));
        messages.add(LLMChatMessage.user(evaluationCase.question()));
        for (int i = 0; i < maxTurns; i++) {
            LLMChatCompletion completion;
            try {
                completion = llmChatClient.complete(messages, toolDefinitions, "auto", false);
            } catch (final IOException | IllegalStateException ex) {
                return artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("model_service_unavailable", ex.getMessage()));
            }
            artifacts.addRawModelOutput(completion.getRawResponse());
            if (completion.getToolCalls().isEmpty()) {
                return createFinalResult(evaluationCase, completion.getContent(), artifacts);
            }
            messages.add(LLMChatMessage.assistant(completion.getContent(), completion.getToolCalls()));
            Optional<EvaluationResult> failure = executeToolCalls(evaluationCase, completion.getToolCalls(), messages, artifacts, toolDefinitions);
            if (failure.isPresent()) {
                return failure.get();
            }
        }
        return artifacts.createResult(evaluationCase, modelProvider, modelName,
                LLME2EAssertionReport.failure("turn_limit_exhausted", "Model did not produce a final answer within the configured turn limit."));
    }
    
    private Optional<EvaluationResult> executeToolCalls(final EvaluationCase evaluationCase, final List<LLMToolCall> toolCalls, final List<LLMChatMessage> messages,
                                                        final EvaluationArtifacts artifacts, final List<Map<String, Object>> toolDefinitions) throws InterruptedException {
        Set<String> availableToolNames = toolDefinitions.stream()
                .map(each -> LLMMCPJsonValues.castToMap(each.get("function")))
                .map(each -> String.valueOf(each.get("name")))
                .collect(Collectors.toSet());
        for (LLMToolCall each : toolCalls) {
            if (!availableToolNames.contains(each.getName())) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", each.getName(),
                        Map.of("rawArgumentsJson", each.getArgumentsJson()), "unexpected_tool_requested"));
                return Optional.of(artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("unexpected_tool_requested", "Model requested a tool that was not advertised for this evaluation.")));
            }
            Map<String, Object> arguments;
            try {
                arguments = LLMMCPJsonValues.parseToolArguments(each.getArgumentsJson());
            } catch (final IllegalArgumentException ex) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", each.getName(),
                        Map.of("rawArgumentsJson", each.getArgumentsJson()), "invalid_tool_arguments"));
                return Optional.of(artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("invalid_tool_arguments", "Model returned invalid tool arguments JSON.")));
            }
            long startTime = System.currentTimeMillis();
            Map<String, Object> response;
            try {
                response = actionExecutor.executeSafely(each.getName(), arguments);
            } catch (final IllegalArgumentException ex) {
                artifacts.addTrace(MCPInteractionTraceRecord.createInvalidAction(artifacts.nextSequence(), "tool_call", each.getName(), arguments, "invalid_tool_arguments"));
                return Optional.of(artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("invalid_tool_arguments", ex.getMessage())));
            } catch (final IllegalStateException ex) {
                return Optional.of(artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("mcp_runtime_unavailable", ex.getMessage())));
            }
            long latencyMillis = System.currentTimeMillis() - startTime;
            artifacts.addTrace(traceRecordFactory.createTraceRecord(artifacts.nextSequence(), each.getName(), MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN,
                    arguments, response, latencyMillis));
            artifacts.addRuntimeLogLine("action=" + each.getName() + " args=" + JsonUtils.toJsonString(arguments));
            artifacts.addRuntimeLogLine("response=" + JsonUtils.toJsonString(response));
            messages.add(LLMChatMessage.tool(each.getId(), JsonUtils.toJsonString(response)));
        }
        return Optional.empty();
    }
    
    private EvaluationResult createFinalResult(final EvaluationCase evaluationCase, final String actualAnswer, final EvaluationArtifacts artifacts) {
        artifacts.setActualAnswer(actualAnswer.trim());
        if (artifacts.getTrace().stream().noneMatch(each -> !MCPInteractionActionNames.LIST_TOOLS.equals(each.getTargetName()) && each.isValid())) {
            return artifacts.createResult(evaluationCase, modelProvider, modelName,
                    LLME2EAssertionReport.failure("missing_mcp_evidence", "Model returned an answer without retrieving MCP evidence."));
        }
        return evaluationCase.answer().equals(artifacts.getActualAnswer())
                ? artifacts.createResult(evaluationCase, modelProvider, modelName, LLME2EAssertionReport.success("Answer matched the live MCP evidence."))
                : artifacts.createResult(evaluationCase, modelProvider, modelName,
                        LLME2EAssertionReport.failure("answer_mismatch", "Final answer did not match the expected value."));
    }
    
    /**
     * Autonomous evaluation result.
     *
     * @param evaluationCase evaluation case
     * @param systemPrompt system prompt
     * @param modelProvider model provider
     * @param modelName model name
     * @param actualAnswer actual answer
     * @param evidence model and MCP evidence
     * @param assertionReport assertion report
     */
    public record EvaluationResult(EvaluationCase evaluationCase, String systemPrompt, String modelProvider, String modelName, String actualAnswer,
                                   EvaluationEvidence evidence, LLME2EAssertionReport assertionReport) {
    }
    
    /**
     * Model and MCP evidence captured for one evaluation case.
     *
     * @param rawModelOutputs raw model outputs
     * @param toolDefinitions tool definitions derived from MCP discovery
     * @param interactionTrace interaction trace
     * @param runtimeLogLines runtime log lines
     */
    public record EvaluationEvidence(List<String> rawModelOutputs, List<Map<String, Object>> toolDefinitions,
                                     List<MCPInteractionTraceRecord> interactionTrace, List<String> runtimeLogLines) {
    }
    
    private static final class EvaluationArtifacts {
        
        private String actualAnswer = "";
        
        private final List<String> rawModelOutputs = new LinkedList<>();
        
        private List<Map<String, Object>> toolDefinitions = List.of();
        
        private final List<MCPInteractionTraceRecord> trace = new LinkedList<>();
        
        private final List<String> runtimeLogLines = new LinkedList<>();
        
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
        
        private void addRuntimeLogLine(final String runtimeLogLine) {
            runtimeLogLines.add(runtimeLogLine);
        }
        
        private EvaluationResult createResult(final EvaluationCase evaluationCase, final String modelProvider, final String modelName,
                                              final LLME2EAssertionReport assertionReport) {
            return new EvaluationResult(evaluationCase, SYSTEM_PROMPT, modelProvider, modelName, actualAnswer,
                    new EvaluationEvidence(rawModelOutputs, toolDefinitions, trace, runtimeLogLines), assertionReport);
        }
    }
}
