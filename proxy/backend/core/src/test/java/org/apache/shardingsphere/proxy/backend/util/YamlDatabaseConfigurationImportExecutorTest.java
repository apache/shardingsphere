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

package org.apache.shardingsphere.proxy.backend.util;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationCheckEngine;
import org.apache.shardingsphere.infra.config.rule.checker.DatabaseRuleConfigurationChecker;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.external.sql.ShardingSphereSQLException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnitNodeMapCreator;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.database.DatabaseRuleBuilder;
import org.apache.shardingsphere.infra.rule.scope.DatabaseRule;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDataSourceConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.YamlProxyDatabaseConfiguration;
import org.apache.shardingsphere.proxy.backend.config.yaml.swapper.YamlProxyDataSourceConfigurationSwapper;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({
        DataSourcePoolPropertiesCreator.class,
        DataSourcePoolCreator.class,
        StorageUnitNodeMapCreator.class,
        DatabaseTypeEngine.class,
        OrderedSPILoader.class,
        DatabaseRuleConfigurationCheckEngine.class
})
class YamlDatabaseConfigurationImportExecutorTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private YamlProxyDataSourceConfigurationSwapper dataSourceConfigSwapper;
    
    @Mock
    private DistSQLDataSourcePoolPropertiesValidator validateHandler;
    
    private YamlDatabaseConfigurationImportExecutor executor;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        executor = new YamlDatabaseConfigurationImportExecutor(contextManager);
        Plugins.getMemberAccessor().set(YamlDatabaseConfigurationImportExecutor.class.getDeclaredField("dataSourceConfigSwapper"), executor, dataSourceConfigSwapper);
        Plugins.getMemberAccessor().set(YamlDatabaseConfigurationImportExecutor.class.getDeclaredField("validateHandler"), executor, validateHandler);
    }
    
    @Test
    void assertImportDatabaseConfigurationWhenRulesNotProvided() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(1, 1F);
        mockMetaDataContexts(mockDatabase(storageUnits, Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(dataSourceConfigSwapper.swap(any(YamlProxyDataSourceConfiguration.class))).thenReturn(dataSourceConfig);
        DataSourcePoolProperties poolProperties = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(DataSourcePoolPropertiesCreator.create(dataSourceConfig)).thenReturn(poolProperties);
        when(StorageUnitNodeMapCreator.create(anyMap())).thenReturn(Collections.singletonMap("foo_ds", mock(StorageNode.class)));
        when(DataSourcePoolCreator.create(poolProperties)).thenReturn(mock(DataSource.class));
        when(DatabaseTypeEngine.getProtocolType(anyMap(), any(ConfigurationProperties.class))).thenReturn(mock(DatabaseType.class));
        try (MockedConstruction<StorageUnit> mockedConstruction = mockConstruction(StorageUnit.class, (mock, context) -> {
        })) {
            YamlProxyDatabaseConfiguration yamlConfig = createYamlConfiguration();
            yamlConfig.setRules(null);
            executor.importDatabaseConfiguration(yamlConfig);
            assertThat(storageUnits.get("foo_ds"), is(mockedConstruction.constructed().get(0)));
        }
        verify(metaDataManagerService).createDatabase("foo_db");
        verify(metaDataManagerService).registerStorageUnits(eq("foo_db"), anyMap());
    }
    
    @Test
    void assertImportDatabaseConfigurationWhenRulesEmpty() {
        mockMetaDataContexts(mockDatabase(new HashMap<>(1, 1F), Collections.emptyList()), new ConfigurationProperties(new Properties()));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        DatabaseRulePersistService databaseRulePersistService = mock(DatabaseRulePersistService.class);
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseRuleService()).thenReturn(databaseRulePersistService);
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(dataSourceConfigSwapper.swap(any(YamlProxyDataSourceConfiguration.class))).thenReturn(dataSourceConfig);
        DataSourcePoolProperties poolProperties = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(DataSourcePoolPropertiesCreator.create(dataSourceConfig)).thenReturn(poolProperties);
        when(StorageUnitNodeMapCreator.create(anyMap())).thenReturn(Collections.singletonMap("foo_ds", mock(StorageNode.class)));
        when(DataSourcePoolCreator.create(poolProperties)).thenReturn(mock(DataSource.class));
        when(DatabaseTypeEngine.getProtocolType(anyMap(), any(ConfigurationProperties.class))).thenReturn(mock(DatabaseType.class));
        try (MockedConstruction<StorageUnit> ignored = mockConstruction(StorageUnit.class)) {
            executor.importDatabaseConfiguration(createYamlConfiguration());
        }
        verify(metaDataManagerService).registerStorageUnits(eq("foo_db"), anyMap());
        verify(databaseRulePersistService, never()).persist(anyString(), anyCollection());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void assertImportDatabaseConfigurationWhenRulesProvided() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(1, 1F);
        LinkedList<ShardingSphereRule> rules = new LinkedList<>();
        mockMetaDataContexts(mockDatabase(storageUnits, rules), new ConfigurationProperties(new Properties()));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        DatabaseRulePersistService databaseRulePersistService = mock(DatabaseRulePersistService.class);
        when(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseRuleService()).thenReturn(databaseRulePersistService);
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(dataSourceConfigSwapper.swap(any(YamlProxyDataSourceConfiguration.class))).thenReturn(dataSourceConfig);
        DataSourcePoolProperties poolProperties = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(DataSourcePoolPropertiesCreator.create(dataSourceConfig)).thenReturn(poolProperties);
        when(StorageUnitNodeMapCreator.create(anyMap())).thenReturn(Collections.singletonMap("foo_ds", mock(StorageNode.class)));
        when(DataSourcePoolCreator.create(poolProperties)).thenReturn(mock(DataSource.class));
        when(DatabaseTypeEngine.getProtocolType(anyMap(), any(ConfigurationProperties.class))).thenReturn(mock(DatabaseType.class));
        YamlRuleConfiguration yamlRuleConfig = mock(YamlRuleConfiguration.class);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(yamlRuleConfig.getRuleConfigurationType()).thenReturn((Class) ruleConfig.getClass());
        YamlRuleConfigurationSwapper swapper = mock(YamlRuleConfigurationSwapper.class);
        when(swapper.swapToObject(yamlRuleConfig)).thenReturn(ruleConfig);
        when(swapper.getOrder()).thenReturn(1);
        when(OrderedSPILoader.getServicesByClass(eq(YamlRuleConfigurationSwapper.class), anyCollection())).thenReturn(Collections.singletonMap(ruleConfig.getClass(), swapper));
        when(OrderedSPILoader.getServicesByClass(eq(DatabaseRuleConfigurationChecker.class), anyCollection())).thenReturn(Collections.singletonMap(ruleConfig.getClass(), null));
        DatabaseRuleBuilder builder = mock(DatabaseRuleBuilder.class);
        DatabaseRule expectedRule = mock(DatabaseRule.class);
        when(builder.build(eq(ruleConfig), anyString(), any(DatabaseType.class), any(ResourceMetaData.class), anyCollection(), any())).thenReturn(expectedRule);
        when(OrderedSPILoader.getServices(eq(DatabaseRuleBuilder.class), eq(Collections.singleton(ruleConfig)))).thenReturn(Collections.singletonMap(ruleConfig, builder));
        YamlProxyDatabaseConfiguration yamlConfig = createYamlConfiguration();
        yamlConfig.setRules(Collections.singletonList(yamlRuleConfig));
        try (MockedConstruction<StorageUnit> mockedConstruction = mockConstruction(StorageUnit.class, (mock, context) -> {
        })) {
            executor.importDatabaseConfiguration(yamlConfig);
            assertThat(storageUnits.get("foo_ds"), is(mockedConstruction.constructed().get(0)));
        }
        assertThat(rules, is(Collections.singletonList(expectedRule)));
        ArgumentCaptor<Collection<RuleConfiguration>> ruleConfigsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(databaseRulePersistService).persist(eq("foo_db"), ruleConfigsCaptor.capture());
        assertThat(ruleConfigsCaptor.getValue(), is(Collections.singletonList(ruleConfig)));
    }
    
    @Test
    void assertImportDatabaseConfigurationWhenImportDataSourcesFailed() {
        ShardingSphereDatabase database = mockDatabase(Collections.emptyMap(), Collections.emptyList());
        mockMetaDataContexts(database, new ConfigurationProperties(new Properties()));
        MetaDataManagerPersistService metaDataManagerService = mock(MetaDataManagerPersistService.class);
        when(contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService()).thenReturn(metaDataManagerService);
        DataSourceConfiguration dataSourceConfig = mock(DataSourceConfiguration.class);
        when(dataSourceConfigSwapper.swap(any(YamlProxyDataSourceConfiguration.class))).thenReturn(dataSourceConfig);
        when(DataSourcePoolPropertiesCreator.create(dataSourceConfig)).thenReturn(mock(DataSourcePoolProperties.class));
        ShardingSphereSQLException sqlException = mock(ShardingSphereSQLException.class);
        doThrow(sqlException).when(validateHandler).validate(anyMap());
        when(DatabaseTypeEngine.getProtocolType(anyMap(), any(ConfigurationProperties.class))).thenReturn(mock(DatabaseType.class));
        assertThrows(ShardingSphereSQLException.class, () -> executor.importDatabaseConfiguration(createYamlConfiguration()));
        verify(metaDataManagerService).createDatabase("foo_db");
        verify(metaDataManagerService).dropDatabase(database);
    }
    
    private YamlProxyDatabaseConfiguration createYamlConfiguration() {
        YamlProxyDatabaseConfiguration result = new YamlProxyDatabaseConfiguration();
        result.setDatabaseName("foo_db");
        YamlProxyDataSourceConfiguration dataSourceConfig = new YamlProxyDataSourceConfiguration();
        dataSourceConfig.setUrl("jdbc:mock://localhost/" + "foo_db");
        result.setDataSources(Collections.singletonMap("foo_ds", dataSourceConfig));
        return result;
    }
    
    private ShardingSphereDatabase mockDatabase(final Map<String, StorageUnit> storageUnits, final List<ShardingSphereRule> rules) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        lenient().when(result.getName()).thenReturn("foo_db");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        when(result.getRuleMetaData().getRules()).thenReturn(rules);
        return result;
    }
    
    private void mockMetaDataContexts(final ShardingSphereDatabase database, final ConfigurationProperties props) {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getProps()).thenReturn(props);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
    }
}
