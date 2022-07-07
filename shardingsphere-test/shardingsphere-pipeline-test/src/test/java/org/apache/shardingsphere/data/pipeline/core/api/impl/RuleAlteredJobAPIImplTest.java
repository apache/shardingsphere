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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyContentCheckResult;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCountCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.core.datasource.creator.PipelineDataSourceCreatorFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineVerifyFailedException;
import org.apache.shardingsphere.data.pipeline.core.util.JobConfigurationBuilder;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineContextUtil;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobPreparer;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Slf4j
public final class RuleAlteredJobAPIImplTest {
    
    private static RuleAlteredJobAPI ruleAlteredJobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        PipelineContextUtil.mockModeConfigAndContextManager();
        ruleAlteredJobAPI = RuleAlteredJobAPIFactory.getInstance();
    }
    
    @Test
    public void assertStartAndList() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        JobInfo jobInfo = getNonNullJobInfo(jobId.get());
        assertTrue(jobInfo.isActive());
        assertThat(jobInfo.getTables(), is("t_order"));
        assertThat(jobInfo.getShardingTotalCount(), is(1));
    }
    
    private Optional<JobInfo> getJobInfo(final String jobId) {
        return ruleAlteredJobAPI.list().stream().filter(each -> Objects.equals(each.getJobId(), jobId)).reduce((a, b) -> a);
    }
    
    private JobInfo getNonNullJobInfo(final String jobId) {
        Optional<JobInfo> result = getJobInfo(jobId);
        assertTrue(result.isPresent());
        return result.get();
    }
    
    @Test
    public void assertStartOrStopById() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
        ruleAlteredJobAPI.stop(jobId.get());
        assertFalse(getNonNullJobInfo(jobId.get()).isActive());
        ruleAlteredJobAPI.startDisabledJob(jobId.get());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
    }
    
    @Test
    public void assertRemove() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        assertTrue(getJobInfo(jobId.get()).isPresent());
        ruleAlteredJobAPI.stop(jobId.get());
        ruleAlteredJobAPI.remove(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertGetProgress() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        Map<Integer, JobProgress> jobProgressMap = ruleAlteredJobAPI.getProgress(jobId.get());
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @Test
    public void assertIsDataConsistencyCheckNeeded() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        assertTrue(ruleAlteredJobAPI.isDataConsistencyCheckNeeded(jobId.get()));
    }
    
    @Test
    public void assertDataConsistencyCheck() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        RuleAlteredJobConfiguration jobConfig = ruleAlteredJobAPI.getJobConfig(jobId.get());
        if (null == jobConfig.getSource()) {
            log.error("source is null, jobConfig={}", YamlEngine.marshal(jobConfig));
        }
        initTableData(jobConfig);
        ruleAlteredJobAPI.stopClusterWriteDB(jobConfig);
        Map<String, DataConsistencyCheckResult> checkResultMap = ruleAlteredJobAPI.dataConsistencyCheck(jobId.get());
        ruleAlteredJobAPI.restoreClusterWriteDB(jobConfig);
        assertThat(checkResultMap.size(), is(1));
    }
    
    @Test
    public void assertDataConsistencyCheckWithAlgorithm() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        RuleAlteredJobConfiguration jobConfig = ruleAlteredJobAPI.getJobConfig(jobId.get());
        initTableData(jobConfig);
        ruleAlteredJobAPI.stopClusterWriteDB(jobConfig);
        Map<String, DataConsistencyCheckResult> checkResultMap = ruleAlteredJobAPI.dataConsistencyCheck(jobId.get(), "FIXTURE", null);
        ruleAlteredJobAPI.restoreClusterWriteDB(jobConfig);
        assertThat(checkResultMap.size(), is(1));
        assertTrue(checkResultMap.get("t_order").getCountCheckResult().isMatched());
        assertThat(checkResultMap.get("t_order").getCountCheckResult().getTargetRecordsCount(), is(2L));
        assertTrue(checkResultMap.get("t_order").getContentCheckResult().isMatched());
    }
    
    @Test
    public void assertAggregateEmptyDataConsistencyCheckResults() {
        assertFalse(ruleAlteredJobAPI.aggregateDataConsistencyCheckResults("foo_job", Collections.emptyMap()));
    }
    
    @Test
    public void assertAggregateDifferentCountDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyCountCheckResult notEqualCountCheckResult = new DataConsistencyCountCheckResult(100, 95);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(notEqualCountCheckResult, equalContentCheckResult));
        assertFalse(ruleAlteredJobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    public void assertAggregateDifferentContentDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        DataConsistencyContentCheckResult notEqualContentCheckResult = new DataConsistencyContentCheckResult(false);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, notEqualContentCheckResult));
        assertFalse(ruleAlteredJobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test
    public void assertAggregateSameDataConsistencyCheckResults() {
        DataConsistencyCountCheckResult equalCountCheckResult = new DataConsistencyCountCheckResult(100, 100);
        DataConsistencyContentCheckResult equalContentCheckResult = new DataConsistencyContentCheckResult(true);
        Map<String, DataConsistencyCheckResult> checkResults = new LinkedHashMap<>(2, 1);
        checkResults.put("foo_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        checkResults.put("bar_tbl", new DataConsistencyCheckResult(equalCountCheckResult, equalContentCheckResult));
        assertTrue(ruleAlteredJobAPI.aggregateDataConsistencyCheckResults("foo_job", checkResults));
    }
    
    @Test(expected = PipelineVerifyFailedException.class)
    public void assertSwitchClusterConfigurationAlreadyFinished() {
        final RuleAlteredJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        Optional<String> jobId = ruleAlteredJobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        final GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig, 0, new JobProgress(), new PipelineDataSourceManager(), new RuleAlteredJobPreparer());
        repositoryAPI.persistJobProgress(jobContext);
        repositoryAPI.persistJobCheckResult(jobId.get(), true);
        repositoryAPI.updateShardingJobStatus(jobId.get(), 0, JobStatus.FINISHED);
        ruleAlteredJobAPI.switchClusterConfiguration(jobId.get());
    }
    
    @Test
    public void assertSwitchClusterConfigurationSucceed() {
        final RuleAlteredJobConfiguration jobConfig = JobConfigurationBuilder.createJobConfiguration();
        Optional<String> jobId = ruleAlteredJobAPI.start(jobConfig);
        assertTrue(jobId.isPresent());
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        RuleAlteredJobContext jobContext = new RuleAlteredJobContext(jobConfig, 0, new JobProgress(), new PipelineDataSourceManager(), new RuleAlteredJobPreparer());
        repositoryAPI.persistJobProgress(jobContext);
        repositoryAPI.persistJobCheckResult(jobId.get(), true);
        repositoryAPI.updateShardingJobStatus(jobId.get(), jobContext.getShardingItem(), JobStatus.EXECUTE_INVENTORY_TASK);
        ruleAlteredJobAPI.switchClusterConfiguration(jobId.get());
        Map<Integer, JobProgress> progress = ruleAlteredJobAPI.getProgress(jobId.get());
        for (Entry<Integer, JobProgress> entry : progress.entrySet()) {
            assertSame(entry.getValue().getStatus(), JobStatus.FINISHED);
        }
    }
    
    @Test
    public void assertResetTargetTable() {
        Optional<String> jobId = ruleAlteredJobAPI.start(JobConfigurationBuilder.createJobConfiguration());
        assertTrue(jobId.isPresent());
        RuleAlteredJobConfiguration jobConfig = ruleAlteredJobAPI.getJobConfig(jobId.get());
        initTableData(jobConfig);
        ruleAlteredJobAPI.stop(jobId.get());
        ruleAlteredJobAPI.reset(jobId.get());
        Map<String, DataConsistencyCheckResult> checkResultMap = ruleAlteredJobAPI.dataConsistencyCheck(jobConfig);
        assertThat(checkResultMap.get("t_order").getCountCheckResult().getTargetRecordsCount(), is(0L));
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final RuleAlteredJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
        initTableData(PipelineDataSourceCreatorFactory.getInstance(sourceDataSourceConfig.getType()).createPipelineDataSource(sourceDataSourceConfig.getDataSourceConfiguration()));
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        initTableData(PipelineDataSourceCreatorFactory.getInstance(targetDataSourceConfig.getType()).createPipelineDataSource(targetDataSourceConfig.getDataSourceConfiguration()));
    }
    
    private void initTableData(final DataSource pipelineDataSource) throws SQLException {
        try (
                Connection connection = pipelineDataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (order_id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (order_id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
