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

package org.apache.shardingsphere.test.e2e.mcp.llm.smoke;

import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactBundle;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EArtifactWriter;
import org.apache.shardingsphere.test.e2e.mcp.llm.artifact.LLME2EAssertionReport;
import org.apache.shardingsphere.test.e2e.mcp.llm.chat.LLMChatModelClient;
import org.apache.shardingsphere.test.e2e.mcp.llm.config.LLME2EConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMMCPConversationRunner;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.production.AbstractProductionRuntimeE2ETest;
import org.junit.jupiter.api.Assumptions;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractLLMMCPE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private static final String SYSTEM_PROMPT_RESOURCE = "llm/minimal-smoke-system-prompt.md";
    
    private static final String USER_PROMPT_RESOURCE = "llm/minimal-smoke-user-prompt.md";
    
    private static final List<String> SMOKE_INTERACTION_SEQUENCE = List.of("search_metadata", "mcp_read_resource", "execute_query");
    
    private final LLME2EConfiguration llmConfiguration = LLME2EConfiguration.load();
    
    private final LLME2EArtifactWriter artifactWriter = new LLME2EArtifactWriter();
    
    protected final void assertLLMSmoke(final Supplier<LLME2EScenario> scenarioSupplier) throws IOException {
        Assumptions.assumeTrue(isLLMSmokeEnabled(),
                "Set -Dmcp.llm.e2e.enabled=true or MCP_LLM_E2E_ENABLED=true to run the LLM MCP smoke tests.");
        launchProductionRuntime();
        LLME2EScenario scenario = scenarioSupplier.get();
        LLME2EArtifactBundle artifactBundle = new LLMMCPConversationRunner(llmConfiguration.maxTurns(),
                new LLMChatModelClient(llmConfiguration, HttpClient.newHttpClient()), new MCPHttpInteractionClient(getEndpointUri(), createHttpClient())).run(scenario);
        Path artifactDirectory = llmConfiguration.createArtifactDirectory(scenario.scenarioId());
        artifactWriter.write(artifactDirectory, artifactBundle);
        LLME2EAssertionReport assertionReport = artifactBundle.assertionReport();
        assertTrue(assertionReport.success(),
                () -> String.format(Locale.ENGLISH, "%s: %s (artifacts: %s)", assertionReport.failureType(), assertionReport.message(), artifactDirectory));
    }
    
    protected final LLME2EScenario createMinimalSmokeScenario(final String scenarioId, final String databaseName,
                                                              final String schemaName, final String tableName,
                                                              final String query, final int totalOrders) {
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        String systemPrompt = loadResource(SYSTEM_PROMPT_RESOURCE);
        String userPrompt = String.format(Locale.ENGLISH, loadResource(USER_PROMPT_RESOURCE),
                databaseName, databaseName, schemaName, tableName, tableResourceUri, databaseName, schemaName, query,
                databaseName, schemaName, tableName, query, totalOrders);
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, SMOKE_INTERACTION_SEQUENCE);
        return new LLME2EScenario(scenarioId, systemPrompt, userPrompt, expectedAnswer, SMOKE_INTERACTION_SEQUENCE, SMOKE_INTERACTION_SEQUENCE);
    }
    
    protected final boolean isLLMSmokeEnabled() {
        return llmConfiguration.enabled();
    }
    
    private String loadResource(final String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (null == inputStream) {
                throw new IllegalStateException("Resource was not found: " + resourcePath);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to load resource: " + resourcePath, ex);
        }
    }
}
