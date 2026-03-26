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
import org.apache.shardingsphere.mcp.bootstrap.transport.http.StreamableHttpMCPServer;
import org.apache.shardingsphere.mcp.bootstrap.transport.stdio.StdioMCPServer;
import org.apache.shardingsphere.mcp.execute.DatabaseRuntime;
import org.apache.shardingsphere.mcp.resource.MetadataCatalog;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPRuntimeTest {
    
    @Test
    void assertGetSessionManager() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntime actual = new MCPRuntime(sessionManager, createRuntimeServices(sessionManager), null, null);
        
        assertThat(actual.getSessionManager(), is(sessionManager));
    }
    
    @Test
    void assertGetRuntimeServices() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = createRuntimeServices(sessionManager);
        
        MCPRuntime actual = new MCPRuntime(sessionManager, runtimeServices, null, null);
        
        assertThat(actual.getRuntimeServices(), is(runtimeServices));
    }
    
    @Test
    void assertGetHttpServer() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = createRuntimeServices(sessionManager);
        StreamableHttpMCPServer httpServer = createHttpServer(sessionManager, runtimeServices);
        
        MCPRuntime actual = new MCPRuntime(sessionManager, runtimeServices, httpServer, null);
        
        assertTrue(actual.getHttpServer().isPresent());
        assertThat(actual.getHttpServer().get(), is(httpServer));
    }
    
    @Test
    void assertGetStdioServer() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = createRuntimeServices(sessionManager);
        StdioMCPServer stdioServer = new StdioMCPServer(sessionManager, runtimeServices);
        
        MCPRuntime actual = new MCPRuntime(sessionManager, runtimeServices, null, stdioServer);
        
        assertTrue(actual.getStdioServer().isPresent());
        assertThat(actual.getStdioServer().get(), is(stdioServer));
    }
    
    @Test
    void assertClose() {
        MCPSessionManager sessionManager = new MCPSessionManager();
        MCPRuntimeServices runtimeServices = createRuntimeServices(sessionManager);
        StdioMCPServer stdioServer = new StdioMCPServer(sessionManager, runtimeServices);
        stdioServer.start();
        
        new MCPRuntime(sessionManager, runtimeServices, null, stdioServer).close();
        
        assertFalse(stdioServer.isRunning());
    }
    
    private MCPRuntimeServices createRuntimeServices(final MCPSessionManager sessionManager) {
        return new MCPRuntimeServices(sessionManager, new MetadataCatalog(Collections.emptyMap(), Collections.emptyList()),
                new DatabaseRuntime(Collections.emptyMap(), Collections.emptyMap()));
    }
    
    private StreamableHttpMCPServer createHttpServer(final MCPSessionManager sessionManager, final MCPRuntimeServices runtimeServices) {
        return new StreamableHttpMCPServer(new HttpTransportConfiguration(true, "127.0.0.1", 0, "/gateway"), sessionManager, runtimeServices,
                new MetadataCatalog(Collections.emptyMap(), Collections.emptyList()), new DatabaseRuntime(Collections.emptyMap(), Collections.emptyMap()));
    }
}
