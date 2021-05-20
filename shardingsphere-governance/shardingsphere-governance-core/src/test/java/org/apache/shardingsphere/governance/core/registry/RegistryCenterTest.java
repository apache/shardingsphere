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
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
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
    
    private static final String SHARDING_RULE_YAML = "yaml/registryCenter/data-sharding-rule.yaml";

    private static final String READWRITE_SPLITTING_RULE_YAML = "yaml/registryCenter/data-readwrite-splitting-rule.yaml";
    
    private static final String DB_DISCOVERY_RULE_YAML = "yaml/registryCenter/data-database-discovery-rule.yaml";
    
    private static final String ENCRYPT_RULE_YAML = "yaml/registryCenter/data-encrypt-rule.yaml";
    
    private static final String SHADOW_RULE_YAML = "yaml/registryCenter/data-shadow-rule.yaml";
    
    private static final String GLOBAL_RULE_YAML = "yaml/registryCenter/data-global-rule.yaml";
    
    private static final String PROPS_YAML = ConfigurationPropertyKey.SQL_SHOW.getKey() + ": false\n";
    
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
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertMoreSchema() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository, times(0)).persist("/metadata", "myTest1,myTest2,sharding_db");
    }
    
    @Test
    public void assertMoreAndContainsSchema() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository, times(0)).persist("/metadata", "myTest1,sharding_db");
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithoutAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithoutAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingWithoutAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(READWRITE_SPLITTING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForDatabaseDiscoveryRuleWithoutAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createDatabaseDiscoveryRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(DB_DISCOVERY_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForShardingRuleWithAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHARDING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForReplicaQueryRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingRuleWithAuthenticationAndIsNotOverwriteAndConfigurationIsNotExisted() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), false);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/rules"), any());
    }
    
    @Test
    public void assertPersistConfigurationForReadwriteSplittingRuleWithAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createReadwriteSplittingRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(READWRITE_SPLITTING_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForDatabaseDiscoveryRuleWithAuthenticationAndIsOverwrite() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createDatabaseDiscoveryRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(DB_DISCOVERY_RULE_YAML));
    }
    
    @Test
    public void assertPersistConfigurationForEncrypt() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createEncryptRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(ENCRYPT_RULE_YAML));
    }
    
    @Test
    public void assertNullRuleConfiguration() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), Collections.emptyList(), true);
    }
    
    @Test
    public void assertPersistConfigurationForShadow() {
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createShadowRuleConfiguration(), true);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/dataSources"), any());
        verify(registryCenterRepository, times(0)).persist("/metadata/sharding_db/rules", readYAML(SHADOW_RULE_YAML));
    }
    
    @Test
    public void assertPersistGlobalConfiguration() {
        registryCenter.persistGlobalConfiguration(createGlobalRuleConfigurations(), createProperties(), true);
        verify(registryCenterRepository).persist(eq("/rules"), any());
        verify(registryCenterRepository).persist("/props", PROPS_YAML);
    }
    
    private Map<String, DataSourceConfiguration> createDataSourceConfigurations() {
        return createDataSourceMap().entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> 
                DataSourceConfiguration.getDataSourceConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
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
    public void assertLoadAllSchemaNames() {
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        Collection<String> actual = registryCenter.loadAllSchemaNames();
        assertThat(actual.size(), is(2));
        assertThat(actual, hasItems("sharding_db"));
        assertThat(actual, hasItems("replica_query_db"));
    }
    
    @SneakyThrows({IOException.class, URISyntaxException.class})
    private String readYAML(final String yamlFile) {
        return Files.readAllLines(Paths.get(ClassLoader.getSystemResource(yamlFile).toURI()))
                .stream().filter(each -> !each.startsWith("#")).map(each -> each + System.lineSeparator()).collect(Collectors.joining());
    }
    
    @Test
    public void assertPersistSchemaNameWithExistSchema() {
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db");
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("sharding_db"));
    }
    
    @Test
    public void assertPersistSchemaNameWithExistAndNewSchema() {
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        registryCenter.persistConfigurations("sharding_db", createDataSourceConfigurations(), createRuleConfigurations(), true);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db,sharding_db"));
    }
    
    @Test
    public void assertRenewDataSourceEvent() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertRenewDataSourceEventHasDataSourceConfig() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
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
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/rules"), anyString());
    }
    
    @Test
    public void assertRenewSchemaNameEventWithDrop() {
        MetaDataDroppedEvent event = new MetaDataDroppedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithDropAndNotExist() {
        MetaDataDroppedEvent event = new MetaDataDroppedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        registryCenter.renew(event);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("replica_query_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAdd() {
        MetaDataCreatedEvent event = new MetaDataCreatedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("replica_query_db");
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata"), eq("replica_query_db,sharding_db"));
    }
    
    @Test
    public void assertRenewSchemaNameEventWithAddAndExist() {
        MetaDataCreatedEvent event = new MetaDataCreatedEvent("sharding_db");
        when(registryCenterRepository.get("/metadata")).thenReturn("sharding_db,replica_query_db");
        registryCenter.renew(event);
        verify(registryCenterRepository, times(0)).persist(eq("/metadata"), eq("sharding_db,replica_query_db"));
    }
    
    @Test
    public void assertPersistSchema() {
        ShardingSphereSchema schema = new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(readYAML(META_DATA_YAML), YamlSchema.class));
        registryCenter.persistSchema("sharding_db", schema);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/schema"), anyString());
    }
    
    @Test
    public void assertLoadSchema() {
        when(registryCenterRepository.get("/metadata/sharding_db/schema")).thenReturn(readYAML(META_DATA_YAML));
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
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(eq("/metadata/sharding_db/schema"), anyString());
    }
    
    @Test
    public void assertDeleteSchema() {
        registryCenter.deleteSchema("sharding_db");
        verify(registryCenterRepository).delete(eq("/metadata/sharding_db"));
    }
    
    @Test
    @SneakyThrows
    public void assertRenewSwitchRuleConfigurationEvent() {
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
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
}
