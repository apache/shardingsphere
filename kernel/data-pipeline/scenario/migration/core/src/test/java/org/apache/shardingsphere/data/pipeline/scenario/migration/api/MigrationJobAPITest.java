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
import org.apache.shardingsphere.database.connector.core.metadata.identifier.IdentifierCasePolicyFactory;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.identifier.DatabaseIdentifierContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;
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
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
    private PipelineDataSourcePersistService dataSourcePersistService;
    
    private MigrationJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        jobAPI = new MigrationJobAPI();
        Plugins.getMemberAccessor().set(MigrationJobAPI.class.getDeclaredField("jobManager"), jobAPI, jobManager);
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
