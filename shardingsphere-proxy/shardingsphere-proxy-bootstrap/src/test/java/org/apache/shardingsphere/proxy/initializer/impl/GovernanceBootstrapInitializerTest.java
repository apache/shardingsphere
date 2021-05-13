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
import org.apache.shardingsphere.governance.context.metadata.GovernanceMetaDataContexts;
import org.apache.shardingsphere.governance.context.transaction.GovernanceTransactionContexts;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureRegistryCenterRepository;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.core.XATransactionManagerType;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class GovernanceBootstrapInitializerTest extends AbstractBootstrapInitializerTest {
    
    private static final String DATA_SOURCE_YAML = "conf/reg_center/data-sources.yaml";
    
    private static final String SHARDING_RULE_YAML = "conf/reg_center/sharding-rule.yaml";
    
    private static final String USERS_YAML = "conf/reg_center/users.yaml";
    
    private static final String PROPS_YAML = "conf/reg_center/props.yaml";
    
    private final FixtureRegistryCenterRepository registryCenterRepository = new FixtureRegistryCenterRepository();
    
    @Test
    public void assertGetProxyConfiguration() throws IOException {
        initConfigCenter();
        YamlProxyConfiguration yamlProxyConfig = ProxyConfigurationLoader.load("/conf/reg_center/");
        assertProxyConfiguration(getInitializer().getProxyConfiguration(yamlProxyConfig));
        closeConfigCenter();
    }
    
    private void initConfigCenter() {
        RegistryCenterNode node = new RegistryCenterNode();
        registryCenterRepository.persist(node.getUsersNode(), readYAML(USERS_YAML));
        registryCenterRepository.persist(node.getPropsPath(), readYAML(PROPS_YAML));
        registryCenterRepository.persist(node.getMetadataNodePath(), "db");
        registryCenterRepository.persist(node.getMetadataDataSourcePath("db"), readYAML(DATA_SOURCE_YAML));
        registryCenterRepository.persist(node.getRulePath("db"), readYAML(SHARDING_RULE_YAML));
    }
    
    @SneakyThrows({URISyntaxException.class, IOException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    private void closeConfigCenter() {
        registryCenterRepository.close();
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
    
    private void assertUsers(final ShardingSphereUsers actual) {
        Optional<ShardingSphereUser> rootUser = actual.findUser(new Grantee("root", ""));
        assertTrue(rootUser.isPresent());
        assertThat(rootUser.get().getPassword(), is("root"));
        Optional<ShardingSphereUser> shardingUser = actual.findUser(new Grantee("sharding", ""));
        assertTrue(shardingUser.isPresent());
        assertThat(shardingUser.get().getPassword(), is("sharding"));
    }
    
    @Test
    public void assertDecorateMetaDataContexts() {
        StandardMetaDataContexts metaDataContexts = mock(StandardMetaDataContexts.class);
        when(metaDataContexts.getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        MetaDataContexts actualMetaDataContexts = getInitializer().decorateMetaDataContexts(metaDataContexts);
        assertNotNull(actualMetaDataContexts);
        assertThat(actualMetaDataContexts, instanceOf(GovernanceMetaDataContexts.class));
        assertThat(actualMetaDataContexts.getDefaultMetaData(), is(metaDataContexts.getDefaultMetaData()));
        assertThat(actualMetaDataContexts.getProps(), is(metaDataContexts.getProps()));
    }
    
    @Test
    public void assertDecorateTransactionContexts() {
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        TransactionContexts actualTransactionContexts = getInitializer().decorateTransactionContexts(transactionContexts, XATransactionManagerType.ATOMIKOS.getType());
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
