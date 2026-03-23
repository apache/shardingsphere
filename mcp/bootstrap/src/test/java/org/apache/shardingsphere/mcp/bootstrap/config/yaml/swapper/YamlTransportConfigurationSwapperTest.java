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

import org.apache.shardingsphere.mcp.bootstrap.config.HttpServerConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.HttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.StdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.TransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlStdioTransportConfiguration;
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlTransportConfiguration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlTransportConfigurationSwapperTest {
    
    private final YamlTransportConfigurationSwapper swapper = new YamlTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithDefaults() {
        TransportConfiguration actual = swapper.swapToObject(new YamlTransportConfiguration());
        
        assertTrue(actual.getHttp().isEnabled());
        assertTrue(actual.getStdio().isEnabled());
        assertThat(actual.getHttp().getServer().getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlTransportConfiguration yamlConfig = new YamlTransportConfiguration();
        YamlHttpTransportConfiguration http = new YamlHttpTransportConfiguration();
        http.setEnabled(false);
        yamlConfig.setHttp(http);
        YamlStdioTransportConfiguration stdio = new YamlStdioTransportConfiguration();
        stdio.setEnabled(true);
        yamlConfig.setStdio(stdio);
        
        TransportConfiguration actual = swapper.swapToObject(yamlConfig, Map.of("http", Map.of("enabled", false), "stdio", Map.of("enabled", true)));
        
        assertFalse(actual.getHttp().isEnabled());
        assertTrue(actual.getStdio().isEnabled());
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlTransportConfiguration actual = swapper.swapToYamlConfiguration(
                new TransportConfiguration(new HttpTransportConfiguration(true, new HttpServerConfiguration("127.0.0.1", 18088, "/mcp")), new StdioTransportConfiguration(false)));
        
        assertTrue(actual.getHttp().isEnabled());
        assertFalse(actual.getStdio().isEnabled());
        assertThat(actual.getHttp().getServer().getPort(), is(18088));
    }
}
