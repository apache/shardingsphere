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

import lombok.Getter;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact.LLME2EArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.client.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

/**
 * Execute one LLM conversation scenario and persist the generated artifacts.
 */
public final class LLMConversationExecutor {
    
    @Getter
    private final LLME2EConfiguration configuration = LLME2EConfiguration.load();
    
    private final LLME2EArtifactWriter artifactWriter = new LLME2EArtifactWriter();
    
    /**
     * Run one conversation scenario.
     *
     * @param scenarioId scenario id
     * @param scenario scenario
     * @param interactionClient MCP interaction client
     * @return conversation result
     * @throws IOException IO exception
     */
    public ConversationResult runConversation(final String scenarioId, final LLME2EScenario scenario,
                                              final MCPInteractionClient interactionClient) throws IOException {
        LLME2EArtifactBundle artifactBundle = new LLMMCPConversationRunner(
                configuration.getMaxTurns(), new LLMChatModelClient(configuration, HttpClient.newHttpClient()), interactionClient).run(scenario);
        Path artifactDirectory = configuration.createArtifactDirectory(scenarioId);
        artifactWriter.write(artifactDirectory, artifactBundle);
        return new ConversationResult(artifactBundle, artifactDirectory);
    }
    
    /**
     * Check whether LLM E2E is enabled.
     *
     * @return whether LLM E2E is enabled
     */
    public boolean isEnabled() {
        return configuration.isEnabled();
    }
    
    /**
     * Conversation result.
     *
     * @param artifactBundle artifact bundle
     * @param artifactDirectory artifact directory
     */
    public record ConversationResult(LLME2EArtifactBundle artifactBundle, Path artifactDirectory) {
    }
}
