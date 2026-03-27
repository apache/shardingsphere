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
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.runtime.DatabaseRuntimeFactory;
import org.apache.shardingsphere.mcp.bootstrap.runtime.JdbcMetadataLoader;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioTransportMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;

import java.io.IOException;

/**
 * MCP runtime launcher.
 */
public final class MCPRuntimeLauncher {
    
    /**
     * Launch.
     *
     * @param config launch configuration
     * @return MCP runtime transport
     * @throws IllegalStateException when the active transport startup fails
     */
    public MCPRuntimeTransport launch(final MCPLaunchConfiguration config) {
        DatabaseRuntimeFactory databaseRuntimeFactory = new DatabaseRuntimeFactory();
        JdbcMetadataLoader metadataLoader = new JdbcMetadataLoader();
        MetadataCatalog metadataCatalog = metadataLoader.load(config.getRuntimeDatabases());
        DatabaseRuntime databaseRuntime = databaseRuntimeFactory.createDatabaseRuntime(config.getRuntimeDatabases(), metadataCatalog, metadataLoader);
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager, metadataCatalog, databaseRuntime);
        MCPRuntimeTransport result = createTransport(config, sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
        try {
            result.start();
        } catch (final IOException ex) {
            result.close();
            throw new IllegalStateException(String.format("Failed to start %s transport.", config.getTransport().getHttp().isEnabled() ? "HTTP" : "STDIO"), ex);
        }
        return result;
    }
    
    private MCPRuntimeTransport createTransport(final MCPLaunchConfiguration config, final MCPSessionManager sessionManager,
                                                final MCPRuntimeServices runtimeServices, final MetadataCatalog metadataCatalog, final DatabaseRuntime databaseRuntime) {
        return config.getTransport().getHttp().isEnabled()
                ? new StreamableHttpMCPServer(config.getTransport().getHttp(), sessionManager, runtimeServices, metadataCatalog, databaseRuntime)
                : new StdioTransportMCPServer(sessionManager, runtimeServices, metadataCatalog, databaseRuntime);
    }
}
