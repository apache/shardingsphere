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

package org.apache.shardingsphere.orchestration.core.configcenter;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootRuleConfigurationsConstructor;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptorRuleConfiguration;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigCenterTest {
    
    private static final String DATA_SOURCE_YAML =
            "ds_0: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_0\n" + "    username: root\n" + "    password: root\n"
            + "ds_1: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
            + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
            + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_1\n" + "    username: root\n" + "    password: root\n";
    
    private static final String DATA_SOURCE_PARAMETER_YAML = "ds_0: !!org.apache.shardingsphere.core.rule.DataSourceParameter\n"
            + "  url: jdbc:mysql://localhost:3306/ds_0\n" + "  username: root\n" + "  password: root\n"
            + "ds_1: !!org.apache.shardingsphere.core.rule.DataSourceParameter\n"
            + "  url: jdbc:mysql://localhost:3306/ds_1\n" + "  username: root\n" + "  password: root\n";
    
    private static final String SHARDING_RULE_YAML = "shardingRule:\n  tables:\n" + "    t_order:\n" + "      actualDataNodes: ds_${0..1}.t_order_${0..1}\n" + "      logicTable: t_order\n" 
            + "      tableStrategy:\n" + "        standard:\n" + "          shardingAlgorithm:\n" + "            props:\n" 
            + "              algorithm.expression: t_order_${order_id % 2}\n" + "            type: INLINE\n" + "          shardingColumn: order_id\n";
    
    private static final String MASTER_SLAVE_RULE_YAML = "masterSlaveRules:\n  ms_ds:\n    masterDataSourceName: master_ds\n"
            + "    name: ms_ds\n" + "    slaveDataSourceNames:\n" + "    - slave_ds_0\n" + "    - slave_ds_1\n";
    
    private static final String ENCRYPT_RULE_YAML = "encryptRule:\n  encryptors:\n" + "    order_encryptor:\n"
            + "      props:\n" + "        aes.key.value: 123456\n" + "      type: aes\n" + "  tables:\n" + "    t_order:\n" + "      columns:\n" 
            + "        order_id:\n" + "          cipherColumn: order_id\n" + "          encryptor: order_encryptor\n";
    
    private static final String SHADOW_RULE_YAML = "shadowRule:\n  column: shadow\n" + "  shadowMappings:\n" + "    ds: shadow_ds\n";
    
    private static final String AUTHENTICATION_YAML = "users:\n" + "  root1:\n" + "    authorizedSchemas: sharding_db\n" + "    password: root1\n" 
            + "  root2:\n" + "    authorizedSchemas: sharding_db,ms_db\n" + "    password: root2\n";
    
    private static final String PROPS_YAML = "sql.show: false\n";
    
    private static final String SHARDING_RULE_YAML_DEFAULT_TABLE_STRATEGY_NONE = "shardingRule:\n  defaultTableStrategy:\n" + "    none: ''\n";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQLS =
            "ds_0: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_0\n" + "    username: root\n" + "    password: root\n"
                    + "    connectionInitSqls:\n" + "        - set names utf8mb4;\n" + "        - set names utf8;\n"
                    + "ds_1: !!" + YamlDataSourceConfiguration.class.getName() + "\n"
                    + "  dataSourceClassName: org.apache.commons.dbcp2.BasicDataSource\n" + "  properties:\n"
                    + "    driverClassName: com.mysql.jdbc.Driver\n" + "    url: jdbc:mysql://localhost:3306/ds_1\n" + "    username: root\n" + "    password: root\n"
                    + "    connectionInitSqls:\n" + "        - set names utf8mb4;\n" + "        - set names utf8;\n";
    
    @Mock
    private ConfigCenterRepository configCenterRepository;
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(configCenterRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), null, createProperties(), false);
        verify(configCenterRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), null, createProperties(), false);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), null, createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(configCenterRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), null, createProperties(), false);
        verify(configCenterRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), null, createProperties(), false);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), null, createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_PARAMETER_YAML);
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        when(configCenterRepository.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        when(configCenterRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), createAuthentication(), createProperties(), false);
        verify(configCenterRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository, times(0)).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), createAuthentication(), createProperties(), false);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), createAuthentication(), createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", SHARDING_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_PARAMETER_YAML);
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        when(configCenterRepository.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        when(configCenterRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), createAuthentication(), createProperties(), false);
        verify(configCenterRepository, times(0)).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository, times(0)).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository, times(0)).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db",
                createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), createAuthentication(), createProperties(), false);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), createAuthentication(), createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", MASTER_SLAVE_RULE_YAML);
        verify(configCenterRepository).persist("/test/config/authentication", AUTHENTICATION_YAML);
        verify(configCenterRepository).persist("/test/config/props", PROPS_YAML);
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), null, createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", ENCRYPT_RULE_YAML);
    }
    
    @Test
    @Ignore
    // TODO process shadow
    public void assertPersistConfigurationForShadow() {
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), null, createProperties(), true);
        verify(configCenterRepository).persist(eq("/test/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configCenterRepository).persist("/test/config/schema/sharding_db/rule", SHADOW_RULE_YAML);
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
        return new RuleRootConfigurationsYamlSwapper().swap(YamlEngine.unmarshal(SHARDING_RULE_YAML, YamlRootRuleConfigurations.class, new YamlRootRuleConfigurationsConstructor()));
    }
    
    private Collection<RuleConfiguration> createMasterSlaveRuleConfiguration() {
        return new RuleRootConfigurationsYamlSwapper().swap(YamlEngine.unmarshal(MASTER_SLAVE_RULE_YAML, YamlRootRuleConfigurations.class));
    }
    
    private Collection<RuleConfiguration> createEncryptRuleConfiguration() {
        return new RuleRootConfigurationsYamlSwapper().swap(YamlEngine.unmarshal(ENCRYPT_RULE_YAML, YamlRootRuleConfigurations.class));
    }
    
    private Collection<RuleConfiguration> createShadowRuleConfiguration() {
        return new RuleRootConfigurationsYamlSwapper().swap(YamlEngine.unmarshal(SHADOW_RULE_YAML, YamlRootRuleConfigurations.class));
    }
    
    private Authentication createAuthentication() {
        return new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(AUTHENTICATION_YAML, YamlAuthenticationConfiguration.class));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfiguration(actual.get("ds_0"), createDataSourceConfiguration(createDataSource("ds_0")));
        assertDataSourceConfiguration(actual.get("ds_1"), createDataSourceConfiguration(createDataSource("ds_1")));
    }
    
    private void assertDataSourceConfiguration(final DataSourceConfiguration actual, final DataSourceConfiguration expected) {
        assertThat(actual.getDataSourceClassName(), is(expected.getDataSourceClassName()));
        assertThat(actual.getProperties().get("url"), is(expected.getProperties().get("url")));
        assertThat(actual.getProperties().get("username"), is(expected.getProperties().get("username")));
        assertThat(actual.getProperties().get("password"), is(expected.getProperties().get("password")));
    }
    
    @Test
    public void assertIsShardingRule() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        assertTrue(configurationService.isShardingRule("sharding_db"));
    }

    @Test
    public void assertIsShardingRuleWithDefaultTableStrategyNone() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML_DEFAULT_TABLE_STRATEGY_NONE);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        assertTrue(configurationService.isShardingRule("sharding_db"));
    }
    
    @Test
    public void assertIsEncryptRule() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        assertTrue(configurationService.isEncryptRule("sharding_db"));
    }
    
    @Test
    public void assertIsShadowRule() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHADOW_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        assertTrue(configurationService.isShadowRule("sharding_db"));
    }
    
    @Test
    public void assertIsNotShardingRule() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        assertFalse(configurationService.isShardingRule("sharding_db"));
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHARDING_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Collection<RuleConfiguration> actual = configurationService.loadRuleConfigurations("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfiguration = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfiguration.getTableRuleConfigs().size(), is(1));
        assertThat(actualShardingRuleConfiguration.getTableRuleConfigs().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(MASTER_SLAVE_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        MasterSlaveRuleConfiguration actual = configurationService.loadMasterSlaveRuleConfiguration("sharding_db");
        assertThat(actual.getName(), is("ms_ds"));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(ENCRYPT_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        EncryptRuleConfiguration actual = configurationService.loadEncryptRuleConfiguration("sharding_db");
        assertThat(actual.getEncryptors().size(), is(1));
        Entry<String, EncryptorRuleConfiguration> entry = actual.getEncryptors().entrySet().iterator().next();
        assertThat(entry.getKey(), is("order_encryptor"));
        assertThat(entry.getValue().getType(), is("aes"));
        assertThat(entry.getValue().getProperties().get("aes.key.value").toString(), is("123456"));
    }
    
    @Test
    @Ignore
    // TODO fix shadow
    public void assertLoadShadowRuleConfiguration() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/rule")).thenReturn(SHADOW_RULE_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        ShadowRuleConfiguration actual = configurationService.loadShadowRuleConfiguration("sharding_db");
        assertThat(actual.getShadowMappings().get("ds"), is("shadow_ds"));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @Test
    public void assertLoadAuthentication() {
        when(configCenterRepository.get("/test/config/authentication")).thenReturn(AUTHENTICATION_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Authentication actual = configurationService.loadAuthentication();
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("root1").getPassword(), is("root1"));
    }
    
    @Test
    public void assertLoadProperties() {
        when(configCenterRepository.get("/test/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Properties actual = configurationService.loadProperties();
        assertThat(actual.get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.FALSE));
    }
    
    @Test
    public void assertGetAllShardingSchemaNames() {
        when(configCenterRepository.get("/test/config/schema")).thenReturn("sharding_db,masterslave_db");
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Collection<String> actual = configurationService.getAllShardingSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("masterslave_db"));
    }
    
    @Test
    public void assertLoadDataSourceConfigurationsWithConnectionInitSqls() {
        when(configCenterRepository.get("/test/config/schema/sharding_db/datasource")).thenReturn(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQLS);
        ConfigCenter configurationService = new ConfigCenter("test", configCenterRepository);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_0"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_0")));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_1"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_1")));
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
        assertThat(actual.getProperties().get("url"), is(expected.getProperties().get("url")));
        assertThat(actual.getProperties().get("username"), is(expected.getProperties().get("username")));
        assertThat(actual.getProperties().get("password"), is(expected.getProperties().get("password")));
        assertThat(actual.getProperties().get("connectionInitSqls"), is(expected.getProperties().get("connectionInitSqls")));
    }
}
