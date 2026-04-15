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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public abstract class AbstractConfigBackedRuntimeE2ETest {
    
    private static final String ENDPOINT_PATH = "/gateway";
    
    @TempDir
    private Path tempDir;
    
    private StreamableHttpMCPServer httpServer;
    
    @AfterEach
    protected void tearDown() {
        if (null != httpServer) {
            httpServer.stop();
            httpServer = null;
        }
    }
    
    protected final void launchRuntime() throws IOException {
        prepareRuntimeFixture();
        Path configFile = tempDir.resolve("mcp.yaml");
        Files.writeString(configFile, createConfigurationContent());
        httpServer = createStartedHttpServer(configFile);
    }
    
    protected final HttpClient createHttpClient() {
        return HttpClient.newHttpClient();
    }
    
    protected abstract Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases();
    
    protected abstract void prepareRuntimeFixture() throws IOException;
    
    protected final Path getTempDir() {
        return tempDir;
    }
    
    protected final URI getEndpointUri() {
        return createEndpointUri();
    }
    
    private URI createEndpointUri() {
        int localPort = httpServer.getLocalPort();
        return URI.create(String.format("http://127.0.0.1:%d%s", localPort, ENDPOINT_PATH));
    }
    
    private StreamableHttpMCPServer createStartedHttpServer(final Path configFile) throws IOException {
        MCPLaunchConfiguration launchConfiguration = MCPConfigurationLoader.load(configFile.toString());
        MCPRuntimeServer server = new MCPRuntimeLauncher().launch(launchConfiguration);
        if (!(server instanceof StreamableHttpMCPServer)) {
            server.stop();
            throw new IllegalStateException("HTTP transport must be enabled for launched runtime E2E tests.");
        }
        return (StreamableHttpMCPServer) server;
    }
    
    private String createConfigurationContent() {
        return YamlEngine.marshal(new YamlMCPLaunchConfigurationSwapper().swapToYamlConfiguration(new MCPLaunchConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", false, "", 0, ENDPOINT_PATH), new StdioTransportConfiguration(false), getRuntimeDatabases())));
    }
}
