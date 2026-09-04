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

package org.apache.shardingsphere.single.distsql.handler.update;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.NoSuchTableException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.datasource.aggregate.AggregatedDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.table.TableMapperRuleAttribute;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.distsql.statement.rdl.UnloadSingleTableStatement;
import org.apache.shardingsphere.single.exception.SingleTableNotFoundException;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UnloadSingleTableExecutorTest {
    
    private final DatabaseType protocolDatabaseType = TypedSPILoader.getService(DatabaseType.class, "PostgreSQL");
    
    private final DatabaseType storageDatabaseType = TypedSPILoader.getService(DatabaseType.class, "MySQL");
    
    private final UnloadSingleTableExecutor executor = (UnloadSingleTableExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, UnloadSingleTableStatement.class);
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock
    private SingleRule rule;
    
    @Mock
    private TableMapperRuleAttribute tableMapperRuleAttribute;
    
    @Mock
    private DataNodeRuleAttribute dataNodeRuleAttribute;
    
    @Mock
    private DataSource dataSource;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
        executor.setRule(rule);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(protocolDatabaseType);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getStorageType()).thenReturn(storageDatabaseType);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class);
        when(resourceMetaData.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        when(database.getResourceMetaData()).thenReturn(resourceMetaData);
        when(database.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        Map<String, DataSource> aggregatedDataSources = new HashMap<>(3, 1F);
        aggregatedDataSources.put("foo_ds", dataSource);
        aggregatedDataSources.put("bar_ds", dataSource);
        aggregatedDataSources.put("readwrite_ds", dataSource);
        when(rule.getAttributes()).thenReturn(new RuleAttributes(
                tableMapperRuleAttribute, dataNodeRuleAttribute, new AggregatedDataSourceRuleAttribute(aggregatedDataSources)));
    }
    
    @AfterEach
    void assertStorageIsNotProbed() throws SQLException {
        verify(dataSource, never()).getConnection();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithFailureArguments")
    void assertCheckBeforeUpdateWithFailure(final String name, final Collection<String> allTables, final Collection<String> singleTables, final Collection<DataNode> dataNodes,
                                            final Collection<String> configuredTables, final Class<? extends RuntimeException> expectedException) {
        Map<String, Collection<DataNode>> tableDataNodes = new HashMap<>(1, 1F);
        tableDataNodes.put("foo_tbl", dataNodes);
        prepareCheckBeforeUpdateContext(allTables, singleTables, tableDataNodes, configuredTables);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithSuccessArguments")
    void assertCheckBeforeUpdate(final String name, final boolean schemaAvailable, final UnloadSingleTableStatement sqlStatement, final Collection<String> allTables,
                                 final Collection<String> singleTables, final Map<String, Collection<DataNode>> tableDataNodes, final Collection<String> configuredTables) {
        prepareCheckBeforeUpdateContext(allTables, singleTables, tableDataNodes, configuredTables);
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(schemaAvailable)) {
            assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
        }
    }
    
    @Test
    void assertCheckBeforeUpdateWithMissingDefaultSchema() {
        when(database.findDefaultSchema()).thenReturn(Optional.empty());
        assertThrows(NoSuchTableException.class, () -> executor.checkBeforeUpdate(new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl"))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildToBeAlteredRuleConfigurationArguments")
    void assertBuildToBeAlteredRuleConfiguration(final String name, final boolean schemaAvailable, final Collection<String> currentTables,
                                                 final Map<String, Collection<DataNode>> tableDataNodes, final UnloadSingleTableStatement sqlStatement,
                                                 final Collection<String> expectedTables) {
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(currentTables), null));
        when(dataNodeRuleAttribute.getDataNodesByTableName(anyString())).thenAnswer(invocation -> tableDataNodes.getOrDefault(invocation.getArgument(0), Collections.emptyList()));
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockDatabaseTypeRegistry(schemaAvailable)) {
            SingleRuleConfiguration actual = executor.buildToBeAlteredRuleConfiguration(sqlStatement);
            assertThat(new HashSet<>(actual.getTables()), is(new HashSet<>(expectedTables)));
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildToBeDroppedRuleConfigurationArguments")
    void assertBuildToBeDroppedRuleConfiguration(final String name, final Collection<String> currentTables, final Collection<String> toBeAlteredTables,
                                                 final boolean expectedNull, final Collection<String> expectedTables) {
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(currentTables), null));
        SingleRuleConfiguration toBeAltered = new SingleRuleConfiguration(new LinkedList<>(toBeAlteredTables), null);
        SingleRuleConfiguration actual = executor.buildToBeDroppedRuleConfiguration(toBeAltered);
        if (expectedNull) {
            assertNull(actual);
        } else {
            assertNotNull(actual);
            assertThat(new HashSet<>(actual.getTables()), is(new HashSet<>(expectedTables)));
        }
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(SingleRule.class));
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockDatabaseTypeRegistry(final boolean storageSchemaAvailable) {
        DialectDatabaseMetaData protocolMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(protocolMetaData.getSchemaOption().isSchemaAvailable()).thenReturn(true);
        DialectDatabaseMetaData storageMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(storageMetaData.getSchemaOption().isSchemaAvailable()).thenReturn(storageSchemaAvailable);
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> when(mock.getDialectDatabaseMetaData())
                .thenReturn(storageDatabaseType == context.arguments().get(0) ? storageMetaData : protocolMetaData));
    }
    
    private void prepareCheckBeforeUpdateContext(final Collection<String> allTables, final Collection<String> singleTables,
                                                 final Map<String, Collection<DataNode>> tableDataNodes, final Collection<String> configuredTables) {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.getAllTables()).thenReturn(
                allTables.stream().map(each -> new ShardingSphereTable(each, Collections.emptyList(), Collections.emptyList(), Collections.emptyList())).collect(Collectors.toList()));
        when(database.findDefaultSchema()).thenReturn(Optional.of(schema));
        when(tableMapperRuleAttribute.getLogicTableNames()).thenReturn(singleTables);
        when(dataNodeRuleAttribute.getDataNodesByTableName(anyString())).thenAnswer(invocation -> tableDataNodes.getOrDefault(invocation.getArgument(0), Collections.emptyList()));
        when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(configuredTables), null));
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithFailureArguments() {
        return Stream.of(
                Arguments.of("missing table throws NoSuchTableException", Collections.emptyList(), Collections.singleton("foo_tbl"), Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.singleton("foo_ds.foo_tbl"), NoSuchTableException.class),
                Arguments.of("non single table throws SingleTableNotFoundException", Collections.singleton("foo_tbl"), Collections.emptyList(), Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.singleton("foo_ds.foo_tbl"), SingleTableNotFoundException.class),
                Arguments.of("missing data node throws MissingRequiredRuleException", Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"), Collections.emptyList(),
                        Collections.singleton("foo_ds.foo_tbl"), MissingRequiredRuleException.class),
                Arguments.of("missing rule config throws MissingRequiredRuleException", Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"),
                        Collections.singleton(new DataNode("foo_ds.foo_tbl")),
                        Collections.emptyList(), MissingRequiredRuleException.class));
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithSuccessArguments() {
        Map<String, Collection<DataNode>> singleTableNodes = new HashMap<>(1, 1F);
        singleTableNodes.put("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        Map<String, Collection<DataNode>> multipleTableNodes = new HashMap<>(2, 1F);
        multipleTableNodes.put("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        multipleTableNodes.put("bar_tbl", Collections.singleton(new DataNode("foo_ds.foo_schema.bar_tbl")));
        Map<String, Collection<DataNode>> schemaTableNodes = new HashMap<>(1, 1F);
        schemaTableNodes.put("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_schema.foo_tbl")));
        return Stream.of(
                Arguments.of("unload all tables bypasses table checks", false, new UnloadSingleTableStatement(true, Collections.emptyList()),
                        Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(), Collections.emptyList()),
                Arguments.of("valid single table passes checks", false, new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl")),
                        Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"), singleTableNodes, Collections.singleton("foo_ds.foo_tbl")),
                Arguments.of("valid multiple tables pass checks", false, new UnloadSingleTableStatement(false, Arrays.asList("foo_tbl", "bar_tbl")),
                        Arrays.asList("foo_tbl", "bar_tbl"), Arrays.asList("foo_tbl", "bar_tbl"), multipleTableNodes, Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl")),
                Arguments.of("valid schema table passes checks", true, new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl")),
                        Collections.singleton("foo_tbl"), Collections.singleton("foo_tbl"), schemaTableNodes, Collections.singleton("foo_ds.foo_schema.foo_tbl")));
    }
    
    private static Stream<Arguments> assertBuildToBeAlteredRuleConfigurationArguments() {
        Map<String, Collection<DataNode>> twoSegmentTableNodes = Collections.singletonMap("foo_tbl", Collections.singleton(new DataNode("foo_ds.foo_tbl")));
        Map<String, Collection<DataNode>> schemaTableNodes = Collections.singletonMap("bar_tbl", Collections.singleton(new DataNode("foo_ds.foo_schema.bar_tbl")));
        Map<String, Collection<DataNode>> dottedTableNodes = Collections.singletonMap(
                "foo.bar", Arrays.asList(new DataNode("readwrite_ds", "foo_schema", "foo.bar"), new DataNode("bar_ds", "foo_schema", "foo.bar")));
        Map<String, Collection<DataNode>> suffixTableNodes = Collections.singletonMap("bar", Collections.singleton(new DataNode("foo_ds", "foo_schema", "bar")));
        return Stream.of(
                Arguments.of("unload all tables keeps altered config empty", false, Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), Collections.emptyMap(),
                        new UnloadSingleTableStatement(true, Collections.emptyList()), Collections.emptyList()),
                Arguments.of("unload one table removes matching two-segment node", false, Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), twoSegmentTableNodes,
                        new UnloadSingleTableStatement(false, Collections.singletonList("foo_tbl")), Collections.singleton("foo_ds.bar_tbl")),
                Arguments.of("unload one table removes matching three-segment node", true, Arrays.asList("foo_ds.foo_schema.bar_tbl", "foo_ds.foo_tbl"), schemaTableNodes,
                        new UnloadSingleTableStatement(false, Collections.singletonList("bar_tbl")), Collections.singleton("foo_ds.foo_tbl")),
                Arguments.of("unload dotted table removes all exact nodes", false, Arrays.asList("readwrite_ds.foo.bar", "bar_ds.foo.bar", "foo_ds.bar"), dottedTableNodes,
                        new UnloadSingleTableStatement(false, Collections.singletonList("foo.bar")), Collections.singleton("foo_ds.bar")),
                Arguments.of("unload suffix table preserves dotted table", false, Arrays.asList("foo_ds.bar", "foo_ds.foo.bar"), suffixTableNodes,
                        new UnloadSingleTableStatement(false, Collections.singletonList("bar")), Collections.singleton("foo_ds.foo.bar")));
    }
    
    private static Stream<Arguments> assertBuildToBeDroppedRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("empty altered config drops current rule config", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), Collections.emptyList(), false,
                        Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl")),
                Arguments.of("non-empty altered config does not drop rule", Collections.singleton("foo_ds.foo_tbl"), Collections.singleton("foo_ds.foo_tbl"), true, Collections.emptyList()),
                Arguments.of("multi-table altered config does not drop rule", Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), Arrays.asList("foo_ds.foo_tbl", "foo_ds.bar_tbl"), true,
                        Collections.emptyList()));
    }
}
