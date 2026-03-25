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

package org.apache.shardingsphere.mcp.bootstrap.config.yaml.swapper;

import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlMCPLaunchConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        MCPLaunchConfiguration actual = swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: true\n"
                + "    bindHost: 0.0.0.0\n"
                + "    port: 9090\n"
                + "    endpointPath: gateway\n"
                + "  stdio:\n"
                + "    enabled: false\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n");
        
        assertThat(actual.getTransport().getHttp().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getTransport().getHttp().getPort(), is(9090));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertFalse(actual.getTransport().getStdio().isEnabled());
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToObjectWithNullSections() {
        YamlMCPLaunchConfiguration yamlConfig = new YamlMCPLaunchConfiguration();
        yamlConfig.setTransport(null);
        yamlConfig.setRuntimeDatabases(null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        
        assertThat(actual.getMessage(), is("At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertSwapToObjectWithDisabledTransports() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "  stdio:\n"
                + "    enabled: false\n"));
        
        assertThat(actual.getMessage(), is("At least one transport must be explicitly enabled. Set `transport.http.enabled` or `transport.stdio.enabled` to true."));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new MCPTransportConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(true)),
                databases);
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
    }
    
    @Test
    void assertSwapToObjectWithLegacyRuntimeProps() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"));
        
        assertThat(actual.getMessage(), is("`runtime.props` is no longer supported. Configure direct runtime databases with `runtimeDatabases`."));
    }
    
    @Test
    void assertSwapToObjectWithLegacyRuntimeDefaults() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtime:\n"
                + "  defaults:\n"
                + "    databaseType: H2\n"));
        
        assertThat(actual.getMessage(), is("`runtime.defaults` is no longer supported. Configure direct runtime databases with `runtimeDatabases`."));
    }
    
    @Test
    void assertSwapToObjectWithLegacyRuntimeDatabaseDefaults() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtime:\n"
                + "  databaseDefaults:\n"
                + "    databaseType: H2\n"));
        
        assertThat(actual.getMessage(), is("`runtime.databaseDefaults` is no longer supported. Configure each runtime database explicitly under `runtimeDatabases`."));
    }
    
    @Test
    void assertSwapToObjectWithLegacyRuntimeDatabases() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject("runtime:\n"
                + "  databases:\n"
                + "    logic_db:\n"
                + "      databaseType: H2\n"
                + "      jdbcUrl: jdbc:h2:mem:logic\n"));
        
        assertThat(actual.getMessage(), is("`runtime.databases` is no longer supported. Configure direct runtime databases with `runtimeDatabases`."));
    }
    
    @Test
    void assertSwapToObjectIgnoreLegacyRuntimeCapabilityOverride() {
        MCPLaunchConfiguration actual = swapper.swapToObject("transport:\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n"
                + "    supportsExplainAnalyze: true\n");
        
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
}
