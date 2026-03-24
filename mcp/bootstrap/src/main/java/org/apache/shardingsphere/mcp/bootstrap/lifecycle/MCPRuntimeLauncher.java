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
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Launch the MCP runtime with Streamable HTTP and STDIO transports.
 */
public final class MCPRuntimeLauncher {
    
    private static final String TRANSPORT_VALIDATION_ERROR_MESSAGE = "At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true.";
    
    private final DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
    
    private final JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
    
    /**
     * Launch the MCP runtime with the supplied transport configuration.
     *
     * @param launchConfiguration launch configuration
     * @return launch state
     */
    public MCPLaunchState launch(final MCPLaunchConfiguration launchConfiguration) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        return launch(serverRegistry, launchConfiguration);
    }
    
    /**
     * Launch the MCP runtime on one existing bootstrap instance.
     *
     * @param serverRegistry server registry
     * @param launchConfiguration launch configuration
     * @return launch state
     */
    public MCPLaunchState launch(final MCPServerRegistry serverRegistry, final MCPLaunchConfiguration launchConfiguration) {
        MCPServerRegistry actualServerRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        MCPLaunchConfiguration actualLaunchConfiguration = Objects.requireNonNull(launchConfiguration, "launchConfiguration cannot be null");
        validateTransportConfiguration(actualLaunchConfiguration.getTransport());
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = createConnectionConfigurations(actualLaunchConfiguration);
        MetadataCatalog metadataCatalog = metadataLoader.load(connectionConfigurations);
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, metadataLoader);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(actualServerRegistry.getSessionManager(), metadataCatalog, databaseRuntime);
        return launch(actualServerRegistry, runtimeServices, actualLaunchConfiguration, metadataCatalog, databaseRuntime);
    }
    
    /**
     * Launch the MCP runtime with caller-provided runtime dependencies.
     *
     * @param serverRegistry server registry
     * @param runtimeServices runtime services
     * @param launchConfiguration launch configuration
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     * @return launch state
     * @throws IllegalStateException when HTTP transport startup fails
     */
    public MCPLaunchState launch(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices, final MCPLaunchConfiguration launchConfiguration,
                                 final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        MCPServerRegistry actualServerRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        MCPRuntimeServices actualRuntimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null");
        MCPLaunchConfiguration actualLaunchConfiguration = Objects.requireNonNull(launchConfiguration, "launchConfiguration cannot be null");
        MetadataCatalog actualMetadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        DatabaseRuntime actualDatabaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
        validateTransportConfiguration(actualLaunchConfiguration.getTransport());
        actualRuntimeServices.registerDefaults(actualServerRegistry);
        StreamableHttpMCPServer httpServer = createHttpServer(actualServerRegistry, actualRuntimeServices, actualLaunchConfiguration, actualMetadataCatalog, actualDatabaseRuntime);
        StdioMCPServer stdioServer = createStdioServer(actualServerRegistry, actualRuntimeServices, actualLaunchConfiguration);
        try {
            actualServerRegistry.start();
            if (null != httpServer) {
                httpServer.start();
            }
            if (null != stdioServer) {
                stdioServer.start();
            }
        } catch (final IOException ex) {
            if (null != httpServer) {
                httpServer.stop();
            }
            if (null != stdioServer) {
                stdioServer.stop();
            }
            actualServerRegistry.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        return new MCPLaunchState(actualServerRegistry, actualRuntimeServices, toTransportList(httpServer), toTransportList(stdioServer));
    }
    
    private void validateTransportConfiguration(final MCPTransportConfiguration transportConfig) {
        if (!transportConfig.hasEnabledTransport()) {
            throw new IllegalArgumentException(TRANSPORT_VALIDATION_ERROR_MESSAGE);
        }
    }
    
    private Map<String, DatabaseConnectionConfiguration> createConnectionConfigurations(final MCPLaunchConfiguration launchConfiguration) {
        if (launchConfiguration.getRuntimeDatabases().isEmpty()) {
            throw new IllegalArgumentException("`runtimeDatabases` must be configured for the default launch path.");
        }
        return databaseRuntimeFactory.createConnectionConfigurations(launchConfiguration.getRuntimeDatabases());
    }
    
    private StreamableHttpMCPServer createHttpServer(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                                                     final MCPLaunchConfiguration launchConfiguration, final MetadataCatalog metadataCatalog,
                                                     final DatabaseRuntime databaseRuntime) {
        if (!launchConfiguration.getTransport().getHttp().isEnabled()) {
            return null;
        }
        return new StreamableHttpMCPServer(launchConfiguration.getTransport().getHttp(), Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null"),
                Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null"), Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null"),
                Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null"));
    }
    
    private StdioMCPServer createStdioServer(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                                             final MCPLaunchConfiguration launchConfiguration) {
        return launchConfiguration.getTransport().getStdio().isEnabled() ? new StdioMCPServer(serverRegistry.getSessionManager(), runtimeServices) : null;
    }
    
    private static <T> Collection<T> toTransportList(final T transport) {
        return null == transport ? Collections.emptyList() : Collections.singletonList(transport);
    }
}
