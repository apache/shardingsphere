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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPRuntimeServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.server.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

/**
 * SDK-backed STDIO MCP server.
 */
public final class StdioMCPServer implements MCPRuntimeServer {
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private final SessionManagedStdioTransportProvider transportProvider;
    
    private McpSyncServer syncServer;
    
    public StdioMCPServer(final MCPRuntimeContext runtimeContext) {
        McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
        syncServerFactory = new MCPSyncServerFactory(runtimeContext, jsonMapper);
        transportProvider = new SessionManagedStdioTransportProvider(runtimeContext, jsonMapper);
    }
    
    @Override
    public void start() {
        if (null == syncServer) {
            syncServer = syncServerFactory.create(transportProvider);
        }
    }
    
    @Override
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
    }
}
