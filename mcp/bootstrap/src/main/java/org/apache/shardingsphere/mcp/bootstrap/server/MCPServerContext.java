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

package org.apache.shardingsphere.mcp.bootstrap.server;

import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Objects;

/**
 * Compatibility wrapper for {@link MCPServerRegistry}.
 *
 * @deprecated Prefer {@link MCPServerRegistry}.
 */
@Deprecated
public final class MCPServerContext {
    
    private final MCPServerRegistry serverRegistry;
    
    public MCPServerContext(final MCPSessionManager sessionManager) {
        serverRegistry = new MCPServerRegistry(sessionManager);
    }
    
    private MCPServerContext(final MCPServerRegistry serverRegistry) {
        this.serverRegistry = Objects.requireNonNull(serverRegistry, "serverRegistry cannot be null");
    }
    
    /**
     * Create one compatibility wrapper from the current server registry.
     *
     * @param serverRegistry server registry
     * @return compatibility wrapper
     */
    public static MCPServerContext fromRegistry(final MCPServerRegistry serverRegistry) {
        return new MCPServerContext(serverRegistry);
    }
    
    /**
     * Get the shared session manager.
     *
     * @return session manager
     */
    public MCPSessionManager getSessionManager() {
        return serverRegistry.getSessionManager();
    }
    
    /**
     * Get the underlying server registry.
     *
     * @return server registry
     */
    public MCPServerRegistry getServerRegistry() {
        return serverRegistry;
    }
    
    /**
     * Register a public MCP resource identifier.
     *
     * @param resourceName resource identifier
     */
    public void registerResource(final String resourceName) {
        serverRegistry.registerResource(resourceName);
    }
    
    /**
     * Register a public MCP tool identifier.
     *
     * @param toolName tool identifier
     */
    public void registerTool(final String toolName) {
        serverRegistry.registerTool(toolName);
    }
    
    /**
     * Start the server context.
     */
    public void start() {
        serverRegistry.start();
    }
    
    /**
     * Stop the server context.
     */
    public void stop() {
        serverRegistry.stop();
    }
    
    /**
     * Check whether the server registry is running.
     *
     * @return running status
     */
    public boolean isRunning() {
        return serverRegistry.isRunning();
    }
    
    /**
     * Get a stable snapshot of the current registration state.
     *
     * @return registration snapshot
     */
    public RegistrationSnapshot snapshot() {
        MCPServerRegistry.RegistrationSnapshot actual = serverRegistry.snapshot();
        return new RegistrationSnapshot(actual.getResources(), actual.getTools(), actual.isRunning());
    }
}
