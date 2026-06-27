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
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatMessage;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMToolCall;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
abstract class AbstractLLMMCPConversationRunnerTest {
    
    protected static final String DATABASE_NAME = "logic_db";
    
    protected static final String SCHEMA_NAME = "public";
    
    protected static final String TABLE_NAME = "orders";
    
    protected static final String QUERY = "SELECT COUNT(*) AS total_orders FROM orders";
    
    protected static final String RESOURCE_URI = "shardingsphere://capabilities";
    
    protected static final String PROMPT_NAME = "inspect_metadata";
    
    @Mock
    private LLMChatModelClient llmChatClient;
    
    @Mock
    private MCPInteractionClient mcpInteractionClient;
    
    protected LLMChatModelClient getLLMChatClient() {
        return llmChatClient;
    }
    
    protected MCPInteractionClient getMCPInteractionClient() {
        return mcpInteractionClient;
    }
    
    protected LLMMCPConversationRunner createRunner(final int maxTurns) {
        return new LLMMCPConversationRunner(maxTurns, llmChatClient, mcpInteractionClient);
    }
    
    protected LLME2EScenario createScenario(final List<String> toolNames) {
        return createScenario(toolNames, "user-prompt");
    }
    
    protected LLME2EScenario createScenario(final List<String> toolNames, final String userPrompt) {
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer(DATABASE_NAME, SCHEMA_NAME, TABLE_NAME, QUERY, 2, toolNames);
        return new LLME2EScenario("scenario-id", "system-prompt", userPrompt, expectedAnswer, toolNames, toolNames);
    }
    
    protected Map<String, Object> createExecuteQueryArguments(final String sql) {
        return Map.of("database", DATABASE_NAME, "schema", SCHEMA_NAME, "sql", sql, "max_rows", 10);
    }
    
    protected LLMChatCompletion createToolCallCompletion(final String toolCallId, final String toolName, final Map<String, Object> arguments, final String rawResponse) {
        return new LLMChatCompletion("", List.of(new LLMToolCall(toolCallId, toolName, JsonUtils.toJsonString(arguments))), rawResponse);
    }
    
    protected LLMChatCompletion createFinalAnswerCompletion(final List<String> interactionSequence, final Object totalOrders, final String rawResponse) {
        return new LLMChatCompletion(JsonUtils.toJsonString(createFinalAnswerPayload(interactionSequence, totalOrders)), List.of(), rawResponse);
    }
    
    protected boolean containsMessage(final List<LLMChatMessage> messages, final String expectedContent) {
        return messages.stream().map(LLMChatMessage::getContent).anyMatch(each -> each.contains(expectedContent));
    }
    
    protected Map<String, Object> createResultSetPayload(final Object totalOrders) {
        return Map.of("result_kind", "result_set", "rows", List.of(List.of(totalOrders)));
    }
    
    protected Map<String, Object> createFinalAnswerPayload(final List<String> interactionSequence, final Object totalOrders) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("database", DATABASE_NAME);
        result.put("schema", SCHEMA_NAME);
        result.put("table", TABLE_NAME);
        result.put("query", QUERY);
        result.put("totalOrders", totalOrders);
        result.put("interactionSequence", interactionSequence);
        return result;
    }
    
    protected static Map<String, Object> createMutatedFinalAnswerPayload(final String fieldName, final Object fieldValue) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("database", DATABASE_NAME);
        result.put("schema", SCHEMA_NAME);
        result.put("table", TABLE_NAME);
        result.put("query", QUERY);
        result.put("totalOrders", 2);
        result.put("interactionSequence", List.of("database_gateway_execute_query"));
        result.put(fieldName, fieldValue);
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ArgumentCaptor<List<Map<String, Object>>> createToolDefinitionsCaptor() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected ArgumentCaptor<List<LLMChatMessage>> createChatMessagesCaptor() {
        return ArgumentCaptor.forClass((Class) List.class);
    }
    
    protected List<List<LLMChatMessage>> captureRequiredChatMessages(final int expectedInvocations) throws IOException, InterruptedException {
        ArgumentCaptor<List<LLMChatMessage>> result = createChatMessagesCaptor();
        verify(llmChatClient, times(expectedInvocations)).complete(result.capture(), anyList(), eq("required"), eq(false));
        return result.getAllValues();
    }
    
    protected List<List<Map<String, Object>>> captureRequiredToolDefinitions(final int expectedInvocations) throws IOException, InterruptedException {
        ArgumentCaptor<List<Map<String, Object>>> result = createToolDefinitionsCaptor();
        verify(llmChatClient, times(expectedInvocations)).complete(anyList(), result.capture(), eq("required"), eq(false));
        return result.getAllValues();
    }
    
    protected List<List<LLMChatMessage>> captureAutoChatMessages() throws IOException, InterruptedException {
        ArgumentCaptor<List<LLMChatMessage>> result = createChatMessagesCaptor();
        verify(llmChatClient).complete(result.capture(), anyList(), eq("auto"), eq(false));
        return result.getAllValues();
    }
    
    protected List<List<LLMChatMessage>> captureFinalChatMessages() throws IOException, InterruptedException {
        ArgumentCaptor<List<LLMChatMessage>> result = createChatMessagesCaptor();
        verify(llmChatClient).complete(result.capture(), eq(List.of()), eq("none"), eq(true));
        return result.getAllValues();
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getFunction(final Map<String, Object> toolDefinition) {
        return (Map<String, Object>) toolDefinition.get("function");
    }
    
    protected String getToolName(final Map<String, Object> toolDefinition) {
        return String.valueOf(getFunction(toolDefinition).get("name"));
    }
    
    protected List<String> getToolNames(final List<Map<String, Object>> toolDefinitions) {
        return toolDefinitions.stream().map(this::getToolName).toList();
    }
    
    protected List<String> getRequiredFields(final Map<String, Object> toolDefinition) {
        return castToStringList(getMap(getFunction(toolDefinition).get("parameters")).get("required"));
    }
    
    protected String getPropertyType(final Map<String, Object> propertyDefinition) {
        return String.valueOf(propertyDefinition.get("type"));
    }
    
    protected String getPropertyType(final Map<String, Object> toolDefinition, final String propertyName) {
        return getPropertyType(getPropertyDefinition(toolDefinition, propertyName));
    }
    
    protected String getNestedPropertyType(final Map<String, Object> toolDefinition, final String propertyName, final String nestedPropertyName) {
        return getPropertyType(getMap(getPropertyDefinition(toolDefinition, propertyName).get(nestedPropertyName)));
    }
    
    protected Map<String, Object> getPropertyDefinition(final Map<String, Object> toolDefinition, final String propertyName) {
        return getMap(getProperty(toolDefinition, propertyName));
    }
    
    protected Object getProperty(final Map<String, Object> toolDefinition, final String propertyName) {
        return getMap(getMap(getFunction(toolDefinition).get("parameters")).get("properties")).get(propertyName);
    }
    
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getMap(final Object value) {
        return (Map<String, Object>) value;
    }
    
    @SuppressWarnings("unchecked")
    protected List<String> castToStringList(final Object value) {
        return (List<String>) value;
    }
}
