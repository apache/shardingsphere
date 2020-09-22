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

package org.apache.shardingsphere.governance.core.config;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.event.model.persist.DataSourcePersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.MetaDataPersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.RulePersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.SchemaNamePersistEvent;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.governance.core.yaml.swapper.RuleSchemaMetaDataYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
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
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
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
    
    private static final String PROPS_YAML = ConfigurationPropertyKey.SQL_SHOW.getKey() + ": false\n";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL = "yaml/configCenter/data-source-init-sql.yaml";
    
    private static final String META_DATA_YAML = "yaml/metadata.yaml";
    
    @Mock
    private ConfigurationRepository configurationRepository;
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertMoreSchema() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/schemas", "myTest1,myTest2,sharding_db");
    }
    
    @Test
    public void assertMoreAndContainsSchema() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository, times(0)).persist("/schemas", "myTest1,sharding_db");
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithoutAuthenticationAndIsOverwrite() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), false);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), ArgumentMatchers.any());
    }
    
    @Test
    public void assertPersistConfigurationForMasterSlaveRuleWithAuthenticationAndIsOverwrite() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createMasterSlaveRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(MASTER_SLAVE_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(ENCRYPT_RULE_YAML));
    }
    
    @Test
    public void assertNullRuleConfiguration() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), Collections.emptyList(), true);
    }
    
    @Test
    public void assertPersistConfigurationForShadow() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), true);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), ArgumentMatchers.any());
        verify(configurationRepository, times(0)).persist("/schemas/sharding_db/rule", readYAML(SHADOW_RULE_YAML));
    }
    
    @Test
    public void assertPersistGlobalConfiguration() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistGlobalConfiguration(createAuthentication(), createProperties(), true);
        verify(configurationRepository, times(0)).persist("/authentication", readYAML(AUTHENTICATION_YAML));
        verify(configurationRepository).persist("/props", PROPS_YAML);
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
        when(configurationRepository.get("/schemas/sharding_db/datasource")).thenReturn(readYAML(DATA_SOURCE_YAM));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Map<String, DataSourceConfiguration> actual = configCenter.loadDataSourceConfigurations("sharding_db");
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
        when(configurationRepository.get("/schemas/sharding_db/datasource")).thenReturn("");
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Map<String, DataSourceConfiguration> actual = configCenter.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertLoadShardingAndEncryptRuleConfiguration() {
        when(configurationRepository.get("/schemas/sharding_db/rule")).thenReturn(readYAML(SHARDING_AND_ENCRYPT_RULE_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Collection<RuleConfiguration> ruleConfigurations = configCenter.loadRuleConfigurations("sharding_db");
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
                assertThat(encryptAlgorithmConfiguration.getProps().get("aes-key-value").toString(), is("123456abcd"));
            }
        }
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(configurationRepository.get("/schemas/sharding_db/rule")).thenReturn(readYAML(SHARDING_RULE_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Collection<RuleConfiguration> actual = configCenter.loadRuleConfigurations("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfig = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfig.getTables().size(), is(1));
        assertThat(actualShardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadMasterSlaveRuleConfiguration() {
        when(configurationRepository.get("/schemas/sharding_db/rule")).thenReturn(readYAML(MASTER_SLAVE_RULE_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Collection<RuleConfiguration> actual = configCenter.loadRuleConfigurations("sharding_db");
        MasterSlaveRuleConfiguration masterSlaveRuleConfiguration = (MasterSlaveRuleConfiguration) actual.iterator().next();
        assertThat(masterSlaveRuleConfiguration.getDataSources().size(), is(1));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getMasterDataSourceName(), is("master_ds"));
        assertThat(masterSlaveRuleConfiguration.getDataSources().iterator().next().getSlaveDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(configurationRepository.get("/schemas/sharding_db/rule")).thenReturn(readYAML(ENCRYPT_RULE_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        EncryptRuleConfiguration actual = (EncryptRuleConfiguration) configCenter.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfiguration = actual.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfiguration.getType(), is("AES"));
        assertThat(encryptAlgorithmConfiguration.getProps().get("aes-key-value").toString(), is("123456"));
    }
    
    @Test
    public void assertLoadShadowRuleConfiguration() {
        when(configurationRepository.get("/schemas/sharding_db/rule")).thenReturn(readYAML(SHADOW_RULE_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        ShadowRuleConfiguration actual = (ShadowRuleConfiguration) configCenter.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getSourceDataSourceNames(), is(Arrays.asList("ds", "ds1")));
        assertThat(actual.getShadowDataSourceNames(), is(Arrays.asList("shadow_ds", "shadow_ds1")));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @Test
    public void assertLoadAuthentication() {
        when(configurationRepository.get("/authentication")).thenReturn(readYAML(AUTHENTICATION_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Authentication actual = configCenter.loadAuthentication();
        assertThat(actual.getUsers().size(), is(2));
        assertThat(actual.getUsers().get("root1").getPassword(), is("root1"));
    }
    
    @Test
    public void assertLoadProperties() {
        when(configurationRepository.get("/props")).thenReturn(PROPS_YAML);
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Properties actual = configCenter.loadProperties();
        assertThat(actual.get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.FALSE));
    }
    
    @Test
    public void assertGetAllSchemaNames() {
        when(configurationRepository.get("/schemas")).thenReturn("sharding_db,masterslave_db");
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Collection<String> actual = configCenter.getAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("masterslave_db"));
    }
    
    @Test
    public void assertLoadDataSourceConfigurationsWithConnectionInitSqls() {
        when(configurationRepository.get("/schemas/sharding_db/datasource")).thenReturn(readYAML(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Map<String, DataSourceConfiguration> actual = configCenter.loadDataSourceConfigurations("sharding_db");
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
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        when(configurationRepository.get("/schemas")).thenReturn("sharding_db");
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository, times(0)).persist(eq("/schemas"), eq("sharding_db"));
    }
    
    @Test
    public void assertPersistSchemaNameWithExistAndNewSchema() {
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        when(configurationRepository.get("/schemas")).thenReturn("master_slave_db");
        configCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(configurationRepository).persist(eq("/schemas"), eq("master_slave_db,sharding_db"));
    }
    
    @Test
    public void assertRenewDataSourceEvent() {
        DataSourcePersistEvent event = new DataSourcePersistEvent("sharding_db", createDataSourceConfigurations());
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/datasource"), anyString());
    }
    
    @Test
    public void assertRenewRuleEvent() {
        RulePersistEvent event = new RulePersistEvent("sharding_db", createRuleConfigurations());
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/rule"), anyString());
    }
    
    @Test
    public void assertRenewSchemaNameEventWithDrop() {
        SchemaNamePersistEvent event = new SchemaNamePersistEvent("sharding_db", true);
        when(configurationRepository.get("/schemas")).thenReturn("sharding_db,master_slave_db");
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas"), eq("master_slave_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAdd() {
        SchemaNamePersistEvent event = new SchemaNamePersistEvent("sharding_db", false);
        when(configurationRepository.get("/schemas")).thenReturn("master_slave_db");
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas"), eq("master_slave_db,sharding_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAddAndExist() {
        SchemaNamePersistEvent event = new SchemaNamePersistEvent("sharding_db", false);
        when(configurationRepository.get("/schemas")).thenReturn("sharding_db,master_slave_db");
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas"), eq("sharding_db,master_slave_db"));
    }
    
    @Test
    public void assertPersistMetaData() {
        RuleSchemaMetaData ruleSchemaMetaData = new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlRuleSchemaMetaData.class));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.persistMetaData("sharding_db", ruleSchemaMetaData);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/table"), anyString());
    }
    
    @Test
    public void assertLoadMetaData() {
        when(configurationRepository.get("/schemas/sharding_db/table")).thenReturn(readYAML(META_DATA_YAML));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        Optional<RuleSchemaMetaData> optionalRuleSchemaMetaData = configCenter.loadMetaData("sharding_db");
        assertTrue(optionalRuleSchemaMetaData.isPresent());
        Optional<RuleSchemaMetaData> empty = configCenter.loadMetaData("test");
        assertThat(empty, is(Optional.empty()));
        RuleSchemaMetaData ruleSchemaMetaData = optionalRuleSchemaMetaData.get();
        verify(configurationRepository).get(eq("/schemas/sharding_db/table"));
        assertNotNull(ruleSchemaMetaData);
        assertNotNull(ruleSchemaMetaData.getConfiguredSchemaMetaData());
        assertNotNull(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap());
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().getAllColumnNames("t_order").size(), is(1));
        assertThat(ruleSchemaMetaData.getConfiguredSchemaMetaData().get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().keySet(), is(Collections.singleton("ds_0")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").getAllTableNames(), is(Collections.singleton("t_user")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").get("t_user").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(ruleSchemaMetaData.getUnconfiguredSchemaMetaDataMap().get("ds_0").get("t_user").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertRenewMetaDataPersistEvent() {
        MetaDataPersistEvent event = new MetaDataPersistEvent("sharding_db", 
                new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlRuleSchemaMetaData.class)));
        ConfigCenter configCenter = new ConfigCenter(configurationRepository);
        configCenter.renew(event);
        verify(configurationRepository).persist(eq("/schemas/sharding_db/table"), anyString());
    }
}
