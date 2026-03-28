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

import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPLaunchConfigurationTest {
    
    @Test
    void assertGetTransports() {
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(Map.of());
        
        assertTrue(launchConfiguration.getHttpTransport().isEnabled());
        assertFalse(launchConfiguration.getStdioTransport().isEnabled());
        assertThat(launchConfiguration.getHttpTransport().getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertGetRuntimeConfiguration() {
        Map<String, RuntimeDatabaseConfiguration> runtimeDatabases = Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(runtimeDatabases);
        
        assertThat(launchConfiguration.getDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertValidateWhenHttpTransportEnabled() {
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(true, false, Map.of());
        
        assertDoesNotThrow(launchConfiguration::validate);
    }
    
    @Test
    void assertValidateWhenStdioTransportEnabled() {
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(false, true, Map.of());
        
        assertDoesNotThrow(launchConfiguration::validate);
    }
    
    @Test
    void assertValidateWhenBothTransportsEnabled() {
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(true, true, Map.of());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, launchConfiguration::validate);
        assertThat(actual.getMessage(), is("HTTP and STDIO transports cannot be enabled at the same time. Choose exactly one transport."));
    }
    
    @Test
    void assertValidateWhenBothTransportsDisabled() {
        MCPLaunchConfiguration launchConfiguration = createLaunchConfiguration(false, false, Map.of());
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, launchConfiguration::validate);
        assertThat(actual.getMessage(), is("Exactly one transport must be explicitly enabled. Set either `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return createLaunchConfiguration(true, false, runtimeDatabases);
    }
    
    private MCPLaunchConfiguration createLaunchConfiguration(final boolean httpEnabled, final boolean stdioEnabled, final Map<String, RuntimeDatabaseConfiguration> runtimeDatabases) {
        return new MCPLaunchConfiguration(new HttpTransportConfiguration(httpEnabled, "127.0.0.1", 0, "/mcp"), new StdioTransportConfiguration(stdioEnabled), runtimeDatabases);
    }
}
