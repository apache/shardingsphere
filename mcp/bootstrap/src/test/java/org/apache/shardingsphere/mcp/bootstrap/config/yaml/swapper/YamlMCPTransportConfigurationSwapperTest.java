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
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPLaunchConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlMCPTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlStdioTransportConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlMCPTransportConfigurationSwapperTest {
    
    private final YamlMCPLaunchConfigurationSwapper swapper = new YamlMCPLaunchConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithMissingHttp() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig();
        yamlConfig.getTransport().setHttp(null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        
        assertThat(actual.getMessage(), is("Property `transport.http` is required."));
    }
    
    @Test
    void assertSwapToObjectWithMissingStdio() {
        YamlMCPLaunchConfiguration yamlConfig = createYamlConfig();
        yamlConfig.getTransport().setStdio(null);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        
        assertThat(actual.getMessage(), is("Property `transport.stdio` is required."));
    }
    
    @Test
    void assertSwapToObject() {
        MCPLaunchConfiguration actual = swapper.swapToObject(createYamlConfig());
        
        assertFalse(actual.getHttpTransport().isEnabled());
        assertThat(actual.getHttpTransport().getBindHost(), is("127.0.0.1"));
        assertThat(actual.getHttpTransport().getPort(), is(18088));
        assertThat(actual.getHttpTransport().getEndpointPath(), is("/mcp"));
        assertTrue(actual.getStdioTransport().isEnabled());
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlMCPLaunchConfiguration actual = swapper.swapToYamlConfiguration(
                new MCPLaunchConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", 18088, "/mcp"), new StdioTransportConfiguration(false), Map.of()));
        
        assertTrue(actual.getTransport().getHttp().isEnabled());
        assertThat(actual.getTransport().getHttp().getPort(), is(18088));
        assertFalse(actual.getTransport().getStdio().isEnabled());
    }
    
    private YamlMCPLaunchConfiguration createYamlConfig() {
        YamlHttpTransportConfiguration http = new YamlHttpTransportConfiguration();
        http.setEnabled(false);
        http.setBindHost("127.0.0.1");
        http.setPort(18088);
        http.setEndpointPath("/mcp");
        YamlStdioTransportConfiguration stdio = new YamlStdioTransportConfiguration();
        stdio.setEnabled(true);
        YamlMCPTransportConfiguration transport = new YamlMCPTransportConfiguration();
        transport.setHttp(http);
        transport.setStdio(stdio);
        YamlMCPLaunchConfiguration result = new YamlMCPLaunchConfiguration();
        result.setTransport(transport);
        result.setRuntimeDatabases(Map.of());
        return result;
    }
}
