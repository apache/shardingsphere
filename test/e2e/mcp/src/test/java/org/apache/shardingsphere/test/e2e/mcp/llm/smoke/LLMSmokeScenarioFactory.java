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

import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLME2EScenario;
import org.apache.shardingsphere.test.e2e.mcp.llm.scenario.LLMStructuredAnswer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

final class LLMSmokeScenarioFactory {
    
    private static final String SYSTEM_PROMPT_RESOURCE = "llm/minimal-smoke-system-prompt.md";
    
    private static final String USER_PROMPT_RESOURCE = "llm/minimal-smoke-user-prompt.md";
    
    private static final List<String> SMOKE_INTERACTION_SEQUENCE = List.of("search_metadata", "mcp_read_resource", "execute_query");
    
    LLME2EScenario createMinimalSmokeScenario(final String scenarioId, final String databaseName, final String schemaName,
                                              final String tableName, final String query, final int totalOrders) {
        String tableResourceUri = String.format(Locale.ENGLISH, "shardingsphere://databases/%s/schemas/%s/tables/%s", databaseName, schemaName, tableName);
        String systemPrompt = loadResource(SYSTEM_PROMPT_RESOURCE);
        String userPrompt = String.format(Locale.ENGLISH, loadResource(USER_PROMPT_RESOURCE),
                databaseName, databaseName, schemaName, tableName, tableResourceUri, databaseName, schemaName, query,
                databaseName, schemaName, tableName, query, totalOrders);
        LLMStructuredAnswer expectedAnswer = new LLMStructuredAnswer(databaseName, schemaName, tableName, query, totalOrders, SMOKE_INTERACTION_SEQUENCE);
        return new LLME2EScenario(scenarioId, systemPrompt, userPrompt, expectedAnswer, SMOKE_INTERACTION_SEQUENCE, SMOKE_INTERACTION_SEQUENCE);
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
