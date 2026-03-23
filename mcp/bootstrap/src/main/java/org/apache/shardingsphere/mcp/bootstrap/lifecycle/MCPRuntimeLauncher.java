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

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.mcp.bootstrap.runtime.MCPRuntimeProvider.LoadedRuntime;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerContext;
import org.apache.shardingsphere.mcp.bootstrap.runtime.ProductionRuntimeLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.execute.ExecuteQueryFacade.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataResourceLoader.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

/**
 * Launch the MCP runtime with Streamable HTTP and STDIO transports.
 */
public final class MCPRuntimeLauncher {
    
    /**
     * Launch the MCP runtime with the supplied transport configuration.
     *
     * @param runtimeConfiguration runtime configuration
     * @return launch state
     */
    public LaunchState launch(final RuntimeConfiguration runtimeConfiguration) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerContext serverContext = new MCPServerContext(sessionManager);
        return launch(serverContext, runtimeConfiguration);
    }
    
    /**
     * Launch the MCP runtime on one existing bootstrap instance.
     *
     * @param serverContext server context
     * @param runtimeConfiguration runtime configuration
     * @return launch state
     * @throws IllegalArgumentException when both transports are disabled
     * @throws IllegalStateException when HTTP transport startup fails
     */
    public LaunchState launch(final MCPServerContext serverContext, final RuntimeConfiguration runtimeConfiguration) {
        MCPServerContext actualServerContext = Objects.requireNonNull(serverContext, "serverContext cannot be null");
        RuntimeConfiguration actualRuntimeConfiguration = Objects.requireNonNull(runtimeConfiguration, "runtimeConfiguration cannot be null");
        validateTransportConfiguration(actualRuntimeConfiguration);
        LoadedRuntime loadedRuntime = new ProductionRuntimeLoader().load(actualRuntimeConfiguration);
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(actualServerContext.getSessionManager(), loadedRuntime.getMetadataCatalog(), loadedRuntime.getDatabaseRuntime());
        return launch(actualServerContext, runtimeContext, actualRuntimeConfiguration, loadedRuntime.getMetadataCatalog(), loadedRuntime.getDatabaseRuntime());
    }
    
    /**
     * Launch the MCP runtime with caller-provided runtime dependencies.
     *
     * @param serverContext server context
     * @param runtimeContext runtime context
     * @param runtimeConfiguration runtime configuration
     * @param metadataCatalog metadata catalog
     * @param databaseRuntime database runtime
     * @return launch state
     * @throws IllegalArgumentException when both transports are disabled
     * @throws IllegalStateException when HTTP transport startup fails
     */
    public LaunchState launch(final MCPServerContext serverContext, final MCPRuntimeContext runtimeContext, final RuntimeConfiguration runtimeConfiguration,
                              final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        MCPServerContext actualServerContext = Objects.requireNonNull(serverContext, "serverContext cannot be null");
        MCPRuntimeContext actualRuntimeContext = Objects.requireNonNull(runtimeContext, "runtimeContext cannot be null");
        RuntimeConfiguration actualRuntimeConfiguration = Objects.requireNonNull(runtimeConfiguration, "runtimeConfiguration cannot be null");
        MetadataCatalog actualMetadataCatalog = Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null");
        DatabaseRuntime actualDatabaseRuntime = Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null");
        validateTransportConfiguration(actualRuntimeConfiguration);
        actualRuntimeContext.registerDefaults(actualServerContext);
        StreamableHttpMCPServer httpServer = createHttpServer(actualServerContext, actualRuntimeContext, actualRuntimeConfiguration, actualMetadataCatalog, actualDatabaseRuntime);
        StdioMCPServer stdioServer = createStdioServer(actualServerContext, actualRuntimeContext, actualRuntimeConfiguration);
        try {
            actualServerContext.start();
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
            actualServerContext.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        return new LaunchState(actualServerContext, actualRuntimeContext, toTransportList(httpServer), toTransportList(stdioServer));
    }
    
    private void validateTransportConfiguration(final RuntimeConfiguration runtimeConfiguration) {
        if (!runtimeConfiguration.isHttpEnabled() && !runtimeConfiguration.isStdioEnabled()) {
            throw new IllegalArgumentException("At least one transport must be enabled.");
        }
    }
    
    private StreamableHttpMCPServer createHttpServer(final MCPServerContext serverContext, final MCPRuntimeContext runtimeContext,
                                                     final RuntimeConfiguration runtimeConfiguration, final MetadataCatalog metadataCatalog,
                                                     final DatabaseRuntime databaseRuntime) {
        if (!runtimeConfiguration.isHttpEnabled()) {
            return null;
        }
        return new StreamableHttpMCPServer(runtimeConfiguration.getServerConfiguration(), Objects.requireNonNull(serverContext, "serverContext cannot be null"),
                Objects.requireNonNull(runtimeContext, "runtimeContext cannot be null"), Objects.requireNonNull(metadataCatalog, "metadataCatalog cannot be null"),
                Objects.requireNonNull(databaseRuntime, "databaseRuntime cannot be null"));
    }
    
    private StdioMCPServer createStdioServer(final MCPServerContext serverContext, final MCPRuntimeContext runtimeContext,
                                             final RuntimeConfiguration runtimeConfiguration) {
        return runtimeConfiguration.isStdioEnabled() ? new StdioMCPServer(serverContext.getSessionManager(), runtimeContext) : null;
    }
    
    private static <T> Collection<T> toTransportList(final T transport) {
        return null == transport ? Collections.emptyList() : Collections.singletonList(transport);
    }
    
    /**
     * Runtime transport configuration.
     */
    @Getter
    public static final class RuntimeConfiguration {
        
        private final ServerConfiguration serverConfiguration;
        
        private final boolean httpEnabled;
        
        private final boolean stdioEnabled;
        
        @Getter(AccessLevel.NONE)
        private final boolean runtimePropsConfigured;
        
        @Getter(AccessLevel.NONE)
        private final Properties runtimeProps;
        
        /**
         * Construct one runtime configuration.
         *
         * @param serverConfiguration server configuration
         * @param httpEnabled HTTP enablement
         * @param stdioEnabled STDIO enablement
         */
        public RuntimeConfiguration(final ServerConfiguration serverConfiguration, final boolean httpEnabled, final boolean stdioEnabled) {
            this.serverConfiguration = Objects.requireNonNull(serverConfiguration, "serverConfiguration cannot be null");
            this.httpEnabled = httpEnabled;
            this.stdioEnabled = stdioEnabled;
            runtimePropsConfigured = false;
            runtimeProps = new Properties();
        }
        
        /**
         * Construct one runtime configuration with one explicit runtime property set.
         *
         * @param serverConfiguration server configuration
         * @param httpEnabled HTTP enablement
         * @param stdioEnabled STDIO enablement
         * @param runtimeProps runtime properties
         */
        public RuntimeConfiguration(final ServerConfiguration serverConfiguration, final boolean httpEnabled, final boolean stdioEnabled,
                                    final Properties runtimeProps) {
            this.serverConfiguration = Objects.requireNonNull(serverConfiguration, "serverConfiguration cannot be null");
            this.httpEnabled = httpEnabled;
            this.stdioEnabled = stdioEnabled;
            runtimePropsConfigured = true;
            this.runtimeProps = copyProperties(runtimeProps);
        }
        
        /**
         * Get runtime properties.
         *
         * @return runtime property copy
         */
        public Optional<Properties> getRuntimeProps() {
            return runtimePropsConfigured ? Optional.of(copyProperties(runtimeProps)) : Optional.empty();
        }
        
        private static Properties copyProperties(final Properties props) {
            Objects.requireNonNull(props, "props cannot be null");
            Properties result = new Properties();
            result.putAll(props);
            return result;
        }
        
        /**
         * Server configuration projection.
         */
        @Getter
        public static final class ServerConfiguration {
            
            private final String bindHost;
            
            private final int port;
            
            private final String endpointPath;
            
            /**
             * Construct one server configuration.
             *
             * @param bindHost bind host
             * @param port bind port
             * @param endpointPath endpoint path
             */
            public ServerConfiguration(final String bindHost, final int port, final String endpointPath) {
                this.bindHost = Objects.requireNonNull(bindHost, "bindHost cannot be null");
                this.port = port;
                this.endpointPath = Objects.requireNonNull(endpointPath, "endpointPath cannot be null");
            }
        }
    }
    
    /**
     * Launch result snapshot.
     */
    @Getter
    public static final class LaunchState {
        
        private final MCPServerContext serverContext;
        
        private final MCPRuntimeContext runtimeContext;
        
        @Getter(AccessLevel.NONE)
        private final List<StreamableHttpMCPServer> httpServers;
        
        @Getter(AccessLevel.NONE)
        private final List<StdioMCPServer> stdioServers;
        
        /**
         * Construct one launch result snapshot.
         *
         * @param serverContext server context
         * @param runtimeContext runtime context
         * @param httpServers HTTP server list
         * @param stdioServers STDIO server list
         */
        public LaunchState(final MCPServerContext serverContext, final MCPRuntimeContext runtimeContext, final Collection<StreamableHttpMCPServer> httpServers,
                           final Collection<StdioMCPServer> stdioServers) {
            this.serverContext = Objects.requireNonNull(serverContext, "serverContext cannot be null");
            this.runtimeContext = Objects.requireNonNull(runtimeContext, "runtimeContext cannot be null");
            this.httpServers = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(httpServers, "httpServers cannot be null")));
            this.stdioServers = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(stdioServers, "stdioServers cannot be null")));
        }
        
        /**
         * Get the HTTP server when one exists.
         *
         * @return optional HTTP server
         */
        public Optional<StreamableHttpMCPServer> getHttpServer() {
            return httpServers.isEmpty() ? Optional.empty() : Optional.of(httpServers.get(0));
        }
        
        /**
         * Get the STDIO server when one exists.
         *
         * @return optional STDIO server
         */
        public Optional<StdioMCPServer> getStdioServer() {
            return stdioServers.isEmpty() ? Optional.empty() : Optional.of(stdioServers.get(0));
        }
    }
}
