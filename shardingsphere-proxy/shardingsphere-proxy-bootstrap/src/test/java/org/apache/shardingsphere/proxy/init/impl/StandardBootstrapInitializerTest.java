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

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlProxyUserConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class StandardBootstrapInitializerTest {
    
    private final StandardBootstrapInitializer initializer = new StandardBootstrapInitializer();
    
    @Before
    public void setUp() {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    @Test
    public void assertGetProxyConfiguration() {
        YamlProxyConfiguration yamlConfig = new YamlProxyConfiguration(createYamlProxyServerConfiguration(), createYamlProxyRuleConfigurationMap());
        ProxyConfiguration actual = initializer.getProxyConfiguration(yamlConfig);
        assertProxyConfiguration(actual);
    }
    
    private Map<String, YamlProxyRuleConfiguration> createYamlProxyRuleConfigurationMap() {
        Map<String, YamlProxyRuleConfiguration> result = new HashMap<>(1, 1);
        result.put("logic-db", createYamlProxyRuleConfiguration());
        return result;
    }
    
    private YamlProxyRuleConfiguration createYamlProxyRuleConfiguration() {
        YamlProxyRuleConfiguration result = new YamlProxyRuleConfiguration();
        result.setDataSources(createYamlDataSourceParameterMap());
        result.setRules(createYamlRuleConfigurations());
        return result;
    }
    
    private Map<String, YamlDataSourceParameter> createYamlDataSourceParameterMap() {
        Map<String, YamlDataSourceParameter> result = new HashMap<>(1, 1);
        result.put("ds", createYamlDataSourceParameter());
        return result;
    }
    
    private YamlDataSourceParameter createYamlDataSourceParameter() {
        YamlDataSourceParameter result = new YamlDataSourceParameter();
        result.setUrl("jdbc:mysql://localhost:3306/ds");
        result.setUsername("root");
        result.setPassword("root");
        result.setReadOnly(false);
        result.setConnectionTimeoutMilliseconds(1000L);
        result.setIdleTimeoutMilliseconds(2000L);
        result.setMaxLifetimeMilliseconds(4000L);
        result.setMaxPoolSize(20);
        result.setMinPoolSize(10);
        return result;
    }
    
    private Collection<YamlRuleConfiguration> createYamlRuleConfigurations() {
        FixtureYamlRuleConfiguration result = new FixtureYamlRuleConfiguration();
        result.setName("testRule");
        return Collections.singletonList(result);
    }
    
    private YamlProxyServerConfiguration createYamlProxyServerConfiguration() {
        YamlProxyServerConfiguration result = new YamlProxyServerConfiguration();
        result.setAuthentication(createYamlAuthenticationConfiguration());
        result.setProps(createProperties());
        return result;
    }
    
    private YamlAuthenticationConfiguration createYamlAuthenticationConfiguration() {
        Map<String, YamlProxyUserConfiguration> users = new HashMap<>(1, 1);
        users.put("root", createYamlProxyUserConfiguration());
        YamlAuthenticationConfiguration result = new YamlAuthenticationConfiguration();
        result.setUsers(users);
        return result;
    }
    
    private YamlProxyUserConfiguration createYamlProxyUserConfiguration() {
        YamlProxyUserConfiguration result = new YamlProxyUserConfiguration();
        result.setPassword("root");
        result.setAuthorizedSchemas("ds-1,ds-2");
        return result;
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.setProperty("alpha-1", "alpha-A");
        result.setProperty("beta-2", "beta-B");
        return result;
    }
    
    private void assertProxyConfiguration(final ProxyConfiguration actual) {
        assertSchemaDataSources(actual.getSchemaDataSources());
        assertSchemaRules(actual.getSchemaRules());
        assertAuthentication(actual.getAuthentication());
        assertProps(actual.getProps());
    }
    
    private void assertSchemaDataSources(final Map<String, Map<String, DataSourceParameter>> actual) {
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("logic-db"));
        Map<String, DataSourceParameter> dataSourceParameterMap = actual.get("logic-db");
        assertThat(dataSourceParameterMap.size(), is(1));
        assertTrue(dataSourceParameterMap.containsKey("ds"));
        assertDataSourceParameter(dataSourceParameterMap.get("ds"));
    }
    
    private void assertDataSourceParameter(final DataSourceParameter actual) {
        assertThat(actual.getUrl(), is("jdbc:mysql://localhost:3306/ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertFalse(actual.isReadOnly());
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(1000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(2000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(4000L));
        assertThat(actual.getMaxPoolSize(), is(20));
        assertThat(actual.getMinPoolSize(), is(10));
    }
    
    private void assertSchemaRules(final Map<String, Collection<RuleConfiguration>> actual) {
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("logic-db"));
        Collection<RuleConfiguration> ruleConfigurations = actual.get("logic-db");
        assertThat(ruleConfigurations.size(), is(1));
        assertRuleConfiguration(ruleConfigurations.iterator().next());
    }
    
    private void assertRuleConfiguration(final RuleConfiguration actual) {
        assertThat(actual, instanceOf(FixtureRuleConfiguration.class));
        assertThat(((FixtureRuleConfiguration) actual).getName(), is("testRule"));
    }
    
    private void assertAuthentication(final Authentication actual) {
        assertThat(actual.getUsers().size(), is(1));
        assertTrue(actual.getUsers().containsKey("root"));
        ProxyUser proxyUser = actual.getUsers().get("root");
        assertThat(proxyUser.getPassword(), is("root"));
        assertThat(proxyUser.getAuthorizedSchemas().size(), is(2));
        assertTrue(proxyUser.getAuthorizedSchemas().contains("ds-1"));
        assertTrue(proxyUser.getAuthorizedSchemas().contains("ds-2"));
    }
    
    private void assertProps(final Properties actual) {
        assertThat(actual.getProperty("alpha-1"), is("alpha-A"));
        assertThat(actual.getProperty("beta-2"), is("beta-B"));
    }
    
    @Test
    public void assertDecorateSchemaContexts() {
        SchemaContexts schemaContexts = mock(SchemaContexts.class);
        assertThat(initializer.decorateSchemaContexts(schemaContexts), is(schemaContexts));
    }
    
    @Test
    public void assertDecorateTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        assertThat(initializer.decorateTransactionContexts(transactionContexts), is(transactionContexts));
    }
}
