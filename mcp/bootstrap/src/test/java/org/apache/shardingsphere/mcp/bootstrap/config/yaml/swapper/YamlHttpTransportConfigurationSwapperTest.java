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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlHttpTransportConfigurationSwapperTest {
    
    private final YamlHttpTransportConfigurationSwapper swapper = new YamlHttpTransportConfigurationSwapper();
    
    @Test
    void assertSwapToObject() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", 18088, "/mcp"));
        assertTrue(actual.isEnabled());
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    @Test
    void assertSwapToObjectWithOmittedEnabled() {
        YamlHttpTransportConfiguration yamlConfig = new YamlHttpTransportConfiguration();
        yamlConfig.setBindHost("127.0.0.1");
        yamlConfig.setPort(18088);
        yamlConfig.setEndpointPath("/mcp");
        HttpTransportConfiguration actual = swapper.swapToObject(yamlConfig);
        assertFalse(actual.isEnabled());
    }
    
    @Test
    void assertSwapToObjectWithMissingBindHost() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig(null, false, "", 18088, "/mcp")));
        assertThat(actual.getMessage(), is("Property `transport.http.bindHost` is required."));
    }
    
    @Test
    void assertSwapToObjectWithLocalhostBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("localhost", false, "", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("localhost"));
    }
    
    @Test
    void assertSwapToObjectWithRemoteBindHost() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", false, "", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertFalse(actual.isAllowRemoteAccess());
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccess() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "foo_token", 18088, "/mcp"));
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertTrue(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is("foo_token"));
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccessAndMissingAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "", 18088, "/mcp"));
        assertTrue(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithAllowedRemoteAccessAndBlankAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig("0.0.0.0", true, "   ", 18088, "/mcp"));
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithDisabledRemoteHttpWithoutAccessToken() {
        HttpTransportConfiguration actual = swapper.swapToObject(createYamlConfig(false, "0.0.0.0", false, "", 18088, "/mcp"));
        assertFalse(actual.isEnabled());
        assertThat(actual.getBindHost(), is("0.0.0.0"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
    }
    
    @Test
    void assertSwapToObjectWithNegativePort() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", -1, "/mcp")));
        assertThat(actual.getMessage(), is("Property `transport.http.port` cannot be negative."));
    }
    
    @Test
    void assertSwapToObjectWithEndpointPathMissingLeadingSlash() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class,
                () -> swapper.swapToObject(createYamlConfig("127.0.0.1", false, "", 18088, "gateway")));
        assertThat(actual.getMessage(), is("Property `transport.http.endpointPath` must start with '/'."));
    }
    
    @Test
    void assertSwapToObjectWithNullConfiguration() {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> swapper.swapToObject(null));
        assertThat(actual.getMessage(), is("Property `transport.http` is required."));
    }
    
    @Test
    void assertSwapToYamlConfiguration() {
        YamlHttpTransportConfiguration actual = swapper.swapToYamlConfiguration(new HttpTransportConfiguration(true, "127.0.0.1", false, "", 18088, "/mcp"));
        assertTrue(actual.isEnabled());
        assertThat(actual.getBindHost(), is("127.0.0.1"));
        assertFalse(actual.isAllowRemoteAccess());
        assertThat(actual.getAccessToken(), is(""));
        assertThat(actual.getPort(), is(18088));
        assertThat(actual.getEndpointPath(), is("/mcp"));
    }
    
    private YamlHttpTransportConfiguration createYamlConfig(final String bindHost, final boolean allowRemoteAccess, final String accessToken, final Integer port, final String endpointPath) {
        return createYamlConfig(true, bindHost, allowRemoteAccess, accessToken, port, endpointPath);
    }
    
    private YamlHttpTransportConfiguration createYamlConfig(final boolean enabled, final String bindHost, final boolean allowRemoteAccess,
                                                            final String accessToken, final Integer port, final String endpointPath) {
        YamlHttpTransportConfiguration result = new YamlHttpTransportConfiguration();
        result.setEnabled(enabled);
        result.setBindHost(bindHost);
        result.setAllowRemoteAccess(allowRemoteAccess);
        result.setAccessToken(accessToken);
        result.setPort(port);
        result.setEndpointPath(endpointPath);
        return result;
    }
}
