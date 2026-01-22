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

package org.apache.shardingsphere.mode.metadata.persist.metadata;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.schema.DefaultSchemaOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.GenericSchemaManager;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.exception.LoadTableMetaDataFailedException;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.SchemaMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableMetaDataPersistDisabledService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.TableMetaDataPersistEnabledService;
import org.apache.shardingsphere.mode.metadata.persist.metadata.service.ViewMetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.persist.service.TableMetaDataPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DataSourcePoolPropertiesCreator.class, GenericSchemaManager.class, GenericSchemaBuilder.class})
class DatabaseMetaDataPersistFacadeTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock
    private SchemaMetaDataPersistService schemaMetaDataService;
    
    @Mock
    private TableMetaDataPersistService tableMetaDataService;
    
    @Mock
    private ViewMetaDataPersistService viewMetaDataService;
    
    @Mock
    private PersistRepository repository;
    
    @Mock
    private VersionPersistService versionPersistService;
    
    private DatabaseMetaDataPersistFacade databaseMetaDataFacade;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseMetaDataFacade = new DatabaseMetaDataPersistFacade(repository, versionPersistService, true);
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistFacade.class.getDeclaredField("schema"), databaseMetaDataFacade, schemaMetaDataService);
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistFacade.class.getDeclaredField("table"), databaseMetaDataFacade, tableMetaDataService);
        Plugins.getMemberAccessor().set(DatabaseMetaDataPersistFacade.class.getDeclaredField("view"), databaseMetaDataFacade, viewMetaDataService);
    }
    
    @Test
    void assertConstructorChoosesTablePersistService() {
        DatabaseMetaDataPersistFacade enabledFacade = new DatabaseMetaDataPersistFacade(repository, versionPersistService, true);
        DatabaseMetaDataPersistFacade disabledFacade = new DatabaseMetaDataPersistFacade(repository, versionPersistService, false);
        assertThat(enabledFacade.getTable(), isA(TableMetaDataPersistEnabledService.class));
        assertThat(disabledFacade.getTable(), isA(TableMetaDataPersistDisabledService.class));
    }
    
    @Test
    void assertPersistReloadDatabase() {
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesDropped(any(), any()))
                .thenReturn(Collections.singleton(new ShardingSphereSchema("Foo_Dropped", mock(DatabaseType.class))));
        when(GenericSchemaManager.getToBeAlteredSchemasWithTablesAdded(any(), any()))
                .thenReturn(Collections.singleton(new ShardingSphereSchema("Foo_Added", mock(DatabaseType.class))));
        databaseMetaDataFacade.persistReloadDatabase("foo_db", mock(ShardingSphereDatabase.class), mock(ShardingSphereDatabase.class));
        verify(tableMetaDataService).persist(eq("foo_db"), eq("foo_added"), anyCollection());
        verify(tableMetaDataService).drop(eq("foo_db"), eq("foo_dropped"), anyCollection());
    }
    
    @Test
    void assertRenameSchemaWithEmptySchema() {
        ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(new ShardingSphereSchema("foo_schema", mock(DatabaseType.class))));
        databaseMetaDataFacade.renameSchema(createMetaData(database), database, "foo_schema", "bar_schema");
        verify(schemaMetaDataService).add("foo_db", "bar_schema");
        verify(schemaMetaDataService).drop("foo_db", "foo_schema");
    }
    
    @Test
    void assertRenameSchemaWhenSchemaNotEmpty() {
        ShardingSphereTable table = new ShardingSphereTable("foo_table",
                Collections.singleton(new ShardingSphereColumn("foo_column", 0, false, false, false, true, false, true)),
                Collections.emptyList(), Collections.emptyList());
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", Collections.singleton(table), Collections.singleton(new ShardingSphereView("foo_view", "select 1")),
                databaseType);
        ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(schema));
        databaseMetaDataFacade.renameSchema(createMetaData(database), database, "foo_schema", "bar_schema");
        verify(tableMetaDataService).persist("foo_db", "bar_schema", schema.getAllTables());
        verify(viewMetaDataService).persist("foo_db", "bar_schema", schema.getAllViews());
        verify(schemaMetaDataService).drop("foo_db", "foo_schema");
        verify(schemaMetaDataService, never()).add(anyString(), anyString());
    }
    
    @Test
    void assertUnregisterStorageUnitsDropsTables() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null));
        try (MockedStatic<DatabaseTypedSPILoader> mocked = mockStatic(DatabaseTypedSPILoader.class)) {
            mocked.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class));
            ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(schema));
            ShardingSphereTable toBeDroppedTable = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            when(GenericSchemaBuilder.build(eq(databaseType), any(GenericSchemaBuilderMaterial.class))).thenReturn(Collections.singletonMap("foo_schema", schema));
            when(GenericSchemaManager.getToBeDroppedTables(schema, schema)).thenReturn(Collections.singleton(toBeDroppedTable));
            databaseMetaDataFacade.unregisterStorageUnits("foo_db", new MetaDataContexts(createMetaData(database), mock()));
            verify(tableMetaDataService).drop("foo_db", "foo_schema", "foo_table");
        }
    }
    
    @Test
    void assertUnregisterStorageUnitsWhenLoadFailed() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null));
        try (MockedStatic<DatabaseTypedSPILoader> mocked = mockStatic(DatabaseTypedSPILoader.class)) {
            mocked.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(new ShardingSphereSchema("foo_schema", mock(DatabaseType.class))));
            MetaDataContexts reloadMetaDataContexts = new MetaDataContexts(createMetaData(database), mock());
            when(GenericSchemaBuilder.build(eq(databaseType), any(GenericSchemaBuilderMaterial.class))).thenThrow(SQLException.class);
            assertThrows(LoadTableMetaDataFailedException.class, () -> databaseMetaDataFacade.unregisterStorageUnits("foo_db", reloadMetaDataContexts));
        }
    }
    
    @Test
    void assertPersistAlteredTables() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null));
        try (MockedStatic<DatabaseTypedSPILoader> mocked = mockStatic(DatabaseTypedSPILoader.class)) {
            mocked.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ShardingSphereSchema existedSchema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class));
            ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(existedSchema));
            MetaDataContexts reloadMetaDataContexts = new MetaDataContexts(createMetaData(database), mock());
            ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema", mock(DatabaseType.class));
            ShardingSphereTable addedTable = new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
            Collection<ShardingSphereTable> expectedTables = Collections.singletonList(addedTable);
            Collection<String> needReloadTables = Collections.singleton("foo_table");
            Map<String, ShardingSphereSchema> schemas = Collections.singletonMap("foo_schema", schema);
            when(GenericSchemaBuilder.build(eq(needReloadTables), eq(databaseType), any(GenericSchemaBuilderMaterial.class))).thenReturn(schemas);
            when(GenericSchemaManager.getToBeAddedTables(schema, existedSchema)).thenReturn(expectedTables);
            Map<String, Collection<ShardingSphereTable>> actualTables = databaseMetaDataFacade.persistAlteredTables("foo_db", reloadMetaDataContexts, needReloadTables);
            assertThat(actualTables.get("foo_schema"), is(expectedTables));
            verify(tableMetaDataService).persist("foo_db", "foo_schema", expectedTables);
        }
    }
    
    @Test
    void assertPersistAlteredTablesWhenLoadFailed() throws SQLException {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class);
        when(dialectDatabaseMetaData.getSchemaOption()).thenReturn(new DefaultSchemaOption(false, null));
        try (MockedStatic<DatabaseTypedSPILoader> mocked = mockStatic(DatabaseTypedSPILoader.class)) {
            mocked.when(() -> DatabaseTypedSPILoader.getService(DialectDatabaseMetaData.class, databaseType)).thenReturn(dialectDatabaseMetaData);
            ShardingSphereDatabase database = createDatabase("foo_db", Collections.singleton(new ShardingSphereSchema("foo_schema", mock(DatabaseType.class))));
            MetaDataContexts reloadMetaDataContexts = new MetaDataContexts(createMetaData(database), mock());
            Collection<String> needReloadTables = Collections.singleton("foo_table");
            when(GenericSchemaBuilder.build(eq(needReloadTables), eq(databaseType), any(GenericSchemaBuilderMaterial.class))).thenThrow(SQLException.class);
            assertThrows(LoadTableMetaDataFailedException.class, () -> databaseMetaDataFacade.persistAlteredTables("foo_db", reloadMetaDataContexts, needReloadTables));
        }
    }
    
    @Test
    void assertPersistCreatedDatabaseSchemas() {
        ShardingSphereSchema emptySchema = new ShardingSphereSchema("foo_empty", mock(DatabaseType.class));
        ShardingSphereSchema schema = new ShardingSphereSchema("foo_schema",
                Collections.singleton(new ShardingSphereTable("foo_table", Collections.emptyList(), Collections.emptyList(), Collections.emptyList())), Collections.emptyList(), databaseType);
        databaseMetaDataFacade.persistCreatedDatabaseSchemas(createDatabase("foo_db", Arrays.asList(emptySchema, schema)));
        verify(schemaMetaDataService).add("foo_db", "foo_empty");
        verify(tableMetaDataService).persist("foo_db", "foo_schema", schema.getAllTables());
    }
    
    private ShardingSphereDatabase createDatabase(final String databaseName, final Collection<ShardingSphereSchema> schemas) {
        return new ShardingSphereDatabase(databaseName, databaseType,
                new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), schemas);
    }
    
    private ShardingSphereMetaData createMetaData(final ShardingSphereDatabase database) {
        return new ShardingSphereMetaData(Collections.singleton(database),
                new ResourceMetaData(Collections.emptyMap(), Collections.emptyMap()), new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
    }
}
