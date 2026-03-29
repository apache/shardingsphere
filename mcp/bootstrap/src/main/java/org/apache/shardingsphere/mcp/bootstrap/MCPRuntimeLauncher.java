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

import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.jdbc.MCPJdbcRuntimeContextAssembler;

import java.io.IOException;

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
        MCPRuntimeContext runtimeContext = new MCPJdbcRuntimeContextAssembler().assemble(config.getDatabases());
        MCPRuntimeServer result = config.getHttpTransport().isEnabled() ? new StreamableHttpMCPServer(config.getHttpTransport(), runtimeContext) : new StdioMCPServer(runtimeContext);
        try {
            result.start();
        } catch (final IOException ex) {
            result.stop();
            throw new IOException(String.format("Failed to start %s server.", config.getHttpTransport().isEnabled() ? "HTTP" : "STDIO"), ex);
        }
        return result;
    }
}
