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
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.jdbc.JDBCInstanceMetaData;
import org.apache.shardingsphere.infra.instance.metadata.proxy.ProxyInstanceMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.builder.global.GlobalRulesBuilder;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.manager.resource.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistFacade;
import org.apache.shardingsphere.mode.persist.PersistServiceFacade;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
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
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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
        database = mockDatabase();
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(database));
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new ProxyInstanceMetaData("foo_id", 3307), Collections.emptyList()));
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
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.emptyList());
        when(result.getAllSchemas()).thenReturn(Collections.singleton(schema));
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
        ShardingSphereDatabase emptyDatabase = new ShardingSphereDatabase("bar_db", databaseType, resourceMetaData, mock(RuleMetaData.class), Collections.emptyList());
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
        ShardingSphereDatabase database = new ShardingSphereDatabase("foo_db", mock(DatabaseType.class), resourceMetaData, mock(RuleMetaData.class), Collections.emptyList());
        when(metaDataContexts.getMetaData().getDatabase("foo_db")).thenReturn(database);
        when(metaDataContexts.getMetaData().containsDatabase("foo_db")).thenReturn(true);
        assertThat(contextManager.getStorageUnits("foo_db").size(), is(1));
    }
    
    @Test
    void assertReloadDatabase() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        when(metaDataContexts.getMetaData().getGlobalResourceMetaData()).thenReturn(mock(ResourceMetaData.class));
        try (
                MockedStatic<GenericSchemaManager> genericSchemaManager = mockStatic(GenericSchemaManager.class);
                MockedStatic<GlobalRulesBuilder> globalRulesBuilder = mockStatic(GlobalRulesBuilder.class);
                MockedStatic<ShardingSphereStatisticsFactory> statisticsFactory = mockStatic(ShardingSphereStatisticsFactory.class);
                MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                        (mock, context) -> when(mock.createChangedDatabase("foo_db", false, switchingResource, Collections.emptyList(), metaDataContexts)).thenReturn(database))) {
            genericSchemaManager.when(() -> GenericSchemaManager.getToBeDroppedSchemaNames(any(ShardingSphereDatabase.class), any(ShardingSphereDatabase.class)))
                    .thenReturn(Collections.singleton("foo_schema"));
            globalRulesBuilder.when(() -> GlobalRulesBuilder.buildRules(anyCollection(), anyCollection(), any(ConfigurationProperties.class))).thenReturn(Collections.emptyList());
            statisticsFactory.when(() -> ShardingSphereStatisticsFactory.create(any(), any())).thenReturn(mock(ShardingSphereStatistics.class));
            contextManager.reloadDatabase(database);
            verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
            verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).alterByRefresh(eq("foo_db"), any(ShardingSphereSchema.class));
        }
    }
    
    @Test
    void assertReloadDatabaseWhenSQLExceptionThrown() {
        setPersistServiceFacade(mockPersistServiceFacade());
        MetaDataContextManager metaDataContextManager = mock(MetaDataContextManager.class, RETURNS_DEEP_STUBS);
        SwitchingResource switchingResource = new SwitchingResource(Collections.emptyMap(), Collections.emptyMap(), Collections.emptyList(), Collections.emptyMap());
        when(metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(any(ResourceMetaData.class), anyMap())).thenReturn(switchingResource);
        setMetaDataContextManager(metaDataContextManager);
        try (MockedConstruction<MetaDataContextsFactory> ignored = mockConstruction(MetaDataContextsFactory.class,
                (mock, context) -> when(mock.createChangedDatabase("foo_db", false, switchingResource, Collections.emptyList(), metaDataContexts)).thenThrow(SQLException.class))) {
            contextManager.reloadDatabase(database);
        }
    }
    
    @Test
    void assertReloadSchemaWhenNoTablesFound() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        when(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getView().load(anyString(), anyString())).thenReturn(Collections.emptyList());
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereSchema emptySchema = new ShardingSphereSchema("foo_schema");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", emptySchema));
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
            verify(database).dropSchema("foo_schema");
            verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).drop("foo_db", "foo_schema");
        }
    }
    
    @Test
    void assertReloadSchemaWithTables() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        when(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getView().load(anyString(), anyString())).thenReturn(Collections.emptyList());
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereSchema reloadedSchema = new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.emptyList());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", reloadedSchema));
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
            verify(database).addSchema(reloadedSchema);
            verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getSchema()).alterByRefresh("foo_db", reloadedSchema);
        }
    }
    
    @Test
    void assertReloadSchemaWithSQLException() {
        setPersistServiceFacade(mockPersistServiceFacade());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(SQLException.class);
            contextManager.reloadSchema(database, "foo_schema", "foo_ds");
        }
    }
    
    @Test
    void assertReloadTable() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereTable table = mock(ShardingSphereTable.class);
        when(table.getName()).thenReturn("foo_tbl");
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.emptyList());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(anySet(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", schema));
            contextManager.reloadTable(database, "foo_schema", "foo_tbl");
            verify(persistServiceFacade.getMetaDataFacade().getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_schema", Collections.singleton(table));
        }
    }
    
    @Test
    void assertReloadTableWithDataSourceName() {
        PersistServiceFacade persistServiceFacade = mockPersistServiceFacade();
        setPersistServiceFacade(persistServiceFacade);
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema");
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(anySet(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class)))
                    .thenReturn(Collections.singletonMap("foo_schema", schema));
            contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_tbl");
            verify(persistServiceFacade.getModeFacade().getMetaDataManagerService()).dropTables(database, "foo_schema", Collections.singleton("foo_tbl"));
        }
    }
    
    @Test
    void assertReloadTableWithSQLException() {
        setPersistServiceFacade(mockPersistServiceFacade());
        try (MockedStatic<GenericSchemaBuilder> schemaBuilderMock = mockStatic(GenericSchemaBuilder.class)) {
            schemaBuilderMock.when(() -> GenericSchemaBuilder.build(anySet(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(SQLException.class);
            contextManager.reloadTable(database, "foo_schema", "foo_tbl");
            contextManager.reloadTable(database, "foo_schema", "foo_ds", "foo_tbl");
        }
    }
    
    @Test
    void assertGetPreSelectedDatabaseNameWithJDBC() {
        when(computeNodeInstanceContext.getInstance()).thenReturn(new ComputeNodeInstance(new JDBCInstanceMetaData("foo_id", "foo_db"), Collections.emptyList()));
        when(metaDataContexts.getMetaData().getAllDatabases()).thenReturn(Collections.singleton(new ShardingSphereDatabase("foo_db", mock(), mock(), mock(), Collections.emptyList())));
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
