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

package org.apache.shardingsphere.data.pipeline.cdc.api;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.cdc.CDCJobId;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporter;
import org.apache.shardingsphere.data.pipeline.cdc.core.importer.CDCImporterManager;
import org.apache.shardingsphere.data.pipeline.cdc.core.pojo.CDCJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.importer.sink.PipelineSink;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.DialectIncrementalPositionManager;
import org.apache.shardingsphere.data.pipeline.core.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobRegistry;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.preparer.incremental.IncrementalTaskPositionManager;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemProcessGovernanceRepository;
import org.apache.shardingsphere.data.pipeline.core.sqlbuilder.dialect.DialectPipelineSQLBuilder;
import org.apache.shardingsphere.data.pipeline.core.task.progress.IncrementalTaskProgress;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.api.bootstrap.impl.OneOffJobBootstrap;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.api.JobConfigurationAPI;
import org.apache.shardingsphere.elasticjob.reg.base.CoordinatorRegistryCenter;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineAPIFactory.class, PipelineJobIdUtils.class, CDCImporterManager.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCJobAPITest {
    
    private MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader;
    
    private CDCJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseTypedSPILoader = mockStatic(DatabaseTypedSPILoader.class, this::mockDatabaseTypedSPILoader);
        jobAPI = createJobAPI();
    }
    
    private Object mockDatabaseTypedSPILoader(final InvocationOnMock invocation) {
        if ("findService".equals(invocation.getMethod().getName())) {
            return Optional.empty();
        }
        Class<?> targetClass = invocation.getArgument(0);
        if (DialectPipelineSQLBuilder.class.equals(targetClass)) {
            return mock(DialectPipelineSQLBuilder.class);
        }
        if (DialectIncrementalPositionManager.class.equals(targetClass)) {
            return mock(DialectIncrementalPositionManager.class);
        }
        return mock(targetClass);
    }
    
    private CDCJobAPI createJobAPI() throws ReflectiveOperationException {
        when(PipelineJobIdUtils.marshal(any())).thenReturn("foo_job");
        when(PipelineJobIdUtils.parseContextKey(anyString())).thenReturn(new PipelineContextKey("foo_db", InstanceType.PROXY));
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS));
        YamlDataSourceConfigurationSwapper dataSourceSwapper = mock(YamlDataSourceConfigurationSwapper.class);
        when(dataSourceSwapper.swapToMap(any())).thenReturn(createStandardDataSourceProperties());
        CDCJobAPI result = new CDCJobAPI();
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("dataSourceConfigSwapper"), result, dataSourceSwapper);
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("ruleConfigSwapperEngine"), result, mock(YamlRuleConfigurationSwapperEngine.class));
        return result;
    }
    
    private Map<String, Object> createStandardDataSourceProperties() {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("url", "jdbc:h2:mem:foo_db");
        result.put("username", "root");
        result.put("password", "pwd");
        result.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        return result;
    }
    
    @AfterEach
    void tearDown() {
        PipelineContextManager.removeContext(new PipelineContextKey("foo_db", InstanceType.PROXY));
        PipelineContextManager.removeContext(new PipelineContextKey(InstanceType.PROXY));
        if (null != databaseTypedSPILoader) {
            databaseTypedSPILoader.close();
        }
    }
    
    @Test
    void assertCreateThrowsWhenJobShardingCountIsZero() {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        StreamDataParameter param = new StreamDataParameter("foo_db", Collections.emptyList(), false, Collections.emptyMap(), false);
        assertThrows(PipelineJobCreationWithInvalidShardingCountException.class, () -> jobAPI.create(param, CDCSinkType.SOCKET, new Properties()));
        verify(governanceFacade, never()).getJobFacade();
    }
    
    @Test
    void assertCreateSkipsExistingJob() {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        List<String> schemaTableNames = Arrays.asList("foo_schema.foo_tbl", "bar_schema.FOO_TBL");
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(governanceFacade.getJobFacade().getConfiguration().isExisted("foo_job")).thenReturn(true);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        StreamDataParameter param = new StreamDataParameter("foo_db", schemaTableNames, true,
                Collections.singletonMap("foo_schema.foo_tbl", Collections.singletonList(new DataNode("foo_readwrite_ds.foo_tbl"))), false);
        assertThat(jobAPI.create(param, CDCSinkType.SOCKET, new Properties()), is("foo_job"));
        verify(governanceFacade.getJobFacade().getJob(), never()).create(anyString(), any());
        verify(governanceFacade.getJobFacade().getConfiguration(), never()).persist(anyString(), any());
    }
    
    @Test
    void assertCreateThrowsWhenDataSourceDoesNotExist() {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        List<String> schemaTableNames = Arrays.asList("foo_schema.foo_tbl", "bar_schema.FOO_TBL");
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        StreamDataParameter param = new StreamDataParameter("foo_db", schemaTableNames, true,
                Collections.singletonMap("foo_schema.foo_tbl", Arrays.asList(new DataNode("foo_ds.foo_tbl"), new DataNode("foo_readwrite_ds.foo_tbl"))), false);
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.create(param, CDCSinkType.SOCKET, new Properties()));
        assertThat(actual.getMessage(), is("There is invalid parameter value. Data source `foo_readwrite_ds` does not exist in job data source configuration."));
        verify(governanceFacade.getJobFacade().getJob(), never()).create(anyString(), any());
        verify(governanceFacade.getJobFacade().getConfiguration(), never()).persist(anyString(), any());
    }
    
    @Test
    void assertCreateThrowsWhenSchemaTableNamesHaveDuplicateBareTable() {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        List<String> schemaTableNames = Arrays.asList("schema_a.t", "schema_b.T");
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        StreamDataParameter param = new StreamDataParameter("foo_db", schemaTableNames, true, Collections.singletonMap("t", Collections.singletonList(new DataNode("foo_ds.t"))), false);
        PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.create(param, CDCSinkType.SOCKET, new Properties()));
        assertThat(actual.getMessage(), is("There is invalid parameter value. More than one schema table has the same table name `T`."));
        verify(governanceFacade.getJobFacade().getJob(), never()).create(anyString(), any());
        verify(governanceFacade.getJobFacade().getConfiguration(), never()).persist(anyString(), any());
    }
    
    @Test
    void assertCreateFullJobSkipsInitIncrementalPosition() {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("bar_ds", mock(StorageUnit.class));
        putContext(storageUnits);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        ArgumentCaptor<CDCJobId> actualJobId = ArgumentCaptor.forClass(CDCJobId.class);
        when(PipelineJobIdUtils.marshal(actualJobId.capture())).thenReturn("foo_job");
        List<String> schemaTableNames = Arrays.asList("foo_schema.foo_tbl", "bar_schema.bar_tbl");
        Map<String, List<DataNode>> tableAndNodes = new LinkedHashMap<>(2, 1F);
        tableAndNodes.put("foo_tbl", Arrays.asList(new DataNode("foo_ds", "foo_schema", "foo_tbl_0"), new DataNode("bar_ds", "bar_schema", "foo_tbl_1")));
        tableAndNodes.put("bar_tbl", Collections.singletonList(new DataNode("bar_ds", "bar_schema", "bar_tbl")));
        Properties sinkProps = PropertiesBuilder.build(new Property("foo_key", "foo_value"));
        StreamDataParameter param = new StreamDataParameter("foo_db", schemaTableNames, true, tableAndNodes, true);
        try (MockedConstruction<IncrementalTaskPositionManager> positionManagerConstruction = mockConstruction(IncrementalTaskPositionManager.class)) {
            assertThat(jobAPI.create(param, CDCSinkType.SOCKET, sinkProps), is("foo_job"));
            assertTrue(positionManagerConstruction.constructed().isEmpty());
        }
        assertThat(actualJobId.getValue().getContextKey(), is(new PipelineContextKey("foo_db", InstanceType.PROXY)));
        assertThat(actualJobId.getValue().getSchemaTableNames(), is(Arrays.asList("bar_schema.bar_tbl", "foo_schema.foo_tbl")));
        assertTrue(actualJobId.getValue().isFull());
        assertThat(actualJobId.getValue().getSinkType(), is(CDCSinkType.SOCKET));
        assertThat(schemaTableNames, is(Arrays.asList("bar_schema.bar_tbl", "foo_schema.foo_tbl")));
        ArgumentCaptor<JobConfigurationPOJO> actualPersistedConfig = ArgumentCaptor.forClass(JobConfigurationPOJO.class);
        verify(governanceFacade.getJobFacade().getConfiguration()).persist(eq("foo_job"), actualPersistedConfig.capture());
        assertTrue(actualPersistedConfig.getValue().isDisabled());
        assertThat(actualPersistedConfig.getValue().getShardingTotalCount(), is(1));
        CDCJobConfiguration actual = new YamlCDCJobConfigurationSwapper().swapToObject(actualPersistedConfig.getValue().getJobParameter());
        assertThat(actual.getJobId(), is("foo_job"));
        assertThat(actual.getDatabaseName(), is("foo_db"));
        assertThat(actual.getSchemaTableNames(), is(Arrays.asList("bar_schema.bar_tbl", "foo_schema.foo_tbl")));
        assertTrue(actual.isFull());
        assertTrue(actual.isDecodeWithTX());
        assertThat(actual.getSourceDatabaseType().getType(), is("H2"));
        assertThat(actual.getSinkConfig().getSinkType(), is(CDCSinkType.SOCKET));
        assertThat(actual.getSinkConfig().getProps(), is(sinkProps));
        assertThat(actual.getConcurrency(), is(1));
        assertThat(actual.getRetryTimes(), is(0));
        assertThat(actual.getJobShardingCount(), is(2));
        assertThat(actual.getTablesFirstDataNodes().marshal(), is("foo_tbl:foo_ds.foo_schema.foo_tbl_0|bar_tbl:bar_ds.bar_schema.bar_tbl"));
        assertThat(actual.getJobShardingDataNodes().stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()),
                is(Arrays.asList("foo_tbl:bar_ds.bar_schema.foo_tbl_1|bar_tbl:bar_ds.bar_schema.bar_tbl", "foo_tbl:foo_ds.foo_schema.foo_tbl_0")));
        YamlRootConfiguration actualDataSourceConfig = YamlEngine.unmarshal(actual.getDataSourceConfig().getParameter(), YamlRootConfiguration.class, true);
        assertThat(actualDataSourceConfig.getDatabaseName(), is("foo_db"));
        assertThat(actualDataSourceConfig.getDataSources().get("foo_ds"), is(createStandardDataSourceProperties()));
        assertThat(actualDataSourceConfig.getDataSources().get("bar_ds"), is(createStandardDataSourceProperties()));
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertCreatePersistAndInitIncrementalPosition() throws ReflectiveOperationException, SQLException {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("bar_ds", mock(StorageUnit.class));
        putContext(storageUnits);
        PipelineJobItemProcessGovernanceRepository processGovernanceRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(governanceFacade.getJobItemFacade().getProcess()).thenReturn(processGovernanceRepository);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(any(CDCJobConfiguration.class))).thenReturn(createJobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<PipelineJobItemManager> ignoredJobItemManager = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> {
                            when(mock.getProgress(anyString(), anyInt())).thenReturn(Optional.empty());
                            when(mock.getProgress("foo_job", 0)).thenReturn(Optional.of(mock(TransmissionJobItemProgress.class)));
                        });
                MockedConstruction<IncrementalTaskPositionManager> positionManagerConstruction = mockConstruction(IncrementalTaskPositionManager.class,
                        (mock, context) -> when(mock.getPosition(any(), any(), any())).thenReturn(new SimpleIngestPosition("binlog_position")))) {
            StreamDataParameter param = buildStreamDataParameter(storageUnits.keySet());
            assertThat(jobAPI.create(param, CDCSinkType.SOCKET, new Properties()), is("foo_job"));
            ArgumentCaptor<CDCJobConfiguration> actualJobConfig = ArgumentCaptor.forClass(CDCJobConfiguration.class);
            verify(jobConfigManager).convertToJobConfigurationPOJO(actualJobConfig.capture());
            assertThat(actualJobConfig.getValue().getJobShardingDataNodes().stream().map(JobDataNodeLine::marshal).collect(Collectors.toList()),
                    is(Arrays.asList("foo_tbl:bar_ds.foo_tbl", "foo_tbl:foo_ds.foo_tbl")));
            assertFalse(actualJobConfig.getValue().isFull());
            assertThat(positionManagerConstruction.constructed().size(), is(1));
            ArgumentCaptor<JobConfigurationPOJO> actualPersistedConfig = ArgumentCaptor.forClass(JobConfigurationPOJO.class);
            InOrder inOrder = inOrder(governanceFacade.getJobFacade().getJob(), governanceFacade.getJobFacade().getConfiguration(),
                    positionManagerConstruction.constructed().get(0), processGovernanceRepository);
            inOrder.verify(governanceFacade.getJobFacade().getJob()).create(eq("foo_job"), any());
            inOrder.verify(governanceFacade.getJobFacade().getConfiguration()).persist(eq("foo_job"), actualPersistedConfig.capture());
            assertTrue(actualPersistedConfig.getValue().isDisabled());
            inOrder.verify(positionManagerConstruction.constructed().get(0)).getPosition(any(), any(), any());
            inOrder.verify(processGovernanceRepository).persist(eq("foo_job"), eq(1), anyString());
            verify(processGovernanceRepository, never()).persist(eq("foo_job"), eq(0), anyString());
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertInitIncrementalPositionThrowsPrepareException() throws ReflectiveOperationException {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(any(CDCJobConfiguration.class))).thenReturn(new JobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<PipelineJobItemManager> ignoredJobItemManager = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> when(mock.getProgress("foo_job", 0)).thenReturn(Optional.empty()));
                MockedConstruction<IncrementalTaskPositionManager> positionManagerConstruction = mockConstruction(
                        IncrementalTaskPositionManager.class, (mock, context) -> when(mock.getPosition(any(), any(), any())).thenThrow(SQLException.class))) {
            StreamDataParameter param = buildStreamDataParameter(Collections.singleton("foo_ds"));
            assertThrows(PrepareJobWithGetBinlogPositionException.class, () -> jobAPI.create(param, CDCSinkType.SOCKET, new Properties()));
            assertThat(positionManagerConstruction.constructed().size(), is(1));
        }
    }
    
    @Test
    void assertDropCleansUpAndHandlesSQLException() throws ReflectiveOperationException {
        PipelineJobManager jobManager = mock(PipelineJobManager.class);
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobManager"), jobAPI, jobManager);
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("foo_ds", createStandardDataSourceProperties());
        dataSources.put("bar_ds", createStandardDataSourceProperties());
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(buildYamlRootConfiguration(dataSources));
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("foo_job", "foo_db", Collections.singletonList("foo_schema.foo_tbl"), false,
                mock(DatabaseType.class), dataSourceConfig, new JobDataNodeLine(Collections.singletonList(
                        new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl"))))),
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl")))))), false,
                new CDCJobConfiguration.SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 0);
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.getJobConfiguration("foo_job")).thenReturn(jobConfig);
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        AtomicInteger positionManagerIndex = new AtomicInteger();
        try (
                MockedConstruction<IncrementalTaskPositionManager> positionManagerConstruction = mockConstruction(
                        IncrementalTaskPositionManager.class, (mock, context) -> {
                            if (0 == positionManagerIndex.getAndIncrement()) {
                                doNothing().when(mock).destroyPosition(eq("foo_job"), any(StandardPipelineDataSourceConfiguration.class));
                            } else {
                                doThrow(SQLException.class).when(mock).destroyPosition(eq("foo_job"), any(StandardPipelineDataSourceConfiguration.class));
                            }
                        })) {
            jobAPI.drop("foo_job");
            verify(jobManager).drop("foo_job");
            assertThat(positionManagerConstruction.constructed().size(), is(2));
        }
    }
    
    @SuppressWarnings("JDBCResourceOpenedButNotSafelyClosed")
    @Test
    void assertGetJobItemInfosCoversPositions() throws SQLException {
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("bar_position");
        when(connection.createStatement().executeQuery(anyString())).thenReturn(resultSet);
        StorageUnit storageUnitWithSQL = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnitWithSQL.getDataSource().getConnection()).thenReturn(connection);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        StorageUnit storageUnitWithoutSQL = mock(StorageUnit.class);
        storageUnits.put("foo_ds", storageUnitWithoutSQL);
        storageUnits.put("bar_ds", storageUnitWithSQL);
        putContext(storageUnits);
        CDCJobConfiguration jobConfig = createJobConfiguration(1);
        try (
                MockedConstruction<PipelineJobConfigurationManager> ignoredJobConfigManager = mockConstruction(PipelineJobConfigurationManager.class,
                        (mock, context) -> when(mock.getJobConfiguration("foo_job")).thenReturn(jobConfig));
                MockedConstruction<TransmissionJobManager> ignoredJobManagerConstruction = mockConstruction(TransmissionJobManager.class,
                        (mock, context) -> when(mock.getJobItemInfos("foo_job")).thenReturn(
                                Arrays.asList(new TransmissionJobItemInfo(0, "foo", null, 0L, 0, null),
                                        buildJobItemInfo("foo_ds", null), buildJobItemInfo("bar_ds", "binlog_001"))))) {
            DialectPipelineSQLBuilder builderWithoutSQL = mock(DialectPipelineSQLBuilder.class);
            when(builderWithoutSQL.buildQueryCurrentPositionSQL()).thenReturn(Optional.empty());
            DialectPipelineSQLBuilder builderWithSQL = mock(DialectPipelineSQLBuilder.class);
            when(builderWithSQL.buildQueryCurrentPositionSQL()).thenReturn(Optional.of("SELECT 1"));
            when(DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, storageUnitWithoutSQL.getStorageType())).thenReturn(builderWithoutSQL);
            when(DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, storageUnitWithSQL.getStorageType())).thenReturn(builderWithSQL);
            List<CDCJobItemInfo> actual = new ArrayList<>(jobAPI.getJobItemInfos("foo_job"));
            assertThat(actual.size(), is(3));
            assertTrue(actual.get(0).getConfirmedPosition().isEmpty());
            assertTrue(actual.get(1).getConfirmedPosition().isEmpty());
            assertThat(actual.get(2).getConfirmedPosition(), is("binlog_001"));
            assertThat(actual.get(2).getCurrentPosition(), is("bar_position"));
        }
    }
    
    @Test
    void assertGetCurrentPositionThrowsException() throws SQLException {
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        putContext(Collections.singletonMap("foo_ds", storageUnit));
        CDCJobConfiguration jobConfig = createJobConfiguration(1);
        try (
                MockedConstruction<PipelineJobConfigurationManager> ignoredJobConfigManager = mockConstruction(PipelineJobConfigurationManager.class,
                        (mock, context) -> when(mock.getJobConfiguration("foo_job")).thenReturn(jobConfig));
                MockedConstruction<TransmissionJobManager> ignoredJobManagerConstruction = mockConstruction(TransmissionJobManager.class,
                        (mock, context) -> when(mock.getJobItemInfos("foo_job")).thenReturn(Collections.singletonList(buildJobItemInfo("foo_ds", "binlog_002"))))) {
            DialectPipelineSQLBuilder builder = mock(DialectPipelineSQLBuilder.class);
            when(builder.buildQueryCurrentPositionSQL()).thenReturn(Optional.of("SELECT 1"));
            when(DatabaseTypedSPILoader.getService(DialectPipelineSQLBuilder.class, storageUnit.getStorageType())).thenReturn(builder);
            DataSource dataSource = storageUnit.getDataSource();
            Connection connection = mock(Connection.class);
            when(dataSource.getConnection()).thenReturn(connection);
            Statement statement = mock(Statement.class);
            when(connection.createStatement()).thenReturn(statement);
            when(statement.executeQuery(anyString())).thenThrow(SQLException.class);
            assertThrows(PipelineInternalException.class, () -> jobAPI.getJobItemInfos("foo_job"));
        }
    }
    
    @Test
    void assertStartEnableDisableAndType() throws ReflectiveOperationException {
        final PipelineJobConfigurationManager jobConfigManager = mockPersistedJobConfiguration(createJobConfiguration(1));
        JobConfigurationPOJO jobConfigPOJO = createJobConfigurationPOJO();
        jobConfigPOJO.setShardingTotalCount(1);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", "2026-08-26 00:00:00");
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        when(PipelineAPIFactory.getJobConfigurationAPI(any())).thenReturn(jobConfigAPI);
        when(PipelineAPIFactory.getRegistryCenter(any())).thenReturn(mock(CoordinatorRegistryCenter.class));
        PipelineSink sink = mock(PipelineSink.class);
        try (
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class);
                MockedConstruction<OneOffJobBootstrap> jobBootstrapConstruction = mockConstruction(OneOffJobBootstrap.class)) {
            jobAPI.start("foo_job", sink);
            verify(jobConfigManager).getJobConfiguration("foo_job");
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job"));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.add(eq("foo_job"), any()));
            assertFalse(jobConfigPOJO.isDisabled());
            assertNotNull(jobConfigPOJO.getProps().getProperty("start_time_millis"));
            assertFalse(jobConfigPOJO.getProps().containsKey("stop_time"));
            verify(jobConfigAPI).updateJobConfiguration(jobConfigPOJO);
            jobAPI.disable("foo_job");
            assertTrue(jobConfigPOJO.isDisabled());
            assertNotNull(jobConfigPOJO.getProps().getProperty("stop_time"));
            jobAPI.commit("foo_job");
            jobAPI.rollback("foo_job");
            assertThat(jobAPI.getType(), is("STREAMING"));
            assertThat(jobBootstrapConstruction.constructed().size(), is(1));
        }
    }
    
    @Test
    void assertStartRejectsPersistedUnresolvedDataSource() throws ReflectiveOperationException {
        mockPersistedJobConfiguration(createJobConfiguration(Collections.singletonList("foo_missing_ds"), false));
        JobConfigurationPOJO jobConfigPOJO = createJobConfigurationPOJO();
        jobConfigPOJO.setDisabled(false);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        when(PipelineAPIFactory.getJobConfigurationAPI(any())).thenReturn(jobConfigAPI);
        try (
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class);
                MockedConstruction<OneOffJobBootstrap> jobBootstrapConstruction = mockConstruction(OneOffJobBootstrap.class)) {
            PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.start("foo_job", mock(PipelineSink.class)));
            assertThat(actual.getMessage(), containsString("foo_missing_ds"));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job"));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.add(anyString(), any()), never());
            assertTrue(jobConfigPOJO.isDisabled());
            verify(jobConfigAPI).updateJobConfiguration(jobConfigPOJO);
            assertTrue(jobBootstrapConstruction.constructed().isEmpty());
        }
    }
    
    @Test
    void assertStartRejectsPersistedDuplicateBareTableWithoutRepeatedGovernanceWrite() throws ReflectiveOperationException {
        mockPersistedJobConfiguration(createJobConfiguration(Collections.singletonList("foo_ds"), false, Arrays.asList("foo_schema.foo_tbl", "bar_schema.FOO_TBL")));
        JobConfigurationPOJO jobConfigPOJO = createJobConfigurationPOJO();
        jobConfigPOJO.setDisabled(true);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        when(PipelineAPIFactory.getJobConfigurationAPI(any())).thenReturn(jobConfigAPI);
        try (
                MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class);
                MockedConstruction<OneOffJobBootstrap> jobBootstrapConstruction = mockConstruction(OneOffJobBootstrap.class)) {
            PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.start("foo_job", mock(PipelineSink.class)));
            assertThat(actual.getMessage(), is("There is invalid parameter value. More than one schema table has the same table name `FOO_TBL`."));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job"));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.add(anyString(), any()), never());
            verify(jobConfigAPI, never()).updateJobConfiguration(any());
            assertTrue(jobBootstrapConstruction.constructed().isEmpty());
        }
    }
    
    @Test
    void assertStartPreservesValidationExceptionWhenCleanupFails() throws ReflectiveOperationException {
        mockPersistedJobConfiguration(createJobConfiguration(Collections.singletonList("foo_missing_ds"), false));
        JobConfigurationPOJO jobConfigPOJO = createJobConfigurationPOJO();
        jobConfigPOJO.setDisabled(false);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        when(PipelineAPIFactory.getJobConfigurationAPI(any())).thenReturn(jobConfigAPI);
        doThrow(new IllegalStateException("governance unavailable")).when(jobConfigAPI).updateJobConfiguration(jobConfigPOJO);
        try (MockedStatic<PipelineJobRegistry> jobRegistryMocked = mockStatic(PipelineJobRegistry.class)) {
            jobRegistryMocked.when(() -> PipelineJobRegistry.stop("foo_job")).thenThrow(new IllegalStateException("stop unavailable"));
            PipelineInvalidParameterException actual = assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.start("foo_job", mock(PipelineSink.class)));
            assertThat(actual.getMessage(), containsString("foo_missing_ds"));
            assertThat(actual.getSuppressed().length, is(2));
            assertThat(actual.getSuppressed()[0].getMessage(), is("stop unavailable"));
            assertThat(actual.getSuppressed()[1].getMessage(), is("governance unavailable"));
            jobRegistryMocked.verify(() -> PipelineJobRegistry.stop("foo_job"));
            assertTrue(jobConfigPOJO.isDisabled());
        }
    }
    
    @Test
    void assertAckWhenImporterMissing() {
        assertDoesNotThrow(() -> jobAPI.ack("foo_bar"));
    }
    
    @Test
    void assertAckWhenImporterExists() {
        CDCImporter importer = mock(CDCImporter.class);
        when(CDCImporterManager.getImporter("foo")).thenReturn(importer);
        jobAPI.ack("foo_bar");
        verify(importer).ack("foo_bar");
    }
    
    private PipelineJobConfigurationManager mockPersistedJobConfiguration(final CDCJobConfiguration jobConfig) throws ReflectiveOperationException {
        PipelineJobConfigurationManager result = mock(PipelineJobConfigurationManager.class);
        when(result.getJobConfiguration("foo_job")).thenReturn(jobConfig);
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, result);
        return result;
    }
    
    private void putContext(final Map<String, StorageUnit> storageUnits) {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getName()).thenReturn("foo_db");
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        PipelineContextManager.putContext(new PipelineContextKey("foo_db", InstanceType.PROXY), contextManager);
        PipelineContextManager.putContext(new PipelineContextKey(InstanceType.PROXY), contextManager);
    }
    
    private CDCJobConfiguration createJobConfiguration(final int shardingCount) {
        return createJobConfiguration(IntStream.range(0, shardingCount).mapToObj(i -> 0 == i ? "foo_ds" : "bar_ds").collect(Collectors.toList()), false);
    }
    
    private CDCJobConfiguration createJobConfiguration(final List<String> dataSourceNames, final boolean full) {
        return createJobConfiguration(dataSourceNames, full, Collections.singletonList("foo_schema.foo_tbl"));
    }
    
    private CDCJobConfiguration createJobConfiguration(final List<String> dataSourceNames, final boolean full, final List<String> schemaTableNames) {
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("foo_ds", createStandardDataSourceProperties());
        dataSources.put("bar_ds", createStandardDataSourceProperties());
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(buildYamlRootConfiguration(dataSources));
        List<JobDataNodeLine> jobDataNodeLines = dataSourceNames.stream().map(
                each -> new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode(each + ".foo_tbl"))))))
                .collect(Collectors.toList());
        return new CDCJobConfiguration("foo_job", "foo_db", schemaTableNames, full, mock(),
                dataSourceConfig, jobDataNodeLines.get(0), jobDataNodeLines, false, new CDCJobConfiguration.SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 0);
    }
    
    private YamlRootConfiguration buildYamlRootConfiguration(final Map<String, Map<String, Object>> dataSources) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName("foo_db");
        result.setDataSources(dataSources);
        result.setRules(Collections.emptyList());
        result.setProps(new Properties());
        return result;
    }
    
    private JobConfigurationPOJO createJobConfigurationPOJO() {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName("foo_job");
        return result;
    }
    
    private StreamDataParameter buildStreamDataParameter(final Collection<String> dataSourceNames) {
        Map<String, List<DataNode>> tableAndNodes = Collections.singletonMap("foo_tbl", dataSourceNames.stream().map(each -> new DataNode(each + ".foo_tbl")).collect(Collectors.toList()));
        return new StreamDataParameter("foo_db", Collections.singletonList("foo_schema.foo_tbl"), false, tableAndNodes, false);
    }
    
    private TransmissionJobItemInfo buildJobItemInfo(final String dataSourceName, final String incrementalPosition) {
        TransmissionJobItemProgress progress = new TransmissionJobItemProgress();
        progress.setDataSourceName(dataSourceName);
        progress.setIncremental(new JobItemIncrementalTasksProgress(null == incrementalPosition ? null : new IncrementalTaskProgress(new SimpleIngestPosition(incrementalPosition))));
        return new TransmissionJobItemInfo(0, "foo_tbl", progress, 0L, 0, null);
    }
    
    @RequiredArgsConstructor
    private static final class SimpleIngestPosition implements IngestPosition {
        
        private final String value;
        
        @Override
        public String toString() {
            return value;
        }
    }
}
