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
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.loader.MCPConfigurationLoader;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper.YamlMCPLaunchConfigurationSwapper;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPStdioInteractionClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

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

    protected abstract Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases();

    protected abstract void prepareRuntimeFixture() throws IOException;

    private void prepareRuntime() throws IOException {
        if (runtimePrepared) {
            return;
        }
        prepareRuntimeFixtureIfNeeded();
        configFile = createConfigurationFile("mcp.yaml", getRuntimeDatabases());
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
        MCPRuntimeServer server = new MCPRuntimeLauncher().launch(launchConfig);
        if (!(server instanceof StreamableHttpMCPServer)) {
            server.stop();
            throw new IllegalStateException("HTTP transport must be enabled for launched runtime E2E tests.");
        }
        return (StreamableHttpMCPServer) server;
    }

    private URI getEndpointUri(final StreamableHttpMCPServer httpServer) {
        return URI.create(String.format("http://%s:%d%s", LOOPBACK_BIND_HOST, httpServer.getLocalPort(), ENDPOINT_PATH));
    }

    private Path createConfigurationFile(final String fileName, final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) throws IOException {
        Path result = tempDir.resolve(fileName);
        Files.writeString(result, createConfigurationContent(runtimeDatabases));
        return result;
    }

    private String createConfigurationContent(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        RuntimeTransport transport = getTransport();
        HttpTransportConfiguration httpTransportConfig = new HttpTransportConfiguration(RuntimeTransport.HTTP == transport, LOOPBACK_BIND_HOST, false, "", 0, ENDPOINT_PATH);
        StdioTransportConfiguration stdioTransportConfig = new StdioTransportConfiguration(RuntimeTransport.STDIO == transport);
        return YamlEngine.marshal(new YamlMCPLaunchConfigurationSwapper().swapToYamlConfiguration(new MCPLaunchConfiguration(
                httpTransportConfig, stdioTransportConfig, runtimeDatabases)));
    }
}
