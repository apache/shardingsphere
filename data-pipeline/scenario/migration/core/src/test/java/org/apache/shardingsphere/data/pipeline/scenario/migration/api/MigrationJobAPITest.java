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

package org.apache.shardingsphere.data.pipeline.scenario.migration.api;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.sql.PipelinePrepareSQLBuilder;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobId;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.config.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.yaml.swapper.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.database.exception.core.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MigrationJobAPITest {
    
    private final PipelineContextKey contextKey = new PipelineContextKey(InstanceType.PROXY);
    
    @Mock
    private PipelineJobManager jobManager;
    
    @Mock
    private PipelineJobConfigurationManager jobConfigManager;
    
    @Mock
    private PipelineDataSourcePersistService dataSourcePersistService;
    
    private MigrationJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        jobAPI = new MigrationJobAPI();
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("jobManager"), jobAPI, jobManager);
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("dataSourcePersistService"), jobAPI, dataSourcePersistService);
    }
    
    @Test
    void assertScheduleThrowsForConflictingSourceTableIdentity() {
        Collection<MigrationSourceTargetEntry> entries = Arrays.asList(
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target"),
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "bar_schema", "FOO_TBL"), "bar_target"));
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.schedule(contextKey, entries, "foo_db"));
        assertThat(actual.getMessage(), is("There is invalid parameter value. More than one source table with the same table name for foo_ds"));
        verify(dataSourcePersistService, never()).load(any(PipelineContextKey.class), anyString());
        verify(jobManager, never()).start(any());
    }
    
    @Test
    void assertScheduleAcceptsExactDuplicateSourceTargetEntry() {
        MigrationSourceTargetEntry entry = new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target");
        IllegalStateException expected = new IllegalStateException("reached configuration");
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenThrow(expected);
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> jobAPI.schedule(contextKey, Arrays.asList(entry, entry), "foo_db"));
        assertThat(actual, is(expected));
        verify(dataSourcePersistService).load(contextKey, "MIGRATION");
        verify(jobManager, never()).start(any());
    }
    
    @Test
    void assertScheduleAcceptsSameTableNameFromDifferentDataSources() {
        Collection<MigrationSourceTargetEntry> entries = Arrays.asList(
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "foo_target"),
                new MigrationSourceTargetEntry(new DataNode("bar_ds", "bar_schema", "FOO_TBL"), "bar_target"));
        IllegalStateException expected = new IllegalStateException("reached configuration");
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenThrow(expected);
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> jobAPI.schedule(contextKey, entries, "foo_db"));
        assertThat(actual, is(expected));
        verify(dataSourcePersistService).load(contextKey, "MIGRATION");
        verify(jobManager, never()).start(any());
    }
    
    @Test
    void assertScheduleBuildsJobConfiguration() {
        Map<String, DataSourcePoolProperties> sources = new LinkedHashMap<>(2, 1F);
        sources.put("bar_ds", createDataSourcePoolProperties("jdbc:h2:mem:bar_db"));
        sources.put("foo_ds", createDataSourcePoolProperties("jdbc:h2:mem:foo_db"));
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenReturn(sources);
        ContextManager contextManager = mock(ContextManager.class, Answers.RETURNS_DEEP_STUBS);
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabase("foo_db");
        when(database.getName()).thenReturn("foo_db");
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSourcePoolProperties()).thenReturn(createDataSourcePoolProperties("jdbc:h2:mem:target_db"));
        when(database.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("target_ds", storageUnit));
        when(database.getRuleMetaData().getConfigurations()).thenReturn(Collections.singleton(new SingleRuleConfiguration(Collections.singletonList("target_ds.foo_target"), "target_ds")));
        MigrationSourceTargetEntry entry = new MigrationSourceTargetEntry(new DataNode("bar_ds", "bar_schema", "bar_tbl"), "foo_target");
        Collection<MigrationSourceTargetEntry> entries = Arrays.asList(entry,
                new MigrationSourceTargetEntry(new DataNode("foo_ds", (String) null, "foo_tbl_item"), "foo_item_target"),
                new MigrationSourceTargetEntry(new DataNode("foo_ds", "foo_schema", "foo_tbl"), "bar_target"), entry);
        try (MockedStatic<PipelineContextManager> ignored = mockStatic(PipelineContextManager.class)) {
            when(PipelineContextManager.getProxyContext()).thenReturn(contextManager);
            String actualJobId = jobAPI.schedule(contextKey, entries, "foo_db");
            ArgumentCaptor<MigrationJobConfiguration> jobConfigCaptor = ArgumentCaptor.forClass(MigrationJobConfiguration.class);
            verify(jobManager).start(jobConfigCaptor.capture());
            MigrationJobConfiguration actual = jobConfigCaptor.getValue();
            List<String> expectedShardingDataNodes = Arrays.asList("foo_target:bar_ds.bar_schema.bar_tbl", "bar_target:foo_ds.foo_schema.foo_tbl|foo_item_target:foo_ds.foo_tbl_item");
            assertThat(actualJobId, is(PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, expectedShardingDataNodes))));
            assertThat(actual.getJobId(), is(actualJobId));
            assertThat(actual.getTargetDatabaseName(), is("foo_db"));
            assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
            assertThat(actual.getTargetDatabaseType().getType(), is("H2"));
            assertThat(actual.getTargetTableNames(), is(Arrays.asList("bar_target", "foo_item_target", "foo_target")));
            assertThat(actual.getSources().keySet(), contains("foo_ds", "bar_ds"));
            assertThat(actual.getTargetTableSchemaMap().get("bar_target"), is("foo_schema"));
            assertNull(actual.getTargetTableSchemaMap().get("foo_item_target"));
            assertThat(actual.getTargetTableSchemaMap().get("foo_target"), is("bar_schema"));
            assertThat(actual.getConcurrency(), is(3));
            assertThat(actual.getRetryTimes(), is(3));
            YamlMigrationJobConfiguration actualYaml = new YamlMigrationJobConfigurationSwapper().swapToYamlConfiguration(actual);
            assertThat(actualYaml.getSources().get("foo_ds").getType(), is("JDBC"));
            Map<String, Object> expectedDataSourceProps = new LinkedHashMap<>(5, 1F);
            expectedDataSourceProps.put("url", "jdbc:h2:mem:foo_db");
            expectedDataSourceProps.put("username", "foo_user");
            expectedDataSourceProps.put("password", "foo_password");
            expectedDataSourceProps.put("minPoolSize", 5);
            expectedDataSourceProps.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
            assertThat(YamlEngine.unmarshal(actualYaml.getSources().get("foo_ds").getParameter(), Map.class), is(expectedDataSourceProps));
            expectedDataSourceProps.put("url", "jdbc:h2:mem:bar_db");
            assertThat(YamlEngine.unmarshal(actualYaml.getSources().get("bar_ds").getParameter(), Map.class), is(expectedDataSourceProps));
            assertThat(actualYaml.getTarget().getType(), is("ShardingSphereJDBC"));
            YamlRootConfiguration actualTarget = YamlEngine.unmarshal(actualYaml.getTarget().getParameter(), YamlRootConfiguration.class, true);
            assertThat(actualTarget.getDatabaseName(), is("foo_db"));
            expectedDataSourceProps.put("url", "jdbc:h2:mem:target_db");
            assertThat(actualTarget.getDataSources(), is(Collections.singletonMap("target_ds", expectedDataSourceProps)));
            assertThat(actualYaml.getTablesFirstDataNodes(), is("bar_target:foo_ds.foo_schema.foo_tbl|foo_item_target:foo_ds.foo_tbl_item|foo_target:bar_ds.bar_schema.bar_tbl"));
            assertThat(actualYaml.getJobShardingDataNodes(), is(expectedShardingDataNodes));
        }
    }
    
    private static DataSourcePoolProperties createDataSourcePoolProperties(final String url) {
        Map<String, Object> props = new LinkedHashMap<>(4, 1F);
        props.put("url", url);
        props.put("username", "foo_user");
        props.put("password", "foo_password");
        props.put("minPoolSize", 5);
        return new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideInvalidSources")
    void assertScheduleWithInvalidSources(final String name, final Collection<MigrationSourceTargetEntry> entries, final Map<String, DataSourcePoolProperties> sources, final String expectedMessage) {
        when(dataSourcePersistService.load(contextKey, "MIGRATION")).thenReturn(sources);
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.schedule(contextKey, entries, "foo_db"));
        assertThat(actual.getMessage(), is(expectedMessage));
        verify(jobManager, never()).start(any());
    }
    
    private static Stream<Arguments> provideInvalidSources() {
        Map<String, DataSourcePoolProperties> mixedSources = new LinkedHashMap<>(2, 1F);
        mixedSources.put("foo_ds", createDataSourcePoolProperties("jdbc:h2:mem:foo_db"));
        mixedSources.put("bar_ds", createDataSourcePoolProperties("jdbc:mysql://localhost:3306/bar_db"));
        return Stream.of(
                Arguments.of("missing source storage unit", Collections.singletonList(new MigrationSourceTargetEntry(new DataNode("foo_ds.foo_tbl"), "foo_target")), Collections.emptyMap(),
                        "There is invalid parameter value. foo_ds doesn't exist. Run `SHOW MIGRATION SOURCE STORAGE UNITS;` to verify it."),
                Arguments.of("duplicate target table", Arrays.asList(new MigrationSourceTargetEntry(new DataNode("foo_ds.foo_tbl"), "foo_target"),
                        new MigrationSourceTargetEntry(new DataNode("bar_ds.bar_tbl"), "foo_target")), Collections.singletonMap("bar_ds", createDataSourcePoolProperties("jdbc:h2:mem:bar_db")),
                        "There is invalid parameter value. More than one source table for foo_target"),
                Arguments.of("mixed source database types", Arrays.asList(new MigrationSourceTargetEntry(new DataNode("foo_ds.foo_tbl"), "bar_target"),
                        new MigrationSourceTargetEntry(new DataNode("bar_ds.bar_tbl"), "foo_target")), mixedSources,
                        "There is invalid parameter value. Source storage units have different database types"));
    }
    
    @Test
    void assertCommit() {
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.emptyList()));
        String checkJobId = "foo_check_job";
        MigrationJobConfiguration jobConfig = mock(MigrationJobConfiguration.class);
        when(jobConfig.getTargetDatabaseName()).thenReturn("foo_db");
        when(jobConfigManager.getJobConfiguration(jobId)).thenReturn(jobConfig);
        ContextManager contextManager = mock(ContextManager.class);
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(contextManager.getDatabase("foo_db")).thenReturn(database);
        try (
                MockedStatic<PipelineAPIFactory> ignoredFactory = mockStatic(PipelineAPIFactory.class, Answers.RETURNS_DEEP_STUBS);
                MockedStatic<PipelineContextManager> ignoredContext = mockStatic(PipelineContextManager.class)) {
            when(PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getJobFacade().getCheck().listCheckJobIds(jobId)).thenReturn(Collections.singleton(checkJobId));
            when(PipelineContextManager.getContext(contextKey)).thenReturn(contextManager);
            jobAPI.commit(jobId);
            InOrder inOrder = inOrder(jobManager, contextManager);
            inOrder.verify(jobManager).stop(jobId);
            inOrder.verify(jobManager).drop(checkJobId);
            inOrder.verify(contextManager).getDatabase("foo_db");
            inOrder.verify(contextManager).reloadDatabase(database);
            inOrder.verify(jobManager).drop(jobId);
        }
    }
    
    @Test
    void assertCommitWithUnknownDatabase() {
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.emptyList()));
        MigrationJobConfiguration jobConfig = mock(MigrationJobConfiguration.class);
        when(jobConfig.getTargetDatabaseName()).thenReturn("foo_db");
        when(jobConfigManager.getJobConfiguration(jobId)).thenReturn(jobConfig);
        ContextManager contextManager = mock(ContextManager.class);
        UnknownDatabaseException expected = new UnknownDatabaseException("foo_db");
        when(contextManager.getDatabase("foo_db")).thenThrow(expected);
        try (
                MockedStatic<PipelineAPIFactory> ignoredFactory = mockStatic(PipelineAPIFactory.class, Answers.RETURNS_DEEP_STUBS);
                MockedStatic<PipelineContextManager> ignoredContext = mockStatic(PipelineContextManager.class)) {
            when(PipelineContextManager.getContext(contextKey)).thenReturn(contextManager);
            UnknownDatabaseException actual = assertThrows(UnknownDatabaseException.class, () -> jobAPI.commit(jobId));
            assertThat(actual, sameInstance(expected));
            verify(jobManager).stop(jobId);
            verify(contextManager, never()).reloadDatabase(any(ShardingSphereDatabase.class));
            verify(jobManager, never()).drop(jobId);
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideRollbackSchemas")
    void assertRollbackNormalizesSchemaAndPreservesActualTable(final String name, final String schemaName, final String expectedSchemaName) throws SQLException {
        String jobId = PipelineJobIdUtils.marshal(new MigrationJobId(contextKey, Collections.emptyList()));
        MigrationJobConfiguration jobConfig = mock(MigrationJobConfiguration.class);
        when(jobConfig.getTargetTableNames()).thenReturn(Collections.singletonList("T_Order"));
        when(jobConfig.getTargetTableSchemaMap()).thenReturn(Collections.singletonMap("T_Order", schemaName));
        Connection connection = mock(Connection.class);
        Statement statement = mock(Statement.class);
        when(connection.createStatement()).thenReturn(statement);
        try (
                MockedStatic<PipelineAPIFactory> ignoredFactory = mockStatic(PipelineAPIFactory.class, Answers.RETURNS_DEEP_STUBS);
                MockedConstruction<PipelineJobConfigurationManager> ignoredManager = mockConstruction(PipelineJobConfigurationManager.class,
                        (mock, context) -> when(mock.getJobConfiguration(jobId)).thenReturn(jobConfig));
                MockedConstruction<PipelineDataSource> ignoredDataSource = mockConstruction(PipelineDataSource.class, (mock, context) -> {
                    when(mock.getConnection()).thenReturn(connection);
                    when(mock.getIdentifierContext()).thenReturn(new DatabaseIdentifierContext(IdentifierCasePolicyFactory.newLowerCasePolicySet()));
                });
                MockedConstruction<PipelinePrepareSQLBuilder> builders = mockConstruction(PipelinePrepareSQLBuilder.class,
                        (mock, context) -> when(mock.buildDropSQL(expectedSchemaName, "T_Order")).thenReturn("DROP TABLE IF EXISTS target_table"))) {
            jobAPI.rollback(jobId);
            verify(builders.constructed().get(0)).buildDropSQL(expectedSchemaName, "T_Order");
            verify(statement).execute("DROP TABLE IF EXISTS target_table");
            verify(jobManager).drop(jobId);
        }
    }
    
    private static Stream<Arguments> provideRollbackSchemas() {
        return Stream.of(
                Arguments.of("uppercase schema reference", "UPPER_SCHEMA", "upper_schema"),
                Arguments.of("absent schema", null, null),
                Arguments.of("empty schema", "", ""));
    }
}
