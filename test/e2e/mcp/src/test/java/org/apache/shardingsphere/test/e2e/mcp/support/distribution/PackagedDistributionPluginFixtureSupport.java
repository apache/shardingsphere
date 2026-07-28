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

package org.apache.shardingsphere.test.e2e.mcp.support.distribution;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.test.e2e.mcp.support.fixture.plugin.PluginFixtureHandlerProvider;
import org.apache.shardingsphere.test.e2e.mcp.support.fixture.plugin.PluginFixturePingToolHandler;
import org.apache.shardingsphere.test.e2e.mcp.support.fixture.plugin.PluginFixtureStatusResourceHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.stream.Stream;

/**
 * Test support for packaged distribution plugin discovery fixtures.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PackagedDistributionPluginFixtureSupport {
    
    private static final String HANDLER_PROVIDER_SERVICE_ENTRY = "META-INF/services/org.apache.shardingsphere.mcp.api.MCPHandlerProvider";
    
    private static final String DESCRIPTOR_ENTRY = "META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-test-fixture.yaml";
    
    private static final List<String> DIRECTORY_ENTRIES = List.of("META-INF/", "META-INF/services/", "META-INF/shardingsphere-mcp/", "META-INF/shardingsphere-mcp/mcp-descriptors/");
    
    private static final List<String> OFFICIAL_FEATURE_ARTIFACT_IDS = List.of(
            "shardingsphere-mcp-feature-encrypt", "shardingsphere-mcp-feature-mask", "shardingsphere-mcp-feature-broadcast",
            "shardingsphere-mcp-feature-readwrite-splitting", "shardingsphere-mcp-feature-shadow", "shardingsphere-mcp-feature-sharding");
    
    private static final List<Class<?>> FIXTURE_PLUGIN_CLASSES = List.of(
            PluginFixtureHandlerProvider.class, PluginFixturePingToolHandler.class, PluginFixtureStatusResourceHandler.class);
    
    private static final String DESCRIPTOR_CONTENT = """
            resources:
              - uri: shardingsphere://features/test-fixture/status
                name: test-fixture-status
                title: Test Fixture Status
                description: "Read the packaged MCP test fixture status used by e2e plugin discovery tests."
                mimeType: application/json
                shardingSphereMetadata:
                  resourceKind: detail
                  objectScope: test-fixture
            tools:
              - name: fixture_ping
                title: Fixture Ping
                description: "Return a fixture ping response for packaged plugin discovery."
                inputSchema:
                  type: object
                  properties:
                    message:
                      type: string
                      description: "Fixture message."
                  required:
                    - message
                  additionalProperties: false
                outputSchema:
                  type: object
                  properties:
                    status:
                      type: string
                      enum:
                        - ready
                    echo:
                      type: string
                  required:
                    - status
                    - echo
                  additionalProperties: false
                  examples:
                    - status: ready
                      echo: hello
                annotations:
                  title: Fixture Ping
                  readOnlyHint: true
                  destructiveHint: false
                  idempotentHint: true
                  openWorldHint: true
                meta:
                  org.apache.shardingsphere/purpose: test-fixture-plugin
            """;
    
    /**
     * Create one fixture plugin jar under packaged distribution plugins directory.
     *
     * @param pluginsDirectory packaged distribution plugins directory
     * @return created fixture plugin jar path
     * @throws IOException I/O exception
     */
    public static Path createFixturePluginJar(final Path pluginsDirectory) throws IOException {
        Files.createDirectories(pluginsDirectory);
        Path result = pluginsDirectory.resolve("shardingsphere-mcp-test-fixture-plugin.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(result))) {
            for (String each : DIRECTORY_ENTRIES) {
                writeDirectoryEntry(output, each);
            }
            writeEntry(output, HANDLER_PROVIDER_SERVICE_ENTRY, (PluginFixtureHandlerProvider.class.getName() + '\n').getBytes(StandardCharsets.UTF_8));
            writeEntry(output, DESCRIPTOR_ENTRY, DESCRIPTOR_CONTENT.getBytes(StandardCharsets.UTF_8));
            for (Class<?> each : FIXTURE_PLUGIN_CLASSES) {
                writeClassEntry(output, each);
            }
        }
        return result;
    }
    
    /**
     * Remove official feature jars from packaged distribution libraries.
     *
     * @param libraryDirectory packaged distribution library directory
     * @return removed jar file names
     * @throws IOException I/O exception
     */
    public static List<String> removeOfficialFeatureJars(final Path libraryDirectory) throws IOException {
        try (Stream<Path> paths = Files.list(libraryDirectory)) {
            List<Path> result = paths.filter(Files::isRegularFile).filter(PackagedDistributionPluginFixtureSupport::isOfficialFeatureJar).toList();
            for (Path each : result) {
                Files.delete(each);
            }
            return result.stream().map(each -> each.getFileName().toString()).toList();
        }
    }
    
    private static boolean isOfficialFeatureJar(final Path path) {
        String jarName = path.getFileName().toString();
        return OFFICIAL_FEATURE_ARTIFACT_IDS.stream().anyMatch(jarName::contains);
    }
    
    private static void writeClassEntry(final JarOutputStream output, final Class<?> classType) throws IOException {
        String resourcePath = classType.getName().replace('.', '/') + ".class";
        try (InputStream input = classType.getClassLoader().getResourceAsStream(resourcePath)) {
            if (null == input) {
                throw new IOException("Fixture plugin class resource `" + resourcePath + "` was not found.");
            }
            writeEntry(output, resourcePath, input.readAllBytes());
        }
    }
    
    private static void writeEntry(final JarOutputStream output, final String entryName, final byte[] content) throws IOException {
        output.putNextEntry(new JarEntry(entryName));
        output.write(content);
        output.closeEntry();
    }
    
    private static void writeDirectoryEntry(final JarOutputStream output, final String entryName) throws IOException {
        output.putNextEntry(new JarEntry(entryName));
        output.closeEntry();
    }
}
