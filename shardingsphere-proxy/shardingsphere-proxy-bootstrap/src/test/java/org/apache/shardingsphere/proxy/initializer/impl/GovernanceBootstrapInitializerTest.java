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

package org.apache.shardingsphere.proxy.initializer.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.context.schema.GovernanceMetaDataContexts;
import org.apache.shardingsphere.governance.context.transaction.GovernanceTransactionContexts;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.schema.MetaDataContexts;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureConfigurationRepository;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class GovernanceBootstrapInitializerTest extends AbstractBootstrapInitializerTest {
    
    private static final String DATA_SOURCE_YAML = "conf/reg_center/config_center/data-sources.yaml";
    
    private static final String SHARDING_RULE_YAML = "conf/reg_center/config_center/sharding-rule.yaml";
    
    private static final String AUTHENTICATION_YAML = "conf/reg_center/config_center/authentication.yaml";
    
    private static final String PROPS_YAML = "conf/reg_center/config_center/props.yaml";
    
    private FixtureConfigurationRepository configurationRepository = new FixtureConfigurationRepository();
    
    @Test
    public void assertGetProxyConfiguration() throws IOException {
        initConfigCenter();
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/reg_center/");
        assertProxyConfiguration(getInitializer().getProxyConfiguration(yamlProxyConfig));
        closeConfigCenter();
    }
    
    @SneakyThrows(IOException.class)
    protected YamlProxyConfiguration makeProxyConfiguration() {
        return ProxyConfigurationLoader.load("/conf/reg_center/");
    }
    
    private void initConfigCenter() {
        ConfigCenterNode node = new ConfigCenterNode();
        configurationRepository.persist(node.getAuthenticationPath(), readYAML(AUTHENTICATION_YAML));
        configurationRepository.persist(node.getPropsPath(), readYAML(PROPS_YAML));
        configurationRepository.persist(node.getSchemasPath(), "db");
        configurationRepository.persist(node.getDataSourcePath("db"), readYAML(DATA_SOURCE_YAML));
        configurationRepository.persist(node.getRulePath("db"), readYAML(SHARDING_RULE_YAML));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    private void closeConfigCenter() {
        configurationRepository.close();
    }
    
    @Test
    public void assertGetProxyConfigurationFromLocalConfiguration() throws IOException {
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/local");
        ProxyConfiguration actual = getInitializer().getProxyConfiguration(yamlProxyConfig);
        assertNotNull(actual);
        assertProxyConfiguration(actual);
        closeConfigCenter();
    }
    
    private void assertProxyConfiguration(final ProxyConfiguration actual) {
        assertNotNull(actual);
        assertSchemaDataSources(actual.getSchemaDataSources());
        assertSchemaRules(actual.getSchemaRules());
        assertAuthentication(actual.getAuthentication());
        assertProps(actual.getProps());
    }
    
    private void assertSchemaDataSources(final Map<String, Map<String, DataSourceParameter>> actual) {
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("db"));
        Map<String, DataSourceParameter> dataSourceParameterMap = actual.get("db");
        assertThat(dataSourceParameterMap.size(), is(2));
        assertTrue(dataSourceParameterMap.containsKey("ds_0"));
        assertDataSourceParameter(dataSourceParameterMap.get("ds_0"));
        assertTrue(dataSourceParameterMap.containsKey("ds_1"));
        assertDataSourceParameter(dataSourceParameterMap.get("ds_1"));
    }
    
    private void assertDataSourceParameter(final DataSourceParameter actual) {
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
    
    private void assertSchemaRules(final Map<String, Collection<RuleConfiguration>> actual) {
        assertThat(actual.size(), is(1));
        assertTrue(actual.containsKey("db"));
        Collection<RuleConfiguration> ruleConfigurations = actual.get("db");
        assertThat(ruleConfigurations.size(), is(1));
        assertRuleConfiguration(ruleConfigurations.iterator().next());
    }
    
    private void assertRuleConfiguration(final RuleConfiguration actual) {
        assertNotNull(actual);
        assertThat(actual, instanceOf(ShardingRuleConfiguration.class));
        ShardingRuleConfiguration shardingRule = (ShardingRuleConfiguration) actual;
        assertShardingTableRules(shardingRule.getTables());
        assertShardingAlgorithms(shardingRule.getShardingAlgorithms());
    }
    
    private void assertShardingTableRules(final Collection<ShardingTableRuleConfiguration> shardingTableRules) {
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
    
    private void assertShardingAlgorithms(final Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithms) {
        assertNotNull(shardingAlgorithms);
        assertThat(shardingAlgorithms.size(), is(2));
        assertTrue(shardingAlgorithms.containsKey("database_inline"));
        assertShardingAlgorithm(shardingAlgorithms.get("database_inline"), "ds_${user_id % 2}");
        assertTrue(shardingAlgorithms.containsKey("t_order_inline"));
        assertShardingAlgorithm(shardingAlgorithms.get("t_order_inline"), "t_order_${order_id % 2}");
    }
    
    private void assertShardingAlgorithm(final ShardingSphereAlgorithmConfiguration shardingAlgorithm, final String expectedAlgorithmExpr) {
        assertNotNull(shardingAlgorithm);
        assertThat(shardingAlgorithm.getType(), is("INLINE"));
        Properties props = shardingAlgorithm.getProps();
        assertNotNull(props);
        assertThat(props.getProperty("algorithm-expression"), is(expectedAlgorithmExpr));
    }
    
    private void assertAuthentication(final Authentication actual) {
        assertThat(actual.getUsers().size(), is(2));
        assertTrue(actual.getUsers().containsKey("root"));
        ProxyUser rootProxyUser = actual.getUsers().get("root");
        assertThat(rootProxyUser.getPassword(), is("root"));
        assertThat(rootProxyUser.getAuthorizedSchemas().size(), is(0));
        assertTrue(actual.getUsers().containsKey("sharding"));
        ProxyUser shardingProxyUser = actual.getUsers().get("sharding");
        assertThat(shardingProxyUser.getPassword(), is("sharding"));
        assertThat(shardingProxyUser.getAuthorizedSchemas().size(), is(1));
        assertTrue(shardingProxyUser.getAuthorizedSchemas().contains("sharding_db"));
    }
    
    @Test
    public void assertDecorateMetaDataContexts() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        MetaDataContexts actualMetaDataContexts = getInitializer().decorateMetaDataContexts(metaDataContexts);
        assertNotNull(actualMetaDataContexts);
        assertThat(actualMetaDataContexts, instanceOf(GovernanceMetaDataContexts.class));
        assertThat(actualMetaDataContexts.getDatabaseType(), is(metaDataContexts.getDatabaseType()));
        assertThat(actualMetaDataContexts.getMetaDataMap(), is(metaDataContexts.getMetaDataMap()));
        assertThat(actualMetaDataContexts.getDefaultMetaData(), is(metaDataContexts.getDefaultMetaData()));
        assertThat(actualMetaDataContexts.getAuthentication(), is(metaDataContexts.getAuthentication()));
        assertThat(actualMetaDataContexts.getProps(), is(metaDataContexts.getProps()));
        assertThat(actualMetaDataContexts.isCircuitBreak(), is(metaDataContexts.isCircuitBreak()));
    }
    
    @Test
    public void assertDecorateTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        TransactionContexts actualTransactionContexts = getInitializer().decorateTransactionContexts(transactionContexts);
        assertNotNull(actualTransactionContexts);
        assertThat(actualTransactionContexts, instanceOf(GovernanceTransactionContexts.class));
        assertThat(actualTransactionContexts.getEngines(), is(transactionContexts.getEngines()));
        assertThat(actualTransactionContexts.getDefaultTransactionManagerEngine(), is(transactionContexts.getDefaultTransactionManagerEngine()));
    }
    
    @Override
    protected void prepareSpecifiedInitializer() {
        setInitializer(new GovernanceBootstrapInitializer());
    }
}
