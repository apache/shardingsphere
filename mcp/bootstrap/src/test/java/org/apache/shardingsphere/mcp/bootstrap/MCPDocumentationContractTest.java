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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDocumentationContractTest {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    @Test
    void assertHttpDockerAndConfigurationDocs() throws IOException {
        String actualEnglish = Files.readString(resolveMCPDirectory().resolve("README.md"));
        String actualChinese = Files.readString(resolveMCPDirectory().resolve("README_ZH.md"));
        assertDocumentationIncludesRuntimeSafety(actualEnglish);
        assertDocumentationIncludesRuntimeSafety(actualChinese);
        assertDocumentationOmitsBuiltInAuthorization(actualEnglish);
        assertDocumentationOmitsBuiltInAuthorization(actualChinese);
    }
    
    @Test
    void assertDocumentationOmitsLegacyPayloadFields() throws IOException {
        String actual = Files.readString(resolveMCPDirectory().resolve("README.md")) + Files.readString(resolveMCPDirectory().resolve("README_ZH.md"));
        for (String each : List.of("pending_questions", "resource_uri", "parent_uri", "next_resource_uris", "read_resources_first", "empty_reason", "not_found_reason")) {
            assertFalse(actual.contains(each));
        }
    }
    
    @Test
    void assertDesignDocumentationOmitsBuiltInAuthorization() throws IOException {
        Path docsDirectory = resolveRepositoryDirectory().resolve("docs/mcp");
        String actual = Files.readString(docsDirectory.resolve("ShardingSphere-MCP-PRD.md"))
                + Files.readString(docsDirectory.resolve("ShardingSphere-MCP-Detailed-Design.md"))
                + Files.readString(docsDirectory.resolve("ShardingSphere-MCP-Technical-Design.md"));
        assertDocumentationOmitsBuiltInAuthorization(actual);
    }
    
    @Test
    void assertRegistryMetadataIncludesConfigVariable() throws IOException {
        Map<String, Object> actual = JSON_MAPPER.readValue(resolveMCPDirectory().resolve("server.json").toFile(), new TypeReference<>() {
        });
        List<?> packages = (List<?>) actual.get("packages");
        assertFalse(packages.isEmpty());
        for (Object each : packages) {
            Map<?, ?> actualConfigVariable = findEnvironmentVariable((Map<?, ?>) each, "SHARDINGSPHERE_MCP_CONFIG");
            assertFalse((Boolean) actualConfigVariable.get("isRequired"));
            assertFalse((Boolean) actualConfigVariable.get("isSecret"));
            assertThat(actualConfigVariable.get("format"), is("string"));
        }
    }
    
    private Map<?, ?> findEnvironmentVariable(final Map<?, ?> packageMetadata, final String name) {
        return ((List<?>) packageMetadata.get("environmentVariables")).stream()
                .map(each -> (Map<?, ?>) each)
                .filter(each -> name.equals(each.get("name")))
                .findFirst()
                .orElseThrow();
    }
    
    private void assertDocumentationIncludesRuntimeSafety(final String content) {
        assertTrue(content.contains("transport.type"));
        assertTrue(content.contains("STREAMABLE_HTTP"));
        assertTrue(content.contains("STDIO"));
        assertTrue(content.contains("transport.http.bindHost"));
        assertTrue(content.contains("transport.http.endpointPath"));
        assertTrue(content.contains("Origin"));
        assertTrue(content.contains("MCP form elicitation"));
        assertTrue(content.contains("URL mode"));
        assertTrue(content.contains("secret manager"));
        assertTrue(content.contains("SHARDINGSPHERE_MCP_CONFIG"));
        assertTrue(content.contains("configuration_required"));
    }
    
    private void assertDocumentationOmitsBuiltInAuthorization(final String content) {
        for (String each : List.of("accessToken", "oauthIntrospection", "authorizationServers", "scopesSupported", "protectedResource",
                "WWW-Authenticate", "Authorization: Bearer <token>", "invalid_token", "insufficient_scope", "Bearer", "http.enabled", "stdio.enabled")) {
            assertFalse(content.contains(each));
        }
    }
    
    private Path resolveMCPDirectory() {
        return resolveRepositoryDirectory().resolve("mcp");
    }
    
    private Path resolveRepositoryDirectory() {
        Path result = Path.of("").toAbsolutePath();
        while (null != result && !Files.exists(result.resolve("mcp/README.md"))) {
            result = result.getParent();
        }
        return result;
    }
}
