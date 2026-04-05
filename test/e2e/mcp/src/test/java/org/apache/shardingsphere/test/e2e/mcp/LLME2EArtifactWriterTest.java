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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

class LLME2EArtifactWriterTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertWrite() throws IOException {
        final LLME2EArtifactBundle artifactBundle = new LLME2EArtifactBundle("scenario-a", "system prompt", "user prompt", "{\"database\":\"logic_db\"}",
                List.of("{\"content\":\"a\"}"), List.of(new MCPToolTraceRecord(1, "list_tables", Map.of("database", "logic_db"), Map.of("items", List.of()))),
                List.of("tool=list_tables"), LLME2EAssertionReport.success("ok"));
        
        new LLME2EArtifactWriter().write(tempDir, artifactBundle);
        
        assertTrue(Files.isRegularFile(tempDir.resolve("system-prompt.md")));
        assertTrue(Files.isRegularFile(tempDir.resolve("user-prompt.md")));
        assertTrue(Files.isRegularFile(tempDir.resolve("tool-trace.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("assertion-report.json")));
        assertTrue(Files.isRegularFile(tempDir.resolve("mcp-runtime.log")));
        assertTrue(Files.isRegularFile(tempDir.resolve("final-answer.json")));
        assertThat(Files.readString(tempDir.resolve("final-answer.json")), containsString("\"logic_db\""));
    }
}
