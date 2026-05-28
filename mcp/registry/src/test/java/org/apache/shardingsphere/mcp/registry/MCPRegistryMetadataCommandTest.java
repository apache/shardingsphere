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

package org.apache.shardingsphere.mcp.registry;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPRegistryMetadataCommandTest {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private static final String PACKAGE_SHAPE_ERROR_MESSAGE = "server.json packages must contain exactly one stdio OCI package and one streamable-http OCI package.";
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertMain() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        assertDoesNotThrow(() -> MCPRegistryMetadataCommand.main(new String[]{"--path", serverPath.toString(), "--validate-only", "--allow-snapshot"}));
    }
    
    @Test
    void assertExecuteForReleaseRewrite() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--version", "5.5.4", "--identifier", "ghcr.io/apache/shardingsphere-mcp:5.5.4");
        Map<String, Object> actual = readServerJson(serverPath);
        assertThat(actual.get("version"), is("5.5.4"));
        assertThat(getPackageValues(actual, "identifier"), is(List.of("ghcr.io/apache/shardingsphere-mcp:5.5.4", "ghcr.io/apache/shardingsphere-mcp:5.5.4")));
        assertThat(getPackageValues(actual, "version"), is(List.of("5.5.4", "5.5.4")));
    }
    
    @Test
    void assertExecuteRejectsReleaseSnapshot() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPRegistryMetadataCommand.execute(
                "--path", serverPath.toString(), "--version", "5.5.4-SNAPSHOT", "--identifier", "ghcr.io/apache/shardingsphere-mcp:5.5.4-SNAPSHOT"));
        assertThat(actual.getMessage(), is("server version must not contain SNAPSHOT for publication."));
    }
    
    @Test
    void assertExecuteValidatesDevelopmentSnapshot() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        assertDoesNotThrow(() -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
    }
    
    @Test
    void assertExecuteRejectsMissingHttpUrl() throws IOException {
        Map<String, Object> server = createServerMetadata();
        getPackages(server).get(1).put("transport", Map.of("type", "streamable-http"));
        Path serverPath = createServerJson(server);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
        assertThat(actual.getMessage(), is("streamable-http transport must define a URL."));
    }
    
    @Test
    void assertExecuteRejectsMismatchedPackageVersion() throws IOException {
        Map<String, Object> server = createServerMetadata();
        getPackages(server).get(0).put("version", "5.5.3");
        Path serverPath = createServerJson(server);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
        assertThat(actual.getMessage(), is("MCP Registry package version must match the server version."));
    }
    
    @Test
    void assertExecuteRejectsDuplicateStdioPackage() throws IOException {
        Map<String, Object> server = createServerMetadata();
        server.put("packages", List.of(createPackage("stdio", ""), createPackage("stdio", "")));
        assertPackageShapeRejected(server);
    }
    
    @Test
    void assertExecuteRejectsDuplicateStreamableHttpPackage() throws IOException {
        Map<String, Object> server = createServerMetadata();
        server.put("packages", List.of(createPackage("streamable-http", "http://127.0.0.1:18088/mcp"), createPackage("streamable-http", "http://127.0.0.1:18088/mcp")));
        assertPackageShapeRejected(server);
    }
    
    @Test
    void assertExecuteRejectsExtraPackage() throws IOException {
        Map<String, Object> server = createServerMetadata();
        server.put("packages", List.of(createPackage("stdio", ""), createPackage("streamable-http", "http://127.0.0.1:18088/mcp"), createPackage("stdio", "")));
        assertPackageShapeRejected(server);
    }
    
    @Test
    void assertExecuteRejectsMissingReleaseArguments() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString()));
        assertThat(actual.getMessage(), is("--version and --identifier are required unless --validate-only is set."));
    }
    
    @Test
    void assertExecuteRejectsRequiredEnvironmentVariable() throws IOException {
        Map<String, Object> server = createServerMetadata();
        getConfigEnvironmentVariable(server).put("isRequired", true);
        assertEnvironmentVariableRejected(server, "MCP Registry metadata for SHARDINGSPHERE_MCP_CONFIG must declare isRequired as false.");
    }
    
    @Test
    void assertExecuteRejectsNonStringEnvironmentVariableFormat() throws IOException {
        Map<String, Object> server = createServerMetadata();
        getConfigEnvironmentVariable(server).put("format", "path");
        assertEnvironmentVariableRejected(server, "MCP Registry metadata for SHARDINGSPHERE_MCP_CONFIG format must be string.");
    }
    
    @Test
    void assertExecuteRejectsSecretEnvironmentVariable() throws IOException {
        Map<String, Object> server = createServerMetadata();
        getConfigEnvironmentVariable(server).put("isSecret", true);
        assertEnvironmentVariableRejected(server, "MCP Registry metadata for SHARDINGSPHERE_MCP_CONFIG must declare isSecret as false.");
    }
    
    @Test
    void assertExecuteValidatesSourceMetadata() {
        Path serverPath = resolveMCPDirectory().resolve("server.json");
        assertDoesNotThrow(() -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
    }
    
    @Test
    void assertExecuteValidatesDockerfileMetadata() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        Path dockerfilePath = createDockerfile(createDockerfileContent());
        assertDoesNotThrow(() -> MCPRegistryMetadataCommand.execute(
                "--path", serverPath.toString(), "--validate-only", "--allow-snapshot", "--dockerfile-path", dockerfilePath.toString()));
    }
    
    @Test
    void assertExecuteRejectsMissingDockerfilePathValue() throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot", "--dockerfile-path"));
        assertThat(actual.getMessage(), is("--dockerfile-path requires a value."));
    }
    
    @Test
    void assertExecuteRejectsMismatchedDockerfileServerName() throws IOException {
        assertDockerfileMetadataRejected(createDockerfileContent().replace(
                "ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp", "ARG MCP_SERVER_NAME=io.github.apache/other-mcp"),
                "Dockerfile must define ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp.");
    }
    
    @Test
    void assertExecuteRejectsMissingDockerfileServerNameLabel() throws IOException {
        assertDockerfileMetadataRejected(createDockerfileContent().replace("      io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\"", ""),
                "Dockerfile must label io.modelcontextprotocol.server.name with ${MCP_SERVER_NAME}.");
    }
    
    @Test
    void assertExecuteRejectsCommentedDockerfileServerNameLabel() throws IOException {
        assertDockerfileMetadataRejected(createDockerfileContent().replace(
                "      io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\"", "#      io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\""),
                "Dockerfile must label io.modelcontextprotocol.server.name with ${MCP_SERVER_NAME}.");
    }
    
    @Test
    void assertExecuteRejectsMissingDockerfileImageVersionArgument() throws IOException {
        assertDockerfileMetadataRejected(createDockerfileContent().replace("ARG MCP_IMAGE_VERSION=unknown", "ARG MCP_IMAGE_VERSION=latest"),
                "Dockerfile must define ARG MCP_IMAGE_VERSION=unknown.");
    }
    
    @Test
    void assertExecuteRejectsMissingDockerfileImageVersionLabel() throws IOException {
        assertDockerfileMetadataRejected(createDockerfileContent().replace(
                "LABEL org.opencontainers.image.version=\"${MCP_IMAGE_VERSION}\" \\", "LABEL io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\""),
                "Dockerfile must label org.opencontainers.image.version with ${MCP_IMAGE_VERSION}.");
    }
    
    @Test
    void assertExecuteValidatesSourceDockerfileMetadata() {
        Path projectRoot = resolveProjectRoot();
        assertDoesNotThrow(() -> MCPRegistryMetadataCommand.execute(
                "--path", projectRoot.resolve("mcp/server.json").toString(),
                "--validate-only", "--allow-snapshot",
                "--dockerfile-path", projectRoot.resolve("distribution/mcp/Dockerfile").toString()));
    }
    
    private Path createServerJson(final Map<String, Object> server) throws IOException {
        Path result = tempDir.resolve("server.json");
        Files.writeString(result, JSON_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(server));
        return result;
    }
    
    private Path createDockerfile(final String content) throws IOException {
        Path result = tempDir.resolve("Dockerfile");
        Files.writeString(result, content);
        return result;
    }
    
    private String createDockerfileContent() {
        return String.join(System.lineSeparator(),
                "FROM eclipse-temurin:21-jre",
                "ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp",
                "ARG MCP_IMAGE_VERSION=unknown",
                "LABEL org.opencontainers.image.version=\"${MCP_IMAGE_VERSION}\" \\",
                "      io.modelcontextprotocol.server.name=\"${MCP_SERVER_NAME}\"");
    }
    
    private void assertPackageShapeRejected(final Map<String, Object> server) throws IOException {
        Path serverPath = createServerJson(server);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
        assertThat(actual.getMessage(), is(PACKAGE_SHAPE_ERROR_MESSAGE));
    }
    
    private void assertDockerfileMetadataRejected(final String dockerfileContent, final String expectedMessage) throws IOException {
        Path serverPath = createServerJson(createServerMetadata());
        Path dockerfilePath = createDockerfile(dockerfileContent);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> MCPRegistryMetadataCommand.execute(
                "--path", serverPath.toString(), "--validate-only", "--allow-snapshot", "--dockerfile-path", dockerfilePath.toString()));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private void assertEnvironmentVariableRejected(final Map<String, Object> server, final String expectedMessage) throws IOException {
        Path serverPath = createServerJson(server);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> MCPRegistryMetadataCommand.execute("--path", serverPath.toString(), "--validate-only", "--allow-snapshot"));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private Map<String, Object> readServerJson(final Path serverPath) throws IOException {
        return JSON_MAPPER.readValue(serverPath.toFile(), new TypeReference<>() {
        });
    }
    
    private Map<String, Object> createServerMetadata() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("$schema", "https://static.modelcontextprotocol.io/schemas/2025-12-11/server.schema.json");
        result.put("name", "io.github.apache/shardingsphere-mcp");
        result.put("title", "Apache ShardingSphere MCP");
        result.put("description", "MCP runtime for Apache ShardingSphere metadata discovery, SQL preview, and rule workflows");
        result.put("version", "5.5.4-SNAPSHOT");
        result.put("packages", List.of(createPackage("stdio", ""), createPackage("streamable-http", "http://127.0.0.1:18088/mcp")));
        return result;
    }
    
    private Map<String, Object> createPackage(final String transportType, final String url) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("registryType", "oci");
        result.put("identifier", "ghcr.io/apache/shardingsphere-mcp:5.5.4-SNAPSHOT");
        result.put("version", "5.5.4-SNAPSHOT");
        result.put("transport", createTransport(transportType, url));
        result.put("environmentVariables", List.of(
                createEnvironmentVariable("SHARDINGSPHERE_MCP_TRANSPORT", "Launch the container in the selected transport mode."),
                createEnvironmentVariable("SHARDINGSPHERE_MCP_CONFIG", "Optional absolute config path inside the OCI container.")));
        return result;
    }
    
    private Map<String, Object> createTransport(final String transportType, final String url) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("type", transportType);
        if (!url.isBlank()) {
            result.put("url", url);
        }
        return result;
    }
    
    private Map<String, Object> createEnvironmentVariable(final String name, final String description) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("name", name);
        result.put("description", description);
        result.put("isRequired", false);
        result.put("format", "string");
        result.put("isSecret", false);
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getPackages(final Map<String, Object> server) {
        return (List<Map<String, Object>>) server.get("packages");
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getEnvironmentVariables(final Map<String, Object> server, final int packageIndex) {
        return (List<Map<String, Object>>) getPackages(server).get(packageIndex).get("environmentVariables");
    }
    
    private Map<String, Object> getConfigEnvironmentVariable(final Map<String, Object> server) {
        return getEnvironmentVariables(server, 0).stream().filter(each -> "SHARDINGSPHERE_MCP_CONFIG".equals(each.get("name"))).findFirst().orElseThrow();
    }
    
    private List<Object> getPackageValues(final Map<String, Object> server, final String key) {
        return getPackages(server).stream().map(each -> each.get(key)).toList();
    }
    
    private Path resolveMCPDirectory() {
        return resolveProjectRoot().resolve("mcp");
    }
    
    private Path resolveProjectRoot() {
        Path result = Path.of("").toAbsolutePath();
        while (null != result && !Files.exists(result.resolve("mcp/README.md"))) {
            result = result.getParent();
        }
        if (null == result) {
            throw new IllegalStateException("Project root was not found.");
        }
        return result;
    }
}
