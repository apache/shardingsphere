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

package org.apache.shardingsphere.test.it.data.pipeline.scenario.migration.api.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeEntry;
import org.apache.shardingsphere.data.pipeline.common.datanode.JobDataNodeLine;
import org.apache.shardingsphere.data.pipeline.common.datasource.PipelineDataSourceWrapper;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.ConsistencyCheckJobItemProgressContext;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.algorithm.DataConsistencyCalculateAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.core.consistencycheck.result.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.core.exception.param.PipelineInvalidParameterException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineDataSourcePersistService;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobType;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.config.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.scenario.migration.context.MigrationJobItemContext;
import org.apache.shardingsphere.data.pipeline.spi.datasource.creator.PipelineDataSourceCreator;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.migration.distsql.statement.MigrateTableStatement;
import org.apache.shardingsphere.migration.distsql.statement.pojo.SourceTargetEntry;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.test.it.data.pipeline.core.util.PipelineContextUtils;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(PipelineDistributedBarrier.class)
class MigrationJobAPITest {
    
    private static MigrationJobAPI jobAPI;
    
    private static DatabaseType databaseType;
    
    @BeforeAll
    static void beforeClass() {
        PipelineContextUtils.mockModeConfigAndContextManager();
        jobAPI = new MigrationJobAPI();
        String jdbcUrl = "jdbc:h2:mem:test_ds_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MySQL";
        databaseType = DatabaseTypeFactory.get(jdbcUrl);
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", jdbcUrl);
        props.put("username", "root");
        props.put("password", "root");
        jobAPI.addMigrationSourceResources(PipelineContextUtils.getContextKey(), Collections.singletonMap("ds_0", new DataSourceProperties("com.zaxxer.hikari.HikariDataSource", props)));
    }
    
    @AfterAll
    static void afterClass() {
        jobAPI.dropMigrationSourceResources(PipelineContextUtils.getContextKey(), Collections.singletonList("ds_0"));
    }
    
