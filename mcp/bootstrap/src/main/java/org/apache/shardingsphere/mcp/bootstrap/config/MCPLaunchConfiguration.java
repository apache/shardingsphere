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

package org.apache.shardingsphere.mcp.bootstrap.config;

import lombok.Getter;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;

import java.util.Map;

/**
 * MCP launch configuration.
 */
public final class MCPLaunchConfiguration {
    
    private final HttpTransportConfiguration httpTransport;
    
    @Getter
    private final Map<String, RuntimeDatabaseConfiguration> databases;
    
    /**
     * Constructs an HTTP MCP launch configuration.
     *
     * @param httpTransport HTTP transport configuration
     * @param databases runtime database configurations
     */
    public MCPLaunchConfiguration(final HttpTransportConfiguration httpTransport, final Map<String, RuntimeDatabaseConfiguration> databases) {
        ShardingSpherePreconditions.checkNotNull(httpTransport, () -> new IllegalArgumentException("HTTP transport configuration cannot be null."));
        this.httpTransport = httpTransport;
        this.databases = databases;
    }
    
    /**
     * Constructs a STDIO MCP launch configuration.
     *
     * @param databases runtime database configurations
     */
    public MCPLaunchConfiguration(final Map<String, RuntimeDatabaseConfiguration> databases) {
        httpTransport = null;
        this.databases = databases;
    }
    
    /**
     * Get transport type.
     *
     * @return transport type
     */
    public MCPTransportType getTransportType() {
        return null == httpTransport ? MCPTransportType.STDIO : MCPTransportType.HTTP;
    }
    
    /**
     * Get HTTP transport configuration.
     *
     * @return HTTP transport configuration
     */
    public HttpTransportConfiguration getHttpTransport() {
        ShardingSpherePreconditions.checkState(null != httpTransport,
                () -> new IllegalStateException("HTTP transport configuration is unavailable for STDIO transport."));
        return httpTransport;
    }
}
