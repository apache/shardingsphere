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

package org.apache.shardingsphere.mcp.bootstrap.lifecycle;

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseConnectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory;
import org.apache.shardingsphere.mcp.bootstrap.runtime.JdbcMetadataLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Launch the MCP runtime with Streamable HTTP and STDIO transports.
 */
public final class MCPRuntimeLauncher {
    
    private final DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
    
    private final JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
    
    /**
     * Launch the MCP runtime on one existing bootstrap instance.
     *
     * @param config launch configuration
     * @return runtime handle
     * @throws IllegalStateException when HTTP transport startup fails
     */
    public MCPRuntime launch(final MCPLaunchConfiguration config) {
        MCPLaunchConfiguration actualConfig = Objects.requireNonNull(config, "launchConfiguration cannot be null");
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = databaseRuntimeFactory.createConnectionConfigurations(actualConfig.getRuntimeDatabases());
        MetadataCatalog metadataCatalog = metadataLoader.load(connectionConfigurations);
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, metadataLoader);
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
        validateTransportConfiguration(actualConfig.getTransport());
        StreamableHttpMCPServer httpServer = createHttpServer(sessionManager, runtimeServices, actualConfig, metadataCatalog, databaseRuntime);
        StdioMCPServer stdioServer = createStdioServer(sessionManager, runtimeServices, actualConfig);
        MCPRuntime result = new MCPRuntime(httpServer, stdioServer);
        try {
            if (null != httpServer) {
                httpServer.start();
            }
        } catch (final IOException ex) {
            result.close();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        return result;
    }
    
    private void validateTransportConfiguration(final MCPTransportConfiguration transportConfig) {
        if (!transportConfig.hasEnabledTransport()) {
            throw new IllegalArgumentException("At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true.");
        }
    }
    
    private StreamableHttpMCPServer createHttpServer(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                                                     final MCPLaunchConfiguration launchConfiguration, final MetadataCatalog metadataCatalog,
                                                     final DatabaseRuntime databaseRuntime) {
        if (!launchConfiguration.getTransport().getHttp().isEnabled()) {
            return null;
        }
        return new StreamableHttpMCPServer(launchConfiguration.getTransport().getHttp(), sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
    }
    
    private StdioMCPServer createStdioServer(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                                             final MCPLaunchConfiguration launchConfiguration) {
        return launchConfiguration.getTransport().getStdio().isEnabled() ? new StdioMCPServer(sessionManager, runtimeServices) : null;
    }
    
}
