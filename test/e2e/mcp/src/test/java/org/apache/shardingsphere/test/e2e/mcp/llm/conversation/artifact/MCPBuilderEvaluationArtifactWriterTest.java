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

package org.apache.shardingsphere.test.e2e.mcp.llm.conversation.artifact;

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner.EvaluationResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.AutonomousMCPBuilderEvaluationRunner.EvaluationEvidence;
import org.apache.shardingsphere.test.e2e.mcp.llm.suite.MCPBuilderEvaluationCatalog.EvaluationCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPBuilderEvaluationArtifactWriterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteRedactsEvaluationArtifacts() throws IOException {
        EvaluationResult result = new EvaluationResult(
                new EvaluationCase("q01", "aggregation", true, "Count rows.", "2"),
                "system", "provider", "model", "2",
                new EvaluationEvidence(List.of("{\"password\":\"fixture-domain-secret\"}"), List.of(), List.of(), List.of()),
                LLME2EAssertionReport.success("ok"));
        MCPBuilderEvaluationArtifactWriter writer = new MCPBuilderEvaluationArtifactWriter();
        writer.write(tempDir, result, Map.of("scoreClosing", false));
        writer.writeScorecard(tempDir.resolve("scorecard.json"), List.of(result));
        assertThat(Files.readString(tempDir.resolve("raw-model-output.txt")), is("{\"password\":\"<redacted>\"}"));
        assertTrue(Files.isRegularFile(tempDir.resolve("available-tools.json")));
        assertTrue(Files.readString(tempDir.resolve("scorecard.json")).contains("\"score\":100.0"));
    }
    
    @Test
    void assertWriteRejectsIncompleteScoreClosingEvidence() {
        EvaluationResult result = new EvaluationResult(
                new EvaluationCase("q01", "aggregation", true, "Count rows.", "2"),
                "system", "provider", "model", "2", new EvaluationEvidence(List.of(), List.of(), List.of(), List.of()), LLME2EAssertionReport.success("ok"));
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> new MCPBuilderEvaluationArtifactWriter().write(tempDir, result, Map.of("scoreClosing", true)));
        assertTrue(actual.getMessage().startsWith("Missing score-closing LLM runtime evidence field"));
    }
}
