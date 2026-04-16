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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * LLM chat message.
 */
@RequiredArgsConstructor
@Getter
public final class LLMChatMessage {
    
    private final String role;
    
    private final String content;
    
    private final String toolCallId;
    
    private final List<LLMToolCall> toolCalls;
    
    /**
     * Get system message.
     *
     * @param content content
     * @return system message
     */
    public static LLMChatMessage system(final String content) {
        return new LLMChatMessage("system", content, "", List.of());
    }
    
    /**
     * Get user message.
     *
     * @param content content
     * @return user message
     */
    public static LLMChatMessage user(final String content) {
        return new LLMChatMessage("user", content, "", List.of());
    }
    
    /**
     * Get assistant message.
     *
     * @param content content
     * @param toolCalls tool calls
     * @return assistant message
     */
    public static LLMChatMessage assistant(final String content, final List<LLMToolCall> toolCalls) {
        return new LLMChatMessage("assistant", content, "", toolCalls);
    }
    
    /**
     * Get tool message.
     *
     * @param toolCallId tool call ID
     * @param content content
     * @return tool message
     */
    public static LLMChatMessage tool(final String toolCallId, final String content) {
        return new LLMChatMessage("tool", content, toolCallId, List.of());
    }
}
