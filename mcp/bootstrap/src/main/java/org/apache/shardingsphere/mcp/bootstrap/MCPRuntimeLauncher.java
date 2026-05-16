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

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.authorization.OAuthProtectedResourceMetadataServlet;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * MCP runtime launcher.
 */
public final class MCPRuntimeLauncher {
    
    /**
     * Launch.
     *
     * @param config launch configuration
     * @return MCP runtime server
     * @throws IOException when the active server startup fails
     */
    public MCPRuntimeServer launch(final MCPLaunchConfiguration config) throws IOException {
        return launch(config, "");
    }
    
    /**
     * Launch.
     *
     * @param config launch configuration
     * @param configPath configuration path
     * @return MCP runtime server
     * @throws IOException when the active server startup fails
     */
    public MCPRuntimeServer launch(final MCPLaunchConfiguration config, final String configPath) throws IOException {
        ShardingSpherePreconditions.checkNotNull(config, () -> new IllegalArgumentException("MCP launch configuration cannot be null."));
        config.validate();
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(config.getDatabases()), new MCPDatabaseCapabilityProvider(config.getDatabases()),
                config.getHttpTransport().isEnabled() ? "http" : "stdio");
        MCPRuntimeServer result = config.getHttpTransport().isEnabled() ? new StreamableHttpMCPServer(config.getHttpTransport(), runtimeContext) : new StdioMCPServer(runtimeContext);
        try {
            result.start();
            printStartupHints(createStartupHints(config, result, configPath));
        } catch (final IOException ex) {
            result.stop();
            throw new IOException(String.format("Failed to start %s server.", config.getHttpTransport().isEnabled() ? "HTTP" : "STDIO"), ex);
        }
        return result;
    }
    
    List<String> createStartupHints(final MCPLaunchConfiguration config, final MCPRuntimeServer server, final String configPath) {
        List<String> result = new LinkedList<>();
        result.add("ShardingSphere MCP runtime started.");
        result.add("Configuration: " + (Objects.toString(configPath, "").isBlank() ? "conf/mcp-http.yaml" : configPath));
        result.add("Logs: logs/mcp.log");
        result.add("Runtime databases: " + config.getDatabases().size());
        if (config.getHttpTransport().isEnabled()) {
            int port = server instanceof StreamableHttpMCPServer ? ((StreamableHttpMCPServer) server).getLocalPort() : config.getHttpTransport().getPort();
            result.add("HTTP endpoint: http://" + config.getHttpTransport().getBindHost() + ":" + port + config.getHttpTransport().getEndpointPath());
            result.add("HTTP bearer token: " + (Objects.toString(config.getHttpTransport().getAccessToken(), "").isBlank() ? "not configured" : "required"));
            if (config.getHttpTransport().isProtectedResourceMetadataEnabled()) {
                result.add("OAuth protected resource metadata: http://" + config.getHttpTransport().getBindHost() + ":" + port
                        + OAuthProtectedResourceMetadataServlet.createEndpointWellKnownPath(config.getHttpTransport().getEndpointPath()));
            }
            result.add("Official list discovery: tools/list, resources/list, resources/templates/list, prompts/list");
            result.add("Argument completion: completion/complete");
            result.add("Optional domain catalog resource: shardingsphere://capabilities");
            return result;
        }
        result.add("STDIO transport: enabled");
        result.add("STDIO stdout: reserved for MCP protocol frames; send diagnostics to stderr or logs.");
        result.add("Official list discovery: tools/list, resources/list, resources/templates/list, prompts/list");
        result.add("Argument completion: completion/complete");
        result.add("Optional domain catalog resource: shardingsphere://capabilities");
        return result;
    }
    
    private void printStartupHints(final List<String> startupHints) {
        startupHints.forEach(System.err::println);
    }
}
