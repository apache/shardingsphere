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
import org.apache.shardingsphere.mcp.bootstrap.config.HttpServerConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.MCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.RuntimeTopologyConfiguration;
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
        MCPLaunchConfiguration actual = swapper.swapToObject("server:\n"
                + "  bindHost: 0.0.0.0\n"
                + "  port: 9090\n"
                + "  endpointPath: gateway\n"
                + "transport:\n"
                + "  httpEnabled: false\n"
                + "  stdioEnabled: true\n"
                + "runtime:\n"
                + "  props:\n"
                + "    databaseName: logic_db\n"
                + "    databaseType: H2\n");
        
        assertThat(actual.getHttpServerConfiguration().getBindHost(), is("0.0.0.0"));
        assertThat(actual.getHttpServerConfiguration().getPort(), is(9090));
        assertThat(actual.getHttpServerConfiguration().getEndpointPath(), is("/gateway"));
        assertFalse(actual.isHttpEnabled());
        assertTrue(actual.isStdioEnabled());
        assertThat(actual.getRuntimeProps().getProperty("databaseName"), is("logic_db"));
    }
    
    @Test
    void assertSwapToObjectWithNullSections() {
        YamlMCPLaunchConfiguration yamlConfig = new YamlMCPLaunchConfiguration();
        yamlConfig.setServer(null);
        yamlConfig.setTransport(null);
        yamlConfig.setRuntime(null);
        
        MCPLaunchConfiguration actual = swapper.swapToObject(yamlConfig);
        
        assertThat(actual.getHttpServerConfiguration().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpServerConfiguration().getPort(), is(18088));
        assertThat(actual.getHttpServerConfiguration().getEndpointPath(), is("/mcp"));
        assertTrue(actual.isHttpEnabled());
        assertTrue(actual.isStdioEnabled());
        assertTrue(actual.getRuntimeProps().isEmpty());
        assertTrue(actual.getRuntimeTopologyConfiguration().getDatabases().isEmpty());
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeProps() {
        Properties runtimeProps = PropertiesBuilder.build(new Property("databaseName", "logic_db"), new Property("databaseType", "H2"));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new HttpServerConfiguration("127.0.0.1", 18088, "/mcp"), true, false,
                runtimeProps, new RuntimeTopologyConfiguration(new LinkedHashMap<>()));
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getServer().getBindHost(), is("127.0.0.1"));
        assertTrue(actual.getTransport().isHttpEnabled());
        assertFalse(actual.getTransport().isStdioEnabled());
        assertThat(actual.getRuntime().getProps().get("databaseName"), is("logic_db"));
        assertTrue(actual.getRuntime().getDatabases().isEmpty());
    }
    
    @Test
    void assertSwapToYamlConfigurationWithRuntimeDatabases() {
        Map<String, RuntimeDatabaseConfiguration> databases = new LinkedHashMap<>(1, 1F);
        databases.put("logic_db", new RuntimeDatabaseConfiguration("H2", "jdbc:h2:mem:logic", "", "", "org.h2.Driver", "public", "public", true, false));
        MCPLaunchConfiguration launchConfig = new MCPLaunchConfiguration(new HttpServerConfiguration("127.0.0.1", 18088, "/mcp"), true, true,
                new Properties(), new RuntimeTopologyConfiguration(databases));
        
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(launchConfig);
        
        assertThat(actual.getRuntime().getDatabases().get("logic_db").getDatabaseType(), is("H2"));
        assertTrue(actual.getRuntime().getProps().isEmpty());
        assertTrue(actual.getRuntime().getDefaults().isEmpty());
    }
    
}
