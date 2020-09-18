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
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.context.schema.GovernanceSchemaContexts;
import org.apache.shardingsphere.governance.context.transaction.GovernanceTransactionContexts;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.ProxyUser;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.apache.shardingsphere.proxy.config.ProxyConfiguration;
import org.apache.shardingsphere.proxy.config.ProxyConfigurationLoader;
import org.apache.shardingsphere.proxy.config.YamlProxyConfiguration;
import org.apache.shardingsphere.proxy.fixture.FixtureConfigurationRepository;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class GovernanceBootstrapInitializerTest extends AbstractBootstrapInitializerTest {
    
    private static final String DATA_SOURCE_YAML = "conf/reg_center/config_center/data-sources.yaml";
    
    private static final String SHARDING_RULE_YAML = "conf/reg_center/config_center/sharding-rule.yaml";
    
    private static final String AUTHENTICATION_YAML = "conf/reg_center/config_center/authentication.yaml";
    
    private static final String PROPS_YAML = "conf/reg_center/config_center/props.yaml";
    
    private FixtureConfigurationRepository configurationRepository = new FixtureConfigurationRepository();
    
    @Test
    public void assertGetProxyConfiguration() {
        super.doAssertGetProxyConfiguration();
    }
    
    protected void beforeAssertGetProxyConfiguration() {
        initConfigCenter();
    }
    
    protected void afterAssertGetProxyConfiguration() {
        closeConfigCenter();
    }
    
    @SneakyThrows
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
    
    @SneakyThrows
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
    
    @Test
    public void assertDecorateSchemaContexts() {
        Pair<SchemaContexts, SchemaContexts> schemaContextsSchemaContextPair = makeDecoratedSchemaContexts();
        SchemaContexts schemaContexts = schemaContextsSchemaContextPair.getKey();
        SchemaContexts actualSchemaContexts = schemaContextsSchemaContextPair.getValue();
        assertNotNull(actualSchemaContexts);
        assertThat(actualSchemaContexts, instanceOf(GovernanceSchemaContexts.class));
        assertThat(actualSchemaContexts.getDatabaseType(), is(schemaContexts.getDatabaseType()));
        assertThat(actualSchemaContexts.getSchemaContexts(), is(schemaContexts.getSchemaContexts()));
        assertThat(actualSchemaContexts.getDefaultSchemaContext(), is(schemaContexts.getDefaultSchemaContext()));
        assertThat(actualSchemaContexts.getAuthentication(), is(schemaContexts.getAuthentication()));
        assertThat(actualSchemaContexts.getProps(), is(schemaContexts.getProps()));
        assertThat(actualSchemaContexts.isCircuitBreak(), is(schemaContexts.isCircuitBreak()));
    }
    
    @Test
    public void assertDecorateTransactionContexts() {
        Pair<TransactionContexts, TransactionContexts> transactionContextsPair = makeDecoratedTransactionContexts();
        TransactionContexts transactionContexts = transactionContextsPair.getKey();
        TransactionContexts actualTransactionContexts = transactionContextsPair.getValue();
        assertNotNull(actualTransactionContexts);
        assertThat(actualTransactionContexts, instanceOf(GovernanceTransactionContexts.class));
        assertThat(actualTransactionContexts.getEngines(), is(transactionContexts.getEngines()));
        assertThat(actualTransactionContexts.getDefaultTransactionManagerEngine(), is(transactionContexts.getDefaultTransactionManagerEngine()));
    }
    
    protected void prepareSpecifiedInitializer() {
        setInitializer(new GovernanceBootstrapInitializer());
    }
    
    protected void assertSchemaDataSources(final Map<String, Map<String, DataSourceParameter>> actual) {
        assertThat(actual.size(), is(1));
        String key = fetchKey();
        assertTrue(actual.containsKey(key));
        Map<String, DataSourceParameter> dataSourceParameterMap = actual.get(key);
        assertThat(dataSourceParameterMap.size(), is(2));
        assertTrue(dataSourceParameterMap.containsKey("ds_0"));
        assertDataSourceParameter(dataSourceParameterMap.get("ds_0"));
        assertTrue(dataSourceParameterMap.containsKey("ds_1"));
        assertDataSourceParameter(dataSourceParameterMap.get("ds_1"));
    }
    
    protected String fetchKey() {
        return "db";
    }
    
    protected void assertRuleConfiguration(final RuleConfiguration actual) {
        assertNotNull(actual);
        assertThat(actual, instanceOf(ShardingRuleConfiguration.class));
        ShardingRuleConfiguration shardingRule = (ShardingRuleConfiguration) actual;
        assertShardingTableRules(shardingRule.getTables());
        assertShardingAlgorithms(shardingRule.getShardingAlgorithms());
    }
    
    protected void assertShardingAlgorithms(final Map<String, ShardingSphereAlgorithmConfiguration> shardingAlgorithms) {
        assertNotNull(shardingAlgorithms);
        assertThat(shardingAlgorithms.size(), is(2));
        assertTrue(shardingAlgorithms.containsKey("database_inline"));
        assertShardingAlgorithm(shardingAlgorithms.get("database_inline"), "ds_${user_id % 2}");
        assertTrue(shardingAlgorithms.containsKey("t_order_inline"));
        assertShardingAlgorithm(shardingAlgorithms.get("t_order_inline"), "t_order_${order_id % 2}");
    }
    
    protected void assertShardingAlgorithm(final ShardingSphereAlgorithmConfiguration shardingAlgorithm, final String expectedAlgorithmExpr) {
        assertNotNull(shardingAlgorithm);
        assertThat(shardingAlgorithm.getType(), is("INLINE"));
        Properties props = shardingAlgorithm.getProps();
        assertNotNull(props);
        assertThat(props.getProperty("algorithm-expression"), is(expectedAlgorithmExpr));
    }
    
    protected void assertAuthentication(final Authentication actual) {
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
}
