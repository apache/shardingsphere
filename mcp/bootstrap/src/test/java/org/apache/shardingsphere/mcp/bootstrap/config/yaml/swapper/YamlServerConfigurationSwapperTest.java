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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlServerConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlServerConfigurationSwapperTest {
    
    private final YamlServerConfigurationSwapper swapper = new YamlServerConfigurationSwapper();
    
    @Test
    void assertSwapToObjectWithDefaults() {
        HttpServerConfiguration actual = swapper.swapToObject(new YamlServerConfiguration());
        
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObject() {
        YamlServerConfiguration yamlConfig = new YamlServerConfiguration();
        yamlConfig.setBindHost("0.0.0.0");
        yamlConfig.setPort(9090);
        yamlConfig.setEndpointPath("gateway");
        
        HttpServerConfiguration actual = swapper.swapToObject(yamlConfig);
        
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertThat(actual.getPort(), is(9090));
        assertThat(actual.getEndpointPath(), is("/gateway"));
    }
    
    @Test
    void assertSwapToObjectWithNegativePort() {
        YamlServerConfiguration yamlConfig = new YamlServerConfiguration();
        yamlConfig.setPort(-1);
        
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(yamlConfig));
        
        assertThat(actual.getMessage(), is("MCP server port cannot be negative."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlServerConfiguration actual = swapper.swapToYamlConfiguration(new HttpServerConfiguration("0.0.0.0", 9090, "/gateway"));
        
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertThat(actual.getPort(), is(9090));
        assertThat(actual.getEndpointPath(), is("/gateway"));
    }
}
