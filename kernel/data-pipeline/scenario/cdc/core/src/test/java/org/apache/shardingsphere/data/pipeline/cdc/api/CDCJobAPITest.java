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
import org.apache.shardingsphere.data.pipeline.api.type.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.type.StandardPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.CDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.config.YamlCDCJobConfiguration;
import org.apache.shardingsphere.data.pipeline.cdc.config.yaml.swapper.YamlCDCJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.cdc.constant.CDCSinkType;
import org.apache.shardingsphere.data.pipeline.cdc.core.pojo.CDCJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextManager;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PrepareJobWithGetBinlogPositionException;
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
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.item.PipelineJobItemFacade;
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
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({PipelineAPIFactory.class, PipelineJobIdUtils.class, PipelineJobRegistry.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class CDCJobAPITest {
    
    private MockedStatic<DatabaseTypedSPILoader> databaseTypedSPILoader;
    
    private CDCJobAPI jobAPI;
    
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        databaseTypedSPILoader = mockDatabaseTypedSPILoader();
        jobAPI = createJobAPI();
    }
    
    private MockedStatic<DatabaseTypedSPILoader> mockDatabaseTypedSPILoader() {
        return mockStatic(DatabaseTypedSPILoader.class, invocation -> {
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
        });
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
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("foo_job", "foo_db", Collections.emptyList(), false, mock(DatabaseType.class),
                mock(ShardingSpherePipelineDataSourceConfiguration.class), new JobDataNodeLine(Collections.emptyList()), Collections.emptyList(),
                false, new CDCJobConfiguration.SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 0);
        try (
                MockedConstruction<YamlCDCJobConfigurationSwapper> jobConfigSwapper = mockConstruction(YamlCDCJobConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToObject(any(YamlCDCJobConfiguration.class))).thenReturn(jobConfig))) {
            StreamDataParameter param = new StreamDataParameter("foo_db", new LinkedList<>(), false, Collections.emptyMap(), false);
            assertThrows(PipelineJobCreationWithInvalidShardingCountException.class, () -> jobAPI.create(param, CDCSinkType.SOCKET, new Properties()));
            assertThat(jobConfigSwapper.constructed().size(), is(1));
        }
    }
    
    @Test
    void assertCreateSkipsExistingJob() throws ReflectiveOperationException {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        CDCJobConfiguration jobConfig = createJobConfiguration(1);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(governanceFacade.getJobFacade().getConfiguration().isExisted("foo_job")).thenReturn(true);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(jobConfig)).thenReturn(createJobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<YamlCDCJobConfigurationSwapper> ignored = mockConstruction(YamlCDCJobConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToObject(any(YamlCDCJobConfiguration.class))).thenReturn(jobConfig))) {
            StreamDataParameter param = new StreamDataParameter("foo_db", new LinkedList<>(Collections.singletonList("foo_schema.foo_tbl")), true,
                    Collections.singletonMap("foo_schema.foo_tbl", Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl"))), false);
            assertThat(jobAPI.create(param, CDCSinkType.SOCKET, new Properties()), is("foo_job"));
        }
    }
    
    @Test
    void assertCreateFullJobSkipsInitIncrementalPosition() throws ReflectiveOperationException {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        CDCJobConfiguration jobConfig = new CDCJobConfiguration("foo_job", "foo_db", Collections.singletonList("foo_schema.foo_tbl"), true,
                mock(DatabaseType.class), createJobConfiguration(1).getDataSourceConfig(), new JobDataNodeLine(Collections.singletonList(
                        new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl"))))),
                Collections.singletonList(new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl",
                        Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl")))))),
                false, new CDCJobConfiguration.SinkConfiguration(CDCSinkType.SOCKET, new Properties()), 1, 0);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS));
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(jobConfig)).thenReturn(createJobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<YamlCDCJobConfigurationSwapper> ignored = mockConstruction(YamlCDCJobConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToObject(any(YamlCDCJobConfiguration.class))).thenReturn(jobConfig));
                MockedConstruction<IncrementalTaskPositionManager> positionManagerConstruction = mockConstruction(IncrementalTaskPositionManager.class)) {
            StreamDataParameter param = new StreamDataParameter("foo_db", new LinkedList<>(Collections.singletonList("foo_schema.foo_tbl")), true,
                    Collections.singletonMap("foo_schema.foo_tbl", Collections.singletonList(new DataNode("foo_ds" + ".foo_tbl"))), false);
            assertThat(jobAPI.create(param, CDCSinkType.SOCKET, new Properties()), is("foo_job"));
            assertTrue(positionManagerConstruction.constructed().isEmpty());
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertCreatePersistAndInitIncrementalPosition() throws ReflectiveOperationException {
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
        storageUnits.put("foo_ds", mock(StorageUnit.class));
        storageUnits.put("bar_ds", mock(StorageUnit.class));
        putContext(storageUnits);
        CDCJobConfiguration jobConfig = createJobConfiguration(2);
        PipelineJobItemProcessGovernanceRepository processGovernanceRepository = mock(PipelineJobItemProcessGovernanceRepository.class);
        PipelineJobItemFacade jobItemFacade = mock(PipelineJobItemFacade.class);
        when(jobItemFacade.getProcess()).thenReturn(processGovernanceRepository);
        PipelineGovernanceFacade governanceFacade = mock(PipelineGovernanceFacade.class, RETURNS_DEEP_STUBS);
        when(governanceFacade.getJobItemFacade()).thenReturn(jobItemFacade);
        when(PipelineAPIFactory.getPipelineGovernanceFacade(any())).thenReturn(governanceFacade);
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(jobConfig)).thenReturn(createJobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<YamlCDCJobConfigurationSwapper> ignoredJobConfigSwapper = mockConstruction(YamlCDCJobConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToObject(any(YamlCDCJobConfiguration.class))).thenReturn(jobConfig));
                MockedConstruction<PipelineJobItemManager> ignoredJobItemManager = mockConstruction(PipelineJobItemManager.class,
                        (mock, context) -> {
                            when(mock.getProgress(anyString(), anyInt())).thenReturn(Optional.empty());
                            when(mock.getProgress("foo_job", 0)).thenReturn(Optional.of(mock(TransmissionJobItemProgress.class)));
                        });
                MockedConstruction<IncrementalTaskPositionManager> ignoredPositionManagerConstruction = mockConstruction(IncrementalTaskPositionManager.class,
                        (mock, context) -> when(mock.getPosition(any(), any(), any())).thenReturn(new SimpleIngestPosition("binlog_position")))) {
            StreamDataParameter param = buildStreamDataParameter(storageUnits.keySet());
            assertThat(jobAPI.create(param, CDCSinkType.SOCKET, new Properties()), is("foo_job"));
            verify(jobConfigManager).convertToJobConfigurationPOJO(jobConfig);
            verify(processGovernanceRepository).persist(anyString(), anyInt(), anyString());
        }
    }
    
    @SuppressWarnings("rawtypes")
    @Test
    void assertInitIncrementalPositionThrowsPrepareException() throws ReflectiveOperationException {
        putContext(Collections.singletonMap("foo_ds", mock(StorageUnit.class)));
        CDCJobConfiguration jobConfig = createJobConfiguration(1);
        PipelineJobConfigurationManager jobConfigManager = mock(PipelineJobConfigurationManager.class);
        when(jobConfigManager.convertToJobConfigurationPOJO(jobConfig)).thenReturn(new JobConfigurationPOJO());
        Plugins.getMemberAccessor().set(CDCJobAPI.class.getDeclaredField("jobConfigManager"), jobAPI, jobConfigManager);
        try (
                MockedConstruction<YamlCDCJobConfigurationSwapper> ignoredJobConfigSwapper = mockConstruction(YamlCDCJobConfigurationSwapper.class,
                        (mock, context) -> when(mock.swapToObject(any(YamlCDCJobConfiguration.class))).thenReturn(jobConfig));
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
        StorageUnit storageUnitWithoutSQL = mock(StorageUnit.class);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("bar_position");
        when(connection.createStatement().executeQuery(anyString())).thenReturn(resultSet);
        StorageUnit storageUnitWithSQL = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnitWithSQL.getDataSource().getConnection()).thenReturn(connection);
        Map<String, StorageUnit> storageUnits = new LinkedHashMap<>(2, 1F);
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
            assertThat(actual, hasSize(3));
            assertThat(actual.get(0).getConfirmedPosition(), emptyString());
            assertThat(actual.get(1).getConfirmedPosition(), emptyString());
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
    void assertStartEnableDisableAndType() {
        JobConfigurationPOJO jobConfigPOJO = createJobConfigurationPOJO();
        jobConfigPOJO.setShardingTotalCount(1);
        when(PipelineJobIdUtils.getElasticJobConfigurationPOJO("foo_job")).thenReturn(jobConfigPOJO);
        JobConfigurationAPI jobConfigAPI = mock(JobConfigurationAPI.class);
        when(PipelineAPIFactory.getJobConfigurationAPI(any())).thenReturn(jobConfigAPI);
        when(PipelineAPIFactory.getRegistryCenter(any())).thenReturn(mock(CoordinatorRegistryCenter.class));
        PipelineSink sink = mock(PipelineSink.class);
        try (MockedConstruction<OneOffJobBootstrap> jobBootstrapConstruction = mockConstruction(OneOffJobBootstrap.class)) {
            jobAPI.start("foo_job", sink);
            assertThat(jobConfigPOJO.getProps().getProperty("start_time_millis"), is(jobConfigPOJO.getProps().getProperty("start_time_millis")));
            verify(jobConfigAPI).updateJobConfiguration(jobConfigPOJO);
            jobAPI.disable("foo_job");
            assertNotNull(jobConfigPOJO.getProps().getProperty("stop_time"));
            jobAPI.commit("foo_job");
            jobAPI.rollback("foo_job");
            assertThat(jobAPI.getType(), is("STREAMING"));
            assertThat(jobBootstrapConstruction.constructed().size(), is(1));
        }
    }
    
    private void putContext(final Map<String, StorageUnit> storageUnits) {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class, RETURNS_DEEP_STUBS);
        when(metaDataContexts.getMetaData().getDatabase("foo_db").getResourceMetaData().getStorageUnits()).thenReturn(storageUnits);
        ContextManager contextManager = mock(ContextManager.class);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        PipelineContextManager.putContext(new PipelineContextKey("foo_db", InstanceType.PROXY), contextManager);
        PipelineContextManager.putContext(new PipelineContextKey(InstanceType.PROXY), contextManager);
    }
    
    private CDCJobConfiguration createJobConfiguration(final int shardingCount) {
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("foo_ds", createStandardDataSourceProperties());
        dataSources.put("bar_ds", createStandardDataSourceProperties());
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(buildYamlRootConfiguration(dataSources));
        List<JobDataNodeLine> jobDataNodeLines = IntStream.range(0, shardingCount).mapToObj(
                i -> new JobDataNodeLine(Collections.singletonList(new JobDataNodeEntry("foo_tbl", Collections.singletonList(new DataNode((0 == i ? "foo_ds" : "bar_ds") + ".foo_tbl"))))))
                .collect(Collectors.toList());
        return new CDCJobConfiguration("foo_job", "foo_db", Collections.singletonList("foo_schema.foo_tbl"), false, mock(),
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
        Map<String, List<DataNode>> tableAndNodes = dataSourceNames.stream()
                .collect(Collectors.toMap(each -> "foo_schema.foo_tbl", each -> Collections.singletonList(new DataNode(each + ".foo_tbl")), (a, b) -> b, LinkedHashMap::new));
        return new StreamDataParameter("foo_db", new LinkedList<>(Collections.singletonList("foo_schema.foo_tbl")), false, tableAndNodes, false);
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
