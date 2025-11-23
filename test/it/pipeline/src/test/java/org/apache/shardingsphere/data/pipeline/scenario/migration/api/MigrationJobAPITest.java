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

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.TableDataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.context.TransmissionProcessContext;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.core.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSource;
import org.apache.shardingsphere.data.pipeline.core.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.progress.config.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.config.YamlTransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobConfigurationManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.TransmissionJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.PipelineDataSourceCreator;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(PipelineDistributedBarrier.class)
class MigrationJobAPITest {
    
    private static PipelineJobType jobType;
    
    private static MigrationJobAPI jobAPI;
    
    private static PipelineJobConfigurationManager jobConfigManager;
    
    private static PipelineJobManager jobManager;
    
    private static TransmissionJobManager transmissionJobManager;
    
    private static PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager;
    
    private static DatabaseType databaseType;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.initPipelineContextManager();
        jobType = new MigrationJobType();
        jobAPI = (MigrationJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION");
        jobConfigManager = new PipelineJobConfigurationManager(jobType.getOption());
        jobManager = new PipelineJobManager(jobType);
        transmissionJobManager = new TransmissionJobManager(jobType);
        jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
        String jdbcUrl = "jdbc:h2:mem:test_ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
        databaseType = DatabaseTypeFactory.get(jdbcUrl);
        Map<String, Object> props = new HashMap<>(3, 1F);
        props.put("jdbcUrl", jdbcUrl);
        props.put("username", "root");
        props.put("password", "root");
        jobAPI.registerMigrationSourceStorageUnits(PipelineContextUtils.getContextKey(), Collections.singletonMap("ds_0", new DataSourcePoolProperties("com.zaxxer.hikari.HikariDataSource", props)));
    }
    
    @AfterAll
    static void afterClass() {
        jobAPI.dropMigrationSourceResources(PipelineContextUtils.getContextKey(), Collections.singletonList("ds_0"));
    }
    
    @Test
    void assertScheduleAndList() {
        PipelineJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        JobConfigurationPOJO jobConfigPOJO = getJobConfigurationPOJO(jobConfig.getJobId());
        assertFalse(jobConfigPOJO.isDisabled());
        assertThat(jobConfigPOJO.getShardingTotalCount(), is(1));
    }
    
    private JobConfigurationPOJO getJobConfigurationPOJO(final String jobId) {
        return PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobConfiguration(jobId);
    }
    
    @Test
    void assertScheduleOrStopById() {
        PipelineJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        assertFalse(getJobConfigurationPOJO(jobConfig.getJobId()).isDisabled());
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobManager.stop(jobConfig.getJobId());
        assertTrue(getJobConfigurationPOJO(jobConfig.getJobId()).isDisabled());
        jobManager.resume(jobConfig.getJobId());
        assertFalse(getJobConfigurationPOJO(jobConfig.getJobId()).isDisabled());
    }
    
    @Test
    void assertRollback() throws SQLException {
        PipelineJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        initTableData(jobConfigManager.<MigrationJobConfiguration>getJobConfiguration(jobConfig.getJobId()));
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobAPI.rollback(jobConfig.getJobId());
        assertNull(getJobConfigurationPOJO(jobConfig.getJobId()));
    }
    
    @Test
    void assertCommit() {
        PipelineJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        initTableData(jobConfigManager.<MigrationJobConfiguration>getJobConfiguration(jobConfig.getJobId()));
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobAPI.commit(jobConfig.getJobId());
        assertNull(getJobConfigurationPOJO(jobConfig.getJobId()));
    }
    
