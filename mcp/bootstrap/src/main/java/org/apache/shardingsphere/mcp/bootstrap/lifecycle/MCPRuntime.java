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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;

import java.util.Optional;

/**
 * Running MCP runtime handle.
 */
@RequiredArgsConstructor
public final class MCPRuntime implements AutoCloseable {
    
    private final StreamableHttpMCPServer httpServer;
    
    private final StdioMCPServer stdioServer;
    
    /**
     * Get the HTTP server when one exists.
     *
     * @return optional HTTP server
     */
    public Optional<StreamableHttpMCPServer> getHttpServer() {
        return Optional.ofNullable(httpServer);
    }
    
    /**
     * Get the STDIO server when one exists.
     *
     * @return optional STDIO server
     */
    public Optional<StdioMCPServer> getStdioServer() {
        return Optional.ofNullable(stdioServer);
    }
    
    @Override
    public void close() {
        if (null != httpServer) {
            httpServer.stop();
        }
    }
}
