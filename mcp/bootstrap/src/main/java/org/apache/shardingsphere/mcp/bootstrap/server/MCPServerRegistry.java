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

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/**
 * MCP server registry with runtime registration support.
 */
public final class MCPServerRegistry {
    
    @Getter
    private final MCPSessionManager sessionManager;
    
    private final Set<String> registeredResources = new LinkedHashSet<>();
    
    private final Set<String> registeredTools = new LinkedHashSet<>();
    
    @Getter
    private boolean running;
    
    public MCPServerRegistry(final MCPSessionManager sessionManager) {
        this.sessionManager = Objects.requireNonNull(sessionManager, "sessionManager cannot be null");
    }
    
    /**
     * Register a public MCP resource identifier.
     *
     * @param resourceName resource identifier
     */
    public void registerResource(final String resourceName) {
        registeredResources.add(normalizeName(resourceName, "resourceName"));
    }
    
    /**
     * Register a public MCP tool identifier.
     *
     * @param toolName tool identifier
     */
    public void registerTool(final String toolName) {
        registeredTools.add(normalizeName(toolName, "toolName"));
    }
    
    private String normalizeName(final String value, final String fieldName) {
        String result = Objects.requireNonNull(value, fieldName + " cannot be null").trim();
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new IllegalArgumentException(fieldName + " cannot be empty."));
        return result;
    }
    
    /**
     * Start the server registry.
     */
    public void start() {
        running = true;
    }
    
    /**
     * Stop the server registry.
     */
    public void stop() {
        running = false;
    }
    
    /**
     * Get a stable snapshot of the current registration state.
     *
     * @return registration snapshot
     */
    public RegistrationSnapshot snapshot() {
        return new RegistrationSnapshot(registeredResources, registeredTools, running);
    }
    
    /**
     * Immutable registration snapshot.
     */
    @Getter
    public static final class RegistrationSnapshot {
        
        private final Set<String> resources;
        
        private final Set<String> tools;
        
        private final boolean running;
        
        /**
         * Construct a registration snapshot.
         *
         * @param resources registered resources
         * @param tools registered tools
         * @param running runtime state
         */
        public RegistrationSnapshot(final Set<String> resources, final Set<String> tools, final boolean running) {
            this.resources = Collections.unmodifiableSet(new LinkedHashSet<>(resources));
            this.tools = Collections.unmodifiableSet(new LinkedHashSet<>(tools));
            this.running = running;
        }
    }
}
