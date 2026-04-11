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

package org.apache.shardingsphere.test.e2e.mcp.llm.stub;

import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatCompletion;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatMessage;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public final class StubLLMChatClient implements LLMChatClient {
    
    private final IOException waitUntilReadyFailure;
    
    private final Queue<LLMChatCompletion> completions;
    
    public StubLLMChatClient(final LLMChatCompletion... completions) {
        this(null, completions);
    }
    
    public StubLLMChatClient(final IOException waitUntilReadyFailure, final LLMChatCompletion... completions) {
        this.waitUntilReadyFailure = waitUntilReadyFailure;
        this.completions = new ArrayDeque<>(List.of(completions));
    }
    
    @Override
    public void waitUntilReady() throws IOException {
        if (null != waitUntilReadyFailure) {
            throw waitUntilReadyFailure;
        }
    }
    
    @Override
    public LLMChatCompletion complete(final List<LLMChatMessage> messages, final List<Map<String, Object>> tools,
                                      final String toolChoice, final boolean jsonResponse) {
        if (completions.isEmpty()) {
            throw new UnsupportedOperationException("No completion should be requested.");
        }
        return completions.remove();
    }
}
