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

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
public abstract class AbstractBootstrapInitializerTest {
    
    private static final String HOST = "127.0.0.1";
    
    private static final int PORT = 2020;
    
    @Getter
    @Setter
    private AbstractBootstrapInitializer initializer;
    
    @Before
    public void setUp() {
        doEnvironmentPrepare();
        prepareSpecifiedInitializer();
    }
    
    protected void doAssertGetProxyConfiguration() {
        beforeAssertGetProxyConfiguration();
        YamlProxyConfiguration yamlConfig = makeProxyConfiguration();
        ProxyConfiguration actual = initializer.getProxyConfiguration(yamlConfig);
        assertNotNull(actual);
        assertProxyConfiguration(actual);
        afterAssertGetProxyConfiguration();
    }
    
    protected void beforeAssertGetProxyConfiguration() {
    
    }
    
    protected void afterAssertGetProxyConfiguration() {
    
    }
    
    protected YamlProxyConfiguration makeProxyConfiguration() {
        return null;
    }
    
    protected void assertProxyConfiguration(final ProxyConfiguration actual) {
        assertSchemaDataSources(actual.getSchemaDataSources());
        assertSchemaRules(actual.getSchemaRules());
        assertAuthentication(actual.getAuthentication());
        assertProps(actual.getProps());
    }
    
    protected void doEnvironmentPrepare() {
    
    }
    
    protected void prepareSpecifiedInitializer() {
    
    }
    
    @SneakyThrows
    @Test
    public void assertInit() {
        new Thread(this::triggerAbstractBootstrapInitializerInit).start();
        TimeUnit.SECONDS.sleep(5);
        assertTrue(isAvailable());
    }
    
    protected Pair<SchemaContexts, SchemaContexts> makeDecoratedSchemaContexts() {
        SchemaContexts original = mock(SchemaContexts.class);
        return new Pair<>(original, initializer.decorateSchemaContexts(original));
    }
    
    protected Pair<TransactionContexts, TransactionContexts> makeDecoratedTransactionContexts() {
        TransactionContexts original = mock(TransactionContexts.class);
        return new Pair<>(original, initializer.decorateTransactionContexts(original));
    }
    
    @SneakyThrows
    private void triggerAbstractBootstrapInitializerInit() {
        AbstractBootstrapInitializer abstractBootstrapInitializer = mock(AbstractBootstrapInitializer.class, Mockito.CALLS_REAL_METHODS);
        doReturn(mock(ProxyConfiguration.class)).when(abstractBootstrapInitializer).getProxyConfiguration(any());
        SchemaContexts schemaContexts = mock(SchemaContexts.class);
        ConfigurationProperties properties = mock(ConfigurationProperties.class);
        when(properties.getValue(any())).thenReturn(Boolean.FALSE);
        when(schemaContexts.getProps()).thenReturn(properties);
        doReturn(schemaContexts).when(abstractBootstrapInitializer).decorateSchemaContexts(any());
        doReturn(mock(TransactionContexts.class)).when(abstractBootstrapInitializer).decorateTransactionContexts(any());
        YamlProxyConfiguration yamlConfig = mock(YamlProxyConfiguration.class);
        abstractBootstrapInitializer.init(yamlConfig, PORT);
    }
    
    private static boolean isAvailable() {
        boolean portFree;
        try (Socket socket = new Socket(HOST, PORT)) {
            portFree = true;
        } catch (UnknownHostException ex) {
            portFree = false;
        } catch (IOException e) {
            portFree = false;
        }
        return portFree;
    }

    protected void assertSchemaDataSources(final Map<String, Map<String, DataSourceParameter>> actual) {
    
    }
    
    protected void assertDataSourceParameter(final DataSourceParameter actual) {
        assertThat(actual.getUrl(), is("jdbc:fixturedb:xxx"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("pwd"));
        assertTrue(actual.isReadOnly());
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(1000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(2000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(4000L));
        assertThat(actual.getMaxPoolSize(), is(20));
        assertThat(actual.getMinPoolSize(), is(10));
    }
    
    protected void assertSchemaRules(final Map<String, Collection<RuleConfiguration>> actual) {
        assertThat(actual.size(), is(1));
        String key = fetchKey();
        assertTrue(actual.containsKey(key));
        Collection<RuleConfiguration> ruleConfigurations = actual.get(key);
        assertThat(ruleConfigurations.size(), is(1));
        assertRuleConfiguration(ruleConfigurations.iterator().next());
    }
    
    protected String fetchKey() {
        return null;
    }
    
    protected void assertRuleConfiguration(final RuleConfiguration actual) {
    
    }
    
    protected void assertShardingTableRules(final Collection<ShardingTableRuleConfiguration> shardingTableRules) {
        assertNotNull(shardingTableRules);
        assertThat(shardingTableRules.size(), is(1));
        ShardingTableRuleConfiguration shardingTableRule = shardingTableRules.iterator().next();
        assertNotNull(shardingTableRule);
        assertThat(shardingTableRule.getLogicTable(), is("t_order"));
        assertThat(shardingTableRule.getActualDataNodes(), is("ds_${0..1}.t_order_${0..1}"));
        assertShardingStrategy(shardingTableRule.getTableShardingStrategy(), "order_id", "t_order_inline");
        assertShardingStrategy(shardingTableRule.getDatabaseShardingStrategy(), "user_id", "database_inline");
        assertNull(shardingTableRule.getKeyGenerateStrategy());
    }
    
    private void assertShardingStrategy(final ShardingStrategyConfiguration shardingStrategy, final String shardingColumn, final String shardingAlgorithmName) {
        assertNotNull(shardingStrategy);
        assertThat(shardingStrategy, instanceOf(StandardShardingStrategyConfiguration.class));
        StandardShardingStrategyConfiguration standardShardingStrategy = (StandardShardingStrategyConfiguration) shardingStrategy;
        assertThat(standardShardingStrategy.getShardingColumn(), is(shardingColumn));
        assertThat(standardShardingStrategy.getShardingAlgorithmName(), is(shardingAlgorithmName));
    }
    
    protected void assertAuthentication(final Authentication actual) {
    
    }
    
    protected void assertProps(final Properties actual) {
        assertThat(actual.getProperty("alpha-1"), is("alpha-A"));
        assertThat(actual.getProperty("beta-2"), is("beta-B"));
    }
}
