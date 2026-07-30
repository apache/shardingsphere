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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPArchitectureBoundaryTest {
    
    private static final List<String> BOOTSTRAP_PACKAGE_IMPORTS = List.of("org.apache.shardingsphere.mcp.bootstrap");
    
    private static final String API_PACKAGE_IMPORT = "org.apache.shardingsphere.mcp.api";
    
    private static final String SUPPORT_PACKAGE_IMPORT = "org.apache.shardingsphere.mcp.support";
    
    private static final String CORE_PACKAGE_IMPORT = "org.apache.shardingsphere.mcp.core";
    
    private static final String REGISTRY_PACKAGE_IMPORT = "org.apache.shardingsphere.mcp.registry";
    
    private static final List<ModuleBoundary> FEATURE_MODULE_BOUNDARIES = List.of(
            new ModuleBoundary("mcp/features/broadcast", "org.apache.shardingsphere.mcp.feature.broadcast"),
            new ModuleBoundary("mcp/features/encrypt", "org.apache.shardingsphere.mcp.feature.encrypt"),
            new ModuleBoundary("mcp/features/mask", "org.apache.shardingsphere.mcp.feature.mask"),
            new ModuleBoundary("mcp/features/readwrite-splitting", "org.apache.shardingsphere.mcp.feature.readwritesplitting"),
            new ModuleBoundary("mcp/features/shadow", "org.apache.shardingsphere.mcp.feature.shadow"),
            new ModuleBoundary("mcp/features/sharding", "org.apache.shardingsphere.mcp.feature.sharding"));
    
    private static final List<String> FEATURE_PACKAGE_IMPORTS = FEATURE_MODULE_BOUNDARIES.stream().map(ModuleBoundary::ownedPackage).toList();
    
    @Test
    void assertGenericModuleImportBoundaries() throws IOException {
        Path projectRoot = findProjectRoot();
        assertNoPackageImport(projectRoot.resolve("mcp/api/src/main/java"), combineImports(
                List.of(SUPPORT_PACKAGE_IMPORT, CORE_PACKAGE_IMPORT, REGISTRY_PACKAGE_IMPORT), BOOTSTRAP_PACKAGE_IMPORTS, FEATURE_PACKAGE_IMPORTS),
                "MCP API must not depend on implementation modules");
        assertNoPackageImport(projectRoot.resolve("mcp/support/src/main/java"), combineImports(
                List.of(CORE_PACKAGE_IMPORT, REGISTRY_PACKAGE_IMPORT), BOOTSTRAP_PACKAGE_IMPORTS, FEATURE_PACKAGE_IMPORTS),
                "MCP support must not depend on core, bootstrap, registry, or features");
        assertNoPackageImport(projectRoot.resolve("mcp/core/src/main/java"), combineImports(
                List.of(REGISTRY_PACKAGE_IMPORT), BOOTSTRAP_PACKAGE_IMPORTS, FEATURE_PACKAGE_IMPORTS),
                "MCP core must not depend on bootstrap, registry, or features");
        assertNoPackageImport(projectRoot.resolve("mcp/bootstrap/src/main/java"), combineImports(List.of(REGISTRY_PACKAGE_IMPORT), FEATURE_PACKAGE_IMPORTS),
                "MCP bootstrap production code must not depend on registry or concrete features");
        assertNoPackageImport(projectRoot.resolve("mcp/registry/src/main/java"), combineImports(
                List.of(API_PACKAGE_IMPORT, SUPPORT_PACKAGE_IMPORT, CORE_PACKAGE_IMPORT), BOOTSTRAP_PACKAGE_IMPORTS, FEATURE_PACKAGE_IMPORTS),
                "MCP registry metadata tooling must remain isolated from runtime modules");
    }
    
    @Test
    void assertFeatureModuleImportBoundaries() throws IOException {
        Path projectRoot = findProjectRoot();
        for (ModuleBoundary each : FEATURE_MODULE_BOUNDARIES) {
            List<String> otherFeatureImports = FEATURE_PACKAGE_IMPORTS.stream().filter(importName -> !importName.equals(each.ownedPackage())).toList();
            assertNoPackageImport(projectRoot.resolve(each.moduleDirectory()).resolve("src/main/java"),
                    combineImports(List.of(CORE_PACKAGE_IMPORT, REGISTRY_PACKAGE_IMPORT), BOOTSTRAP_PACKAGE_IMPORTS, otherFeatureImports),
                    "MCP feature modules must not depend on core, bootstrap, registry, or another feature");
        }
    }
    
    @Test
    void assertMavenProductionDependencyBoundaries() throws IOException, ParserConfigurationException, SAXException {
        Map<String, Set<String>> allowedDependencies = new LinkedHashMap<>();
        allowedDependencies.put("mcp/api", Set.of());
        allowedDependencies.put("mcp/support", Set.of("shardingsphere-mcp-api"));
        allowedDependencies.put("mcp/core", Set.of("shardingsphere-mcp-support"));
        allowedDependencies.put("mcp/bootstrap", Set.of("shardingsphere-mcp-core"));
        allowedDependencies.put("mcp/registry", Set.of());
        for (ModuleBoundary each : FEATURE_MODULE_BOUNDARIES) {
            allowedDependencies.put(each.moduleDirectory(), Set.of("shardingsphere-mcp-support"));
        }
        Path projectRoot = findProjectRoot();
        for (Entry<String, Set<String>> entry : allowedDependencies.entrySet()) {
            Set<String> actualDependencies = getMCPProductionDependencies(projectRoot.resolve(entry.getKey()).resolve("pom.xml"));
            assertTrue(entry.getValue().containsAll(actualDependencies),
                    () -> String.format("Forbidden MCP production dependencies in %s: %s", entry.getKey(), difference(actualDependencies, entry.getValue())));
        }
    }
    
    @SafeVarargs
    private List<String> combineImports(final Collection<String>... imports) {
        return Stream.of(imports).flatMap(Collection::stream).distinct().toList();
    }
    
    private void assertNoPackageImport(final Path sourceDirectory, final Collection<String> forbiddenImports, final String message) throws IOException {
        try (Stream<Path> paths = Files.walk(sourceDirectory)) {
            List<Path> actualViolations = paths.filter(Files::isRegularFile).filter(each -> each.toString().endsWith(".java"))
                    .filter(each -> containsPackageImport(each, forbiddenImports)).toList();
            assertTrue(actualViolations.isEmpty(), () -> message + ": " + actualViolations);
        }
    }
    
    private boolean containsPackageImport(final Path path, final Collection<String> forbiddenImports) {
        try {
            String source = Files.readString(path);
            return forbiddenImports.stream().anyMatch(each -> source.contains("import " + each) || source.contains("import static " + each));
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
    
    private Set<String> getMCPProductionDependencies(final Path pomFile) throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = factory.newDocumentBuilder().parse(pomFile.toFile());
        Set<String> result = new LinkedHashSet<>();
        NodeList dependencies = document.getDocumentElement().getElementsByTagName("dependency");
        for (int i = 0; i < dependencies.getLength(); i++) {
            Element each = (Element) dependencies.item(i);
            if (!"org.apache.shardingsphere".equals(readDirectChild(each, "groupId")) || "test".equals(readDirectChild(each, "scope"))) {
                continue;
            }
            String artifactId = readDirectChild(each, "artifactId");
            if (artifactId.startsWith("shardingsphere-mcp-")) {
                result.add(artifactId);
            }
        }
        return result;
    }
    
    private String readDirectChild(final Element parent, final String childName) {
        for (Node each = parent.getFirstChild(); null != each; each = each.getNextSibling()) {
            if (each instanceof Element && childName.equals(each.getNodeName())) {
                return each.getTextContent().trim();
            }
        }
        return "";
    }
    
    private Set<String> difference(final Set<String> actual, final Set<String> allowed) {
        Set<String> result = new LinkedHashSet<>(actual);
        result.removeAll(allowed);
        return result;
    }
    
    private record ModuleBoundary(String moduleDirectory, String ownedPackage) {
    }
}
