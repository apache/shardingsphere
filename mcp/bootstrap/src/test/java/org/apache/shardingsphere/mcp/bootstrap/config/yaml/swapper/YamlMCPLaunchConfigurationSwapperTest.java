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

import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
                + "runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: jdbc:h2:mem:logic\n");
        
        assertThat(actual.getTransport().getHttp().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getTransport().getHttp().getPort(), is(9090));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/gateway"));
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertFalse(actual.getTransport().getStdio().isEnabled());
        assertTrue(actual.getRuntimeProps().isEmpty());
        assertThat(actual.getRuntimeDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToObjectWithNullSections() {
        YamlMCPLaunchConfiguration yamlConfig = new YamlMCPLaunchConfiguration();
        yamlConfig.setTransport(null);
        yamlConfig.setRuntime(null);
        
        MCPLaunchConfiguration actual = swapper.swapToObject(yamlConfig);
        
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getTransport().getHttp().getPort(), is(18088));
        assertThat(actual.getTransport().getHttp().getEndpointPath(), is("/mcp"));
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertTrue(actual.getTransport().getStdio().isEnabled());
        assertTrue(actual.getRuntimeProps().isEmpty());
        assertTrue(actual.getRuntimeDatabases().isEmpty());
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeProps() {
        Properties runtimeProps = PropertiesBuilder.build(
                new Property("databaseName", "logic_db"), new Property("databaseType", "H2"), new Property("jdbcUrl", "jdbc:h2:mem:logic"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new TransportConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(false)),
                runtimeProps, new LinkedHashMap<>());
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getTransport().getHttp().getBindHost(), is("127.0.0.1"));
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertFalse(actual.getTransport().getStdio().isEnabled());
        assertTrue(actual.getRuntime().getProps().isEmpty());
        assertThat(actual.getRuntime().getDatabases().get("logic_db").getDatabaseType(), is("H2"));
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver", false, false, false, false));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new TransportConfiguration(
                new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(true)),
                new Properties(), databases);
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getRuntime().getDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertTrue(actual.getRuntime().getProps().isEmpty());
        assertTrue(actual.getRuntime().getDefaults().isEmpty());
    }
    
}
