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
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;

import java.util.List;
import java.util.Optional;

/**
 * MCP launch result snapshot.
 */
@Getter
public final class MCPLaunchState {
    
    private final MCPServerRegistry serverRegistry;
    
    private final MCPRuntimeServices runtimeServices;
    
    @Getter(AccessLevel.NONE)
    private final List<StreamableHttpMCPServer> httpServers;
    
    @Getter(AccessLevel.NONE)
    private final List<StdioMCPServer> stdioServers;
    
    /**
     * Construct one MCP launch result snapshot.
     *
     * @param serverRegistry server registry
     * @param runtimeServices runtime services
     * @param httpServers HTTP server list
     * @param stdioServers STDIO server list
     */
    public MCPLaunchState(final MCPServerRegistry serverRegistry, final MCPRuntimeServices runtimeServices,
                          final List<StreamableHttpMCPServer> httpServers, final List<StdioMCPServer> stdioServers) {
        this.serverRegistry = serverRegistry;
        this.runtimeServices = runtimeServices;
        this.httpServers = httpServers;
        this.stdioServers = stdioServers;
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
