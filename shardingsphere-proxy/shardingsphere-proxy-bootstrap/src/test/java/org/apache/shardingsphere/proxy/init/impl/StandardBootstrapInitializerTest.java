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

package org.apache.shardingsphere.proxy.init.impl;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureYamlRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class StandardBootstrapInitializerTest {
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertGetProxyConfiguration() {
        Map<String, YamlProxyRuleConfiguration> ruleConfigurations = generateYamlProxyRuleConfiguration();
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class);
        when(yamlConfig.getRuleConfigurations()).thenReturn(ruleConfigurations);
        StandardBootstrapInitializer standardBootstrapInitializer = new StandardBootstrapInitializer();
        ProxyConfiguration proxyConfiguration = standardBootstrapInitializer.getProxyConfiguration(yamlConfig);
    }
    
    private Map<String, YamlProxyRuleConfiguration> generateYamlProxyRuleConfiguration() {
        YamlDataSourceParameter yamlDataSourceParameter = new YamlDataSourceParameter();
        yamlDataSourceParameter.setUsername("root");
        yamlDataSourceParameter.setPassword("root");
        yamlDataSourceParameter.setReadOnly(false);
        Map<String, YamlDataSourceParameter> dataSources = new HashMap<>();
        dataSources.put("hikari", yamlDataSourceParameter);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = new YamlProxyRuleConfiguration();
        yamlProxyRuleConfiguration.setDataSources(dataSources);
        List<YamlRuleConfiguration> rules = Lists.newArrayList(new FixtureYamlRuleConfiguration());
        yamlProxyRuleConfiguration.setRules(rules);
        Map<String, YamlProxyRuleConfiguration> ruleConfigurations = new HashMap<>();
        ruleConfigurations.put("datasource-0", yamlProxyRuleConfiguration);
        return ruleConfigurations;
    }
}
