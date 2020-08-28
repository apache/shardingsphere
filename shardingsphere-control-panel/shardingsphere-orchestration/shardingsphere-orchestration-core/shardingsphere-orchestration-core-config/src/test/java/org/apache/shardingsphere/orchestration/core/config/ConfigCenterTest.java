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
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ConfigCenterTest {
    
    private static final String DATA_SOURCE_YAM = "yaml/configCenter/data-source.yaml";
    
    private static final String SHARDING_RULE_YAML = "yaml/configCenter/data-sharding-rule.yaml";
    
    private static final String SHARDING_AND_ENCRYPT_RULE_YAML = "yaml/configCenter/data-sharding-encrypt-rule.yaml";
    
    private static final String MASTER_SLAVE_RULE_YAML = "yaml/configCenter/data-master-slave-rule.yaml";
    
    private static final String ENCRYPT_RULE_YAML = "yaml/configCenter/data-encrypt-rule.yaml";
    
    private static final String SHADOW_RULE_YAML = "yaml/configCenter/data-shadow-rule.yaml";
    
    private static final String AUTHENTICATION_YAML = "yaml/configCenter/data-authentication.yaml";
    
    private static final String PROPS_YAML = "sql.show: false\n";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL = "yaml/configCenter/data-source-init-sql.yaml";
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertMoreShardingSchema() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/config/schema", "myTest1,myTest2,sharding_db");
    }
    
    @Test
    public void assertMoreAndContainsShardingSchema() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/config/schema", "myTest1,sharding_db");
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
        
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db",
                createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository, times(0)).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(ENCRYPT_RULE_YAML));
    }
    
    @Test
    public void assertNullRuleConfiguration() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), Collections.emptyList(), true);
    }
    
    @Test
    public void assertPersistConfigurationForShadow() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/config/schema/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/config/schema/sharding_db/rule", readYAML(SHADOW_RULE_YAML));
    }
    
    @Test
    public void assertPersistGlobalConfiguration() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        configurationService.persistGlobalConfiguration(createAuthentication(), createProperties(), true);
        verify(configurationRepository, times(0)).persist("/config/authentication", readYAML(AUTHENTICATION_YAML));
        verify(configurationRepository).persist("/config/props", PROPS_YAML);
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
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(SHARDING_RULE_YAML), YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createMasterSlaveRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(MASTER_SLAVE_RULE_YAML), YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createEncryptRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(ENCRYPT_RULE_YAML), YamlRootRuleConfigurations.class).getRules());
    }
    
    private Collection<RuleConfiguration> createShadowRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(SHADOW_RULE_YAML), YamlRootRuleConfigurations.class).getRules());
    }
    
    private Authentication createAuthentication() {
        return new AuthenticationYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(AUTHENTICATION_YAML), YamlAuthenticationConfiguration.class));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        when(configurationRepository.get("/config/schema/sharding_db/datasource")).thenReturn(readYAML(DATA_SOURCE_YAM));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
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
    public void assertLoadDataSourceConfigurationsNotExistPath() {
        when(configurationRepository.get("/config/schema/sharding_db/datasource")).thenReturn("");
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Map<String, DataSourceConfiguration> actual = configurationService.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertLoadShardingAndEncryptRuleConfiguration() {
        when(configurationRepository.get("/config/schema/sharding_db/rule")).thenReturn(readYAML(SHARDING_AND_ENCRYPT_RULE_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
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
        when(configurationRepository.get("/config/schema/sharding_db/rule")).thenReturn(readYAML(SHARDING_RULE_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Collection<RuleConfiguration> actual = configurationService.loadRuleConfigurations("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfiguration = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfiguration.getTables().size(), is(1));
        assertThat(actualShardingRuleConfiguration.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        when(configurationRepository.get("/config/schema/sharding_db/rule")).thenReturn(readYAML(MASTER_SLAVE_RULE_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Collection<RuleConfiguration> actual = configurationService.loadRuleConfigurations("sharding_db");
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = (MasterSlaveRuleConfiguration) actual.iterator().next();
        assertThat(masterSlaveRuleConfiguration.getDataSources().size(), is(1));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getMasterDataSourceName(), is("master_ds"));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getSlaveDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(configurationRepository.get("/config/schema/sharding_db/rule")).thenReturn(readYAML(ENCRYPT_RULE_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        EncryptRuleConfiguration actual = (EncryptRuleConfiguration) configurationService.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfiguration = actual.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
        assertThat(encryptAlgorithmConfiguration.getProps().get("aes.key.value").toString(), is("123456"));
    }
    
    @Test
    public void assertLoadShadowRuleConfiguration() {
        when(configurationRepository.get("/config/schema/sharding_db/rule")).thenReturn(readYAML(SHADOW_RULE_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        ShadowRuleConfiguration actual = (ShadowRuleConfiguration) configurationService.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getShadowMappings().get("ds"), is("shadow_ds"));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @Test
    public void assertLoadAuthentication() {
        when(configurationRepository.get("/config/authentication")).thenReturn(readYAML(AUTHENTICATION_YAML));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Authentication actual = configurationService.loadAuthentication();
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("root1").getPassword(), is("root1"));
    }
    
    @Test
    public void assertLoadProperties() {
        when(configurationRepository.get("/config/props")).thenReturn(PROPS_YAML);
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Properties actual = configurationService.loadProperties();
        assertThat(actual.get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.FALSE));
    }
    
    @Test
    public void assertGetAllSchemaNames() {
        when(configurationRepository.get("/config/schema")).thenReturn("sharding_db,masterslave_db");
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        Collection<String> actual = configurationService.getAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("masterslave_db"));
    }
    
    @Test
    public void assertLoadDataSourceConfigurationsWithConnectionInitSqls() {
        when(configurationRepository.get("/config/schema/sharding_db/datasource")).thenReturn(readYAML(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL));
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
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
        assertThat(actual.getProps().get("url"), is(expected.getProps().get("url")));
        assertThat(actual.getProps().get("username"), is(expected.getProps().get("username")));
        assertThat(actual.getProps().get("password"), is(expected.getProps().get("password")));
        assertThat(actual.getProps().get("connectionInitSqls"), is(expected.getProps().get("connectionInitSqls")));
    }
    
    @SneakyThrows
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    @Test
    public void assertPersistSchemaNameWithExistSchema() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        when(configurationRepository.get("/config/schema")).thenReturn("sharding_db");
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository, times(0)).persist(eq("/config/schema"), eq("sharding_db"));
    }
    
    @Test
    public void assertPersistSchemaNameWithExistAndNewSchema() {
        ConfigCenter configurationService = new ConfigCenter(configurationRepository);
        when(configurationRepository.get("/config/schema")).thenReturn("master_slave_db");
        configurationService.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/config/schema"), eq("master_slave_db,sharding_db"));
    }
}