    @Test
    void assertGetProgress() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        Map<Integer, TransmissionJobItemProgress> jobProgressMap = transmissionJobManager.getJobProgress(jobConfig);
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    void assertDataConsistencyCheck() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        initTableData(jobConfig);
        jobManager.start(jobConfig);
        PipelineProcessConfiguration processConfig = PipelineProcessConfigurationUtils.fillInDefaultValue(
                new PipelineProcessConfigurationPersistService().load(PipelineJobIdUtils.parseContextKey(jobConfig.getJobId()), jobType.getType()));
        TransmissionProcessContext processContext = new TransmissionProcessContext(jobConfig.getJobId(), processConfig);
        Map<String, TableDataConsistencyCheckResult> checkResultMap = jobType.buildDataConsistencyChecker(
                jobConfig, processContext, new ConsistencyCheckJobItemProgressContext(jobConfig.getJobId(), 0, "H2")).check("FIXTURE", null);
        assertThat(checkResultMap.size(), is(1));
        String checkKey = "t_order";
        assertTrue(checkResultMap.get(checkKey).isMatched());
        assertTrue(checkResultMap.get(checkKey).isMatched());
    }
    
    @Test
    void assertSwitchClusterConfigurationSucceed() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(jobConfig);
        jobItemManager.persistProgress(jobItemContext);
        jobItemManager.updateStatus(jobConfig.getJobId(), jobItemContext.getShardingItem(), JobStatus.EXECUTE_INVENTORY_TASK);
        Map<Integer, TransmissionJobItemProgress> progress = transmissionJobManager.getJobProgress(jobConfig);
        for (Entry<Integer, TransmissionJobItemProgress> entry : progress.entrySet()) {
            assertThat(entry.getValue().getStatus(), is(JobStatus.EXECUTE_INVENTORY_TASK));
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final MigrationJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration source = jobConfig.getSources().values().iterator().next();
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(source.getType(), source.getParameter());
        initTableData(TypedSPILoader.getService(
                PipelineDataSourceCreator.class, sourceDataSourceConfig.getType()).create(sourceDataSourceConfig.getDataSourceConfiguration()));
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        initTableData(TypedSPILoader.getService(
                PipelineDataSourceCreator.class, targetDataSourceConfig.getType()).create(targetDataSourceConfig.getDataSourceConfiguration()));
    }
    
    private void initTableData(final DataSource pipelineDataSource) throws SQLException {
        try (
                Connection connection = pipelineDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT(11))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 0), (999, 15)");
        }
    }
    
    @Test
    void assertRenewJobStatus() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(jobConfig);
        jobItemManager.persistProgress(jobItemContext);
        jobItemManager.updateStatus(jobConfig.getJobId(), 0, JobStatus.FINISHED);
        Optional<TransmissionJobItemProgress> actual = jobItemManager.getProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getStatus(), is(JobStatus.FINISHED));
    }
    
    @Test
    void assertAddMigrationSourceResources() {
        PipelineDataSourcePersistService persistService = new PipelineDataSourcePersistService();
        Map<String, DataSourcePoolProperties> actual = persistService.load(PipelineContextUtils.getContextKey(), "MIGRATION");
        assertTrue(actual.containsKey("ds_0"));
    }
    
    @Test
    void assertCreateJobConfigFailedOnMoreThanOneSourceTable() {
        Collection<MigrationSourceTargetEntry> sourceTargetEntries = Stream.of("t_order_0", "t_order_1")
                .map(each -> new MigrationSourceTargetEntry(new DataNode("ds_0", (String) null, each), "t_order")).collect(Collectors.toList());
        assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.schedule(PipelineContextUtils.getContextKey(), sourceTargetEntries, "logic_db"));
    }
    
    @Test
    void assertCreateJobConfigFailedOnDataSourceNotExist() {
        Collection<MigrationSourceTargetEntry> sourceTargetEntries = Collections.singleton(new MigrationSourceTargetEntry(new DataNode("ds_not_exists", (String) null, "t_order"), "t_order"));
        assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.schedule(PipelineContextUtils.getContextKey(), sourceTargetEntries, "logic_db"));
    }
    
    @Test
    void assertCreateJobConfig() throws SQLException {
        initIntPrimaryEnvironment();
        MigrationSourceTargetEntry sourceTargetEntry = new MigrationSourceTargetEntry(new DataNode("ds_0", (String) null, "t_order"), "t_order");
        String jobId = jobAPI.schedule(PipelineContextUtils.getContextKey(), Collections.singleton(sourceTargetEntry), "logic_db");
        MigrationJobConfiguration actual = jobConfigManager.getJobConfiguration(jobId);
        assertThat(actual.getTargetDatabaseName(), is("logic_db"));
        List<JobDataNodeLine> dataNodeLines = actual.getJobShardingDataNodes();
        assertThat(dataNodeLines.size(), is(1));
        assertThat(dataNodeLines.get(0).getEntries().size(), is(1));
        JobDataNodeEntry entry = dataNodeLines.get(0).getEntries().get(0);
        assertThat(entry.getDataNodes().size(), is(1));
        DataNode dataNode = entry.getDataNodes().get(0);
        assertThat(dataNode.getDataSourceName(), is("ds_0"));
        assertThat(dataNode.getTableName(), is("t_order"));
        assertThat(entry.getLogicTableName(), is("t_order"));
    }
    
    private void initIntPrimaryEnvironment() throws SQLException {
        Map<String, DataSourcePoolProperties> metaDataDataSource = new PipelineDataSourcePersistService().load(PipelineContextUtils.getContextKey(), "MIGRATION");
        DataSourcePoolProperties props = metaDataDataSource.get("ds_0");
        try (
                PipelineDataSource dataSource = new PipelineDataSource(DataSourcePoolCreator.create(props), databaseType);
                Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id INT)");
        }
    }
    
    @Test
    void assertShowMigrationSourceResources() {
        Collection<Collection<Object>> actual = jobAPI.listMigrationSourceResources(PipelineContextUtils.getContextKey());
        assertThat(actual.size(), is(1));
        Collection<Object> objects = actual.iterator().next();
        assertThat(objects.toArray()[0], is("ds_0"));
    }
    
    @Test
    void assertGetJobItemInfosAtBegin() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        YamlTransmissionJobItemProgress yamlJobItemProgress = new YamlTransmissionJobItemProgress();
        yamlJobItemProgress.setStatus(JobStatus.RUNNING.name());
        yamlJobItemProgress.setSourceDatabaseType("MySQL");
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey()).getJobItemFacade().getProcess().persist(jobConfig.getJobId(), 0, YamlEngine.marshal(yamlJobItemProgress));
        Collection<TransmissionJobItemInfo> jobItemInfos = transmissionJobManager.getJobItemInfos(jobConfig.getJobId());
        assertThat(jobItemInfos.size(), is(1));
        TransmissionJobItemInfo jobItemInfo = jobItemInfos.iterator().next();
        assertThat(jobItemInfo.getJobItemProgress().getStatus(), is(JobStatus.RUNNING));
        assertThat(jobItemInfo.getInventoryFinishedPercentage(), is(0));
    }
    
    @Test
    void assertGetJobItemInfosAtIncrementTask() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        jobManager.start(jobConfig);
        YamlTransmissionJobItemProgress yamlJobItemProgress = new YamlTransmissionJobItemProgress();
        yamlJobItemProgress.setSourceDatabaseType("MySQL");
        yamlJobItemProgress.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK.name());
        yamlJobItemProgress.setProcessedRecordsCount(100L);
        yamlJobItemProgress.setInventoryRecordsCount(50L);
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineContextUtils.getContextKey()).getJobItemFacade().getProcess().persist(jobConfig.getJobId(), 0, YamlEngine.marshal(yamlJobItemProgress));
        Collection<TransmissionJobItemInfo> jobItemInfos = transmissionJobManager.getJobItemInfos(jobConfig.getJobId());
        TransmissionJobItemInfo jobItemInfo = jobItemInfos.stream().iterator().next();
        assertThat(jobItemInfo.getJobItemProgress().getStatus(), is(JobStatus.EXECUTE_INCREMENTAL_TASK));
        assertThat(jobItemInfo.getInventoryFinishedPercentage(), is(100));
    }
}
