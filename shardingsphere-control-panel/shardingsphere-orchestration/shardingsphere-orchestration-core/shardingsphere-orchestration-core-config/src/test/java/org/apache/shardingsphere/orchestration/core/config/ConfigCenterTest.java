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

package org.apache.shardingsphere.orchestration.core.config;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigCenterTest {
    
    private static final String DATA_SOURCE_YAML = ""
            + "ds_0: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  props:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n"
            + "    url: jdbc:mysql://localhost:3306/ds_0\n"
            + "    username: root\n" + "    password: root\n"
            + "ds_1: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  props:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n"
            + "    url: jdbc:mysql://localhost:3306/ds_1\n"
            + "    username: root\n"
            + "    password: root\n";
    
    private static final String SHARDING_RULE_YAML = ""
            + "rules:\n"
            + "- !SHARDING\n"
            + "  shardingAlgorithms:\n"
            + "    table_inline:\n"
            + "      props:\n"
            + "        algorithm.expression: t_order_${order_id % 2}\n"
            + "      type: INLINE\n"
            + "  tables:\n"
            + "    t_order:\n"
            + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "      logicTable: t_order\n"
            + "      tableStrategy:\n"
            + "        standard:\n"
            + "          shardingAlgorithmName: table_inline\n"
            + "          shardingColumn: order_id\n";
    
    private static final String SHARDING_AND_ENCRYPT_RULE_YAML = ""
            + "rules:\n"
            + "- !SHARDING\n"
            + "  tables:\n"
            + "    t_order:\n"
            + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n"
            + "      tableStrategy:\n"
            + "        standard:\n"
            + "          shardingColumn: order_id\n"
            + "          shardingAlgorithmName: table_inline\n"
            + "      keyGenerateStrategy:\n"
            + "        column: order_id\n"
            + "        keyGeneratorName: snowflake\n"
            + "  shardingAlgorithms:\n"
            + "    table_inline:\n"
            + "      type: INLINE\n"
            + "      props:\n"
            + "        algorithm.expression: t_order_${order_id % 2}\n"
            + "  keyGenerators:\n"
            + "    snowflake:\n"
            + "      type: SNOWFLAKE\n"
            + "- !ENCRYPT\n"
            + "  encryptors:\n"
            + "    aes_encryptor:\n"
            + "      type: AES\n"
            + "      props:\n"
            + "        aes.key.value: 123456abcd\n"
            + "    md5_encryptor:\n"
            + "      type: MD5\n"
            + "  tables:\n"
            + "    t_encrypt:\n"
            + "      columns:\n"
            + "        user_id:\n"
            + "          plainColumn: user_plain\n"
            + "          cipherColumn: user_cipher\n"
            + "          encryptorName: aes_encryptor\n"
            + "        order_id:\n"
            + "          cipherColumn: order_cipher\n"
            + "          encryptorName: md5_encryptor";
    
    private static final String MASTER_SLAVE_RULE_YAML = ""
            + "rules:\n"
            + "- !MASTER_SLAVE\n"
            + "  dataSources:\n"
            + "    ms_ds:\n"
            + "      masterDataSourceName: master_ds\n"
            + "      name: ms_ds\n"
            + "      slaveDataSourceNames:\n"
            + "      - slave_ds_0\n"
            + "      - slave_ds_1\n";
    
    private static final String ENCRYPT_RULE_YAML = ""
            + "rules:\n"
            + "- !ENCRYPT\n"
            + "  encryptors:\n"
            + "    order_encryptor:\n"
            + "      props:\n"
            + "        aes.key.value: 123456\n"
            + "      type: AES\n"
            + "  tables:\n"
            + "    t_order:\n"
            + "      columns:\n"
            + "        order_id:\n"
            + "          cipherColumn: order_id\n"
            + "          encryptorName: order_encryptor\n";
    
    private static final String SHADOW_RULE_YAML = ""
            + "rules:\n"
            + "- !SHADOW\n"
            + "  column: shadow\n"
            + "  shadowMappings:\n"
            + "    ds: shadow_ds\n";
    
    private static final String AUTHENTICATION_YAML = ""
            + "users:\n"
            + "  root1:\n"
            + "    authorizedSchemas: sharding_db\n"
            + "    password: root1\n"
            + "  root2:\n"
            + "    authorizedSchemas: sharding_db,ms_db\n"
            + "    password: root2\n";
    
    private static final String METRICS_YAML = ""
            + "  name: prometheus\n"
            + "  host: 127.0.0.1\n"
            + "  port: 9190\n";
    
    private static final String PROPS_YAML = "sql.show: false\n";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQLS = ""
            + "ds_0: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  props:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n"
            + "    url: jdbc:mysql://localhost:3306/ds_0\n"
            + "    username: root\n"
            + "    password: root\n"
            + "    connectionInitSqls:\n"
            + "        - set names utf8mb4;\n"
            + "        - set names utf8;\n"
            + "ds_1: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n"
            + "  props:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n"
            + "    url: jdbc:mysql://localhost:3306/ds_1\n"
            + "    username: root\n"
            + "    password: root\n"
            + "    connectionInitSqls:\n"
            + "        - set names utf8mb4;\n"
            + "        - set names utf8;\n";
    
    private static final String DATA_CLUSTER_YAML = "yaml/data-cluster.yaml";
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
    }
    
    @Test
    public void assertMoreShardingSchema() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/test/config/schema", "myTest1,myTest2,sharding_db");
    }
    
    @Test
    public void assertMoreAndContainsShardingSchema() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/test/config/schema", "myTest1,sharding_db");
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db",
                createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", ENCRYPT_RULE_YAML);
    }
    
    @Test
    public void assertNullRuleConfiguration() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), Collections.emptyList(), true);
    }
    
    @Test
    @Ignore
    public void assertPersistConfigurationForShadow() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist("/test/config/schema/sharding_db/rule", SHADOW_RULE_YAML);
    }
    
    @Test
    public void assertPersistGlobalConfiguration() {
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistGlobalConfiguration(createAuthentication(), createProperties(), true);
        verify(configurationRepository).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configurationRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> DataSourceConfiguration.getDataSourceConfiguration(entry.getValue())));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration(final DataSource dataSource) {
        return DataSourceConfiguration.getDataSourceConfiguration(dataSource);
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createDataSource("ds_0"));
        result.put("ds_1", createDataSource("ds_1"));
        return result;
    }
    
    private DataSource createDataSource(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private Collection<RuleConfiguration> createRuleConfigurations() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(SHARDING_RULE_YAML, YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createMasterSlaveRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(MASTER_SLAVE_RULE_YAML, YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createEncryptRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(ENCRYPT_RULE_YAML, YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createShadowRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(SHADOW_RULE_YAML, YamlRootRuleConfigurations.class).getRules());
    }
    
    private Authentication createAuthentication() {
        return new AuthenticationYamlSwapper().swapToObject(YamlEngine.unmarshal(AUTHENTICATION_YAML, YamlAuthenticationConfiguration.class));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        when(configurationRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), createDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), createDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
    }
    
    @Test
    public void assertLoadShardingAndEncryptRuleConfiguration() {
        when(configurationRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_AND_ENCRYPT_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Collection<RuleConfiguration> ruleConfigurations = configurationService.loadRuleConfigurations("sharding_db");
        assertThat(ruleConfigurations.size(), is(2));
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfiguration = (ShardingRuleConfiguration) each;
                assertThat(shardingRuleConfiguration.getTables().size(), is(1));
                assertThat(shardingRuleConfiguration.getTables().iterator().next().getLogicTable(), is("t_order"));
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfiguration = (EncryptRuleConfiguration) each;
                assertThat(encryptRuleConfiguration.getEncryptors().size(), is(2));
                ShardingSphereAlgorithmConfiguration encryptAlgorithmConfiguration = encryptRuleConfiguration.getEncryptors().get("aes_encryptor");
                assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
                assertThat(encryptAlgorithmConfiguration.getProps().get("aes.key.value").toString(), is("123456abcd"));
            }
        }
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(configurationRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Collection<RuleConfiguration> actual = configurationService.loadRuleConfigurations("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfiguration = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfiguration.getTables().size(), is(1));
        assertThat(actualShardingRuleConfiguration.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        when(configurationRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Collection<RuleConfiguration> actual = configurationService.loadRuleConfigurations("sharding_db");
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = (MasterSlaveRuleConfiguration) actual.iterator().next();
        assertThat(masterSlaveRuleConfiguration.getDataSources().size(), is(1));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getMasterDataSourceName(), is("master_ds"));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getSlaveDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(configurationRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        EncryptRuleConfiguration actual = (EncryptRuleConfiguration) configurationService.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfiguration = actual.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
        assertThat(encryptAlgorithmConfiguration.getProps().get("aes.key.value").toString(), is("123456"));
    }
    
    @Test
    @Ignore
    // TODO fix shadow
    public void assertLoadShadowRuleConfiguration() {
        when(configurationRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHADOW_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        ShadowRuleConfiguration actual = (ShadowRuleConfiguration) configurationService.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getShadowMappings().get("ds"), is("shadow_ds"));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @Test
    public void assertLoadAuthentication() {
        when(configurationRepository.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Authentication actual = configurationService.loadAuthentication();
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("root1").getPassword(), is("root1"));
    }
    
    @Test
    public void assertLoadMetricsConfiguration() {
        when(configurationRepository.get("/test/config/metrics")).thenReturn(METRICS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        MetricsConfiguration actual = configurationService.loadMetricsConfiguration();
        assertNotNull(actual);
        assertThat(actual.getMetricsName(), is("prometheus"));
        assertThat(actual.getPort(), is(9190));
        assertThat(actual.getHost(), is("127.0.0.1"));
        assertTrue(actual.getAsync());
        assertTrue(actual.getEnable());
    }
    
    @Test
    public void assertLoadProperties() {
        when(configurationRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Properties actual = configurationService.loadProperties();
        assertThat(actual.get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.FALSE));
    }
    
    @Test
    public void assertGetAllSchemaNames() {
        when(configurationRepository.get("/test/config/schema")).thenReturn("sharding_db,masterslave_db");
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Collection<String> actual = configurationService.getAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("masterslave_db"));
    }
    
    @Test
    public void assertLoadDataSourceConfigurationsWithConnectionInitSqls() {
        when(configurationRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQLS);
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_0"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_0")));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_1"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_1")));
    }
    
    @Test
    public void assertPersistClusterConfiguration() {
        ClusterConfiguration clusterConfiguration = new ClusterConfigurationYamlSwapper()
                .swapToObject(YamlEngine.unmarshal(readYAML(DATA_CLUSTER_YAML), YamlClusterConfiguration.class));
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        configurationService.persistClusterConfiguration(clusterConfiguration, true);
        verify(configurationRepository, times(0)).persist(eq("/test/config/cluster"), eq(readYAML(DATA_CLUSTER_YAML)));
    }
    
    @Test
    public void loadClusterConfiguration() {
        when(configurationRepository.get("/test/config/cluster")).thenReturn(readYAML(DATA_CLUSTER_YAML));
        ConfigCenter configurationService = new ConfigCenter("test", configurationRepository);
        ClusterConfiguration clusterConfiguration = configurationService.loadClusterConfiguration();
        assertNotNull(clusterConfiguration);
        assertNotNull(clusterConfiguration.getHeartbeat());
        assertThat(clusterConfiguration.getHeartbeat().getSql(), is("select 1"));
        assertThat(clusterConfiguration.getHeartbeat().getThreadCount(), is(1));
        assertThat(clusterConfiguration.getHeartbeat().getInterval(), is(60));
        assertFalse(clusterConfiguration.getHeartbeat().isRetryEnable());
        assertThat(clusterConfiguration.getHeartbeat().getRetryMaximum(), is(3));
        assertThat(clusterConfiguration.getHeartbeat().getRetryInterval(), is(3));
    }
    
    private DataSource createDataSourceWithConnectionInitSqls(final String name) {
        BasicDataSource result = new BasicDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        result.setConnectionInitSqls(Arrays.asList("set names utf8mb4;", "set names utf8;"));
        return result;
    }
    
    private void assertDataSourceConfigurationWithConnectionInitSqls(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
        assertThat(actual.getProps().get("connectionInitSqls"), is(expected.getProps().get("connectionInitSqls")));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI())).stream().map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
}
