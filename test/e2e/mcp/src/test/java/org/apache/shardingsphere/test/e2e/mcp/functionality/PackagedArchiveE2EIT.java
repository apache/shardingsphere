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
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
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
    
    @Test
    void assertPackagedArchive() throws IOException, NoSuchAlgorithmException {
        Path distributionHome = findDistributionHome();
        String distributionName = distributionHome.getFileName().toString();
        String version = distributionName.substring(DISTRIBUTION_PREFIX.length());
        Path archive = distributionHome.resolveSibling("apache-shardingsphere-" + version + "-shardingsphere-mcp-bin.tar.gz");
        assertTrue(Files.isRegularFile(archive), "Missing MCP archive " + archive + "; run ./mvnw -pl distribution/mcp -am -Prelease,default-dep -DskipTests package first.");
        Path checksum = archive.resolveSibling(archive.getFileName() + ".sha512");
        assertTrue(Files.isRegularFile(checksum), "Missing MCP archive checksum " + checksum);
        try (InputStream input = Files.newInputStream(archive)) {
            assertThat(Files.readString(checksum), is(calculateChecksum(input) + "  " + archive.getFileName()));
        }
        Set<String> expectedPaths;
        try (Stream<Path> paths = Files.walk(distributionHome)) {
            expectedPaths = paths.map(each -> distributionHome.getParent().relativize(each).toString().replace('\\', '/')).collect(Collectors.toSet());
        }
        Set<String> actualPaths = HashSet.newHashSet(expectedPaths.size());
        Set<String> actualFiles = new HashSet<>();
        Set<String> actualDirectories = new HashSet<>();
        try (TarArchiveInputStream input = new TarArchiveInputStream(new GZIPInputStream(Files.newInputStream(archive)))) {
            for (TarArchiveEntry each = input.getNextEntry(); null != each; each = input.getNextEntry()) {
                String actualPath = Path.of(each.getName()).toString().replace('\\', '/');
                assertTrue(expectedPaths.contains(actualPath), "Unexpected archive path " + actualPath);
                actualPaths.add(actualPath);
                if (each.isDirectory()) {
                    actualDirectories.add(actualPath);
                } else {
                    assertTrue(each.isFile(), "Unexpected archive entry type " + actualPath);
                    assertTrue(actualFiles.add(actualPath), "Duplicate archive file " + actualPath);
                    assertThat("Archive file mode " + actualPath, each.getMode() & 0777, is(actualPath.endsWith(".sh") ? 0755 : 0644));
                    try (InputStream expectedInput = Files.newInputStream(distributionHome.getParent().resolve(actualPath))) {
                        String actualChecksum = calculateChecksum(input);
                        assertThat("Archive file content " + actualPath, actualChecksum, is(calculateChecksum(expectedInput)));
                    }
                }
            }
        }
        assertThat(actualPaths, is(expectedPaths));
        for (String each : List.of("bin", "conf", "lib", "plugins", "logs")) {
            assertTrue(actualDirectories.contains(distributionName + "/" + each), "Missing archive directory " + each);
        }
        assertTrue(actualPaths.stream().noneMatch(each -> each.startsWith(distributionName + "/plugins/") || each.startsWith(distributionName + "/logs/")));
    }
    
    private Path findDistributionHome() throws IOException {
        Path repositoryRoot = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "../../..")).toAbsolutePath().normalize();
        String configuredHome = EnvironmentPropertiesLoader.loadProperties().getProperty("mcp.distribution.home", "").trim();
        if (!configuredHome.isEmpty()) {
            return repositoryRoot.resolve(configuredHome).toAbsolutePath().normalize();
        }
        Path targetDirectory = repositoryRoot.resolve("distribution/mcp/target");
        assertTrue(Files.isDirectory(targetDirectory), "Missing MCP distribution output " + targetDirectory + "; build distribution/mcp with -Prelease,default-dep first.");
        try (Stream<Path> paths = Files.list(targetDirectory)) {
            Collection<Path> result = paths.filter(Files::isDirectory).filter(each -> each.getFileName().toString().startsWith(DISTRIBUTION_PREFIX)).toList();
            assertThat("Expected one MCP distribution home under " + targetDirectory, result.size(), is(1));
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
}