    @Test
    void assertStartAndList() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        JobConfigurationPOJO jobConfigPOJO = getJobConfigurationPOJO(jobId.get());
        assertFalse(jobConfigPOJO.isDisabled());
        assertThat(jobConfigPOJO.getShardingTotalCount(), is(1));
    }
    
    private JobConfigurationPOJO getJobConfigurationPOJO(final String jobId) {
        return PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobConfiguration(jobId);
    }
    
    @Test
    void assertStartOrStopById() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        assertFalse(getJobConfigurationPOJO(jobId.get()).isDisabled());
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobAPI.stop(jobId.get());
        assertTrue(getJobConfigurationPOJO(jobId.get()).isDisabled());
        jobAPI.startDisabledJob(jobId.get());
        assertFalse(getJobConfigurationPOJO(jobId.get()).isDisabled());
    }
    
    @Test
    void assertRollback() throws SQLException {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        MigrationJobConfiguration jobConfig = jobAPI.getJobConfiguration(jobId.get());
        initTableData(jobConfig);
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobAPI.rollback(jobId.get());
        assertNull(getJobConfigurationPOJO(jobId.get()));
    }
    
    @Test
    void assertCommit() {
        Optional<String> jobId = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        MigrationJobConfiguration jobConfig = jobAPI.getJobConfiguration(jobId.get());
        initTableData(jobConfig);
        PipelineDistributedBarrier mockBarrier = mock(PipelineDistributedBarrier.class);
        when(PipelineDistributedBarrier.getInstance(any())).thenReturn(mockBarrier);
        jobAPI.commit(jobId.get());
        assertNull(getJobConfigurationPOJO(jobId.get()));
    }
    
    @Test
    void assertGetProgress() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        Optional<String> jobId = jobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgressMap = jobAPI.getJobProgress(jobConfig);
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @Test
    void assertBuildNullDataConsistencyCalculateAlgorithm() {
        DataConsistencyCalculateAlgorithm actual = jobAPI.buildDataConsistencyCalculateAlgorithm(null, null);
        assertInstanceOf(DataConsistencyCalculateAlgorithm.class, actual);
    }
    
    @Test
    void assertDataConsistencyCheck() {
        MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        initTableData(jobConfig);
        Optional<String> jobId = jobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        DataConsistencyCalculateAlgorithm calculateAlgorithm = jobAPI.buildDataConsistencyCalculateAlgorithm("FIXTURE", null);
        Map<String, DataConsistencyCheckResult> checkResultMap = jobAPI.dataConsistencyCheck(jobConfig, calculateAlgorithm, new ConsistencyCheckJobItemProgressContext(jobId.get(), 0));
        assertThat(checkResultMap.size(), is(1));
        String checkKey = "ds_0.t_order";
        assertTrue(checkResultMap.get(checkKey).getCountCheckResult().isMatched());
        assertThat(checkResultMap.get(checkKey).getCountCheckResult().getTargetRecordsCount(), is(2L));
        assertTrue(checkResultMap.get(checkKey).getContentCheckResult().isMatched());
    }
    
    @Test
    void assertAggregateEmptyDataConsistencyCheckResults() {
        assertThrows(IllegalArgumentException.class, () -> jobAPI.aggregateDataConsistencyCheckResults("foo_job", Collections.emptyMap()));
    }
    
    @Test
    void assertAggregateDifferentCountDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyCountCheckResult notEqualCountCheckResult = new DataConsistencyCountCheckResult(100, 95);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1F);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(notEqualCountCheckResult, equalContentCheckResult));
        assertFalse(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    void assertAggregateDifferentContentDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        DataConsistencyContentCheckResult notEqualContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1F);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, notEqualContentCheckResult));
        assertFalse(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    void assertAggregateSameDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1F);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        assertTrue(jobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    void assertSwitchClusterConfigurationSucceed() {
        final MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        Optional<String> jobId = jobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(jobConfig);
        jobAPI.persistJobItemProgress(jobItemContext);
        jobAPI.updateJobItemStatus(jobId.get(), jobItemContext.getShardingItem(), JobStatus.EXECUTE_INVENTORY_TASK);
        Map<Integer, InventoryIncrementalJobItemProgress> progress = jobAPI.getJobProgress(jobConfig);
        for (Entry<Integer, InventoryIncrementalJobItemProgress> entry : progress.entrySet()) {
            assertThat(entry.getValue().getStatus(), is(JobStatus.EXECUTE_INVENTORY_TASK));
        }
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final MigrationJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration source = jobConfig.getSources().values().iterator().next();
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(source.getType(), source.getParameter());
        initTableData(TypedSPILoader.getService(
                PipelineDataSourceCreator.class, sourceDataSourceConfig.getType()).createPipelineDataSource(sourceDataSourceConfig.getDataSourceConfiguration()));
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        initTableData(TypedSPILoader.getService(
                PipelineDataSourceCreator.class, targetDataSourceConfig.getType()).createPipelineDataSource(targetDataSourceConfig.getDataSourceConfiguration()));
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
        final MigrationJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        MigrationJobItemContext jobItemContext = PipelineContextUtils.mockMigrationJobItemContext(jobConfig);
        jobAPI.persistJobItemProgress(jobItemContext);
        jobAPI.updateJobItemStatus(jobConfig.getJobId(), 0, JobStatus.FINISHED);
        Optional<InventoryIncrementalJobItemProgress> actual = jobAPI.getJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem());
        assertTrue(actual.isPresent());
        assertThat(actual.get().getStatus(), is(JobStatus.FINISHED));
    }
    
    @Test
    void assertAddMigrationSourceResources() {
        PipelineDataSourcePersistService persistService = new PipelineDataSourcePersistService();
        Map<String, DataSourceProperties> actual = persistService.load(PipelineContextUtils.getContextKey(), new MigrationJobType());
        assertTrue(actual.containsKey("ds_0"));
    }
    
    @Test
    void assertCreateJobConfigFailedOnMoreThanOneSourceTable() {
        List<SourceTargetEntry> sourceTargetEntries = Stream.of("t_order_0", "t_order_1")
                .map(each -> new SourceTargetEntry("logic_db", new DataNode("ds_0", each), "t_order")).collect(Collectors.toList());
        assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.createJobAndStart(PipelineContextUtils.getContextKey(), new MigrateTableStatement(sourceTargetEntries, "logic_db")));
    }
    
    @Test
    void assertCreateJobConfigFailedOnDataSourceNotExist() {
        List<SourceTargetEntry> sourceTargetEntries = Collections.singletonList(new SourceTargetEntry("logic_db", new DataNode("ds_not_exists", "t_order"), "t_order"));
        assertThrows(PipelineInvalidParameterException.class, () -> jobAPI.createJobAndStart(PipelineContextUtils.getContextKey(), new MigrateTableStatement(sourceTargetEntries, "logic_db")));
    }
    
    @Test
    void assertCreateJobConfig() throws SQLException {
        initIntPrimaryEnvironment();
        SourceTargetEntry sourceTargetEntry = new SourceTargetEntry("logic_db", new DataNode("ds_0", "t_order"), "t_order");
        String jobId = jobAPI.createJobAndStart(PipelineContextUtils.getContextKey(), new MigrateTableStatement(Collections.singletonList(sourceTargetEntry), "logic_db"));
        MigrationJobConfiguration actual = jobAPI.getJobConfiguration(jobId);
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
        Map<String, DataSourceProperties> metaDataDataSource = new PipelineDataSourcePersistService().load(PipelineContextUtils.getContextKey(), new MigrationJobType());
        DataSourceProperties dataSourceProps = metaDataDataSource.get("ds_0");
        try (
                PipelineDataSourceWrapper dataSource = new PipelineDataSourceWrapper(DataSourcePoolCreator.create(dataSourceProps), databaseType);
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
        Optional<String> optional = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(optional.isPresent());
        String jobId = optional.get();
        YamlInventoryIncrementalJobItemProgress yamlJobItemProgress = new YamlInventoryIncrementalJobItemProgress();
        yamlJobItemProgress.setStatus(JobStatus.RUNNING.name());
        yamlJobItemProgress.setSourceDatabaseType("MySQL");
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey()).persistJobItemProgress(jobId, 0, YamlEngine.marshal(yamlJobItemProgress));
        List<InventoryIncrementalJobItemInfo> jobItemInfos = jobAPI.getJobItemInfos(jobId);
        assertThat(jobItemInfos.size(), is(1));
        InventoryIncrementalJobItemInfo jobItemInfo = jobItemInfos.get(0);
        assertThat(jobItemInfo.getJobItemProgress().getStatus(), is(JobStatus.RUNNING));
        assertThat(jobItemInfo.getInventoryFinishedPercentage(), is(0));
    }
    
    @Test
    void assertGetJobItemInfosAtIncrementTask() {
        Optional<String> optional = jobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(optional.isPresent());
        YamlInventoryIncrementalJobItemProgress yamlJobItemProgress = new YamlInventoryIncrementalJobItemProgress();
        yamlJobItemProgress.setSourceDatabaseType("MySQL");
        yamlJobItemProgress.setStatus(JobStatus.EXECUTE_INCREMENTAL_TASK.name());
        yamlJobItemProgress.setProcessedRecordsCount(100);
        yamlJobItemProgress.setInventoryRecordsCount(50);
        String jobId = optional.get();
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineContextUtils.getContextKey()).persistJobItemProgress(jobId, 0, YamlEngine.marshal(yamlJobItemProgress));
        List<InventoryIncrementalJobItemInfo> jobItemInfos = jobAPI.getJobItemInfos(jobId);
        InventoryIncrementalJobItemInfo jobItemInfo = jobItemInfos.get(0);
        assertThat(jobItemInfo.getJobItemProgress().getStatus(), is(JobStatus.EXECUTE_INCREMENTAL_TASK));
        assertThat(jobItemInfo.getInventoryFinishedPercentage(), is(100));
    }
}
