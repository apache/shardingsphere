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
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfigurationValidator;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseConnectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory;
import org.apache.shardingsphere.mcp.bootstrap.runtime.JdbcMetadataLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioTransportMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Map;

/**
 * Launch the MCP runtime bootstrap process.
 */
public final class MCPRuntimeLauncher {
    
    private final DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
    
    private final JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
    
    /**
     * Launch the MCP runtime on one existing bootstrap instance.
     *
     * @param config launch configuration
     * @return active transport handle
     * @throws IllegalStateException when the active transport startup fails
     */
    public MCPRuntimeTransport launch(final MCPLaunchConfiguration config) {
        MCPTransportConfiguration transportConfig = config.getTransport();
        MCPTransportConfigurationValidator.validate(transportConfig);
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = databaseRuntimeFactory.createConnectionConfigurations(config.getRuntimeDatabases());
        MetadataCatalog metadataCatalog = metadataLoader.load(connectionConfigurations);
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, metadataLoader);
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
        MCPRuntimeTransport transport = createTransport(sessionManager, runtimeServices, config, metadataCatalog, databaseRuntime);
        try {
            transport.start();
        } catch (final IOException ex) {
            transport.close();
            throw new IllegalStateException(transportConfig.getHttp().isEnabled() ? "Failed to start HTTP transport." : "Failed to start STDIO transport.", ex);
        }
        return transport;
    }
    
    private MCPRuntimeTransport createTransport(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices,
                                                final MCPLaunchConfiguration launchConfiguration, final MetadataCatalog metadataCatalog,
                                                final DatabaseRuntime databaseRuntime) {
        return launchConfiguration.getTransport().getHttp().isEnabled()
                ? new StreamableHttpMCPServer(launchConfiguration.getTransport().getHttp(), sessionManager, runtimeServices, metadataCatalog, databaseRuntime)
                : new StdioTransportMCPServer(sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
    }
}
