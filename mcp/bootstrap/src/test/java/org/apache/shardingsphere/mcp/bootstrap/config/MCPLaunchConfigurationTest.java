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

import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertGetTransport() {
        MCPLaunchConfiguration<Map<String, RuntimeDatabaseConfiguration>> launchConfiguration = createLaunchConfiguration(Map.of());
        
        assertTrue(launchConfiguration.getTransport().getHttp().isEnabled());
        assertFalse(launchConfiguration.getTransport().getStdio().isEnabled());
        assertThat(launchConfiguration.getTransport().getHttp().getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertGetRuntimeConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MCPLaunchConfiguration<Map<String, RuntimeDatabaseConfiguration>> launchConfiguration = createLaunchConfiguration(runtimeDatabases);
        
        assertThat(launchConfiguration.getRuntimeConfiguration().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    private MCPLaunchConfiguration<Map<String, RuntimeDatabaseConfiguration>> createLaunchConfiguration(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return new MCPLaunchConfiguration<>(createTransportConfiguration(true, false, "/mcp"), runtimeDatabases);
    }
    
    private MCPTransportConfiguration createTransportConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final String endpointPath) {
        return new MCPTransportConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, endpointPath), new StdioTransportConfiguration(stdioEnabled));
    }
}
