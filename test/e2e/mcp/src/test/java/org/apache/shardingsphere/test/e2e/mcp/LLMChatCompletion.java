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

import java.util.List;

final class LLMChatCompletion {
    
    private final String content;
    
    private final List<LLMToolCall> toolCalls;
    
    private final String rawResponse;
    
    LLMChatCompletion(final String content, final List<LLMToolCall> toolCalls, final String rawResponse) {
        this.content = content;
        this.toolCalls = List.copyOf(toolCalls);
        this.rawResponse = rawResponse;
    }
    
    String content() {
        return content;
    }
    
    List<LLMToolCall> toolCalls() {
        return toolCalls;
    }
    
    String rawResponse() {
        return rawResponse;
    }
}
