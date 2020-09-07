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
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureYamlRuleConfiguration;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
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
        YamlProxyServerConfiguration serverConfiguration = generateYamlProxyServerConfiguration();
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class);
        when(yamlConfig.getRuleConfigurations()).thenReturn(ruleConfigurations);
        when(yamlConfig.getServerConfiguration()).thenReturn(serverConfiguration);
        ProxyConfiguration proxyConfiguration = new StandardBootstrapInitializer().getProxyConfiguration(yamlConfig);
        assertSchemaDataSources(proxyConfiguration.getSchemaDataSources());
    }
    
    private Map<String, YamlProxyRuleConfiguration> generateYamlProxyRuleConfiguration() {
        YamlDataSourceParameter yamlDataSourceParameter = new YamlDataSourceParameter();
        yamlDataSourceParameter.setUrl("jdbc:mysql://localhost:3306/demo_ds");
        yamlDataSourceParameter.setUsername("root");
        yamlDataSourceParameter.setPassword("root");
        yamlDataSourceParameter.setReadOnly(false);
        yamlDataSourceParameter.setConnectionTimeoutMilliseconds(1000L);
        yamlDataSourceParameter.setIdleTimeoutMilliseconds(2000L);
        yamlDataSourceParameter.setMaxLifetimeMilliseconds(4000L);
        yamlDataSourceParameter.setMaxPoolSize(20);
        yamlDataSourceParameter.setMinPoolSize(10);
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
    
    private YamlProxyServerConfiguration generateYamlProxyServerConfiguration() {
        YamlProxyServerConfiguration serverConfiguration = new YamlProxyServerConfiguration();
        YamlAuthenticationConfiguration authentication = new YamlAuthenticationConfiguration();
        YamlProxyUserConfiguration yamlProxyUserConfiguration = new YamlProxyUserConfiguration();
        yamlProxyUserConfiguration.setPassword("root");
        yamlProxyUserConfiguration.setAuthorizedSchemas("ds-1,ds-2");
        Map<String, YamlProxyUserConfiguration> users = new HashMap<>();
        users.put("root", yamlProxyUserConfiguration);
        authentication.setUsers(users);
        serverConfiguration.setAuthentication(authentication);
        Properties props = new Properties();
        props.setProperty("alpha-1", "alpha-A");
        props.setProperty("beta-2", "beta-B");
        serverConfiguration.setProps(props);
        return serverConfiguration;
    }
    
    private void assertSchemaDataSources(Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
        assertThat(schemaDataSources.size(), is(1));
        assertTrue("there is no such key !", schemaDataSources.containsKey("datasource-0"));
        Map<String, DataSourceParameter> dataSourceParameterMap = schemaDataSources.get("datasource-0");
        assertThat(dataSourceParameterMap.size(), is(1));
        assertTrue("there is no such key !", dataSourceParameterMap.containsKey("hikari"));
        DataSourceParameter dataSourceParameter = dataSourceParameterMap.get("hikari");
        assertThat(dataSourceParameter.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(dataSourceParameter.getUsername(), is("root"));
        assertThat(dataSourceParameter.getPassword(), is("root"));
        assertThat(dataSourceParameter.isReadOnly(), is(false));
        assertThat(dataSourceParameter.getConnectionTimeoutMilliseconds(), is(1000L));
        assertThat(dataSourceParameter.getIdleTimeoutMilliseconds(), is(2000L));
        assertThat(dataSourceParameter.getMaxLifetimeMilliseconds(), is(4000L));
        assertThat(dataSourceParameter.getMaxPoolSize(), is(20));
        assertThat(dataSourceParameter.getMinPoolSize(), is(10));
    }
}
