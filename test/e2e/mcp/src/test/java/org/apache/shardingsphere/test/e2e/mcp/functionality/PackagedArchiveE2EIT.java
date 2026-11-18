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

package org.apache.shardingsphere.test.e2e.mcp.functionality;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.shardingsphere.test.e2e.env.runtime.EnvironmentPropertiesLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition#isDockerEnabled")
class PackagedArchiveE2EIT {
    
    private static final String DISTRIBUTION_PREFIX = "apache-shardingsphere-mcp-";
    
    private static final String ARCHIVE_PREFIX = "apache-shardingsphere-";
    
    private static final String ARCHIVE_SUFFIX = "-shardingsphere-mcp-bin.tar.gz";
    
    @Test
    void assertPackagedArchive() throws IOException, NoSuchAlgorithmException {
        Path archive = findDistributionArchive();
        String archiveName = archive.getFileName().toString();
        String version = archiveName.substring(ARCHIVE_PREFIX.length(), archiveName.length() - ARCHIVE_SUFFIX.length());
        String distributionName = DISTRIBUTION_PREFIX + version;
        assertTrue(Files.isRegularFile(archive), "Missing MCP archive " + archive + "; run ./mvnw -pl distribution/mcp -am -Prelease,default-dep -DskipTests package first.");
        Path checksum = archive.resolveSibling(archive.getFileName() + ".sha512");
        assertTrue(Files.isRegularFile(checksum), "Missing MCP archive checksum " + checksum);
        try (InputStream input = Files.newInputStream(archive)) {
            assertThat(Files.readString(checksum), is(calculateChecksum(input) + "  " + archive.getFileName()));
        }
        Map<String, Path> expectedAssets = getExpectedAssets(distributionName);
        Set<String> expectedDirectories = expectedAssets.entrySet().stream().filter(entry -> Files.isDirectory(entry.getValue())).map(Entry::getKey).collect(Collectors.toSet());
        expectedDirectories.addAll(List.of(distributionName + "/lib", distributionName + "/plugins", distributionName + "/logs"));
        Set<String> actualFiles = new HashSet<>();
        Set<String> actualDirectories = new HashSet<>();
        try (TarArchiveInputStream input = new TarArchiveInputStream(new GZIPInputStream(Files.newInputStream(archive)))) {
            for (TarArchiveEntry each = input.getNextEntry(); null != each; each = input.getNextEntry()) {
                String actualPath = Path.of(each.getName()).toString().replace('\\', '/');
                if (each.isDirectory()) {
                    actualDirectories.add(actualPath);
                } else {
                    assertTrue(each.isFile(), "Unexpected archive entry type " + actualPath);
                    assertTrue(actualFiles.add(actualPath), "Duplicate archive file " + actualPath);
                    assertThat("Archive file mode " + actualPath, each.getMode() & 0777, is(actualPath.endsWith(".sh") ? 0755 : 0644));
                    if (expectedAssets.containsKey(actualPath)) {
                        try (InputStream expectedInput = Files.newInputStream(expectedAssets.get(actualPath))) {
                            String actualChecksum = calculateChecksum(input);
                            assertThat("Archive file content " + actualPath, actualChecksum, is(calculateChecksum(expectedInput)));
                        }
                    } else {
                        assertTrue(actualPath.startsWith(distributionName + "/lib/") && actualPath.endsWith(".jar") && 3 == Path.of(actualPath).getNameCount(),
                                "Unexpected archive file " + actualPath);
                    }
                }
            }
        }
        assertThat(actualDirectories, is(expectedDirectories));
        Set<String> expectedAssetFiles = expectedAssets.entrySet().stream().filter(entry -> Files.isRegularFile(entry.getValue())).map(Entry::getKey).collect(Collectors.toSet());
        assertThat(actualFiles.stream().filter(each -> !each.startsWith(distributionName + "/lib/")).collect(Collectors.toSet()), is(expectedAssetFiles));
        assertTrue(actualFiles.contains(distributionName + "/lib/shardingsphere-mcp-bootstrap-" + version + ".jar"), "Missing MCP bootstrap JAR");
    }
    
    private Path findDistributionArchive() throws IOException {
        Path repositoryRoot = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "../../..")).toAbsolutePath().normalize();
        String configuredHome = EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.distribution.home", "").trim();
        if (!configuredHome.isEmpty()) {
            Path distributionHome = repositoryRoot.resolve(configuredHome).toAbsolutePath().normalize();
            String version = distributionHome.getFileName().toString().substring(DISTRIBUTION_PREFIX.length());
            return distributionHome.resolveSibling(ARCHIVE_PREFIX + version + ARCHIVE_SUFFIX);
        }
        Path targetDirectory = repositoryRoot.resolve("distribution/mcp/target");
        assertTrue(Files.isDirectory(targetDirectory), "Missing MCP distribution output " + targetDirectory + "; build distribution/mcp with -Prelease,default-dep first.");
        try (Stream<Path> paths = Files.list(targetDirectory)) {
            Collection<Path> result = paths.filter(Files::isRegularFile)
                    .filter(each -> each.getFileName().toString().startsWith(ARCHIVE_PREFIX) && each.getFileName().toString().endsWith(ARCHIVE_SUFFIX)).toList();
            assertThat("Expected one MCP distribution archive under " + targetDirectory, result.size(), is(1));
            return result.iterator().next();
        }
    }
    
    private String calculateChecksum(final InputStream input) throws IOException, NoSuchAlgorithmException {
        MessageDigest checksum = MessageDigest.getInstance("SHA-512");
        byte[] buffer = new byte[8192];
        for (int length = input.read(buffer); -1 != length; length = input.read(buffer)) {
            checksum.update(buffer, 0, length);
        }
        return HexFormat.of().formatHex(checksum.digest());
    }
    
    private Map<String, Path> getExpectedAssets(final String distributionName) throws IOException {
        Path sourceDirectory = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "../../..")).toAbsolutePath().normalize().resolve("distribution/mcp/src/main");
        Map<String, Path> result = new HashMap<>();
        addExpectedAssets(result, sourceDirectory.resolve("bin"), distributionName + "/bin");
        addExpectedAssets(result, sourceDirectory.resolve("resources"), distributionName);
        addExpectedAssets(result, sourceDirectory.resolve("release-docs"), distributionName);
        return result;
    }
    
    private void addExpectedAssets(final Map<String, Path> expectedAssets, final Path source, final String archiveDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(source)) {
            for (Path each : paths.toList()) {
                expectedAssets.put(Path.of(archiveDirectory).resolve(source.relativize(each)).toString().replace('\\', '/'), each);
            }
        }
    }
}
