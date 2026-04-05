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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class LLME2EConfigurationTest {
    
    @TempDir
    private Path tempDir;
    
    @AfterEach
    void tearDown() {
        System.clearProperty("mcp.llm.e2e.enabled");
        System.clearProperty("mcp.llm.base-url");
        System.clearProperty("mcp.llm.model");
        System.clearProperty("mcp.llm.api-key");
        System.clearProperty("mcp.llm.ready-timeout-seconds");
        System.clearProperty("mcp.llm.request-timeout-seconds");
        System.clearProperty("mcp.llm.max-turns");
        System.clearProperty("mcp.llm.artifact-root");
        System.clearProperty("mcp.llm.run-id");
    }
    
    @Test
    void assertLoad() throws IOException {
        System.setProperty("mcp.llm.e2e.enabled", "true");
        System.setProperty("mcp.llm.base-url", "http://127.0.0.1:11434/v1/");
        System.setProperty("mcp.llm.model", "mock-model");
        System.setProperty("mcp.llm.api-key", "mock-key");
        System.setProperty("mcp.llm.ready-timeout-seconds", "5");
        System.setProperty("mcp.llm.request-timeout-seconds", "7");
        System.setProperty("mcp.llm.max-turns", "9");
        System.setProperty("mcp.llm.artifact-root", tempDir.toString());
        System.setProperty("mcp.llm.run-id", "fixed-run");
        
        final LLME2EConfiguration actual = LLME2EConfiguration.load();
        
        assertTrue(actual.enabled());
        assertThat(actual.baseUrl(), is("http://127.0.0.1:11434/v1"));
        assertThat(actual.modelName(), is("mock-model"));
        assertThat(actual.apiKey(), is("mock-key"));
        assertThat(actual.readyTimeoutSeconds(), is(5));
        assertThat(actual.requestTimeoutSeconds(), is(7));
        assertThat(actual.maxTurns(), is(9));
        assertThat(actual.runId(), is("fixed-run"));
        assertThat(actual.getChatCompletionsUrl(), is("http://127.0.0.1:11434/v1/chat/completions"));
        assertThat(actual.getModelsUrl(), is("http://127.0.0.1:11434/v1/models"));
        final Path artifactDirectory = actual.createArtifactDirectory("scenario-a");
        assertNotNull(artifactDirectory);
        assertTrue(Files.isDirectory(artifactDirectory));
    }
}
