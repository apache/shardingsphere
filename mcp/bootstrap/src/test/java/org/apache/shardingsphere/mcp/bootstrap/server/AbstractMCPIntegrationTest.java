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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Shared integration test harness for the MCP bootstrap layer.
 */
public abstract class AbstractMCPIntegrationTest {
    
    private MCPServerContext serverContext;
    
    @BeforeEach
    void setUpContext() {
        serverContext = createServerContext();
        serverContext.start();
    }
    
    @AfterEach
    void tearDownContext() {
        if (null != serverContext) {
            serverContext.stop();
        }
    }
    
    /**
     * Create the server context instance for the current integration test.
     *
     * @return server context instance
     */
    protected abstract MCPServerContext createServerContext();
    
    /**
     * Get the running server context instance.
     *
     * @return server context instance
     */
    protected final MCPServerContext getServerContext() {
        assertNotNull(serverContext);
        return serverContext;
    }
    
    /**
     * Assert that a tool has already been wired into the server context snapshot.
     *
     * @param toolName tool identifier
     */
    protected final void assertToolRegistered(final String toolName) {
        assertTrue(getServerContext().snapshot().getTools().contains(toolName));
    }
}
