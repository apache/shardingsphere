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

package org.apache.shardingsphere.mcp.bootstrap;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDocumentationContractTest {
    
    @Test
    void assertHttpDockerAndPlaceholderDocs() throws IOException {
        String actualEnglish = Files.readString(resolveMCPDirectory().resolve("README.md"));
        String actualChinese = Files.readString(resolveMCPDirectory().resolve("README_ZH.md"));
        assertDocumentationIncludesRuntimeSafety(actualEnglish);
        assertDocumentationIncludesRuntimeSafety(actualChinese);
        assertFalse(actualEnglish.contains("accessToken: foo_token"));
        assertFalse(actualChinese.contains("accessToken: foo_token"));
    }
    
    @Test
    void assertDocumentationOmitsLegacyPayloadFields() throws IOException {
        String actual = Files.readString(resolveMCPDirectory().resolve("README.md")) + Files.readString(resolveMCPDirectory().resolve("README_ZH.md"));
        for (String each : List.of("pending_questions", "resource_uri", "parent_uri", "next_resource_uris", "read_resources_first", "empty_reason", "not_found_reason")) {
            assertFalse(actual.contains(each));
        }
    }
    
    private void assertDocumentationIncludesRuntimeSafety(final String content) {
        assertTrue(content.contains("transport.http.bindHost"));
        assertTrue(content.contains("transport.http.allowRemoteAccess"));
        assertTrue(content.contains("transport.http.accessToken"));
        assertTrue(content.contains("${ENV_NAME}"));
        assertTrue(content.contains("Authorization: Bearer <token>"));
    }
    
    private Path resolveMCPDirectory() {
        Path result = Path.of("").toAbsolutePath();
        while (null != result && !Files.exists(result.resolve("mcp/README.md"))) {
            result = result.getParent();
        }
        return result.resolve("mcp");
    }
}
