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

package org.apache.shardingsphere.test.e2e.mcp.llm;

import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMMCPConversationRunner;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.AbstractConfigBackedRuntimeE2ETest;

import java.io.IOException;
import java.net.http.HttpClient;
import java.nio.file.Path;

public abstract class AbstractLLMRuntimeE2ETest extends AbstractConfigBackedRuntimeE2ETest {
    
    private final LLME2EConfiguration llmConfiguration = LLME2EConfiguration.load();
    
    private final LLME2EArtifactWriter artifactWriter = new LLME2EArtifactWriter();
    
    protected final LLME2EConversationResult runConversation(final String scenarioId, final LLME2EScenario scenario) throws IOException {
        LLME2EArtifactBundle artifactBundle = new LLMMCPConversationRunner(llmConfiguration.getMaxTurns(),
                new LLMChatModelClient(llmConfiguration, HttpClient.newHttpClient()), createInteractionClient()).run(scenario);
        Path artifactDirectory = llmConfiguration.createArtifactDirectory(scenarioId);
        artifactWriter.write(artifactDirectory, artifactBundle);
        return new LLME2EConversationResult(artifactBundle, artifactDirectory);
    }
    
    protected final LLME2EConfiguration getLLMConfiguration() {
        return llmConfiguration;
    }
    
    protected final boolean isLLMEnabled() {
        return llmConfiguration.isEnabled();
    }
    
    protected record LLME2EConversationResult(LLME2EArtifactBundle artifactBundle, Path artifactDirectory) {
    }
}
