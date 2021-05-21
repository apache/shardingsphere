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
import org.apache.shardingsphere.governance.core.registry.service.config.impl.DataSourceRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.GlobalRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.PropertiesRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.SchemaRuleRegistryService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class RegistryCenterTest {
    
    private static final String SHARDING_RULE_YAML = "yaml/regcenter/data-schema-rule.yaml";
    
    private static final String GLOBAL_RULE_YAML = "yaml/regcenter/data-global-rule.yaml";
    
    private static final String META_DATA_YAML = "yaml/schema.yaml";
    
    @Mock
    private RegistryCenterRepository registryCenterRepository;
    
    @Mock
    private DataSourceRegistryService dataSourceService;
    
    @Mock
    private SchemaRuleRegistryService schemaRuleService;
    
    @Mock
    private GlobalRuleRegistryService globalRuleService;
    
    @Mock
    private PropertiesRegistryService propsService;
    
    @Mock
    private RegistryCacheManager registryCacheManager;
    
    private RegistryCenter registryCenter;
    
    @Before
    public void setUp() throws ReflectiveOperationException {
        registryCenter = new RegistryCenter(registryCenterRepository);
        setField("repository", registryCenterRepository);
        setField("dataSourceService", dataSourceService);
        setField("schemaRuleService", schemaRuleService);
        setField("globalRuleService", globalRuleService);
        setField("propsService", propsService);
    }
    
    private void setField(final String name, final Object value) throws ReflectiveOperationException {
        Field field = registryCenter.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(registryCenter, value);
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
    public void assertPersistConfigurations() {
        Map<String, DataSourceConfiguration> dataSourceConfigs = createDataSourceConfigurations();
        Collection<RuleConfiguration> schemaRuleConfigs = createRuleConfigurations();
        Collection<RuleConfiguration> globalRuleConfigs = createGlobalRuleConfigurations();
        Properties props = createProperties();
        registryCenter.persistConfigurations(
                Collections.singletonMap("sharding_db", dataSourceConfigs), Collections.singletonMap("sharding_db", schemaRuleConfigs), globalRuleConfigs, props, false);
        verify(dataSourceService).persist("sharding_db", dataSourceConfigs, false);
        verify(schemaRuleService).persist("sharding_db", schemaRuleConfigs, false);
        verify(globalRuleService).persist(globalRuleConfigs, false);
        verify(propsService).persist(props, false);
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
    public void assertRenewDataSourceEvent() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertRenewDataSourceEventHasDataSourceConfig() {
        DataSourceAddedEvent event = new DataSourceAddedEvent("sharding_db", createDataSourceConfigurations());
        registryCenter.renew(event);
        verify(registryCenterRepository).persist(startsWith("/metadata/sharding_db/dataSources"), anyString());
    }
    
    @Test
    public void assertRenewRuleEvent() {
        RuleConfigurationsAlteredEvent event = new RuleConfigurationsAlteredEvent("sharding_db", createRuleConfigurations());
        registryCenter.renew(event);
        verify(schemaRuleService).persist(event.getSchemaName(), event.getRuleConfigurations());
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
        // TODO finish verify
    }
    
    @Test
    public void assertRenewDataSourceAlteredEvent() {
        DataSourceAlteredEvent event = new DataSourceAlteredEvent("sharding_db", createDataSourceConfigurations());
        registryCenter.renew(event);
        verify(dataSourceService).persist(event.getSchemaName(), event.getDataSourceConfigurations());
    }
}
