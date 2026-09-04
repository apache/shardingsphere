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

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.metadata.loader.PipelineSchemaUtils;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationSourceTargetEntry;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.segment.MigrationSourceTargetSegment;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.MigrateTableStatement;
import org.apache.shardingsphere.database.connector.core.metadata.database.enums.QuoteCharacter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicy;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierNormalizeEngine;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierScope;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrateTableExecutorTest {
    
    private static final String SOURCE_DATABASE_NAME = "FOO_SOURCE";
    
    private static final String TARGET_DATABASE_NAME = "foo_target";
    
    private final MigrateTableExecutor executor = new MigrateTableExecutor();
    
    @Mock
    private ShardingSphereDatabase database;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ContextManager contextManager;
    
    @Mock
    private MigrationJobAPI jobAPI;
    
    @Mock
    private DatabaseType schemaCapableDatabaseType;
    
    @Mock
    private DatabaseType schemaLessDatabaseType;
    
    @Mock
    private DataSourcePoolProperties sourceProps;
    
    @Captor
    private ArgumentCaptor<Collection<MigrationSourceTargetEntry>> entriesCaptor;
    
    @BeforeEach
    void setUp() {
        executor.setDatabase(database);
        when(database.getName()).thenReturn(TARGET_DATABASE_NAME);
        when(contextManager.getMetaDataContexts().getMetaData().containsDatabase(TARGET_DATABASE_NAME)).thenReturn(true);
    }
    
    @Test
    void assertExecuteUpdateUsesSourceDatabaseTypeForDefaultSchema() {
        mockSourceProps();
        try (
                MockedConstruction<PipelineDataSourcePersistService> ignored = mockConstruction(PipelineDataSourcePersistService.class,
                        (mock, context) -> when(mock.load(any(PipelineContextKey.class), eq("MIGRATION"))).thenReturn(Collections.singletonMap(SOURCE_DATABASE_NAME, sourceProps)));
                MockedConstruction<StandardPipelineDataSourceConfiguration> ignoredConfig = mockConstruction(StandardPipelineDataSourceConfiguration.class,
                        (mock, context) -> when(mock.getDatabaseType()).thenReturn(schemaCapableDatabaseType));
                MockedConstruction<DatabaseTypeRegistry> ignoredRegistry = mockDatabaseTypeRegistries();
                MockedStatic<TypedSPILoader> spiLoader = mockStatic(TypedSPILoader.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<PipelineSchemaUtils> schemaUtils = mockStatic(PipelineSchemaUtils.class)) {
            spiLoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION")).thenReturn(jobAPI);
            schemaUtils.when(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class))).thenReturn("CaseSchema");
            executor.executeUpdate(createStatement(null), contextManager);
            verify(jobAPI).schedule(any(PipelineContextKey.class), entriesCaptor.capture(), eq(TARGET_DATABASE_NAME));
            assertThat(entriesCaptor.getValue().iterator().next().getSource().getSchemaName(), is("CaseSchema"));
            verify(database, never()).getProtocolType();
        }
    }
    
    @Test
    void assertExecuteUpdateDoesNotLoadDefaultSchemaForSchemaLessSource() {
        mockSourceProps();
        try (
                MockedConstruction<PipelineDataSourcePersistService> ignored = mockConstruction(PipelineDataSourcePersistService.class,
                        (mock, context) -> when(mock.load(any(PipelineContextKey.class), eq("MIGRATION"))).thenReturn(Collections.singletonMap(SOURCE_DATABASE_NAME, sourceProps)));
                MockedConstruction<StandardPipelineDataSourceConfiguration> ignoredConfig = mockConstruction(StandardPipelineDataSourceConfiguration.class,
                        (mock, context) -> when(mock.getDatabaseType()).thenReturn(schemaLessDatabaseType));
                MockedConstruction<DatabaseTypeRegistry> ignoredRegistry = mockDatabaseTypeRegistries();
                MockedStatic<TypedSPILoader> spiLoader = mockStatic(TypedSPILoader.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<PipelineSchemaUtils> schemaUtils = mockStatic(PipelineSchemaUtils.class)) {
            spiLoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION")).thenReturn(jobAPI);
            schemaUtils.when(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class))).thenReturn("public");
            executor.executeUpdate(createStatement(null), contextManager);
            verify(jobAPI).schedule(any(PipelineContextKey.class), entriesCaptor.capture(), eq(TARGET_DATABASE_NAME));
            MigrationSourceTargetEntry actual = entriesCaptor.getValue().iterator().next();
            assertNull(actual.getSource().getSchemaName());
            assertThat(actual.getSource().getTableName(), is("FOO_TBL"));
            schemaUtils.verify(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class)), never());
            verify(database, never()).getProtocolType();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExplicitSourceIdentifiers")
    void assertExecuteUpdateCanonicalizesExplicitSourceIdentifiers(final String name, final String sourceSchemaName, final String expectedSchemaName,
                                                                   final IdentifierValue sourceTableIdentifier, final String expectedTableName) {
        IdentifierCasePolicy schemaIdentifierCasePolicy = IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.SCHEMA);
        IdentifierCasePolicy tableIdentifierCasePolicy = IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.TABLE);
        try (
                MockedConstruction<PipelineDataSourcePersistService> ignored = mockConstruction(PipelineDataSourcePersistService.class,
                        (mock, context) -> when(mock.load(any(PipelineContextKey.class), eq("MIGRATION"))).thenReturn(Collections.singletonMap(SOURCE_DATABASE_NAME, sourceProps)));
                MockedConstruction<YamlDataSourceConfigurationSwapper> ignoredSwapper = mockConstruction(YamlDataSourceConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToMap(sourceProps)).thenReturn(Collections.emptyMap()));
                MockedConstruction<StandardPipelineDataSourceConfiguration> ignoredConfig = mockConstruction(StandardPipelineDataSourceConfiguration.class,
                        (mock, context) -> when(mock.getDatabaseType()).thenReturn(schemaCapableDatabaseType));
                MockedConstruction<DatabaseTypeRegistry> ignoredRegistry = mockDatabaseTypeRegistries();
                MockedStatic<TypedSPILoader> spiLoader = mockStatic(TypedSPILoader.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<PipelineSchemaUtils> schemaUtils = mockStatic(PipelineSchemaUtils.class);
                MockedStatic<IdentifierNormalizeEngine> normalizeEngine = mockStatic(IdentifierNormalizeEngine.class, Answers.CALLS_REAL_METHODS)) {
            spiLoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION")).thenReturn(jobAPI);
            normalizeEngine.when(() -> IdentifierNormalizeEngine.resolvePolicy(schemaCapableDatabaseType, null, IdentifierScope.SCHEMA)).thenReturn(schemaIdentifierCasePolicy);
            normalizeEngine.when(() -> IdentifierNormalizeEngine.resolvePolicy(schemaCapableDatabaseType, null, IdentifierScope.TABLE)).thenReturn(tableIdentifierCasePolicy);
            executor.executeUpdate(createStatement(sourceSchemaName, sourceTableIdentifier), contextManager);
            verify(jobAPI).schedule(any(PipelineContextKey.class), entriesCaptor.capture(), eq(TARGET_DATABASE_NAME));
            MigrationSourceTargetEntry actual = entriesCaptor.getValue().iterator().next();
            assertThat(actual.getSource().getDataSourceName(), is(SOURCE_DATABASE_NAME));
            assertThat(actual.getSource().getSchemaName(), is(expectedSchemaName));
            assertThat(actual.getSource().getTableName(), is(expectedTableName));
            assertThat(actual.getTargetTableName(), is("foo_tbl"));
            schemaUtils.verify(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class)), never());
        }
    }
    
    private static Stream<Arguments> provideExplicitSourceIdentifiers() {
        return Stream.of(
                Arguments.of("lower-case schema", "foo_schema", "foo_schema", new IdentifierValue("FOO_TBL", QuoteCharacter.BACK_QUOTE), "FOO_TBL"),
                Arguments.of("unquoted upper-case schema", "FOO_SCHEMA", "foo_schema", new IdentifierValue("FOO_TBL", QuoteCharacter.BACK_QUOTE), "FOO_TBL"),
                Arguments.of("quoted upper-case schema", "\"FOO_SCHEMA\"", "FOO_SCHEMA", new IdentifierValue("FOO_TBL", QuoteCharacter.BACK_QUOTE), "FOO_TBL"),
                Arguments.of("quoted mixed-case schema", "\"CaseSchema\"", "CaseSchema", new IdentifierValue("FOO_TBL", QuoteCharacter.BACK_QUOTE), "FOO_TBL"),
                Arguments.of("unquoted upper-case table", "foo_schema", "foo_schema", new IdentifierValue("FOO_TBL", QuoteCharacter.NONE), "foo_tbl"));
    }
    
    @Test
    void assertExecuteUpdateDefersMissingSourceValidation() {
        try (
                MockedConstruction<PipelineDataSourcePersistService> ignored = mockConstruction(PipelineDataSourcePersistService.class,
                        (mock, context) -> when(mock.load(any(PipelineContextKey.class), eq("MIGRATION"))).thenReturn(Collections.emptyMap()));
                MockedStatic<TypedSPILoader> spiLoader = mockStatic(TypedSPILoader.class, Answers.CALLS_REAL_METHODS);
                MockedStatic<PipelineSchemaUtils> schemaUtils = mockStatic(PipelineSchemaUtils.class)) {
            spiLoader.when(() -> TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION")).thenReturn(jobAPI);
            executor.executeUpdate(createStatement(null), contextManager);
            verify(jobAPI).schedule(any(PipelineContextKey.class), entriesCaptor.capture(), eq(TARGET_DATABASE_NAME));
            assertNull(entriesCaptor.getValue().iterator().next().getSource().getSchemaName());
            schemaUtils.verify(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class)), never());
            verify(database, never()).getProtocolType();
        }
    }
    
    private MigrateTableStatement createStatement(final String sourceSchemaName) {
        return createStatement(sourceSchemaName, new IdentifierValue("FOO_TBL", QuoteCharacter.BACK_QUOTE));
    }
    
    private MigrateTableStatement createStatement(final String sourceSchemaName, final IdentifierValue sourceTableIdentifier) {
        MigrationSourceTargetSegment entry = new MigrationSourceTargetSegment(new IdentifierValue(SOURCE_DATABASE_NAME, QuoteCharacter.BACK_QUOTE),
                null == sourceSchemaName ? null : new IdentifierValue(sourceSchemaName), sourceTableIdentifier,
                new IdentifierValue("foo_tbl", QuoteCharacter.NONE));
        return new MigrateTableStatement(null, Collections.singleton(entry));
    }
    
    private void mockSourceProps() {
        when(sourceProps.getAllStandardProperties()).thenReturn(Collections.emptyMap());
        when(sourceProps.getPoolClassName()).thenReturn("com.zaxxer.hikari.HikariDataSource");
    }
    
    private MockedConstruction<DatabaseTypeRegistry> mockDatabaseTypeRegistries() {
        return mockConstruction(DatabaseTypeRegistry.class, (mock, context) -> {
            DialectDatabaseMetaData databaseMetaData = mock(DialectDatabaseMetaData.class, Answers.RETURNS_DEEP_STUBS);
            when(databaseMetaData.getSchemaOption().isSchemaAvailable()).thenReturn(schemaCapableDatabaseType == context.arguments().get(0));
            when(mock.getDialectDatabaseMetaData()).thenReturn(databaseMetaData);
        });
    }
}
