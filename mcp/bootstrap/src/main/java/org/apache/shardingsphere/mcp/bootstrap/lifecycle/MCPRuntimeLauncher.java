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
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.MCPLaunchRuntimeLoader;
import org.apache.shardingsphere.mcp.bootstrap.runtime.LoadedRuntime;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerContext;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Launch the MCP runtime with Streamable HTTP and STDIO transports.
 */
public final class MCPRuntimeLauncher {
    
    /**
     * Launch the MCP runtime with the supplied transport configuration.
     *
     * @param launchConfiguration launch configuration
     * @return launch state
     */
    public LaunchState launch(final MCPLaunchConfiguration launchConfiguration) {
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
    public LaunchState launch(final MCPServerRegistry serverRegistry, final MCPLaunchConfiguration launchConfiguration) {
        MCPServerRegistry actualServerRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        MCPLaunchConfiguration actualLaunchConfiguration = Objects.requireNonNull(launchConfiguration, "launchConfiguration cannot be null");
        validateTransportConfiguration(actualLaunchConfiguration);
        LoadedRuntime loadedRuntime = new MCPLaunchRuntimeLoader().load(actualLaunchConfiguration);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(actualServerRegistry.getSessionManager(), loadedRuntime.getMetadataCatalog(), loadedRuntime.getDatabaseRuntime());
        return launch(actualServerRegistry, runtimeServices, actualLaunchConfiguration, loadedRuntime.getMetadataCatalog(), loadedRuntime.getDatabaseRuntime());
    }
    
    /**
     * Launch the MCP runtime on one existing bootstrap instance.
     *
     * @param serverContext server context
     * @param launchConfiguration launch configuration
     * @return launch state
     * @deprecated Prefer {@link #launch(MCPServerRegistry, MCPLaunchConfiguration)}.
     */
    @Deprecated
    public LaunchState launch(final MCPServerContext serverContext, final MCPLaunchConfiguration launchConfiguration) {
        return launch(Objects.requireNonNull(serverContext, "serverContext cannot be null").getServerRegistry(), launchConfiguration);
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
    public LaunchState launch(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices, final MCPLaunchConfiguration launchConfiguration,
                              final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        MCPServerRegistry actualServerRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
        MCPRuntimeServices actualRuntimeServices = Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null");
        MCPLaunchConfiguration actualLaunchConfiguration = Objects.requireNonNull(launchConfiguration, "launchConfiguration cannot be null");
        MetadataCatalog actualMetadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        DatabaseRuntime actualDatabaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
        validateTransportConfiguration(actualLaunchConfiguration);
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
        return new LaunchState(actualServerRegistry, actualRuntimeServices, toTransportList(httpServer), toTransportList(stdioServer));
    }
    
    /**
     * Launch the MCP runtime with caller-provided runtime dependencies.
     *
     * @param serverContext server context
     * @param runtimeContext runtime context
     * @param launchConfiguration launch configuration
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     * @return launch state
     * @throws IllegalStateException when HTTP transport startup fails
     * @deprecated Prefer {@link #launch(MCPServerRegistry, MCPRuntimeServices, MCPLaunchConfiguration, MetadataCatalog, DatabaseRuntime)}.
     */
    @Deprecated
    public LaunchState launch(final MCPServerContext serverContext, final MCPRuntimeContext runtimeContext, final MCPLaunchConfiguration launchConfiguration,
                              final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        return launch(Objects.requireNonNull(serverContext, "serverContext cannot be null").getServerRegistry(),
                Objects.requireNonNull(runtimeContext, "runtimeContext cannot be null").getRuntimeServices(), launchConfiguration, metadataCatalog, databaseRuntime);
    }
    
    private void validateTransportConfiguration(final MCPLaunchConfiguration launchConfiguration) {
        if (!launchConfiguration.isHttpEnabled() && !launchConfiguration.isStdioEnabled()) {
            throw new IllegalArgumentException("At least one transport must be enabled.");
        }
    }
    
    private StreamableHttpMCPServer createHttpServer(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                                                     final MCPLaunchConfiguration launchConfiguration, final MetadataCatalog metadataCatalog,
                                                     final DatabaseRuntime databaseRuntime) {
        if (!launchConfiguration.isHttpEnabled()) {
            return null;
        }
        return new StreamableHttpMCPServer(launchConfiguration.getHttpServerConfiguration(), Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null"),
                Objects.requireNonNull(runtimeServices, "runtimeServices cannot be null"), Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null"),
                Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null"));
    }
    
    private StdioMCPServer createStdioServer(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                                             final MCPLaunchConfiguration launchConfiguration) {
        return launchConfiguration.isStdioEnabled() ? new StdioMCPServer(serverRegistry.getSessionManager(), runtimeServices) : null;
    }
    
    private static <T> Collection<T> toTransportList(final T transport) {
        return null == transport ? Collections.emptyList() : Collections.singletonList(transport);
    }
}
