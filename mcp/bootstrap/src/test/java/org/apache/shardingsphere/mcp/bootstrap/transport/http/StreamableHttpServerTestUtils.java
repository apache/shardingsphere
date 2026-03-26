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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseConnectionConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory;
import org.apache.shardingsphere.mcp.bootstrap.runtime.JdbcMetadataLoader;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;
import java.util.Map;

/**
 * Test utilities for started HTTP MCP servers.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
final class StreamableHttpServerTestUtils {
    
    static StreamableHttpMCPServer start(final MCPLaunchConfiguration launchConfiguration) {
        if (!launchConfiguration.getTransport().getHttp().isEnabled()) {
            throw new IllegalArgumentException("HTTP transport must be enabled for HTTP integration tests.");
        }
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
        Map<String, DatabaseConnectionConfiguration> connectionConfigurations = databaseRuntimeFactory.createConnectionConfigurations(launchConfiguration.getRuntimeDatabases());
        MetadataCatalog metadataCatalog = metadataLoader.load(connectionConfigurations);
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(connectionConfigurations, metadataCatalog, metadataLoader);
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
        StreamableHttpMCPServer result = new StreamableHttpMCPServer(launchConfiguration.getTransport().getHttp(),
                sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
        try {
            result.start();
        } catch (final IOException ex) {
            result.stop();
            throw new IllegalStateException("Failed to start HTTP transport.", ex);
        }
        return result;
    }
}
