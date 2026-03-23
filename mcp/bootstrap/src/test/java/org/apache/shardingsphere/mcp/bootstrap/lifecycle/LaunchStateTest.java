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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.context.MCPRuntimeServices;
import org.apache.shardingsphere.mcp.bootstrap.server.MCPServerRegistry;
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LaunchStateTest {
    
    @Test
    void assertGetServerRegistry() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager);
        
        LaunchState actual = new LaunchState(serverRegistry, runtimeServices, Collections.emptyList(), Collections.emptyList());
        
        assertThat(actual.getServerRegistry(), is(serverRegistry));
    }
    
    @Test
    void assertGetRuntimeServices() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager);
        
        LaunchState actual = new LaunchState(serverRegistry, runtimeServices, Collections.emptyList(), Collections.emptyList());
        
        assertThat(actual.getRuntimeServices(), is(runtimeServices));
    }
    
    @Test
    void assertGetHttpServer() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager);
        StreamableHttpMCPServer httpServer = new StreamableHttpMCPServer(new HttpTransportConfiguration(true, "127.0.0.1", 0, "/gateway"), serverRegistry, runtimeServices);
        
        LaunchState actual = new LaunchState(serverRegistry, runtimeServices, Collections.singletonList(httpServer), Collections.emptyList());
        
        assertTrue(actual.getHttpServer().isPresent());
        assertThat(actual.getHttpServer().get(), is(httpServer));
    }
    
    @Test
    void assertGetHttpServerWhenAbsent() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        LaunchState actual = new LaunchState(new MCPServerRegistry(sessionManager), new MCPRuntimeServices(sessionManager), Collections.emptyList(), Collections.emptyList());
        
        assertFalse(actual.getHttpServer().isPresent());
    }
    
    @Test
    void assertGetStdioServer() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPServerRegistry serverRegistry = new MCPServerRegistry(sessionManager);
        MCPRuntimeServices runtimeServices = new MCPRuntimeServices(sessionManager);
        StdioMCPServer stdioServer = new StdioMCPServer(sessionManager, runtimeServices);
        
        LaunchState actual = new LaunchState(serverRegistry, runtimeServices, Collections.emptyList(), Collections.singletonList(stdioServer));
        
        assertTrue(actual.getStdioServer().isPresent());
        assertThat(actual.getStdioServer().get(), is(stdioServer));
    }
    
    @Test
    void assertGetStdioServerWhenAbsent() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        LaunchState actual = new LaunchState(new MCPServerRegistry(sessionManager), new MCPRuntimeServices(sessionManager), Collections.emptyList(), Collections.emptyList());
        
        assertFalse(actual.getStdioServer().isPresent());
    }
}
