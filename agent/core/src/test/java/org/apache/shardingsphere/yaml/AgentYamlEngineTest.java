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

package org.apache.shardingsphere.yaml;

import org.apache.shardingsphere.agent.api.PluginConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlAdvisorsConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.entity.YamlPointcutParameterConfiguration;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlAdviceFixture;
import org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlTargetObjectFixture;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.entity.YamlAgentConfiguration;
import org.apache.shardingsphere.agent.core.plugin.config.yaml.swapper.YamlPluginsConfigurationSwapper;
import org.apache.shardingsphere.agent.core.yaml.AgentYamlEngine;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class AgentYamlEngineTest {
    
    @Test
    void assertUnmarshalYamlAgentConfiguration() throws IOException {
        try (InputStream inputStream = Files.newInputStream(new File(getResourceURL(), "/conf/agent.yaml").toPath())) {
            YamlAgentConfiguration yamlAgentConfig = AgentYamlEngine.unmarshalYamlAgentConfiguration(inputStream);
            Map<String, PluginConfiguration> actual = YamlPluginsConfigurationSwapper.swap(yamlAgentConfig);
            assertThat(actual.size(), is(3));
            assertLogFixturePluginConfiguration(actual.get("log_fixture"));
            assertMetricsPluginConfiguration(actual.get("metrics_fixture"));
            assertTracingPluginConfiguration(actual.get("tracing_fixture"));
        }
    }
    
    @Test
    void assertUnmarshalYamlAdvisorsConfiguration() {
        InputStream inputStream = getClass().getResourceAsStream("/META-INF/conf/advisors.yaml");
        YamlAdvisorsConfiguration actual = AgentYamlEngine.unmarshalYamlAdvisorsConfiguration(inputStream);
        assertYamlAdvisorConfiguration(actual.getAdvisors().iterator().next());
    }
    
    private String getResourceURL() throws UnsupportedEncodingException {
        return URLDecoder.decode(Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getFile(), "UTF8");
    }
    
    private void assertLogFixturePluginConfiguration(final PluginConfiguration actual) {
        assertNull(actual.getHost());
        assertNull(actual.getPassword());
        assertThat(actual.getPort(), is(8080));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().get("key"), is("value"));
    }
    
    private void assertMetricsPluginConfiguration(final PluginConfiguration actual) {
        assertThat(actual.getHost(), is("localhost"));
        assertThat(actual.getPassword(), is("random"));
        assertThat(actual.getPort(), is(8081));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().get("key"), is("value"));
    }
    
    private void assertTracingPluginConfiguration(final PluginConfiguration actual) {
        assertThat(actual.getHost(), is("localhost"));
        assertThat(actual.getPassword(), is("random"));
        assertThat(actual.getPort(), is(8082));
        assertThat(actual.getProps().size(), is(1));
        assertThat(actual.getProps().get("key"), is("value"));
    }
    
    private void assertYamlAdvisorConfiguration(final YamlAdvisorConfiguration actual) {
        assertThat(actual.getTarget(), is(YamlTargetObjectFixture.class.getName()));
        assertThat(actual.getAdvice(), is(YamlAdviceFixture.class.getName()));
        assertThat(actual.getTarget(), is("org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlTargetObjectFixture"));
        assertThat(actual.getAdvice(), is("org.apache.shardingsphere.agent.core.advisor.config.yaml.fixture.YamlAdviceFixture"));
        assertThat(actual.getPointcuts().size(), is(8));
        List<YamlPointcutConfiguration> actualYamlPointcutConfigs = new ArrayList<>(actual.getPointcuts());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(0), null, "constructor", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(1), null, "constructor", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(2), "call", "method", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(3), "call", "method", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(4), "call", "method",
                Arrays.asList(createYamlPointcutParameterConfiguration(0, "java.lang.String"), createYamlPointcutParameterConfiguration(1, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(5), "staticCall", "method", Collections.emptyList());
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(6), "staticCall", "method", Collections.singletonList(createYamlPointcutParameterConfiguration(0, "java.lang.String")));
        assertYamlPointcutConfiguration(actualYamlPointcutConfigs.get(7), "staticCall", "method",
                Arrays.asList(createYamlPointcutParameterConfiguration(0, "java.lang.String"), createYamlPointcutParameterConfiguration(1, "java.lang.String")));
    }
    
    private void assertYamlPointcutConfiguration(final YamlPointcutConfiguration actual,
                                                 final String expectedName, final String expectedType, final List<YamlPointcutParameterConfiguration> expectedParams) {
        assertThat(actual.getName(), is(expectedName));
        assertThat(actual.getType(), is(expectedType));
        assertThat(actual.getParams().size(), is(expectedParams.size()));
        int count = 0;
        for (YamlPointcutParameterConfiguration each : actual.getParams()) {
            assertYamlPointcutParameterConfiguration(each, expectedParams.get(count));
            count++;
        }
    }
    
    private void assertYamlPointcutParameterConfiguration(final YamlPointcutParameterConfiguration actual, final YamlPointcutParameterConfiguration expected) {
        assertThat(actual.getIndex(), is(expected.getIndex()));
        assertThat(actual.getType(), is(expected.getType()));
    }
    
    private YamlPointcutParameterConfiguration createYamlPointcutParameterConfiguration(final int index, final String type) {
        YamlPointcutParameterConfiguration result = new YamlPointcutParameterConfiguration();
        result.setIndex(index);
        result.setType(type);
        return result;
    }
}
