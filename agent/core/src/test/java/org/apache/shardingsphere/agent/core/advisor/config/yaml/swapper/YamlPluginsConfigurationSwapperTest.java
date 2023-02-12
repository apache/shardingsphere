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

package org.apache.shardingsphere.agent.core.advisor.config.yaml.swapper;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper.YamlPluginsConfigurationSwapper;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.is;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public final class YamlPluginsConfigurationSwapperTest {
    
    private static final String CONFIG_PATH = "/conf/agent.yaml";
    
    @Test
    public void assertSwap() throws IOException {
        YamlAgentConfiguration yamlAgentConfiguration = getAgentConfiguration();
        Map<String, PluginConfiguration> pluginConfigurationMap =
                YamlPluginsConfigurationSwapper.swap(yamlAgentConfiguration);
        assertThat(pluginConfigurationMap.size(), is(3));
        assertLogFixturePluginConfiguration(pluginConfigurationMap.get("LogFixture"));
        assertMetricsPluginConfiguration(pluginConfigurationMap.get("MetricsFixture"));
        assertTracingPluginConfiguration(pluginConfigurationMap.get("TracingFixture"));
    }
    
    private YamlAgentConfiguration getAgentConfiguration() throws UnsupportedEncodingException, FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(new File(getResourceURL(), CONFIG_PATH));
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        return new Yaml().loadAs(inputStreamReader, YamlAgentConfiguration.class);
    }
    
    private String getResourceURL() throws UnsupportedEncodingException {
        return URLDecoder.decode(
                Objects.requireNonNull(YamlPluginsConfigurationSwapper.class.getClassLoader().getResource(""))
                        .getFile(),
                "UTF8");
    }
    
    private void assertLogFixturePluginConfiguration(final PluginConfiguration pluginConfiguration) {
        assertNull(pluginConfiguration.getHost());
        assertThat(pluginConfiguration.getPort(), is(0));
        assertNull(pluginConfiguration.getPassword());
        assertThat(pluginConfiguration.getProps().size(), is(1));
        assertThat(pluginConfiguration.getProps().getProperty("logging_key"), is("logging_value"));
    }
    
    private void assertMetricsPluginConfiguration(final PluginConfiguration pluginConfiguration) {
        assertThat(pluginConfiguration.getHost(), is("metrics.host"));
        assertThat(pluginConfiguration.getPort(), is(1));
        assertThat(pluginConfiguration.getPassword(), is("metrics.pwd"));
        assertThat(pluginConfiguration.getProps().size(), is(1));
        assertThat(pluginConfiguration.getProps().getProperty("metrics_key"), is("metrics_value"));
    }
    
    private void assertTracingPluginConfiguration(final PluginConfiguration pluginConfiguration) {
        assertThat(pluginConfiguration.getHost(), is("tracing.host"));
        assertThat(pluginConfiguration.getPort(), is(2));
        assertThat(pluginConfiguration.getPassword(), is("tracing.pwd"));
        assertThat(pluginConfiguration.getProps().size(), is(1));
        assertThat(pluginConfiguration.getProps().getProperty("tracing_key"), is("tracing_value"));
    }
}
