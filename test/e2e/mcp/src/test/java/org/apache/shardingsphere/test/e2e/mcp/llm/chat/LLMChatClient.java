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

package org.apache.shardingsphere.test.e2e.mcp.llm.chat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * LLM chat client.
 */
public interface LLMChatClient {
    
    /**
     * Wait until ready.
     *
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    void waitUntilReady() throws IOException, InterruptedException;
    
    /**
     * Complete.
     *
     * @param messages messages
     * @param tools tools
     * @param toolChoice tool choice
     * @param jsonResponse json response
     * @return LLM chat completion
     * @throws IOException IO exception
     * @throws InterruptedException interrupted exception
     */
    LLMChatCompletion complete(List<LLMChatMessage> messages, List<Map<String, Object>> tools, String toolChoice, boolean jsonResponse) throws IOException, InterruptedException;
}
