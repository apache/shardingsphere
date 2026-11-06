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

package org.apache.shardingsphere.mode.manager.cluster.persist.service;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.cluster.exception.ReloadMetaDataContextFailedException;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerCoordinatorType;
import org.apache.shardingsphere.mode.manager.cluster.persist.coordinator.database.ClusterDatabaseListenerPersistCoordinator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DatabaseRulePersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.DatabaseMetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.retry.RetryExecutor;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(TableRefreshUtils.class)
class ClusterMetaDataManagerPersistServiceTest {
    
    private ClusterMetaDataManagerPersistService metaDataManagerPersistService;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataPersistFacade metaDataPersistFacade;
    
    @Mock
    private ClusterDatabaseListenerPersistCoordinator clusterDatabaseListenerPersistCoordinator;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContextManager metaDataContextManager;
    
    @SneakyThrows(ReflectiveOperationException.class)
    @BeforeEach
    void setUp() {
        metaDataManagerPersistService = new ClusterMetaDataManagerPersistService(metaDataContextManager, mock(ClusterPersistRepository.class));
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("metaDataPersistFacade"), metaDataManagerPersistService, metaDataPersistFacade);
        Plugins.getMemberAccessor().set(ClusterMetaDataManagerPersistService.class.getDeclaredField("clusterDatabaseListenerPersistCoordinator"),
                metaDataManagerPersistService, clusterDatabaseListenerPersistCoordinator);
    }
    
    @Test
    void assertCreateDatabase() {
        metaDataManagerPersistService.createDatabase("foo_db");
        DatabaseMetaDataPersistService databaseMetaDataPersistService = metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase();
        InOrder actual = inOrder(databaseMetaDataPersistService, clusterDatabaseListenerPersistCoordinator);
        actual.verify(databaseMetaDataPersistService).add("foo_db");
        actual.verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.CREATE);
        actual.verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertCreateDatabaseWithExceptionStillDeletesCoordinator() {
        DatabaseMetaDataPersistFacade databaseMetaDataPersistFacade = metaDataPersistFacade.getDatabaseMetaDataFacade();
        doThrow(new IllegalStateException("mocked")).when(databaseMetaDataPersistFacade).persistCreatedDatabaseSchemas(any());
        assertThrows(IllegalStateException.class, () -> metaDataManagerPersistService.createDatabase("foo_db"));
        verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.CREATE);
        verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertDropDatabase() {
        metaDataManagerPersistService.dropDatabase(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())));
        DatabaseMetaDataPersistService databaseMetaDataPersistService = metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase();
        InOrder actual = inOrder(databaseMetaDataPersistService, clusterDatabaseListenerPersistCoordinator);
        actual.verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.DROP);
        actual.verify(databaseMetaDataPersistService).drop("foo_db");
        actual.verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertDropDatabaseWithExceptionStillDeletesCoordinator() {
        DatabaseMetaDataPersistService databaseMetaDataPersistService = metaDataPersistFacade.getDatabaseMetaDataFacade().getDatabase();
        doThrow(new IllegalStateException("mocked")).when(databaseMetaDataPersistService).drop("foo_db");
        assertThrows(IllegalStateException.class, () -> metaDataManagerPersistService
                .dropDatabase(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties()))));
        verify(clusterDatabaseListenerPersistCoordinator).persist("foo_db", ClusterDatabaseListenerCoordinatorType.DROP);
        verify(clusterDatabaseListenerPersistCoordinator).delete("foo_db");
    }
    
    @Test
    void assertCreateSchema() {
        metaDataManagerPersistService.createSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).add("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenameSchema() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db").getSchema("foo_schema")).thenReturn(schema);
        metaDataManagerPersistService.renameSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), "foo_schema",
                "bar_schema");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).renameSchema(any(), any(), eq("foo_schema"), eq("bar_schema"));
    }
    
    @Test
    void assertDropSchema() {
        metaDataManagerPersistService.dropSchema(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                Collections.singleton("foo_schema"));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertRegisterStorageUnits() {
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        Map<String, StorageUnit> storageUnits = Collections.singletonMap("foo_ds", storageUnit);
        Map<String, DataSourcePoolProperties> propsMap = Collections.singletonMap("foo_ds", mock(DataSourcePoolProperties.class));
        MetaDataContexts metaDataContexts = createRegistrationMetaDataContexts(Collections.emptyMap());
        when(metaDataContextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        DataSourceUnitPersistService dataSourceUnitPersistService = metaDataPersistFacade.getDataSourceUnitService();
        doAnswer(invocation -> {
            metaDataContexts.update(createRegistrationMetaDataContexts(storageUnits));
            return null;
        }).when(dataSourceUnitPersistService).persist("foo_db", propsMap);
        try (
                MockedConstruction<RetryExecutor> ignored = mockConstruction(RetryExecutor.class, (mock, context) -> when(mock.execute(any(), any())).thenAnswer(invocation -> {
                    Predicate<Object> predicate = invocation.getArgument(0);
                    return predicate.test(invocation.getArgument(1));
                }));
                MockedStatic<GenericSchemaBuilder> schemaBuilder = mockStatic(GenericSchemaBuilder.class)) {
            metaDataManagerPersistService.registerStorageUnits("foo_db", propsMap);
            schemaBuilder.verify(() -> GenericSchemaBuilder.build(any(DatabaseType.class), argThat(material -> storageUnits.equals(material.getStorageUnits()))));
            verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabase(eq("foo_db"), argThat(each -> storageUnits.equals(each.getResourceMetaData().getStorageUnits())), any());
        }
    }
    
    @Test
    void assertRegisterStorageUnitsWithPartialNotification() {
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        Map<String, StorageUnit> partialStorageUnits = Collections.singletonMap("foo_ds_0", storageUnit);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(partialStorageUnits);
        storageUnits.put("foo_ds_1", storageUnit);
        Map<String, DataSourcePoolProperties> propsMap = new LinkedHashMap<>(2, 1F);
        propsMap.put("foo_ds_0", mock(DataSourcePoolProperties.class));
        propsMap.put("foo_ds_1", mock(DataSourcePoolProperties.class));
        MetaDataContexts metaDataContexts = createRegistrationMetaDataContexts(Collections.emptyMap());
        when(metaDataContextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        DataSourceUnitPersistService dataSourceUnitPersistService = metaDataPersistFacade.getDataSourceUnitService();
        doAnswer(invocation -> {
            metaDataContexts.update(createRegistrationMetaDataContexts(partialStorageUnits));
            return null;
        }).when(dataSourceUnitPersistService).persist("foo_db", propsMap);
        try (
                MockedStatic<GenericSchemaBuilder> schemaBuilder = mockStatic(GenericSchemaBuilder.class);
                MockedConstruction<RetryExecutor> ignored = mockConstruction(RetryExecutor.class, (mock, context) -> when(mock.execute(any(), any())).thenAnswer(invocation -> {
                    Predicate<Object> predicate = invocation.getArgument(0);
                    Object arg = invocation.getArgument(1);
                    if (!(arg instanceof Collection<?>)) {
                        return predicate.test(arg);
                    }
                    assertFalse(predicate.test(arg));
                    schemaBuilder.verifyNoInteractions();
                    verify(metaDataPersistFacade.getDatabaseMetaDataFacade(), never()).persistReloadDatabase(any(), any(), any());
                    metaDataContexts.update(createRegistrationMetaDataContexts(storageUnits));
                    assertTrue(predicate.test(arg));
                    return true;
                }))) {
            metaDataManagerPersistService.registerStorageUnits("foo_db", propsMap);
            schemaBuilder.verify(() -> GenericSchemaBuilder.build(any(DatabaseType.class), argThat(material -> storageUnits.equals(material.getStorageUnits()))));
            verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabase(eq("foo_db"), argThat(each -> storageUnits.equals(each.getResourceMetaData().getStorageUnits())), any());
        }
    }
    
    @Test
    void assertRegisterStorageUnitsWithNotificationTimeout() {
        Map<String, DataSourcePoolProperties> propsMap = Collections.singletonMap("foo_ds", mock(DataSourcePoolProperties.class));
        MetaDataContexts metaDataContexts = createRegistrationMetaDataContexts(Collections.emptyMap());
        when(metaDataContextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        try (
                MockedStatic<GenericSchemaBuilder> schemaBuilder = mockStatic(GenericSchemaBuilder.class);
                MockedConstruction<RetryExecutor> ignored = mockConstruction(RetryExecutor.class, (mock, context) -> when(mock.execute(any(), any())).thenAnswer(invocation -> {
                    Predicate<Object> predicate = invocation.getArgument(0);
                    Object arg = invocation.getArgument(1);
                    if (!(arg instanceof Collection<?>)) {
                        return predicate.test(arg);
                    }
                    assertFalse(predicate.test(arg));
                    return false;
                }))) {
            assertThrows(ReloadMetaDataContextFailedException.class, () -> metaDataManagerPersistService.registerStorageUnits("foo_db", propsMap));
            verify(metaDataPersistFacade.getDataSourceUnitService()).persist("foo_db", propsMap);
            schemaBuilder.verifyNoInteractions();
            verify(metaDataPersistFacade.getDatabaseMetaDataFacade(), never()).persistReloadDatabase(any(), any(), any());
        }
    }
    
    private MetaDataContexts createRegistrationMetaDataContexts(final Map<String, StorageUnit> storageUnits) {
        DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), storageUnits);
        RuleMetaData ruleMetaData = new RuleMetaData(Collections.emptyList());
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", databaseType, resourceMetaData, ruleMetaData, Collections.emptyList(), props);
        return new MetaDataContexts(new ShardingSphereMetaData(Collections.singleton(database), new ResourceMetaData(Collections.emptyMap()), ruleMetaData, props, databaseType),
                new ShardingSphereStatistics());
    }
    
    @Test
    void assertAlterSingleRuleConfiguration() {
        SingleRule singleRule = mock(SingleRule.class);
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        when(singleRule.getConfiguration()).thenReturn(singleRuleConfig);
        when(metaDataPersistFacade.getDatabaseRuleService().persist("foo_db", Collections.singleton(singleRuleConfig))).thenReturn(Collections.emptyList());
        metaDataManagerPersistService.alterSingleRuleConfiguration(
                new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                new RuleMetaData(Collections.singleton(singleRule)));
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist("foo_db", Collections.singleton(singleRuleConfig));
    }
    
    @Test
    void assertAlterNullRuleConfiguration() {
        metaDataManagerPersistService.alterRuleConfiguration(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                null);
        verify(metaDataPersistFacade.getDatabaseRuleService(), never()).persist(eq("foo_db"), any());
    }
    
    @Test
    void assertAlterRuleConfigurationWithSingleRule() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterRuleConfiguration(database, ruleConfig);
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabase(eq("foo_db"), any(), any());
    }
    
    @Test
    void assertAlterRuleConfiguration() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(database.getDefaultSchemaName()).thenReturn("foo_default_schema");
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(database), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts().getMetaData()).thenReturn(metaData);
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        metaDataManagerPersistService.alterRuleConfiguration(database, ruleConfig);
        verify(metaDataPersistFacade.getDatabaseRuleService()).persist("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabase(eq("foo_db"), any(), any());
    }
    
    @Test
    void assertRemoveNullRuleConfigurationItem() {
        metaDataManagerPersistService.removeRuleConfigurationItem(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())),
                null);
        verify(metaDataPersistFacade.getDatabaseRuleService(), never()).delete(eq("foo_db"), anyCollection());
    }
    
    @Test
    void assertRemoveRuleConfigurationItem() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singleton("ds_0.t_order"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        mockReloadedMetaDataContext();
        metaDataManagerPersistService.removeRuleConfigurationItem(database, ruleConfig);
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabaseByUnloadSingleTable(
                eq("foo_db"), argThat(each -> each.containsSchema("foo_default_schema")), any());
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade(), never()).persistAlteredTables(eq("foo_db"), any(), any());
    }
    
    @Test
    void assertRemoveRuleConfiguration() {
        RuleConfiguration ruleConfig = mock(RuleConfiguration.class);
        when(ruleConfig.getLogicTableNames()).thenReturn(Collections.singleton("t_order"));
        metaDataManagerPersistService.removeRuleConfiguration(
                new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList(), new ConfigurationProperties(new Properties())), ruleConfig, "fixtureRule");
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", "fixtureRule");
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistAlteredTables(eq("foo_db"), any(), eq(Collections.singleton("t_order")));
    }
    
    @Test
    void assertRemoveSingleRuleConfiguration() {
        SingleRuleConfiguration ruleConfig = new SingleRuleConfiguration();
        ruleConfig.setTables(Collections.singleton("ds_0.t_order"));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        mockReloadedMetaDataContext();
        metaDataManagerPersistService.removeRuleConfiguration(database, ruleConfig, "SINGLE");
        verify(metaDataPersistFacade.getDatabaseRuleService()).delete("foo_db", Collections.singleton(ruleConfig));
        verify(metaDataPersistFacade.getDatabaseMetaDataFacade()).persistReloadDatabaseByUnloadSingleTable(eq("foo_db"), any(), any());
        verify(metaDataPersistFacade.getDatabaseRuleService(), never()).delete("foo_db", "SINGLE");
    }
    
    @Test
    void assertAlterGlobalRuleConfiguration() {
        RuleConfiguration ruleConfig = new SingleRuleConfiguration();
        metaDataManagerPersistService.alterGlobalRuleConfiguration(ruleConfig);
        verify(metaDataPersistFacade.getGlobalRuleService()).persist(Collections.singleton(ruleConfig));
    }
    
    @Test
    void assertAlterProperties() {
        Properties props = new Properties();
        metaDataManagerPersistService.alterProperties(props);
        verify(metaDataPersistFacade.getPropsService()).persist(props);
    }
    
    @Test
    void assertCreateTable() {
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        SingleRule singleRule = mock(SingleRule.class);
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        when(ruleMetaData.getSingleRule(SingleRule.class)).thenReturn(singleRule);
        when(singleRule.getConfiguration()).thenReturn(singleRuleConfig);
        when(TableRefreshUtils.isSingleTable("foo_tbl", database)).thenReturn(true);
        when(TableRefreshUtils.isNeedRefresh(ruleMetaData, "foo_schema", "foo_tbl")).thenReturn(true);
        metaDataManagerPersistService.createTable(database, "foo_schema", table);
        TableMetaDataPersistService tableMetaDataPersistService = metaDataPersistFacade.getDatabaseMetaDataFacade().getTable();
        DatabaseRulePersistService databaseRulePersistService = metaDataPersistFacade.getDatabaseRuleService();
        InOrder actual = inOrder(tableMetaDataPersistService, databaseRulePersistService);
        actual.verify(tableMetaDataPersistService).persist("foo_db", "foo_schema", Collections.singleton(table));
        actual.verify(databaseRulePersistService).persist("foo_db", Collections.singleton(singleRuleConfig));
    }
    
    @Test
    void assertDropTables() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        RuleMetaData ruleMetaData = mock(RuleMetaData.class);
        when(database.getRuleMetaData()).thenReturn(ruleMetaData);
        SingleRule singleRule = mock(SingleRule.class);
        SingleRuleConfiguration singleRuleConfig = new SingleRuleConfiguration();
        when(ruleMetaData.getSingleRule(SingleRule.class)).thenReturn(singleRule);
        when(singleRule.getConfiguration()).thenReturn(singleRuleConfig);
        when(TableRefreshUtils.isNeedRefresh(ruleMetaData, "foo_schema", Collections.singleton("foo_tbl"))).thenReturn(true);
        when(TableRefreshUtils.isSingleTable("foo_tbl", database)).thenReturn(true);
        metaDataManagerPersistService.dropTables(database, "foo_schema", Collections.singleton("foo_tbl"));
        TableMetaDataPersistService tableMetaDataPersistService = metaDataPersistFacade.getDatabaseMetaDataFacade().getTable();
        DatabaseRulePersistService databaseRulePersistService = metaDataPersistFacade.getDatabaseRuleService();
        InOrder actual = inOrder(tableMetaDataPersistService, databaseRulePersistService);
        actual.verify(tableMetaDataPersistService).drop("foo_db", "foo_schema", "foo_tbl");
        actual.verify(databaseRulePersistService).persist("foo_db", Collections.singleton(singleRuleConfig));
    }
    
    private void mockReloadedMetaDataContext() {
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes());
        ShardingSphereDatabase loadedDatabase = mock(ShardingSphereDatabase.class, Answers.RETURNS_DEEP_STUBS);
        when(loadedDatabase.getName()).thenReturn("foo_db");
        when(loadedDatabase.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "MySQL"));
        when(loadedDatabase.getDefaultSchemaName()).thenReturn("foo_default_schema");
        when(loadedDatabase.getRuleMetaData().getRules()).thenReturn(Collections.singleton(rule));
        when(loadedDatabase.getResourceMetaData().getStorageUnits()).thenReturn(new LinkedHashMap<>());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.singleton(loadedDatabase), mock(), mock(), new ConfigurationProperties(new Properties()));
        when(metaDataContextManager.getMetaDataContexts()).thenReturn(new MetaDataContexts(metaData, null));
    }
}
