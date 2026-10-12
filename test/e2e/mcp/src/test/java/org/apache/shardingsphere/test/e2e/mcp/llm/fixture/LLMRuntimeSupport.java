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

package org.apache.shardingsphere.test.e2e.mcp.llm.fixture;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration.RuntimeMode;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.DockerRuntimeTestSupport;

import java.net.http.HttpClient;

/**
 * Docker Model Runner readiness support for MCP LLM E2E.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LLMRuntimeSupport {
    
    /**
     * Check the LLM runtime prepared by the Docker Model Runner CLI.
     *
     * @param config LLM E2E configuration
     * @throws IllegalStateException when the runtime is unavailable
     * @throws InterruptedException interrupted exception
     */
    public static void prepare(final LLME2EConfiguration config) throws InterruptedException {
        if (RuntimeMode.EXTERNAL_DEBUG == config.getRuntimeMode()) {
            if (!isModelReady(config)) {
                throw new IllegalStateException("MCP LLM external-debug mode requires a ready OpenAI-compatible endpoint.");
            }
            return;
        }
        DockerRuntimeTestSupport.requireAvailable("Docker is required to run Docker Model Runner for MCP LLM E2E.");
        new LLMChatModelClient(config, HttpClient.newHttpClient()).waitUntilReady();
    }
    
    private static boolean isModelReady(final LLME2EConfiguration config) throws InterruptedException {
        try {
            new LLMChatModelClient(config.withReadinessTimeouts(2, 2), HttpClient.newHttpClient()).waitUntilReady();
            return true;
        } catch (final IllegalStateException ignored) {
            return false;
        }
    }
}
