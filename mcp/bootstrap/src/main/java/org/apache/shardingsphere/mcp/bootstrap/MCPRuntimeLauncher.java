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

package org.apache.shardingsphere.mcp.bootstrap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportType;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;

import java.io.IOException;
import java.util.List;

/**
 * MCP runtime launcher.
 */
@RequiredArgsConstructor
@Slf4j
public final class MCPRuntimeLauncher {
    
    private static final String LOG_PATH = "logs/mcp.log";
    
    private final String configPath;
    
    /**
     * Launch.
     *
     * @param config launch configuration
     * @return MCP runtime server
     * @throws IOException when the active server startup fails
     */
    public MCPRuntimeServer launch(final MCPLaunchConfiguration config) throws IOException {
        ShardingSpherePreconditions.checkNotNull(config, () -> new IllegalArgumentException("MCP launch configuration cannot be null."));
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(config.getDatabases()), new MCPDatabaseCapabilityProvider(config.getDatabases()),
                isHttpTransport(config) ? "http" : "stdio");
        MCPRuntimeServer result = isHttpTransport(config) ? new StreamableHttpMCPServer(config.getHttpTransport(), runtimeContext) : new StdioMCPServer(runtimeContext);
        try {
            result.start();
            createStartupLogMessages(config, result).forEach(log::info);
        } catch (final IOException ex) {
            result.stop();
            throw new IOException(String.format("Failed to start %s server.", isHttpTransport(config) ? "HTTP" : "STDIO"), ex);
        }
        return result;
    }
    
    List<String> createStartupLogMessages(final MCPLaunchConfiguration config, final MCPRuntimeServer server) {
        return isHttpTransport(config) ? createHttpStartupLogMessages(config, server) : createStdioStartupLogMessages(config);
    }
    
    private List<String> createHttpStartupLogMessages(final MCPLaunchConfiguration config, final MCPRuntimeServer server) {
        int port = server instanceof StreamableHttpMCPServer ? ((StreamableHttpMCPServer) server).getLocalPort() : config.getHttpTransport().getPort();
        String endpoint = String.format("http://%s:%d%s", config.getHttpTransport().getBindHost(), port, config.getHttpTransport().getEndpointPath());
        return List.of(String.format("ShardingSphere MCP runtime started, transport=http, config=%s, databases=%d, endpoint=%s, authorization=none, logs=%s.",
                configPath, config.getDatabases().size(), endpoint, LOG_PATH));
    }
    
    private List<String> createStdioStartupLogMessages(final MCPLaunchConfiguration config) {
        return List.of(String.format("ShardingSphere MCP runtime started, transport=stdio, config=%s, databases=%d, logs=%s. Stdout is reserved for MCP protocol frames.",
                configPath, config.getDatabases().size(), LOG_PATH));
    }
    
    private boolean isHttpTransport(final MCPLaunchConfiguration config) {
        return MCPTransportType.STREAMABLE_HTTP == config.getTransportType();
    }
}
