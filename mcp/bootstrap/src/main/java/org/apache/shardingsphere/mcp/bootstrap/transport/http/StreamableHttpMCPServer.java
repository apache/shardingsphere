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

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPRuntimeTransport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPSyncServerFactory;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportJsonMapperFactory;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * Streamable HTTP MCP server.
 */
public final class StreamableHttpMCPServer implements MCPRuntimeTransport {
    
    private final HttpTransportConfiguration config;
    
    private final MCPRuntimeContext runtimeContext;
    
    private final McpJsonMapper jsonMapper;
    
    private final MCPSyncServerFactory syncServerFactory;
    
    private Tomcat tomcat;
    
    private Connector connector;
    
    private Path baseDirectory;
    
    private StreamableHttpMCPServlet transportProvider;
    
    private McpSyncServer syncServer;
    
    private boolean running;
    
    public StreamableHttpMCPServer(final HttpTransportConfiguration config, final MCPRuntimeContext runtimeContext) {
        this.config = config;
        this.runtimeContext = runtimeContext;
        jsonMapper = MCPTransportJsonMapperFactory.create();
        syncServerFactory = new MCPSyncServerFactory(runtimeContext, jsonMapper);
    }
    
    @Override
    public void start() throws IOException {
        if (running) {
            return;
        }
        transportProvider = new StreamableHttpMCPServlet(runtimeContext, jsonMapper, config.getBindHost(), config.getEndpointPath());
        syncServer = syncServerFactory.create(transportProvider);
        try {
            tomcat = new Tomcat();
            connector = new Connector();
            connector.setPort(config.getPort());
            connector.setProperty("address", config.getBindHost());
            tomcat.setConnector(connector);
            baseDirectory = Files.createTempDirectory("shardingsphere-mcp-tomcat");
            tomcat.setBaseDir(baseDirectory.toString());
            Context context = tomcat.addContext("", baseDirectory.toString());
            ((StandardContext) context).setClearReferencesRmiTargets(false);
            Wrapper servletWrapper = Tomcat.addServlet(context, "mcp-streamable-http", transportProvider);
            servletWrapper.setAsyncSupported(true);
            context.addServletMappingDecoded(config.getEndpointPath(), "mcp-streamable-http");
            tomcat.start();
            running = true;
        } catch (final LifecycleException ex) {
            stop();
            throw new IOException("Failed to start embedded Tomcat runtime.", ex);
        }
    }
    
    @Override
    public void stop() {
        closeSyncServer();
        transportProvider = null;
        closeTomcat();
        connector = null;
        deleteBaseDirectory();
        running = false;
    }
    
    private void closeSyncServer() {
        if (null != syncServer) {
            syncServer.closeGracefully();
            syncServer = null;
        }
    }
    
    private void closeTomcat() {
        if (null == tomcat) {
            return;
        }
        try {
            tomcat.stop();
        } catch (final LifecycleException ignored) {
        }
        try {
            tomcat.destroy();
        } catch (final LifecycleException ignored) {
        }
        tomcat = null;
    }
    
    private void deleteBaseDirectory() {
        if (null == baseDirectory) {
            return;
        }
        try {
            Files.walk(baseDirectory)
                    .sorted(Comparator.reverseOrder())
                    .forEach(each -> {
                        try {
                            Files.deleteIfExists(each);
                        } catch (final IOException ignored) {
                        }
                    });
        } catch (final IOException ignored) {
        }
        baseDirectory = null;
    }
    
    /**
     * Get the local port that the listener actually bound to.
     *
     * @return bound port
     */
    public int getLocalPort() {
        return null == connector ? config.getPort() : connector.getLocalPort();
    }
}
