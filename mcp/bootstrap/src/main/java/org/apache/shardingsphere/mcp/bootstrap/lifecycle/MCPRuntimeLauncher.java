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
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioTransportMCPServer;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.jdbc.runtime.MCPJdbcRuntimeContextFactory;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * MCP runtime launcher.
 */
public final class MCPRuntimeLauncher {
    
    private final BiFunction<MCPSessionManager, Map<String, RuntimeDatabaseConfiguration>, MCPRuntimeContext> runtimeContextCreator;
    
    public MCPRuntimeLauncher() {
        this(new MCPJdbcRuntimeContextFactory()::create);
    }
    
    MCPRuntimeLauncher(final BiFunction<MCPSessionManager, Map<String, RuntimeDatabaseConfiguration>, MCPRuntimeContext> runtimeContextCreator) {
        this.runtimeContextCreator = runtimeContextCreator;
    }
    
    /**
     * Launch.
     *
     * @param config launch configuration
     * @return MCP runtime transport
     * @throws IllegalStateException when the active transport startup fails
     */
    public MCPRuntimeTransport launch(final MCPLaunchConfiguration config) {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeContext runtimeContext = runtimeContextCreator.apply(sessionManager, config.getDatabases());
        MCPRuntimeTransport result = createTransport(config, runtimeContext);
        try {
            result.start();
        } catch (final IOException ex) {
            result.close();
            throw new IllegalStateException(String.format("Failed to start %s transport.", config.getHttpTransport().isEnabled() ? "HTTP" : "STDIO"), ex);
        }
        return result;
    }
    
    private MCPRuntimeTransport createTransport(final MCPLaunchConfiguration config, final MCPRuntimeContext runtimeContext) {
        return config.getHttpTransport().isEnabled()
                ? new StreamableHttpMCPServer(config.getHttpTransport(), runtimeContext)
                : new StdioTransportMCPServer(runtimeContext);
    }
}
