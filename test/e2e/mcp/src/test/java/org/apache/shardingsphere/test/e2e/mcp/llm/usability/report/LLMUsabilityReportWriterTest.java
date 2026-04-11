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

package org.apache.shardingsphere.test.e2e.mcp.llm.usability.report;

import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityDimension;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScenarioResult;
import org.apache.shardingsphere.test.e2e.mcp.llm.usability.model.LLMUsabilityScorecard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LLMUsabilityReportWriterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWriteScorecard() throws IOException {
        LLMUsabilityScorecard scorecard = new LLMUsabilityScorecard("suite-a", "run-a", 1.0D, 1.0D, 0.0D, 2.0D,
                1.0D, 0.0D, 1.0D, 1.0D, List.of(), List.of(
                        new LLMUsabilityScenarioResult("scenario-a", LLMUsabilityDimension.QUERY, "h2", true, "", "ok", true, 0, 2, true, true, 1.0D, false, false, List.of())));
        
        LLMUsabilityReportWriter actual = new LLMUsabilityReportWriter();
        actual.writeScorecard(tempDir, scorecard);
        
        assertTrue(Files.isRegularFile(tempDir.resolve("scorecard.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("scenario-results.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("summary.md")));
        assertTrue(Files.readString(tempDir.resolve("summary.md")).startsWith("# LLM Usability Scorecard"));
    }
}
