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
import org.apache.shardingsphere.mcp.bootstrap.config.yaml.config.YamlHttpTransportConfiguration;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class YamlHttpTransportConfigurationSwapperTest {
    
    private final YamlHttpTransportConfigurationSwapper swapper = new YamlHttpTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("127.0.0.1", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithDefaults() {
        HttpTransportConfiguration actual = swapper.swapToObject(null);
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithPartialDefaults() {
        YamlHttpTransportConfiguration yamlConfig = new YamlHttpTransportConfiguration();
        yamlConfig.setPort(0);
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig);
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(0));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithLocalhostBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("localhost", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("localhost"));
    }
    
    @Test
    void assertSwapToObjectWithRemoteBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("0.0.0.0"));
    }
    
    @Test
    void assertSwapToObjectWithUrlBindHost() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("http://127.0.0.1:18088", 18088, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `bindHost` must be a local bind host or IP address."));
    }
    
    @Test
    void assertSwapToObjectWithBlankBindHost() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("", 18088, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `bindHost` must be a local bind host or IP address."));
    }
    
    @Test
    void assertSwapToObjectWithNegativePort() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", -1, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `port` must be between 0 and 65535."));
    }
    
    @Test
    void assertSwapToObjectWithPortOutOfRange() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", 65536, "/mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `port` must be between 0 and 65535."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathMissingLeadingSlash() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", 18088, "gateway")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `endpointPath` must be a single absolute path without query or fragment."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathDoubleSlash() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", 18088, "//mcp")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `endpointPath` must be a single absolute path without query or fragment."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathQuery() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", 18088, "/mcp?debug=true")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `endpointPath` must be a single absolute path without query or fragment."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathFragment() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(createYamlConfig("127.0.0.1", 18088, "/mcp#debug")));
        assertThat(actual.getMessage(), is("MCP HTTP transport configuration property `endpointPath` must be a single absolute path without query or fragment."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlHttpTransportConfiguration actual = swapper.swapToYamlConfiguration(new HttpTransportConfiguration("127.0.0.1", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    private YamlHttpTransportConfiguration createYamlConfig(final String bindHost, final Integer port, final String endpointPath) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setBindHost(bindHost);
        result.setPort(port);
        result.setEndpointPath(endpointPath);
        return result;
    }
}
