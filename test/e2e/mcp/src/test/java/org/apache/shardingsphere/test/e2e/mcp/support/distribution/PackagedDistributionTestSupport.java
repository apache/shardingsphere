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

import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper.YamlMCPLaunchConfigurationSwapper;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Packaged distribution support for MCP E2E tests.
 */
public final class PackagedDistributionTestSupport {
    
    private static final String DIST_PATTERN = "apache-shardingsphere-mcp-";
    
    private PackagedDistributionTestSupport() {
    }
    
    /**
     * Prepare one packaged MCP distribution copy for smoke tests.
     *
     * @param tempDir temporary directory
     * @param transport runtime transport
     * @return prepared packaged distribution
     * @throws IOException I/O exception
     */
    public static PreparedPackagedDistribution prepare(final Path tempDir, final RuntimeTransport transport) throws IOException {
        Path workingHome = prepareWorkingHome(tempDir);
        int httpPort = resolveHttpPort(transport);
        Path configFile = rewriteConfiguration(workingHome, transport, httpPort);
        return new PreparedPackagedDistribution(workingHome, configFile, transport, httpPort);
    }
    
    /**
     * Find the packaged MCP distribution home.
     *
     * @return packaged MCP distribution home
     * @throws IOException I/O exception
     * @throws IllegalStateException configured distribution home does not exist
     */
    public static Optional<Path> findDistributionHome() throws IOException {
        Optional<Path> configuredDistributionHome = resolveConfiguredDistributionHome();
        if (configuredDistributionHome.isPresent()) {
            return configuredDistributionHome;
        }
        return findBuiltDistributionHome();
    }
    
    private static Path prepareWorkingHome(final Path tempDir) throws IOException {
        Path sourceDistributionHome = findRequiredDistributionHome();
        Path workingHome = copyDistributionHome(sourceDistributionHome, tempDir.resolve("distribution-home"));
        resetRuntimeDirectories(workingHome);
        makeStartScriptExecutable(workingHome);
        return workingHome;
    }
    
    private static Path findRequiredDistributionHome() throws IOException {
        return findDistributionHome().orElseThrow(() -> new IllegalStateException(
                "Packaged MCP distribution was not found. Run `./mvnw -pl distribution/mcp -am -DskipTests package` first."));
    }
    
    private static Optional<Path> resolveConfiguredDistributionHome() {
        String configuredHome = System.getProperty("mcp.distribution.home", "").trim();
        if (configuredHome.isEmpty()) {
            return Optional.empty();
        }
        Path result = Paths.get(configuredHome).toAbsolutePath().normalize();
        if (!Files.isDirectory(result)) {
            throw new IllegalStateException("Configured mcp.distribution.home `" + result + "` does not exist.");
        }
        return Optional.of(result);
    }
    
    private static Optional<Path> findBuiltDistributionHome() throws IOException {
        Path targetDirectory = findRepositoryRoot().resolve("distribution/mcp/target");
        if (!Files.isDirectory(targetDirectory)) {
            return Optional.empty();
        }
        try (Stream<Path> paths = Files.list(targetDirectory)) {
            return paths.filter(Files::isDirectory).filter(each -> each.getFileName().toString().startsWith(DIST_PATTERN)).min(Comparator.comparing(Path::toString));
        }
    }
    
    private static Path findRepositoryRoot() {
        String multiModuleProjectDirectory = System.getProperty("maven.multiModuleProjectDirectory", "").trim();
        if (!multiModuleProjectDirectory.isEmpty()) {
            return Paths.get(multiModuleProjectDirectory).toAbsolutePath().normalize();
        }
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (null != current) {
            if (Files.exists(current.resolve("pom.xml")) && Files.isDirectory(current.resolve("distribution/mcp"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Repository root was not found.");
    }
    
    private static Path copyDistributionHome(final Path source, final Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            
            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                Path actualTarget = target.resolve(source.relativize(dir).toString());
                Files.createDirectories(actualTarget);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                Path actualTarget = target.resolve(source.relativize(file).toString());
                Files.copy(file, actualTarget, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                return FileVisitResult.CONTINUE;
            }
        });
        return target;
    }
    
    private static void resetRuntimeDirectories(final Path workingHome) throws IOException {
        deleteDirectoryIfExists(workingHome.resolve("data"));
        deleteDirectoryIfExists(workingHome.resolve("logs"));
        deleteDirectoryIfExists(workingHome.resolve("plugins"));
    }
    
    private static void makeStartScriptExecutable(final Path workingHome) {
        Path startScript = workingHome.resolve("bin/start.sh");
        if (Files.exists(startScript)) {
            startScript.toFile().setExecutable(true);
        }
    }
    
    private static void deleteDirectoryIfExists(final Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path each : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(each);
            }
        }
    }
    
    private static int allocatePort() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            return serverSocket.getLocalPort();
        }
    }
    
    private static int resolveHttpPort(final RuntimeTransport transport) throws IOException {
        return RuntimeTransport.HTTP == transport ? allocatePort() : -1;
    }
    
    private static Path rewriteConfiguration(final Path workingHome, final RuntimeTransport transport, final int httpPort) throws IOException {
        Path result = resolveConfigFile(workingHome, transport);
        MCPLaunchConfiguration sourceConfig = MCPConfigurationLoader.load(result.toString());
        MCPLaunchConfiguration actualConfig = createRuntimeConfiguration(sourceConfig, transport, httpPort);
        Files.writeString(result, YamlEngine.marshal(new YamlMCPLaunchConfigurationSwapper().swapToYamlConfiguration(actualConfig)));
        return result;
    }
    
    private static Path resolveConfigFile(final Path workingHome, final RuntimeTransport transport) {
        return RuntimeTransport.HTTP == transport ? workingHome.resolve("conf/mcp.yaml") : workingHome.resolve("conf/mcp-stdio.yaml");
    }
    
    private static MCPLaunchConfiguration createRuntimeConfiguration(final MCPLaunchConfiguration sourceConfig, final RuntimeTransport transport, final int httpPort) {
        HttpTransportConfiguration actualHttpTransport = new HttpTransportConfiguration(RuntimeTransport.HTTP == transport,
                sourceConfig.getHttpTransport().getBindHost(), sourceConfig.getHttpTransport().isAllowRemoteAccess(),
                sourceConfig.getHttpTransport().getAccessToken(), RuntimeTransport.HTTP == transport ? httpPort : sourceConfig.getHttpTransport().getPort(),
                sourceConfig.getHttpTransport().getEndpointPath());
        StdioTransportConfiguration actualStdioTransport = new StdioTransportConfiguration(RuntimeTransport.STDIO == transport);
        return new MCPLaunchConfiguration(actualHttpTransport, actualStdioTransport, sourceConfig.getDatabases());
    }
    
    public record PreparedPackagedDistribution(Path home, Path configFile, RuntimeTransport transport, int httpPort) {
        
        public Path getStartScript() {
            return PackagedDistributionProcessSupport.resolveStartScript(home);
        }
        
        public URI getEndpointUri() {
            return URI.create(String.format("http://127.0.0.1:%d/mcp", httpPort));
        }
    }
}
