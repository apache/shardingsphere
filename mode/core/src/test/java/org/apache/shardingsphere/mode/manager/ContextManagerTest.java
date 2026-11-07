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

package org.apache.shardingsphere.mode.manager;

import lombok.SneakyThrows;
import org.apache.shardingsphere.database.connector.core.exception.RuleAndStorageMetaDataMismatchedException;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoader;
import org.apache.shardingsphere.database.connector.core.metadata.data.loader.MetaDataLoaderMaterial;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.SchemaMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.data.model.TableMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationProperties;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContextFactory;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.exception.LoadTableMetaDataFailedException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureExtension;
import org.apache.shardingsphere.test.infra.framework.extension.log.LogCaptureSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, LogCaptureExtension.class})
@LogCaptureSettings(suppressOutput = true)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextManagerTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private MetaDataContexts metaDataContexts;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ComputeNodeInstanceContext computeNodeInstanceContext;
    
    private ShardingSphereDatabase database;
    
    private ContextManager contextManager;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(metaDataContexts.getMetaData().getProps()).thenReturn(new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData().getTemporaryProps()).thenReturn(new TemporaryConfigurationProperties(new Properties()));
        database = mockDatabase();
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getDatabase("bar_db")).thenReturn(null);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new ProxyInstanceMetaData("foo_id", 3307)));
        when(computeNodeInstanceContext.getModeConfiguration()).thenReturn(new ModeConfiguration("FIXTURE", mock()));
        contextManager = new ContextManager(metaDataContexts, computeNodeInstanceContext, mock(), mock());
    }
    
    private ShardingSphereDatabase mockDatabase() throws SQLException {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(databaseType);
        MutableDataNodeRuleAttribute ruleAttribute = mock(MutableDataNodeRuleAttribute.class);
        when(ruleAttribute.findTableDataNode("foo_schema", "foo_tbl")).thenReturn(Optional.of(mock(DataNode.class)));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(result.containsSchema("foo_schema")).thenReturn(true);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(table), Collections.emptyList());
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
        when(result.getSchema("foo_schema")).thenReturn(schema);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType()).thenReturn(databaseType);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true, false);
        when(connection.getMetaData().getTables(null, null, "foo_tbl", null)).thenReturn(resultSet);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource(connection));
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        return result;
    }
    
    @Test
    void assertGetDatabaseType() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap());
        ShardingSphereDatabase emptyDatabase =
                new ShardingSphereDatabase("bar_db", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(emptyDatabase));
        assertThat(contextManager.getDatabaseType(), is(DatabaseTypeEngine.getDefaultStorageType()));
    }
    
    @Test
    void assertGetAllDatabaseNames() {
        assertThat(contextManager.getAllDatabaseNames(), is(Collections.singletonList("foo_db")));
    }
    
    @Test
    void assertGetDatabase() {
        assertNotNull(contextManager.getDatabase("foo_db"));
    }
    
    @Test
    void assertGetDatabaseWithNull() {
        assertThrows(NoDatabaseSelectedException.class, () -> contextManager.getDatabase(null));
    }
    
    @Test
    void assertGetDatabaseWhenNotExisted() {
        assertThrows(UnknownDatabaseException.class, () -> contextManager.getDatabase("bar_db"));
    }
    
    @Test
    void assertGetStorageUnits() {
        ResourceMetaData resourceMetaData = new ResourceMetaData(Collections.singletonMap("foo_ds", new MockedDataSource()));
        ShardingSphereDatabase database =
                new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, mock(RuleMetaData.class), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        assertThat(contextManager.getStorageUnits("foo_db").size(), is(1));
    }
    
    @Test
    void assertReloadDatabase() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        when(metaDataContexts.getMetaData().getGlobalResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        try (
                MockedStatic<GenericSchemaManager> genericSchemaManager = mockStatic(GenericSchemaManager.class);
                MockedStatic<GlobalRulesBuilder> globalRulesBuilder = mockStatic(GlobalRulesBuilder.class);
                MockedStatic<ShardingSphereStatisticsFactory> statisticsFactory = mockStatic(ShardingSphereStatisticsFactory.class);
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(database))) {
            genericSchemaManager.when(() -> GenericSchemaManager.getToBeDroppedSchemaNames(any(ShardingSphereDatabase.class), any(ShardingSphereDatabase.class)))
                    .thenReturn(Collections.singleton("foo_schema"));
            globalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
            statisticsFactory.when(() -> ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(mock(ShardingSphereStatistics.class));
            contextManager.reloadDatabase(database);
        }
        verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
        verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).alterByRefresh(eq("foo_db"), any(ShardingSphereSchema.class));
    }
    
    @Test
    void assertReloadDatabaseWhenDatabaseExists() throws SQLException {
        setPersistServiceFacade(mockPersistServiceFacade());
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = mock(SwitchingResource.class);
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        ShardingSphereDatabase currentDatabase = mockDatabase();
        ShardingSphereTable unrelatedTable = new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema unrelatedSchema = new ShardingSphereSchema("bar_schema", databaseType, Collections.singleton(unrelatedTable), Collections.emptyList());
        ShardingSphereDatabase unrelatedDatabase = new ShardingSphereDatabase("bar_db", databaseType, new ResourceMetaData(Collections.emptyMap()), new RuleMetaData(Collections.emptyList()),
                Collections.singleton(unrelatedSchema), new ConfigurationProperties(new Properties()));
        TablesContext unrelatedBindings = new TablesContext(Collections.emptyList());
        unrelatedBindings.getBoundTables().add(unrelatedTable);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Arrays.asList(currentDatabase, unrelatedDatabase));
        when(metaDataContexts.getMetaData().getGlobalResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        ShardingSphereDatabase candidate = mockDatabase();
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(candidate));
                MockedStatic<ShardingSphereStatisticsFactory> statisticsFactory = mockStatic(ShardingSphereStatisticsFactory.class)) {
            statisticsFactory.when(() -> ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(mock(ShardingSphereStatistics.class));
            contextManager.reloadDatabase(database);
        }
        ArgumentCaptor<MetaDataContexts> metaDataContextsCaptor = ArgumentCaptor.forClass(MetaDataContexts.class);
        verify(metaDataContexts).update(metaDataContextsCaptor.capture());
        ShardingSphereMetaData actual = metaDataContextsCaptor.getValue().getMetaData();
        assertThat(actual.getDatabase("foo_db"), sameInstance(candidate));
        assertThat(actual.getDatabase("bar_db"), sameInstance(unrelatedDatabase));
        assertThat(actual.getAllDatabases().size(), is(2));
        assertDoesNotThrow(unrelatedBindings::checkMetadata);
        verify(switchingResource).closeStaleDataSources();
        verify(switchingResource, never()).closeNewDataSources();
    }
    
    @Test
    void assertReloadDatabaseWhenDatabaseIsMissing() throws SQLException {
        setPersistServiceFacade(mockPersistServiceFacade());
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = mock(SwitchingResource.class);
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.emptyList());
        when(metaDataContexts.getMetaData().getGlobalResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        ShardingSphereDatabase candidate = mockDatabase();
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(candidate));
                MockedStatic<ShardingSphereStatisticsFactory> statisticsFactory = mockStatic(ShardingSphereStatisticsFactory.class)) {
            statisticsFactory.when(() -> ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(mock(ShardingSphereStatistics.class));
            contextManager.reloadDatabase(database);
        }
        verify(metaDataContexts).update(argThat(actual -> candidate == actual.getMetaData().getDatabase("foo_db")));
        verify(switchingResource).closeStaleDataSources();
        verify(switchingResource, never()).closeNewDataSources();
    }
    
    @Test
    void assertReloadDatabaseClosesCandidateResourcesWhenDroppingSchemaFails() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = mock(SwitchingResource.class);
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        when(metaDataContexts.getMetaData().getGlobalResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        IllegalStateException expectedException = new IllegalStateException("repository unavailable");
        SchemaMetaDataPersistService schemaPersistService = persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema();
        doThrow(expectedException).when(schemaPersistService).drop("foo_db", "foo_schema");
        try (
                MockedStatic<GenericSchemaManager> genericSchemaManager = mockStatic(GenericSchemaManager.class);
                MockedStatic<ShardingSphereStatisticsFactory> statisticsFactory = mockStatic(ShardingSphereStatisticsFactory.class);
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(database))) {
            genericSchemaManager.when(() -> GenericSchemaManager.getToBeDroppedSchemaNames(any(ShardingSphereDatabase.class), any(ShardingSphereDatabase.class)))
                    .thenReturn(Collections.singleton("foo_schema"));
            statisticsFactory.when(() -> ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(mock(ShardingSphereStatistics.class));
            assertThat(assertThrows(IllegalStateException.class, () -> contextManager.reloadDatabase(database)), sameInstance(expectedException));
        }
        verify(metaDataContexts, never()).update(any());
        verify(switchingResource).closeNewDataSources();
        verify(switchingResource, never()).closeStaleDataSources();
    }
    
    @Test
    void assertReloadDatabaseWhenSQLExceptionThrown() {
        setPersistServiceFacade(mockPersistServiceFacade());
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = mock(SwitchingResource.class);
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        SQLException expectedException = new SQLException("metadata unavailable");
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenThrow(expectedException))) {
            LoadTableMetaDataFailedException actual = assertThrows(LoadTableMetaDataFailedException.class, () -> contextManager.reloadDatabase(database));
            assertThat(actual.getCause(), sameInstance(expectedException));
        }
        verify(metaDataContexts, never()).update(any());
        verify(metaDataContexts.getMetaData(), never()).putDatabase(any());
        verify(switchingResource).closeNewDataSources();
        verify(switchingResource, never()).closeStaleDataSources();
    }
    
    @Test
    void assertReloadDatabaseWhenCandidatePreparationFails() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = mock(SwitchingResource.class);
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap(), anyBoolean())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        ShardingSphereDatabase candidate = mock(ShardingSphereDatabase.class);
        IllegalStateException expectedException = new IllegalStateException("global rule failure");
        try (
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabaseByRebuild("foo_db", switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(candidate));
                MockedStatic<GlobalRulesBuilder> globalRulesBuilder = mockStatic(GlobalRulesBuilder.class)) {
            globalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenThrow(expectedException);
            assertThat(assertThrows(IllegalStateException.class, () -> contextManager.reloadDatabase(database)), sameInstance(expectedException));
        }
        verify(metaDataContexts.getMetaData(), never()).putDatabase(any());
        verify(metaDataContexts, never()).update(any());
        verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema(), never()).alterByRefresh(anyString(), any());
        verify(switchingResource).closeNewDataSources();
        verify(switchingResource, never()).closeStaleDataSources();
    }
    
    @Test
    void assertReloadSchemaWhenNoTablesFound() {
        mockReloadableSchemaRule();
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereSchema emptySchema = new ShardingSphereSchema("foo_schema", databaseType);
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", emptySchema));
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).dropSchema(database, Collections.singleton("foo_schema"));
    }
    
    private void mockReloadableSchemaRule() {
        ShardingSphereRule rule = database.getRuleMetaData().getRules().iterator().next();
        MutableDataNodeRuleAttribute ruleAttribute = rule.getAttributes().getAttribute(MutableDataNodeRuleAttribute.class);
        when(rule.getConfiguration()).thenReturn(mock(RuleConfiguration.class));
        TableMapperRuleAttribute tableMapper = mock(TableMapperRuleAttribute.class);
        when(tableMapper.getLogicTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        when(rule.getAttributes()).thenReturn(new RuleAttributes(ruleAttribute, tableMapper));
        when(ruleAttribute.reloadRule(any(), anyString(), anyMap(), anyCollection())).thenReturn(rule);
    }
    
    @Test
    void assertReloadSchemaPreservesViewsWhenAllSelectedTablesWereDropped() {
        mockReloadableSchemaRule();
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable selectedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(selectedTable),
                Collections.singleton(new ShardingSphereView("foo_view", "SELECT 1")));
        when(database.getSchema("foo_schema")).thenReturn(currentSchema);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(currentSchema));
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenReturn(Collections.emptyMap());
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).dropTables(database, "foo_schema", Collections.singletonList("foo_tbl"));
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropSchema(any(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterViews(any(), anyString(), any());
    }
    
    @Test
    void assertReloadSchemaWithTables() {
        mockReloadableSchemaRule();
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereSchema reloadedSchema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class), Collections.singleton(table), Collections.emptyList());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", reloadedSchema));
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).alterTables(eq(database), eq("foo_schema"),
                argThat(tables -> 1 == tables.size() && table == tables.iterator().next()));
    }
    
    @Test
    void assertReloadSchemaRefreshesMutableRulesWhenStructureIsUnchanged() {
        mockReloadableSchemaRule();
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereSchema currentSchema = database.getSchema("foo_schema");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", currentSchema));
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
        verify(database).reloadRules();
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropSchema(any(), any());
    }
    
    @Test
    void assertReloadSchemaWithSQLException() {
        mockReloadableSchemaRule();
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        SQLException expectedException = new SQLException("metadata unavailable");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(expectedException);
            LoadTableMetaDataFailedException actual = assertThrows(LoadTableMetaDataFailedException.class, () -> contextManager.reloadSchema(database, "foo_schema", "foo_ds"));
            assertThat(actual.getCause(), sameInstance(expectedException));
        }
        verify(database, never()).reloadRules();
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropSchema(any(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropTables(any(), anyString(), any());
    }
    
    @Test
    void assertReloadSchemaPreservesUnrelatedTables() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable unrelatedTable = new ShardingSphereTable("bar_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereTable selectedTable = new ShardingSphereTable("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("foo_schema", databaseType, Arrays.asList(selectedTable, unrelatedTable), Collections.emptyList());
        when(database.getSchema("foo_schema")).thenReturn(currentSchema);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(currentSchema));
        TableMapperRuleAttribute tableMapper = mock(TableMapperRuleAttribute.class);
        when(tableMapper.getLogicTableNames()).thenReturn(Arrays.asList("foo_tbl", "bar_tbl"));
        DataNodeRuleAttribute dataNodes = mock(DataNodeRuleAttribute.class);
        when(dataNodes.getDataNodesByTableName("foo_tbl")).thenReturn(Collections.singleton(new DataNode("foo_ds", "foo_schema", "foo_tbl")));
        when(dataNodes.getDataNodesByTableName("bar_tbl")).thenReturn(Collections.singleton(new DataNode("bar_ds", "foo_schema", "bar_tbl")));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(tableMapper, dataNodes));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(eq(Collections.singleton("foo_tbl")), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.emptyMap());
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).dropTables(eq(database), eq("foo_schema"), argThat(tables -> tables.equals(Collections.singletonList("foo_tbl"))));
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropSchema(any(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
    }
    
    @Test
    void assertReloadTable() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(table), Collections.emptyList());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", schema));
            contextManager.reloadTable(database, "foo_schema", "foo_tbl");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).alterTables(database, "foo_schema", Collections.singleton(table));
    }
    
    @Test
    void assertReloadTableWithQuotedIdentifier() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("FOO_TBL");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", databaseType, Collections.singleton(table), Collections.emptyList());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", schema));
            contextManager.reloadTable(database, "foo_schema", new IdentifierValue("\"FOO_TBL\""));
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).alterTables(database, "foo_schema", Collections.singleton(table));
    }
    
    @Test
    void assertReloadTableWithDataSourceName() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class));
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", schema));
            contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_tbl");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).dropTables(database, "foo_schema", Collections.singleton("foo_tbl"));
    }
    
    @Test
    void assertReloadSchemaWithCanonicalSchemaName() {
        DatabaseType protocolType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
        DatabaseType storageType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(storageType);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(protocolType);
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        TableMapperRuleAttribute tableMapperRuleAttribute = mock(TableMapperRuleAttribute.class);
        when(tableMapperRuleAttribute.getLogicTableNames()).thenReturn(Collections.singleton("foo_tbl"));
        ShardingSphereRule rule = mock(ShardingSphereRule.class);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(tableMapperRuleAttribute));
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        when(database.getIdentifierContext()).thenReturn(DatabaseIdentifierContextFactory.createDefault());
        ShardingSphereSchema currentSchema = new ShardingSphereSchema("FOO_DB", protocolType);
        when(database.getAllSchemas()).thenReturn(Collections.singleton(currentSchema));
        when(database.getSchema("FOO_DB")).thenReturn(currentSchema);
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        TableMetaData tableMetaData = new TableMetaData("foo_tbl", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        try (MockedStatic<MetaDataLoader> metaDataLoader = mockStatic(MetaDataLoader.class)) {
            metaDataLoader.when(() -> MetaDataLoader.load(any())).thenAnswer(invocation -> {
                Collection<MetaDataLoaderMaterial> loaderMaterials = invocation.getArgument(0);
                String storageSchemaName = loaderMaterials.iterator().next().getDefaultSchemaName();
                return Collections.singletonMap(storageSchemaName, new SchemaMetaData(storageSchemaName, Collections.singleton(tableMetaData)));
            });
            contextManager.reloadSchema(database, "FOO_DB", "foo_ds");
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).alterTables(eq(database), eq("FOO_DB"),
                argThat(tables -> 1 == tables.size() && "foo_tbl".equals(tables.iterator().next().getName())));
    }
    
    @Test
    void assertReloadTableWithSQLException() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        SQLException expectedException = new SQLException("metadata unavailable");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(expectedException);
            LoadTableMetaDataFailedException actual = assertThrows(LoadTableMetaDataFailedException.class, () -> contextManager.reloadTable(database, "foo_schema", "foo_tbl"));
            assertThat(actual.getCause(), sameInstance(expectedException));
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropTables(any(), anyString(), any());
    }
    
    @Test
    void assertReloadTableFromStorageUnitWithSQLException() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        SQLException expectedException = new SQLException("metadata unavailable");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(expectedException);
            LoadTableMetaDataFailedException actual = assertThrows(LoadTableMetaDataFailedException.class, () -> contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_tbl"));
            assertThat(actual.getCause(), sameInstance(expectedException));
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropTables(any(), anyString(), any());
    }
    
    @Test
    void assertReloadTableFromStorageUnitWithInconsistentMetadata() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        RuleAndStorageMetaDataMismatchedException expectedException = new RuleAndStorageMetaDataMismatchedException("inconsistent columns");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.buildForRefresh(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(expectedException);
            assertThat(assertThrows(RuleAndStorageMetaDataMismatchedException.class, () -> contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_tbl")), sameInstance(expectedException));
        }
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).alterTables(any(), anyString(), any());
        verify(persistServiceFacade.getModeFacade().getMetaDataManagerService(), never()).dropTables(any(), anyString(), any());
    }
    
    @Test
    void assertGetPreSelectedDatabaseNameWithJDBC() {
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new JDBCInstanceMetaData("foo_id", "foo_db")));
        ShardingSphereDatabase database =
                new ShardingSphereDatabase("foo_db", databaseType, mock(ResourceMetaData.class), mock(RuleMetaData.class), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        assertThat(contextManager.getPreSelectedDatabaseName(), is("foo_db"));
    }
    
    @Test
    void assertGetPreSelectedDatabaseNameWithProxy() {
        assertNull(contextManager.getPreSelectedDatabaseName());
    }
    
    @Test
    void assertClose() {
        contextManager.close();
        verify(metaDataContexts.getMetaData()).close();
    }
    
    private PersistServiceFacade mockPersistServiceFacade() {
        MetaDataPersistFacade metaDataPersistFacade = mock(MetaDataPersistFacade.class, RETURNS_DEEP_STUBS);
        when(metaDataPersistFacade.getDataSourceUnitService().load("foo_db")).thenReturn(Collections.emptyMap());
        when(metaDataPersistFacade.getDatabaseRuleService().load("foo_db")).thenReturn(Collections.emptyList());
        when(metaDataPersistFacade.getPropsService().load()).thenReturn(new Properties());
        when(metaDataPersistFacade.getGlobalRuleService().load()).thenReturn(Collections.emptyList());
        when(metaDataPersistFacade.getDatabaseMetaDataFacade().getView().load(anyString(), anyString())).thenReturn(Collections.emptyList());
        PersistServiceFacade result = mock(PersistServiceFacade.class, RETURNS_DEEP_STUBS);
        when(result.getMetaDataFacade()).thenReturn(metaDataPersistFacade);
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setPersistServiceFacade(final PersistServiceFacade persistServiceFacade) {
        Plugins.getMemberAccessor().set(ContextManager.class.getDeclaredField("persistServiceFacade"), contextManager, persistServiceFacade);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setMetaDataContextManager(final MetaDataContextManager metaDataContextManager) {
        Plugins.getMemberAccessor().set(ContextManager.class.getDeclaredField("metaDataContextManager"), contextManager, metaDataContextManager);
    }
}
