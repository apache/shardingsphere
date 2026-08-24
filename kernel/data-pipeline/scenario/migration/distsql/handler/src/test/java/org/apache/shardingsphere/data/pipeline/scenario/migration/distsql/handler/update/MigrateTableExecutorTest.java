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
    
    private static final String SOURCE_DATABASE_NAME = "foo_source";
    
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
            assertNull(entriesCaptor.getValue().iterator().next().getSource().getSchemaName());
            schemaUtils.verify(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class)), never());
            verify(database, never()).getProtocolType();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExplicitSchemaNames")
    void assertExecuteUpdateCanonicalizesExplicitSchema(final String name, final String sourceSchemaName, final String expectedSchemaName) {
        IdentifierCasePolicy identifierCasePolicy = IdentifierCasePolicyFactory.newLowerCasePolicySet().getPolicy(IdentifierScope.SCHEMA);
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
            normalizeEngine.when(() -> IdentifierNormalizeEngine.resolvePolicy(schemaCapableDatabaseType, null, IdentifierScope.SCHEMA)).thenReturn(identifierCasePolicy);
            executor.executeUpdate(createStatement(sourceSchemaName), contextManager);
            verify(jobAPI).schedule(any(PipelineContextKey.class), entriesCaptor.capture(), eq(TARGET_DATABASE_NAME));
            assertThat(entriesCaptor.getValue().iterator().next().getSource().getSchemaName(), is(expectedSchemaName));
            schemaUtils.verify(() -> PipelineSchemaUtils.getDefaultSchema(any(StandardPipelineDataSourceConfiguration.class)), never());
        }
    }
    
    private static Stream<Arguments> provideExplicitSchemaNames() {
        return Stream.of(
                Arguments.of("lower-case schema", "foo_schema", "foo_schema"),
                Arguments.of("unquoted upper-case schema", "FOO_SCHEMA", "foo_schema"),
                Arguments.of("quoted upper-case schema", "\"FOO_SCHEMA\"", "FOO_SCHEMA"),
                Arguments.of("quoted mixed-case schema", "\"CaseSchema\"", "CaseSchema"));
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
        MigrationSourceTargetSegment entry = new MigrationSourceTargetSegment(SOURCE_DATABASE_NAME, sourceSchemaName, "foo_tbl", "foo_tbl");
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
