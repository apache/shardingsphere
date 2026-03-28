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

package org.apache.shardingsphere.mcp.bootstrap.transport.stdio;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSessionCloser;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

/**
 * SDK-backed STDIO MCP transport server.
 */
public final class StdioTransportMCPServer implements MCPRuntimeTransport {
    
    private final SessionManagedStdioTransportProvider transportProvider;
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private final CountDownLatch terminationLatch = new CountDownLatch(1);
    
    private McpSyncServer syncServer;
    
    private boolean running;
    
    public StdioTransportMCPServer(final MCPRuntimeContext runtimeContext) {
        McpJsonMapper jsonMapper = MCPTransportJsonMapperFactory.create();
        transportProvider = createTransportProvider(runtimeContext, jsonMapper);
        syncServerFactory = new MCPSyncServerFactory(runtimeContext, jsonMapper);
    }
    
    private SessionManagedStdioTransportProvider createTransportProvider(final MCPRuntimeContext runtimeContext, final McpJsonMapper jsonMapper) {
        return new SessionManagedStdioTransportProvider(runtimeContext, new MCPSessionCloser(runtimeContext), jsonMapper,
                new LifecycleAwareInputStream(System.in, terminationLatch::countDown), System.out, terminationLatch::countDown);
    }
    
    @Override
    public void start() {
        if (running) {
            return;
        }
        syncServer = syncServerFactory.create(transportProvider);
        running = true;
    }
    
    @Override
    public void stop() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
        running = false;
        terminationLatch.countDown();
    }
    
    @Override
    public void awaitTermination() throws InterruptedException {
        terminationLatch.await();
    }
    
    private static final class LifecycleAwareInputStream extends FilterInputStream {
        
        private final Runnable closeCallback;
        
        private LifecycleAwareInputStream(final InputStream inputStream, final Runnable closeCallback) {
            super(inputStream);
            this.closeCallback = closeCallback;
        }
        
        @Override
        public int read() throws IOException {
            int result = super.read();
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public int read(final byte[] bytes, final int offset, final int length) throws IOException {
            int result = super.read(bytes, offset, length);
            if (-1 == result) {
                closeCallback.run();
            }
            return result;
        }
        
        @Override
        public void close() throws IOException {
            try {
                super.close();
            } finally {
                closeCallback.run();
            }
        }
    }
}
