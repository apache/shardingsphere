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

package org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginCategoryConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlPluginConfiguration;
import org.apache.shardingsphere.agent.core.yaml.AgentYamlEngine;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class YamlPluginsConfigurationSwapperTest {
    
    private static final String CONFIG_PATH = "/conf/agent.yaml";
    
    @Test
    void assertSwapWithNullPluginCategory() {
        assertTrue(YamlPluginsConfigurationSwapper.swap(new YamlAgentConfiguration()).isEmpty());
    }
    
    @Test
    void assertSwapWithNullPlugins() {
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(new YamlPluginCategoryConfiguration());
        assertTrue(YamlPluginsConfigurationSwapper.swap(yamlAgentConfig).isEmpty());
    }
    
    @Test
    void assertSwapWithSinglePluginConfiguration() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(Collections.singletonMap("log_fixture", createYamlPluginConfiguration("localhost", "random", 8080, new Properties())));
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(1));
        PluginConfiguration actualLogFixtureConfig = actual.get("log_fixture");
        assertThat(actualLogFixtureConfig.getHost(), is("localhost"));
        assertThat(actualLogFixtureConfig.getPassword(), is("random"));
        assertThat(actualLogFixtureConfig.getPort(), is(8080));
        assertTrue(actualLogFixtureConfig.getProps().isEmpty());
    }
    
    @Test
    void assertSwapWithMultiplePluginConfigurations() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(Collections.singletonMap("log_fixture", createYamlPluginConfiguration(null, null, 8080, createProperties())));
        yamlPluginCategoryConfig.setMetrics(Collections.singletonMap("metrics_fixture", createYamlPluginConfiguration("localhost", "random", 8081, createProperties())));
        yamlPluginCategoryConfig.setTracing(Collections.singletonMap("tracing_fixture", createYamlPluginConfiguration("localhost", "random", 8082, createProperties())));
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(3));
        assertLogFixturePluginConfiguration(actual.get("log_fixture"));
        assertMetricsPluginConfiguration(actual.get("metrics_fixture"));
        assertTracingPluginConfiguration(actual.get("tracing_fixture"));
    }
    
    @Test
    void assertSwapWithNullLoggingPlugins() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        yamlPluginCategoryConfig.setLogging(null);
        yamlPluginCategoryConfig.setMetrics(Collections.singletonMap("metrics_fixture", createYamlPluginConfiguration("localhost", "random", 8081, createProperties())));
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(1));
        assertMetricsPluginConfiguration(actual.get("metrics_fixture"));
    }
    
    @Test
    void assertSwapWithNullPluginConfigurationValue() {
        YamlPluginCategoryConfiguration yamlPluginCategoryConfig = new YamlPluginCategoryConfiguration();
        Map<String, YamlPluginConfiguration> logging = new LinkedHashMap<>();
        logging.put("log_fixture", null);
        yamlPluginCategoryConfig.setLogging(logging);
        YamlAgentConfiguration yamlAgentConfig = new YamlAgentConfiguration();
        yamlAgentConfig.setPlugins(yamlPluginCategoryConfig);
        Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
        assertThat(actual.size(), is(1));
        PluginConfiguration actualLogFixtureConfig = actual.get("log_fixture");
        assertNull(actualLogFixtureConfig.getHost());
        assertNull(actualLogFixtureConfig.getPassword());
        assertThat(actualLogFixtureConfig.getPort(), is(0));
        assertTrue(actualLogFixtureConfig.getProps().isEmpty());
    }
    
    @Test
    void assertSwapWithFile() throws IOException {
        try (InputStream inputStream = Files.newInputStream(new File(getResourceURL(), CONFIG_PATH).toPath())) {
            YamlAgentConfiguration yamlAgentConfig = AgentYamlEngine.unmarshalYamlAgentConfiguration(inputStream);
            Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
            assertThat(actual.size(), is(3));
            assertLogFixturePluginConfiguration(actual.get("log_fixture"));
            assertMetricsPluginConfiguration(actual.get("metrics_fixture"));
            assertTracingPluginConfiguration(actual.get("tracing_fixture"));
        }
    }
    
    private String getResourceURL() throws UnsupportedEncodingException {
        return URLDecoder.decode(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getFile(), "UTF8");
    }
    
    private void assertLogFixturePluginConfiguration(final PluginConfiguration actual) {
        assertNull(actual.getHost());
        assertNull(actual.getPassword());
        assertThat(actual.getPort(), is(8080));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    private void assertMetricsPluginConfiguration(final PluginConfiguration actual) {
        assertThat(actual.getHost(), is("localhost"));
        assertThat(actual.getPassword(), is("random"));
        assertThat(actual.getPort(), is(8081));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    private void assertTracingPluginConfiguration(final PluginConfiguration actual) {
        assertThat(actual.getHost(), is("localhost"));
        assertThat(actual.getPassword(), is("random"));
        assertThat(actual.getPort(), is(8082));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().getProperty("key"), is("value"));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("key", "value");
        return result;
    }
    
    private YamlPluginConfiguration createYamlPluginConfiguration(final String host, final String password, final int port, final Properties props) {
        YamlPluginConfiguration result = new YamlPluginConfiguration();
        result.setHost(host);
        result.setPassword(password);
        result.setPort(port);
        result.setProps(props);
        return result;
    }
}
