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

package org.apache.shardingsphere.test.e2e.mcp.support.runtime;

import lombok.Getter;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mcp.bootstrap.MCPRuntimeLauncher;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper.YamlMCPLaunchConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPStdioInteractionClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class AbstractConfigBackedRuntimeE2ETest {
    
    private static final String LOOPBACK_BIND_HOST = "127.0.0.1";
    
    private static final String ENDPOINT_PATH = "/gateway";
    
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }
    
    @Getter
    @TempDir
    private Path tempDir;
    
    private StreamableHttpMCPServer httpServer;
    
    private final List<StreamableHttpMCPServer> additionalHttpServers = new LinkedList<>();
    
    private Path configFile;
    
    private boolean runtimeFixturePrepared;
    
    private boolean runtimePrepared;
    
    private int customConfigurationSequence;
    
    @AfterEach
    protected final void tearDown() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
        for (StreamableHttpMCPServer each : additionalHttpServers) {
            each.stop();
        }
        additionalHttpServers.clear();
        configFile = null;
        runtimeFixturePrepared = false;
        runtimePrepared = false;
        customConfigurationSequence = 0;
    }
    
    protected abstract RuntimeTransport getTransport();
    
    protected final MCPInteractionClient createInteractionClient() throws IOException {
        prepareRuntime();
        return createInteractionClient(configFile, httpServer);
    }
    
    protected final MCPInteractionClient createInteractionClient(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) throws IOException {
        prepareRuntimeFixtureIfNeeded();
        Path actualConfigFile = createConfigurationFile(String.format("mcp-custom-%d.yaml", customConfigurationSequence++), runtimeDatabases);
        if (RuntimeTransport.HTTP == getTransport()) {
            StreamableHttpMCPServer actualHttpServer = createStartedHttpServer(actualConfigFile);
            additionalHttpServers.add(actualHttpServer);
            return createInteractionClient(actualConfigFile, actualHttpServer);
        }
        return createInteractionClient(actualConfigFile, null);
    }
    
    private MCPInteractionClient createInteractionClient(final Path configFile, final StreamableHttpMCPServer httpServer) {
        RuntimeTransport transport = getTransport();
        return RuntimeTransport.HTTP == transport
                ? new MCPHttpInteractionClient(getEndpointUri(httpServer), HttpClient.newHttpClient())
                : new MCPStdioInteractionClient(configFile);
    }
    
    protected final MCPInteractionClient createOpenedInteractionClient() throws IOException, InterruptedException {
        MCPInteractionClient result = createInteractionClient();
        result.open();
        return result;
    }
    
    protected final MCPInteractionClient createOpenedInteractionClient(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) throws IOException, InterruptedException {
        MCPInteractionClient result = createInteractionClient(runtimeDatabases);
        result.open();
        return result;
    }
    
    protected final URI getHttpEndpointUri() throws IOException {
        prepareRuntime();
        if (null == httpServer) {
            throw new IllegalStateException("HTTP transport is not enabled for current runtime E2E test.");
        }
        return getEndpointUri(httpServer);
    }
    
    protected final Path getConfigFile() throws IOException {
        prepareRuntime();
        return configFile;
    }
    
    protected abstract Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases();
    
    protected abstract void prepareRuntimeFixture() throws IOException;
    
    protected final void prepareRuntime() throws IOException {
        if (runtimePrepared) {
            return;
        }
        prepareRuntimeFixtureIfNeeded();
        configFile = createConfigurationFile("mcp-http.yaml", getRuntimeDatabases());
        if (RuntimeTransport.HTTP == getTransport()) {
            httpServer = createStartedHttpServer(configFile);
        }
        runtimePrepared = true;
    }
    
    private void prepareRuntimeFixtureIfNeeded() throws IOException {
        if (runtimeFixturePrepared) {
            return;
        }
        prepareRuntimeFixture();
        runtimeFixturePrepared = true;
    }
    
    private StreamableHttpMCPServer createStartedHttpServer(final Path configFile) throws IOException {
        MCPLaunchConfiguration launchConfig = MCPConfigurationLoader.load(configFile.toString());
        MCPRuntimeServer server = new MCPRuntimeLauncher(configFile.toFile().getName()).launch(launchConfig);
        if (!(server instanceof StreamableHttpMCPServer)) {
            server.stop();
            throw new IllegalStateException("HTTP transport must be enabled for launched runtime E2E tests.");
        }
        return (StreamableHttpMCPServer) server;
    }
    
    private URI getEndpointUri(final StreamableHttpMCPServer httpServer) {
        return URI.create(String.format("http://%s:%d%s", LOOPBACK_BIND_HOST, httpServer.getLocalPort(), getHttpEndpointPath()));
    }
    
    private Path createConfigurationFile(final String fileName, final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) throws IOException {
        Path result = tempDir.resolve(fileName);
        Files.writeString(result, createConfigurationContent(runtimeDatabases));
        return result;
    }
    
    private String createConfigurationContent(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        RuntimeTransport transport = getTransport();
        MCPTransportType transportType = RuntimeTransport.HTTP == transport ? MCPTransportType.STREAMABLE_HTTP : MCPTransportType.STDIO;
        return YamlEngine.marshal(new YamlMCPLaunchConfigurationSwapper().swapToYamlConfiguration(
                new MCPLaunchConfiguration(transportType, createHttpTransportConfiguration(), runtimeDatabases)));
    }
    
    protected HttpTransportConfiguration createHttpTransportConfiguration() {
        return new HttpTransportConfiguration(LOOPBACK_BIND_HOST, 0, getHttpEndpointPath());
    }
    
    protected String getHttpEndpointPath() {
        return ENDPOINT_PATH;
    }
}
