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
import org.apache.shardingsphere.database.exception.core.exception.syntax.table.TableExistsException;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.DatabaseRuleDefinitionExecutor;
import org.apache.shardingsphere.infra.exception.kernel.metadata.TableNotFoundException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.datanode.InvalidDataNodeFormatException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.InvalidStorageUnitStatusException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.PhysicalDataSourceAggregator;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.datanode.SingleTableDataNodeLoader;
import org.apache.shardingsphere.single.distsql.segment.SingleTableSegment;
import org.apache.shardingsphere.single.distsql.statement.rdl.LoadSingleTableStatement;
import org.apache.shardingsphere.single.rule.SingleRule;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({SingleTableDataNodeLoader.class, PhysicalDataSourceAggregator.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class LoadSingleTableExecutorTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private final LoadSingleTableExecutor executor = (LoadSingleTableExecutor) TypedSPILoader.getService(DatabaseRuleDefinitionExecutor.class, LoadSingleTableStatement.class);
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ShardingSphereDatabase database;
    
    @BeforeEach
    void setUp() {
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        executor.setDatabase(database);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithPreValidationFailureArguments")
    void assertCheckBeforeUpdateWithPreValidationFailure(final String name, final boolean schemaSupported, final SingleTableSegment tableSegment,
                                                         final boolean tableExists, final Class<? extends RuntimeException> expectedException) {
        prepareStorageUnits();
        prepareSchema(tableExists, schemaSupported ? "foo_schema" : "foo_db");
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Collections.singleton(tableSegment));
        if (schemaSupported) {
            try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaSupportedDatabaseTypeRegistry()) {
                assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
            }
        } else {
            assertThrows(expectedException, () -> executor.checkBeforeUpdate(sqlStatement));
        }
    }
    
    @Test
    void assertCheckBeforeUpdateWithEmptyStorageUnits() {
        prepareSchema(false, "foo_db");
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.emptyMap());
        assertThrows(EmptyStorageUnitException.class, () -> executor.checkBeforeUpdate(new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("foo_ds", "foo_tbl")))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCheckBeforeUpdateWithActualTableValidationFailureArguments")
    void assertCheckBeforeUpdateWithActualTableValidationFailure(final String name, final Map<String, DataSource> aggregatedDataSources,
                                                                 final Map<String, Collection<String>> schemaTableNames, final Class<? extends RuntimeException> expectedException) {
        prepareActualTableValidationScenario(aggregatedDataSources, schemaTableNames);
        assertThrows(expectedException, () -> executor.checkBeforeUpdate(new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("foo_ds", "foo_tbl")))));
    }
    
    @Test
    void assertCheckBeforeUpdateWithSchemaSupportedDatabaseType() {
        prepareActualTableValidationScenario(Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.singletonMap("foo_schema", Collections.singleton("foo_tbl")));
        LoadSingleTableStatement sqlStatement = new LoadSingleTableStatement(Arrays.asList(new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), new SingleTableSegment("*", "*")));
        try (MockedConstruction<DatabaseTypeRegistry> ignored = mockSchemaSupportedDatabaseTypeRegistry()) {
            prepareSchema(false, "foo_schema");
            assertDoesNotThrow(() -> executor.checkBeforeUpdate(sqlStatement));
        }
    }
    
    @Test
    void assertCheckBeforeUpdateWithAllTablesPattern() {
        prepareSchema(false, "foo_db");
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("*", "*")))));
    }
    
    @Test
    void assertCheckBeforeUpdateWithAllSchemaTablesPattern() {
        prepareSchema(false, "foo_db");
        assertDoesNotThrow(() -> executor.checkBeforeUpdate(new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("*", "*", "*")))));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertBuildToBeCreatedRuleConfigurationArguments")
    void assertBuildToBeCreatedRuleConfiguration(final String name, final Collection<String> currentTables, final LoadSingleTableStatement sqlStatement, final Collection<String> expectedTables) {
        if (null != currentTables) {
            SingleRule rule = mock(SingleRule.class);
            when(rule.getConfiguration()).thenReturn(new SingleRuleConfiguration(new LinkedList<>(currentTables), null));
            executor.setRule(rule);
        }
        assertThat(new HashSet<>(executor.buildToBeCreatedRuleConfiguration(sqlStatement).getTables()), is(new HashSet<>(expectedTables)));
    }
    
    @Test
    void assertGetRuleClass() {
        assertThat(executor.getRuleClass(), is(SingleRule.class));
    }
    
    private void prepareStorageUnits() {
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
    }
    
    private void prepareSchema(final boolean tableExists, final String schemaName) {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        when(schema.containsTable("foo_tbl")).thenReturn(tableExists);
        when(database.getSchema(schemaName)).thenReturn(schema);
    }
    
    private void prepareActualTableValidationScenario(final Map<String, DataSource> aggregatedDataSources, final Map<String, Collection<String>> schemaTableNames) {
        prepareStorageUnits();
        prepareSchema(false, "foo_db");
        when(PhysicalDataSourceAggregator.getAggregatedDataSources(any(), any())).thenReturn(aggregatedDataSources);
        if (aggregatedDataSources.containsKey("foo_ds")) {
            when(SingleTableDataNodeLoader.loadSchemaTableNames(eq("foo_db"), any(), any(), eq("foo_ds"), eq(Collections.emptyList()))).thenReturn(schemaTableNames);
        }
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockSchemaSupportedDatabaseTypeRegistry() {
        DialectDatabaseMetaData dialectDatabaseMetaData = mock(DialectDatabaseMetaData.class, RETURNS_DEEP_STUBS);
        when(dialectDatabaseMetaData.getSchemaOption().getDefaultSchema()).thenReturn(Optional.of("foo_schema"));
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
            when(mock.getDefaultSchemaName("foo_db")).thenReturn("foo_schema");
            when(mock.getDialectDatabaseMetaData()).thenReturn(dialectDatabaseMetaData);
        });
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithPreValidationFailureArguments() {
        return Stream.of(
                Arguments.of("schema unsupported rejects schema name", false, new SingleTableSegment("foo_ds", "foo_schema", "foo_tbl"), false, InvalidDataNodeFormatException.class),
                Arguments.of("schema required rejects missing schema", true, new SingleTableSegment("foo_ds", "foo_tbl"), false, InvalidDataNodeFormatException.class),
                Arguments.of("logic table existence is rejected", false, new SingleTableSegment("foo_ds", "foo_tbl"), true, TableExistsException.class));
    }
    
    private static Stream<Arguments> assertCheckBeforeUpdateWithActualTableValidationFailureArguments() {
        return Stream.of(
                Arguments.of("invalid storage unit is rejected", Collections.<String, DataSource>emptyMap(), Collections.emptyMap(), InvalidStorageUnitStatusException.class),
                Arguments.of("empty actual table nodes are rejected", Collections.singletonMap("foo_ds", new MockedDataSource()), Collections.emptyMap(), TableNotFoundException.class),
                Arguments.of("missing table in actual table nodes is rejected", Collections.singletonMap("foo_ds", new MockedDataSource()),
                        Collections.singletonMap("foo_db", Collections.singleton("bar_tbl")), TableNotFoundException.class));
    }
    
    private static Stream<Arguments> assertBuildToBeCreatedRuleConfigurationArguments() {
        return Stream.of(
                Arguments.of("without current rule keeps all requested tables", null,
                        new LoadSingleTableStatement(Arrays.asList(new SingleTableSegment("foo_ds", "foo_tbl"), new SingleTableSegment("foo_ds", "bar_tbl"))),
                        Arrays.asList("foo_ds" + "." + "foo_tbl", "foo_ds" + "." + "bar_tbl")),
                Arguments.of("with current rule skips duplicated table", Collections.singletonList("foo_ds" + "." + "foo_tbl"),
                        new LoadSingleTableStatement(Arrays.asList(new SingleTableSegment("foo_ds", "foo_tbl"), new SingleTableSegment("foo_ds", "bar_tbl"))),
                        Arrays.asList("foo_ds" + "." + "foo_tbl", "foo_ds" + "." + "bar_tbl")),
                Arguments.of("with current rule keeps existing set when all requested exist", Collections.singletonList("foo_ds" + "." + "foo_tbl"),
                        new LoadSingleTableStatement(Collections.singletonList(new SingleTableSegment("foo_ds", "foo_tbl"))),
                        Collections.singletonList("foo_ds" + "." + "foo_tbl")));
    }
}
