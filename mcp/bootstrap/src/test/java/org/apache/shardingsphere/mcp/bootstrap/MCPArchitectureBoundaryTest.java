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
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPArchitectureBoundaryTest {
    
    private static final List<String> GENERIC_MODULE_SOURCE_DIRECTORIES = List.of(
            "mcp/api/src/main/java",
            "mcp/support/src/main/java",
            "mcp/core/src/main/java",
            "mcp/bootstrap/src/main/java");
    
    private static final List<String> FEATURE_PACKAGE_IMPORTS = List.of(
            "org.apache.shardingsphere.mcp.feature.broadcast",
            "org.apache.shardingsphere.mcp.feature.encrypt",
            "org.apache.shardingsphere.mcp.feature.mask",
            "org.apache.shardingsphere.mcp.feature.readwritesplitting",
            "org.apache.shardingsphere.mcp.feature.shadow",
            "org.apache.shardingsphere.mcp.feature.sharding");
    
    @Test
    void assertGenericModulesDoNotImportFeatures() throws IOException {
        Path projectRoot = findProjectRoot();
        for (String each : GENERIC_MODULE_SOURCE_DIRECTORIES) {
            assertNoFeatureImport(projectRoot.resolve(each));
        }
    }
    
    private void assertNoFeatureImport(final Path sourceDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            List<Path> actualViolations = paths.filter(Files::isRegularFile).filter(each -> each.toString().endsWith(".java"))
                    .filter(this::containsFeaturePackageImport).toList();
            assertTrue(actualViolations.isEmpty(), () -> "Generic MCP modules must not import feature packages: " + actualViolations);
        }
    }
    
    private boolean containsFeaturePackageImport(final Path path) {
        try {
            String source = Files.readString(path);
            return FEATURE_PACKAGE_IMPORTS.stream().anyMatch(each -> source.contains("import " + each));
        } catch (final IOException ex) {
            throw new IllegalStateException("Failed to read " + path, ex);
        }
    }
    
    private Path findProjectRoot() {
        String configuredRoot = System.getProperty("maven.multiModuleProjectDirectory", "");
        if (!configuredRoot.isBlank()) {
            return Paths.get(configuredRoot).toAbsolutePath().normalize();
        }
        Path result = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (null != result && !Files.isDirectory(result.resolve("mcp"))) {
            result = result.getParent();
        }
        return result;
    }
}
