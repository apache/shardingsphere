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

package org.apache.shardingsphere.proxy.backend.mysql.listener;

import org.apache.shardingsphere.database.connector.core.metadata.database.enums.TableType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilder;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.GenericSchemaBuilderMaterial;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereColumn;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereConstraint;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereIndex;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.listener.ContextManagerLifecycleListener;
import org.apache.shardingsphere.mode.metadata.manager.database.DatabaseMetaDataManager;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(GenericSchemaBuilder.class)
class MySQLContextManagerLifecycleListenerTest {
    
    private final MySQLContextManagerLifecycleListener listener = ShardingSphereServiceLoader.getServiceInstances(ContextManagerLifecycleListener.class).stream()
            .filter(MySQLContextManagerLifecycleListener.class::isInstance).map(MySQLContextManagerLifecycleListener.class::cast).findFirst().orElseThrow(IllegalStateException::new);
    
    private final DatabaseType mysqlType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    @Test
    void assertOnInitializedWithUnknownTypes() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn originalDatetime = new ShardingSphereColumn("foo_dt", Types.TIMESTAMP, true, true, true, true, true, false, null);
        ShardingSphereColumn originalTimestamp = new ShardingSphereColumn("foo_ts", Types.TIMESTAMP, false, false, false, true, false, true, null);
        ShardingSphereColumn knownColumn = column("foo_known", Types.TIMESTAMP, true);
        ShardingSphereColumn unrelatedColumn = column("foo_id", Types.INTEGER, null);
        ShardingSphereIndex index = new ShardingSphereIndex("foo_index", Collections.singletonList("foo_dt"), true);
        ShardingSphereConstraint constraint = new ShardingSphereConstraint("foo_constraint", "bar_table");
        ShardingSphereTable original = new ShardingSphereTable("foo_table", Arrays.asList(originalDatetime, originalTimestamp, knownColumn, unrelatedColumn),
                Collections.singletonList(index), Collections.singletonList(constraint), TableType.VIEW);
        ShardingSphereSchema schema = schema(original);
        mockDatabase(contextManager, database, schema, new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Arrays.asList(new DataNode("foo_ds", "foo_db", "foo_table"), new DataNode("bar_ds", "bar_schema", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        ShardingSphereTable loaded = new ShardingSphereTable("foo_table", Arrays.asList(column("foo_ts", Types.TIMESTAMP, false), column("foo_dt", Types.TIMESTAMP, true),
                column("foo_known", Types.TIMESTAMP, false), column("bar_new", Types.INTEGER, false)), Collections.emptyList(), Collections.emptyList());
        when(database.getName()).thenReturn("foo_db");
        when(GenericSchemaBuilder.build(eq(Collections.singletonList("foo_table")), eq(mysqlType), any(GenericSchemaBuilderMaterial.class))).thenAnswer(invocation -> {
            GenericSchemaBuilderMaterial material = invocation.getArgument(2);
            assertThat(material.getStorageUnits(), sameInstance(storageUnits));
            assertThat(material.getDefaultSchemaName(), is("foo_db"));
            assertThat(material.getRevisionCandidateSchemas(), sameInstance(database.getAllSchemas()));
            return Collections.singletonMap("foo_db", schema(loaded));
        });
        listener.onInitialized(contextManager);
        ArgumentCaptor<ShardingSphereTable> actualTable = ArgumentCaptor.forClass(ShardingSphereTable.class);
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable(eq("foo_db"), eq("foo_db"), actualTable.capture());
        verify(contextManager.getPersistServiceFacade().getMetaDataFacade().getDatabaseMetaDataFacade().getTable()).persist("foo_db", "foo_db", Collections.singletonList(actualTable.getValue()));
        assertThat(actualTable.getValue().getName(), is(original.getName()));
        assertThat(actualTable.getValue().getType(), is(TableType.VIEW));
        assertThat(actualTable.getValue().getAllColumns().size(), is(4));
        assertThat(actualTable.getValue().getColumnNames(), is(original.getColumnNames()));
        assertThat(actualTable.getValue().getVisibleColumns(), is(original.getVisibleColumns()));
        assertTrue(actualTable.getValue().getColumn("foo_dt").isDatetime());
        assertFalse(actualTable.getValue().getColumn("foo_ts").isDatetime());
        assertThat(actualTable.getValue().getColumn("foo_known"), sameInstance(knownColumn));
        assertThat(actualTable.getValue().getColumn("foo_id"), sameInstance(unrelatedColumn));
        assertThat(actualTable.getValue().getAllIndexes().iterator().next(), sameInstance(index));
        assertThat(actualTable.getValue().getAllConstraints().iterator().next(), sameInstance(constraint));
        assertColumnAttributes(actualTable.getValue().getColumn("foo_dt"), originalDatetime);
        assertColumnAttributes(actualTable.getValue().getColumn("foo_ts"), originalTimestamp);
    }
    
    private static ShardingSphereColumn column(final String name, final int jdbcType, final Boolean datetime) {
        return new ShardingSphereColumn(name, jdbcType, false, false, false, true, false, true, datetime);
    }
    
    private static ShardingSphereSchema schema(final ShardingSphereTable table) {
        return new ShardingSphereSchema("foo_db", TypedSPILoader.getService(DatabaseType.class, "MySQL"), Collections.singletonList(table), Collections.emptyList());
    }
    
    private void mockDatabase(final ContextManager contextManager, final ShardingSphereDatabase database, final ShardingSphereSchema schema, final Properties props) {
        when(contextManager.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Collections.singletonList(database));
        when(contextManager.getMetaDataContexts().getMetaData().getProps()).thenReturn(new ConfigurationProperties(props));
        when(database.getProtocolType()).thenReturn(mysqlType);
        when(database.getAllSchemas()).thenReturn(Collections.singletonList(schema));
    }
    
    private Map<String, StorageUnit> sources(final ShardingSphereDatabase database, final Collection<DataNode> dataNodes) {
        DataNodeRuleAttribute attribute = mock(DataNodeRuleAttribute.class);
        when(attribute.getDataNodesByTableName("foo_table")).thenReturn(dataNodes);
        ShardingSphereRule rule = mock(ShardingSphereRule.class, RETURNS_DEEP_STUBS);
        when(rule.getAttributes().findAttribute(DataNodeRuleAttribute.class)).thenReturn(Optional.of(attribute));
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.singletonList(rule));
        Map<String, StorageUnit> result = new LinkedHashMap<>(2, 1F);
        result.put("foo_ds", mock(StorageUnit.class));
        result.put("bar_ds", mock(StorageUnit.class));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(result);
        return result;
    }
    
    private void assertColumnAttributes(final ShardingSphereColumn actualColumn, final ShardingSphereColumn expectedColumn) {
        assertThat(actualColumn.getName(), is(expectedColumn.getName()));
        assertThat(actualColumn.getDataType(), is(expectedColumn.getDataType()));
        assertTrue(actualColumn.isPrimaryKey() == expectedColumn.isPrimaryKey());
        assertTrue(actualColumn.isGenerated() == expectedColumn.isGenerated());
        assertTrue(actualColumn.isCaseSensitive() == expectedColumn.isCaseSensitive());
        assertTrue(actualColumn.isVisible() == expectedColumn.isVisible());
        assertTrue(actualColumn.isUnsigned() == expectedColumn.isUnsigned());
        assertTrue(actualColumn.isNullable() == expectedColumn.isNullable());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("knownTypeArguments")
    void assertOnInitializedWithKnownTypes(final String name, final int jdbcType, final Boolean datetime) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_column", jdbcType, datetime))), new Properties());
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    private static ShardingSphereTable table(final ShardingSphereColumn column) {
        return new ShardingSphereTable("foo_table", Collections.singletonList(column), Collections.emptyList(), Collections.emptyList());
    }
    
    private static Stream<Arguments> knownTypeArguments() {
        return Stream.of(Arguments.of("known-datetime", Types.TIMESTAMP, true), Arguments.of("known-timestamp", Types.TIMESTAMP, false), Arguments.of("non-temporal", Types.INTEGER, null));
    }
    
    @Test
    void assertOnInitializedWithOtherProtocol() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts().getMetaData().getAllDatabases()).thenReturn(Collections.singletonList(database));
        when(database.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"PostgreSQL", "H2", "MariaDB"})
    void assertOnInitializedWithOtherStorage(final String storageType) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("bar_ds", "foo_db", "foo_table")));
        when(storageUnits.get("bar_ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, storageType));
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedWithoutDataNodes() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        when(database.getRuleMetaData().getRules()).thenReturn(Collections.emptyList());
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedWithMetadataCheck() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString()));
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), props);
        Map<String, StorageUnit> storageUnits = sources(database,
                Arrays.asList(new DataNode("foo_ds", "foo_db", "foo_table"), new DataNode("bar_ds", "bar_schema", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(storageUnits.get("bar_ds").getStorageType()).thenReturn(mysqlType);
        when(database.getName()).thenReturn("foo_db");
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class))).thenAnswer(invocation -> {
            GenericSchemaBuilderMaterial material = invocation.getArgument(2);
            assertThat(material.getStorageUnits(), sameInstance(storageUnits));
            assertTrue(material.getProps().<Boolean>getValue(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED));
            return Collections.singletonMap("foo_db", schema(table(column("foo_dt", Types.TIMESTAMP, true))));
        });
        listener.onInitialized(contextManager);
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable(eq("foo_db"), eq("foo_db"), any(ShardingSphereTable.class));
    }
    
    @Test
    void assertOnInitializedWithMixedCheckedSources() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        Properties props = PropertiesBuilder.build(new Property(ConfigurationPropertyKey.CHECK_TABLE_METADATA_ENABLED.getKey(), Boolean.TRUE.toString()));
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), props);
        Map<String, StorageUnit> storageUnits = sources(database, Arrays.asList(new DataNode("foo_ds", "foo_db", "foo_table"), new DataNode("bar_ds", "bar_schema", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(storageUnits.get("bar_ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedWithQualifiedSource() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds.foo_db", (String) null, "foo_table")));
        StorageUnit qualifiedUnit = mock(StorageUnit.class);
        storageUnits.put("foo_ds.foo_db", qualifiedUnit);
        when(qualifiedUnit.getStorageType()).thenReturn(mysqlType);
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(database.getName()).thenReturn("foo_db");
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class)))
                .thenReturn(Collections.singletonMap("foo_db", schema(table(column("foo_dt", Types.TIMESTAMP, true)))));
        listener.onInitialized(contextManager);
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable(eq("foo_db"), eq("foo_db"), any(ShardingSphereTable.class));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("untrustedQualifiedSourceArguments")
    void assertOnInitializedWithUntrustedQualifiedSource(final String name, final Map<String, StorageUnit> sourceUnits) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds.foo_db", "foo_db", "foo_table")));
        storageUnits.clear();
        storageUnits.putAll(sourceUnits);
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    private static Stream<Arguments> untrustedQualifiedSourceArguments() {
        Map<String, StorageUnit> missingQualified = Collections.singletonMap("foo_ds", mock(StorageUnit.class));
        Map<String, StorageUnit> missingConnection = Collections.singletonMap("foo_ds.foo_db", storageUnit("MySQL"));
        Map<String, StorageUnit> otherQualified = new LinkedHashMap<>(2, 1F);
        otherQualified.put("foo_ds", mock(StorageUnit.class));
        otherQualified.put("foo_ds.foo_db", storageUnit("PostgreSQL"));
        Map<String, StorageUnit> otherConnection = new LinkedHashMap<>(2, 1F);
        otherConnection.put("foo_ds", storageUnit("PostgreSQL"));
        otherConnection.put("foo_ds.foo_db", storageUnit("MySQL"));
        return Stream.of(
                Arguments.of("missing-qualified", missingQualified),
                Arguments.of("missing-connection", missingConnection),
                Arguments.of("other-qualified", otherQualified),
                Arguments.of("other-connection", otherConnection));
    }
    
    private static StorageUnit storageUnit(final String storageType) {
        StorageUnit result = mock(StorageUnit.class);
        when(result.getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, storageType));
        return result;
    }
    
    @Test
    void assertOnInitializedWithOtherSchema() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        sources(database, Collections.singletonList(new DataNode("foo_ds", "bar_schema", "foo_table")));
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedWithMixedSources() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Arrays.asList(new DataNode("foo_ds", "foo_db", "foo_table"), new DataNode("bar_ds", "foo_db", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(storageUnits.get("bar_ds").getStorageType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        when(GenericSchemaBuilder.build(anyCollection(), any(DatabaseType.class), any(GenericSchemaBuilderMaterial.class))).thenThrow(new AssertionError("Unexpected metadata load"));
        listener.onInitialized(contextManager);
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedRepeatedly() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereSchema schema = schema(table(column("foo_dt", Types.TIMESTAMP, null)));
        mockDatabase(contextManager, database, schema, new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds", "foo_db", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(database.getName()).thenReturn("foo_db");
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class)))
                .thenReturn(Collections.singletonMap("foo_db", schema(table(column("foo_dt", Types.TIMESTAMP, true))))).thenThrow(new AssertionError("Repeated metadata load"));
        DatabaseMetaDataManager databaseMetaDataManager = contextManager.getMetaDataContextManager().getDatabaseMetaDataManager();
        doAnswer(invocation -> {
            schema.putTable(invocation.getArgument(2));
            return null;
        }).when(databaseMetaDataManager).alterTable(eq("foo_db"), eq("foo_db"), any(ShardingSphereTable.class));
        listener.onInitialized(contextManager);
        listener.onInitialized(contextManager);
        assertTrue(schema.getTable("foo_table").getColumn("foo_dt").isDatetime());
        verify(contextManager.getMetaDataContextManager().getDatabaseMetaDataManager()).alterTable(eq("foo_db"), eq("foo_db"), any(ShardingSphereTable.class));
    }
    
    @Test
    void assertOnInitializedWithLoadFailure() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = column("foo_dt", Types.TIMESTAMP, null);
        mockDatabase(contextManager, database, schema(table(column)), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds", "foo_db", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(database.getName()).thenReturn("foo_db");
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class))).thenThrow(new SQLException("foo_probe"));
        listener.onInitialized(contextManager);
        assertNull(column.getDatetime());
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @Test
    void assertOnInitializedWithRuntimeFailure() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        mockDatabase(contextManager, database, schema(table(column("foo_dt", Types.TIMESTAMP, null))), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds", "foo_db", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        IllegalStateException expected = new IllegalStateException("foo_failure");
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class))).thenThrow(expected);
        assertThat(assertThrows(IllegalStateException.class, () -> listener.onInitialized(contextManager)), sameInstance(expected));
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("unresolvedMetadataArguments")
    void assertOnInitializedWithUnresolvedMetadata(final String name, final Map<String, ShardingSphereSchema> loadedSchemas) throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        ShardingSphereColumn column = column("foo_dt", Types.TIMESTAMP, null);
        mockDatabase(contextManager, database, schema(table(column)), new Properties());
        Map<String, StorageUnit> storageUnits = sources(database, Collections.singletonList(new DataNode("foo_ds", "foo_db", "foo_table")));
        when(storageUnits.get("foo_ds").getStorageType()).thenReturn(mysqlType);
        when(GenericSchemaBuilder.build(anyCollection(), eq(mysqlType), any(GenericSchemaBuilderMaterial.class))).thenReturn(loadedSchemas);
        listener.onInitialized(contextManager);
        assertNull(column.getDatetime());
        verifyNoInteractions(contextManager.getPersistServiceFacade(), contextManager.getMetaDataContextManager());
    }
    
    private static Stream<Arguments> unresolvedMetadataArguments() {
        return Stream.of(
                Arguments.of("missing-schema", Collections.emptyMap()),
                Arguments.of("missing-table", Collections.singletonMap("foo_db", schema(new ShardingSphereTable("bar_table", Collections.emptyList(), Collections.emptyList(),
                        Collections.emptyList())))),
                Arguments.of("missing-column", Collections.singletonMap("foo_db", schema(table(column("bar_column", Types.TIMESTAMP, true))))),
                Arguments.of("changed-jdbc-type", Collections.singletonMap("foo_db", schema(table(column("foo_dt", Types.INTEGER, false))))));
    }
    
}
