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
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyServerConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureRuleConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureYamlRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public final class StandardBootstrapInitializerTest {
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertGetProxyConfiguration() {
        Map<String, YamlProxyRuleConfiguration> ruleConfigurations = generateYamlProxyRuleConfiguration();
        YamlProxyServerConfiguration serverConfiguration = generateYamlProxyServerConfiguration();
        YamlProxyConfiguration yamlConfig = new YamlProxyConfiguration(serverConfiguration, ruleConfigurations);
        ProxyConfiguration proxyConfiguration = new StandardBootstrapInitializer().getProxyConfiguration(yamlConfig);
        assertSchemaDataSources(proxyConfiguration.getSchemaDataSources());
        assertSchemaRules(proxyConfiguration.getSchemaRules());
        assertAuthentication(proxyConfiguration.getAuthentication());
        assertProps(proxyConfiguration.getProps());
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
        Map<String, YamlDataSourceParameter> dataSources = new HashMap<>(1, 1);
        dataSources.put("hikari", yamlDataSourceParameter);
        YamlProxyRuleConfiguration yamlProxyRuleConfiguration = new YamlProxyRuleConfiguration();
        yamlProxyRuleConfiguration.setDataSources(dataSources);
        FixtureYamlRuleConfiguration fixtureYamlRuleConfiguration = new FixtureYamlRuleConfiguration();
        fixtureYamlRuleConfiguration.setName("testRule");
        List<YamlRuleConfiguration> rules = Lists.newArrayList(fixtureYamlRuleConfiguration);
        yamlProxyRuleConfiguration.setRules(rules);
        Map<String, YamlProxyRuleConfiguration> result = new HashMap<>(1, 1);
        result.put("datasource-0", yamlProxyRuleConfiguration);
        return result;
    }
    
    private YamlProxyServerConfiguration generateYamlProxyServerConfiguration() {
        YamlProxyUserConfiguration yamlProxyUserConfiguration = new YamlProxyUserConfiguration();
        yamlProxyUserConfiguration.setPassword("root");
        yamlProxyUserConfiguration.setAuthorizedSchemas("ds-1,ds-2");
        Map<String, YamlProxyUserConfiguration> users = new HashMap<>(1, 1);
        users.put("root", yamlProxyUserConfiguration);
        YamlAuthenticationConfiguration authentication = new YamlAuthenticationConfiguration();
        authentication.setUsers(users);
        YamlProxyServerConfiguration result = new YamlProxyServerConfiguration();
        result.setAuthentication(authentication);
        Properties props = new Properties();
        props.setProperty("alpha-1", "alpha-A");
        props.setProperty("beta-2", "beta-B");
        result.setProps(props);
        return result;
    }
    
    private void assertSchemaDataSources(final Map<String, Map<String, DataSourceParameter>> schemaDataSources) {
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
    
    private void assertSchemaRules(final Map<String, Collection<RuleConfiguration>> schemaRules) {
        assertThat(schemaRules.size(), is(1));
        assertTrue("there is no such key !", schemaRules.containsKey("datasource-0"));
        Collection<RuleConfiguration> ruleConfigurations = schemaRules.get("datasource-0");
        assertThat(ruleConfigurations.size(), is(1));
        RuleConfiguration ruleConfiguration = ruleConfigurations.iterator().next();
        assertThat(ruleConfiguration, instanceOf(FixtureRuleConfiguration.class));
        assertThat(((FixtureRuleConfiguration) ruleConfiguration).getName(), is("testRule"));
    }
    
    private void assertAuthentication(final Authentication authentication) {
        assertThat(authentication.getUsers().size(), is(1));
        assertTrue("there is no such key !", authentication.getUsers().containsKey("root"));
        ProxyUser proxyUser = authentication.getUsers().get("root");
        assertThat(proxyUser.getPassword(), is("root"));
        assertThat(proxyUser.getAuthorizedSchemas().size(), is(2));
        assertTrue("there is no such element !", proxyUser.getAuthorizedSchemas().contains("ds-1"));
        assertTrue("there is no such element !", proxyUser.getAuthorizedSchemas().contains("ds-2"));
    }
    
    private void assertProps(final Properties props) {
        assertThat(props.getProperty("alpha-1"), is("alpha-A"));
        assertThat(props.getProperty("beta-2"), is("beta-B"));
    }
    
    @Test
    public void assertDecorateSchemaContexts() {
        SchemaContexts schemaContexts = mock(SchemaContexts.class);
        StandardBootstrapInitializer standardBootstrapInitializer = spy(StandardBootstrapInitializer.class);
        SchemaContexts newSchemaContexts = standardBootstrapInitializer.decorateSchemaContexts(schemaContexts);
        assertThat(schemaContexts, is(newSchemaContexts));
    }
    
    @Test
    public void assertDecorateTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        StandardBootstrapInitializer standardBootstrapInitializer = spy(StandardBootstrapInitializer.class);
        TransactionContexts newTransactionContexts = standardBootstrapInitializer.decorateTransactionContexts(transactionContexts);
        assertThat(transactionContexts, is(newTransactionContexts));
    }
}
