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

package org.apache.shardingsphere.mcp.core.context;

import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.core.session.MCPSessionManager;
import org.apache.shardingsphere.mcp.support.database.capability.MCPDatabaseCapabilityProvider;
import org.apache.shardingsphere.mcp.support.database.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPFeatureRuntimeRequestContextTest {
    
    @Test
    void assertSessionIdentity() {
        MCPSessionIdentity sessionIdentity = new MCPSessionIdentity("session-1", "subject", "gateway", Map.of("cluster", "demo"));
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(Map.of()),
                new MCPDatabaseCapabilityProvider(Map.of()), MCPTransportType.HTTP);
        assertThat(new MCPFeatureRuntimeRequestContext(runtimeContext, sessionIdentity).getSessionIdentity(), is(sessionIdentity));
    }
    
    @Test
    void assertFindRuntimeDatabaseConfiguration() {
        RuntimeDatabaseConfiguration runtimeDatabaseConfig = new RuntimeDatabaseConfiguration("jdbc:test:profile", "demo", "", "com.mysql.cj.jdbc.Driver");
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(Map.of("logic_db", runtimeDatabaseConfig)),
                new MCPDatabaseCapabilityProvider(Map.of()), MCPTransportType.HTTP);
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(runtimeContext, new MCPSessionIdentity("session-1", "", "", Map.of()));
        assertThat(requestContext.findRuntimeDatabaseConfiguration("logic_db"), is(Optional.of(runtimeDatabaseConfig)));
    }
    
    @Test
    void assertFindMissingRuntimeDatabaseConfiguration() {
        MCPRuntimeContext runtimeContext = new MCPRuntimeContext(new MCPSessionManager(Map.of()),
                new MCPDatabaseCapabilityProvider(Map.of()), MCPTransportType.HTTP);
        MCPFeatureRuntimeRequestContext requestContext = new MCPFeatureRuntimeRequestContext(runtimeContext, new MCPSessionIdentity("session-1", "", "", Map.of()));
        assertThat(requestContext.findRuntimeDatabaseConfiguration("missing_db"), is(Optional.empty()));
    }
}
