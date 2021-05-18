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

package org.apache.shardingsphere.governance.core.registry;

import lombok.SneakyThrows;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAddedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAlteredEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataCreatedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDroppedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.schema.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RegistryCenterTest {
    
    private static final String DATA_SOURCE_YAM = "yaml/registryCenter/data-source.yaml";
    
    private static final String SHARDING_RULE_YAML = "yaml/registryCenter/data-sharding-rule.yaml";
    
    private static final String SHARDING_AND_ENCRYPT_RULE_YAML = "yaml/registryCenter/data-sharding-encrypt-rule.yaml";

    private static final String READWRITE_SPLITTING_RULE_YAML = "yaml/registryCenter/data-readwrite-splitting-rule.yaml";
    
    private static final String DB_DISCOVERY_RULE_YAML = "yaml/registryCenter/data-database-discovery-rule.yaml";
    
    private static final String ENCRYPT_RULE_YAML = "yaml/registryCenter/data-encrypt-rule.yaml";
    
    private static final String SHADOW_RULE_YAML = "yaml/registryCenter/data-shadow-rule.yaml";
    
    private static final String USERS_YAML = "yaml/registryCenter/data-users.yaml";
    
    private static final String GLOBAL_RULE_YAML = "yaml/registryCenter/data-global-rule.yaml";
    
    private static final String PROPS_YAML = ConfigurationPropertyKey.SQL_SHOW.getKey() + ": false\n";
    
    private static final String DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL = "yaml/registryCenter/data-source-init-sql.yaml";
    
    private static final String META_DATA_YAML = "yaml/schema.yaml";
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Mock
    private RegistryCacheManager registryCacheManager;
    
    private RegistryCenter registryCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        registryCenter = new RegistryCenter(registryCenterRepository);
        Field field = registryCenter.getClass().getDeclaredField("repository");
        field.setAccessible(true);
        field.set(registryCenter, registryCenterRepository);
    }
    
    @Test
    public void assertPersistInstanceOnline() {
        registryCenter.persistInstanceOnline();
        verify(registryCenterRepository).persistEphemeral(anyString(), anyString());
    }
    
    @Test
    public void assertPersistDataSourcesNode() {
        registryCenter.persistDataNodes();
        verify(registryCenterRepository).persist("/states/datanodes", "");
    }
    
    @Test
    public void assertPersistInstanceData() {
        registryCenter.persistInstanceData("test");
        verify(registryCenterRepository).persist(anyString(), eq("test"));
    }
    
    @Test
    public void assertLoadInstanceData() {
        registryCenter.loadInstanceData();
        verify(registryCenterRepository).get(anyString());
    }
    
    @Test
    public void assertLoadDisabledDataSources() {
        List<String> disabledDataSources = Collections.singletonList("replica_ds_0");
        when(registryCenterRepository.getChildrenKeys(anyString())).thenReturn(disabledDataSources);
        registryCenter.loadDisabledDataSources("replica_query_db");
        verify(registryCenterRepository).getChildrenKeys(anyString());
        verify(registryCenterRepository).get(anyString());
    }
    
    @Test
    public void assertTryLock() {
        registryCenter.tryLock("test", 50L);
        verify(registryCenterRepository).tryLock(eq(new LockNode().getLockNodePath("test")), eq(50L), eq(TimeUnit.MILLISECONDS));
    }
    
    @Test
    public void assertReleaseLock() {
        registryCenter.releaseLock("test");
        verify(registryCenterRepository).releaseLock(eq(new LockNode().getLockNodePath("test")));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertMoreSchema() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository, times(0)).persist("/metadata", "myTest1,myTest2,sharding_db");
    }
    
    @Test
    public void assertMoreAndContainsSchema() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository, times(0)).persist("/metadata", "myTest1,sharding_db");
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingWithoutAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(READWRITE_SPLITTING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForDatabaseDiscoveryRuleWithoutAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createDatabaseDiscoveryRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(DB_DISCOVERY_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingRuleWithAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(READWRITE_SPLITTING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForDatabaseDiscoveryRuleWithAuthenticationAndIsOverwrite() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createDatabaseDiscoveryRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(DB_DISCOVERY_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(ENCRYPT_RULE_YAML));
    }
    
    @Test
    public void assertNullRuleConfiguration() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), Collections.emptyList(), true);
    }
    
    @Test
    public void assertPersistConfigurationForShadow() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHADOW_RULE_YAML));
    }
    
    @Test
    public void assertPersistGlobalConfiguration() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistGlobalConfiguration(createGlobalRuleConfigurations(), createProperties(), true);
        verify(registryCenterRepository).persist(eq("/rules"), any());
        verify(registryCenterRepository).persist("/props", PROPS_YAML);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
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
        MockDataSource result = new MockDataSource();
        result.setDriverClassName("com.mysql.jdbc.Driver");
        result.setUrl("jdbc:mysql://localhost:3306/" + name);
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createRuleConfigurations() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(SHARDING_RULE_YAML), Collection.class));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createReadwriteSplittingRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(READWRITE_SPLITTING_RULE_YAML), Collection.class));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createDatabaseDiscoveryRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(DB_DISCOVERY_RULE_YAML), Collection.class));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createEncryptRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(ENCRYPT_RULE_YAML), Collection.class));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createShadowRuleConfiguration() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(SHADOW_RULE_YAML), Collection.class));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> createGlobalRuleConfigurations() {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(readYAML(GLOBAL_RULE_YAML), Collection.class));
    }
    
    private Properties createProperties() {
        Properties result = new Properties();
        result.put(ConfigurationPropertyKey.SQL_SHOW.getKey(), Boolean.FALSE);
        return result;
    }
    
    @Test
    public void assertLoadDataSourceConfigurations() {
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn(readYAML(DATA_SOURCE_YAM));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Map<String, DataSourceConfiguration> actual = registryCenter.loadDataSourceConfigurations("sharding_db");
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
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn("");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Map<String, DataSourceConfiguration> actual = registryCenter.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(0));
    }
    
    @Test
    public void assertLoadShardingAndEncryptRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHARDING_AND_ENCRYPT_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<RuleConfiguration> ruleConfigurations = registryCenter.loadRuleConfigurations("sharding_db");
        assertThat(ruleConfigurations.size(), is(2));
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration shardingRuleConfig = (ShardingRuleConfiguration) each;
                assertThat(shardingRuleConfig.getTables().size(), is(1));
                assertThat(shardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration encryptRuleConfig = (EncryptRuleConfiguration) each;
                assertThat(encryptRuleConfig.getEncryptors().size(), is(2));
                ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = encryptRuleConfig.getEncryptors().get("aes_encryptor");
                assertThat(encryptAlgorithmConfig.getType(), is("AES"));
                assertThat(encryptAlgorithmConfig.getProps().get("aes-key-value").toString(), is("123456abcd"));
            }
        }
    }
    
    @Test
    public void assertLoadShardingRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHARDING_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<RuleConfiguration> actual = registryCenter.loadRuleConfigurations("sharding_db");
        assertThat(actual.size(), is(1));
        ShardingRuleConfiguration actualShardingRuleConfig = (ShardingRuleConfiguration) actual.iterator().next();
        assertThat(actualShardingRuleConfig.getTables().size(), is(1));
        assertThat(actualShardingRuleConfig.getTables().iterator().next().getLogicTable(), is("t_order"));
    }
    
    @Test
    public void assertLoadReadwriteSplittingRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(READWRITE_SPLITTING_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<RuleConfiguration> actual = registryCenter.loadRuleConfigurations("sharding_db");
        ReadwriteSplittingRuleConfiguration config = (ReadwriteSplittingRuleConfiguration) actual.iterator().next();
        assertThat(config.getDataSources().size(), is(1));
        assertThat(config.getDataSources().iterator().next().getWriteDataSourceName(), is("write_ds"));
        assertThat(config.getDataSources().iterator().next().getReadDataSourceNames().size(), is(2));
    }
    
    @Test
    public void assertLoadDatabaseDiscoveryRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(DB_DISCOVERY_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<RuleConfiguration> actual = registryCenter.loadRuleConfigurations("sharding_db");
        DatabaseDiscoveryRuleConfiguration config = (DatabaseDiscoveryRuleConfiguration) actual.iterator().next();
        assertThat(config.getDataSources().size(), is(1));
        assertThat(config.getDataSources().iterator().next().getDataSourceNames().size(), is(3));
    }
    
    @Test
    public void assertLoadEncryptRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(ENCRYPT_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        EncryptRuleConfiguration actual = (EncryptRuleConfiguration) registryCenter.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getEncryptors().size(), is(1));
        ShardingSphereAlgorithmConfiguration encryptAlgorithmConfig = actual.getEncryptors().get("order_encryptor");
        assertThat(encryptAlgorithmConfig.getType(), is("AES"));
        assertThat(encryptAlgorithmConfig.getProps().get("aes-key-value").toString(), is("123456"));
    }
    
    @Test
    public void assertLoadShadowRuleConfiguration() {
        when(registryCenterRepository.get("/metadata/sharding_db/rules")).thenReturn(readYAML(SHADOW_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        ShadowRuleConfiguration actual = (ShadowRuleConfiguration) registryCenter.loadRuleConfigurations("sharding_db").iterator().next();
        assertThat(actual.getSourceDataSourceNames(), is(Arrays.asList("ds", "ds1")));
        assertThat(actual.getShadowDataSourceNames(), is(Arrays.asList("shadow_ds", "shadow_ds1")));
        assertThat(actual.getColumn(), is("shadow"));
    }
    
    @Test
    public void assertLoadProperties() {
        when(registryCenterRepository.get("/props")).thenReturn(PROPS_YAML);
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Properties actual = registryCenter.loadProperties();
        assertThat(actual.get(ConfigurationPropertyKey.SQL_SHOW.getKey()), is(Boolean.FALSE));
    }
    
    @Test
    public void assertLoadGlobalRuleConfigurations() {
        when(registryCenterRepository.get("/rules")).thenReturn(readYAML(GLOBAL_RULE_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<RuleConfiguration> globalRuleConfigs = registryCenter.loadGlobalRuleConfigurations();
        assertFalse(globalRuleConfigs.isEmpty());
        Collection<ShardingSphereUser> users = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .flatMap(each -> ((AuthorityRuleConfiguration) each).getUsers().stream()).collect(Collectors.toList());
        Optional<ShardingSphereUser> user = users.stream().filter(each -> each.getGrantee().equals(new Grantee("root", ""))).findFirst();
        assertTrue(user.isPresent());
        assertThat(user.get().getPassword(), is("root"));
        Collection<ShardingSphereAlgorithmConfiguration> providers = globalRuleConfigs.stream()
                .filter(each -> each instanceof AuthorityRuleConfiguration && Objects.nonNull(((AuthorityRuleConfiguration) each).getProvider()))
                .map(each -> ((AuthorityRuleConfiguration) each).getProvider()).collect(Collectors.toList());
        assertFalse(providers.isEmpty());
        Optional<ShardingSphereAlgorithmConfiguration> nativeProvider = providers.stream().filter(each -> "NATIVE".equals(each.getType())).findFirst();
        assertTrue(nativeProvider.isPresent());
    }
    
    @Test
    public void assertLoadAllSchemaNames() {
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Collection<String> actual = registryCenter.loadAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("replica_query_db"));
    }
    
    @Test
    public void assertLoadDataSourceConfigurationsWithConnectionInitSQLs() {
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn(readYAML(DATA_SOURCE_YAML_WITH_CONNECTION_INIT_SQL));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Map<String, DataSourceConfiguration> actual = registryCenter.loadDataSourceConfigurations("sharding_db");
        assertThat(actual.size(), is(2));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_0"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_0")));
        assertDataSourceConfigurationWithConnectionInitSqls(actual.get("ds_1"), createDataSourceConfiguration(createDataSourceWithConnectionInitSqls("ds_1")));
    }
    
    private DataSource createDataSourceWithConnectionInitSqls(final String name) {
        MockDataSource result = new MockDataSource();
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
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    @Test
    public void assertPersistSchemaNameWithExistSchema() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db");
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("sharding_db"));
    }
    
    @Test
    public void assertPersistSchemaNameWithExistAndNewSchema() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db,sharding_db"));
    }
    
    @Test
    public void assertRenewDataSourceEvent() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertRenewDataSourceEventHasDataSourceConfig() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        String dataSourceYaml = "ds_0:\n"
            + "  dataSourceClassName: xxx\n"
            + "  url: jdbc:mysql://127.0.0.1:3306/demo_ds_0?serverTimezone=UTC&useSSL=false\n"
            + "  username: root\n"
            + "  password: root\n"
            + "  connectionTimeoutMilliseconds: 30000\n"
            + "  idleTimeoutMilliseconds: 60000\n"
            + "  maxLifetimeMilliseconds: 1800000\n"
            + "  maxPoolSize: 50\n"
            + "  minPoolSize: 1\n"
            + "  maintenanceIntervalMilliseconds: 30000\n";
        when(registryCenterRepository.get("/metadata/sharding_db/dataSources")).thenReturn(dataSourceYaml);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertRenewRuleEvent() {
        RuleConfigurationsAlteredEvent event = new RuleConfigurationsAlteredEvent("sharding_db", createRuleConfigurations());
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/rules"), anyString());
    }
    
    @Test
    public void assertRenewSchemaNameEventWithDrop() {
        MetaDataDroppedEvent event = new MetaDataDroppedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithDropAndNotExist() {
        MetaDataDroppedEvent event = new MetaDataDroppedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("replica_query_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAdd() {
        MetaDataCreatedEvent event = new MetaDataCreatedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db,sharding_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAddAndExist() {
        MetaDataCreatedEvent event = new MetaDataCreatedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("sharding_db,replica_query_db"));
    }
    
    @Test
    public void assertPersistSchema() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlSchema.class));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.persistSchema("sharding_db", schema);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/schema"), anyString());
    }
    
    @Test
    public void assertLoadSchema() {
        when(registryCenterRepository.get("/metadata/sharding_db/schema")).thenReturn(readYAML(META_DATA_YAML));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Optional<ShardingSphereSchema> schemaOptional = registryCenter.loadSchema("sharding_db");
        assertTrue(schemaOptional.isPresent());
        Optional<ShardingSphereSchema> empty = registryCenter.loadSchema("test");
        assertThat(empty, is(Optional.empty()));
        ShardingSphereSchema schema = schemaOptional.get();
        verify(registryCenterRepository).get(eq("/metadata/sharding_db/schema"));
        assertThat(schema.getAllTableNames(), is(Collections.singleton("t_order")));
        assertThat(schema.get("t_order").getIndexes().keySet(), is(Collections.singleton("primary")));
        assertThat(schema.getAllColumnNames("t_order").size(), is(1));
        assertThat(schema.get("t_order").getColumns().keySet(), is(Collections.singleton("id")));
    }
    
    @Test
    public void assertRenewSchemaAlteredEvent() {
        SchemaAlteredEvent event = new SchemaAlteredEvent("sharding_db", new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlSchema.class)));
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/schema"), anyString());
    }
    
    @Test
    public void assertDeleteSchema() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.deleteSchema("sharding_db");
        verify(registryCenterRepository).delete(eq("/metadata/sharding_db"));
    }
    
    @Test
    @SneakyThrows
    public void assertRenewSwitchRuleConfigurationEvent() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        Field field = RegistryCenter.class.getDeclaredField("registryCacheManager");
        field.setAccessible(true);
        field.set(registryCenter, registryCacheManager);
        when(registryCacheManager.loadCache(anyString(), eq("testCacheId"))).thenReturn(readYAML(SHARDING_RULE_YAML));
        SwitchRuleConfigurationEvent event = new SwitchRuleConfigurationEvent("sharding_db", "testCacheId");
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), anyString());
        verify(registryCacheManager).deleteCache(eq("/metadata/sharding_db/rules"), eq("testCacheId"));
    }
    
    @Test
    public void assertRenewDataSourceAlteredEvent() {
        DataSourceAlteredEvent event = new DataSourceAlteredEvent("sharding_db", createDataSourceConfigurations());
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertDeleteLockAck() {
        RegistryCenter registryCenter = new RegistryCenter(registryCenterRepository);
        registryCenter.deleteLockAck("test");
        verify(registryCenterRepository).delete(anyString());
    }
}
