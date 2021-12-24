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

package org.apache.shardingsphere.data.pipeline.api.impl;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.DataConsistencyCheckAlgorithmInfo;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.data.pipeline.core.fixture.FixtureDataConsistencyCheckAlgorithm;
import org.apache.shardingsphere.data.pipeline.core.util.ResourceUtil;
import org.apache.shardingsphere.data.pipeline.core.util.RuleAlteredContextUtil;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceYamlConfigurationSwapper;
import org.apache.shardingsphere.infra.config.datasource.jdbc.creator.JDBCDataSourceCreatorFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class PipelineJobAPIImplTest {
    
    private static PipelineJobAPI pipelineJobAPI;
    
    @BeforeClass
    public static void beforeClass() {
        EmbedTestingServer.start();
        RuleAlteredContextUtil.mockModeConfig();
        pipelineJobAPI = PipelineJobAPIFactory.getPipelineJobAPI();
    }
    
    @Test
    public void assertStartAndList() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobInfo jobInfo = getNonNullJobInfo(jobId.get());
        assertTrue(jobInfo.isActive());
        assertThat(jobInfo.getTables(), is("t_order"));
        assertThat(jobInfo.getShardingTotalCount(), is(1));
        List<Long> uncompletedJobIds = pipelineJobAPI.getUncompletedJobIds("logic_db");
        assertTrue(uncompletedJobIds.size() > 0);
    }
    
    private Optional<JobInfo> getJobInfo(final long jobId) {
        return pipelineJobAPI.list().stream().filter(each -> each.getJobId() == jobId).reduce((a, b) -> a);
    }
    
    private JobInfo getNonNullJobInfo(final long jobId) {
        Optional<JobInfo> result = getJobInfo(jobId);
        assertTrue(result.isPresent());
        return result.get();
    }
    
    @Test
    public void assertStartOrStopById() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
        pipelineJobAPI.stop(jobId.get());
        assertFalse(getNonNullJobInfo(jobId.get()).isActive());
        pipelineJobAPI.start(jobId.get());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
    }
    
    @Test
    public void assertRemove() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        assertTrue(getJobInfo(jobId.get()).isPresent());
        pipelineJobAPI.remove(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertGetProgress() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        Map<Integer, JobProgress> jobProgressMap = pipelineJobAPI.getProgress(jobId.get());
        assertThat(jobProgressMap.size(), is(1));
    }
    
    @Test
    public void assertListDataConsistencyCheckAlgorithms() {
        Collection<DataConsistencyCheckAlgorithmInfo> algorithmInfos = pipelineJobAPI.listDataConsistencyCheckAlgorithms();
        assertTrue(algorithmInfos.size() > 0);
        Optional<DataConsistencyCheckAlgorithmInfo> algorithmInfoOptional = algorithmInfos.stream().filter(each -> each.getType().equals(FixtureDataConsistencyCheckAlgorithm.TYPE)).findFirst();
        assertTrue(algorithmInfoOptional.isPresent());
        DataConsistencyCheckAlgorithmInfo algorithmInfo = algorithmInfoOptional.get();
        assertThat(algorithmInfo.getType(), is(FixtureDataConsistencyCheckAlgorithm.TYPE));
        FixtureDataConsistencyCheckAlgorithm fixtureAlgorithm = new FixtureDataConsistencyCheckAlgorithm();
        assertThat(algorithmInfo.getDescription(), is(fixtureAlgorithm.getDescription()));
        assertThat(algorithmInfo.getSupportedDatabaseTypes(), is(fixtureAlgorithm.getSupportedDatabaseTypes()));
        assertThat(algorithmInfo.getProvider(), is(fixtureAlgorithm.getProvider()));
    }
    
    @Test
    public void assertIsDataConsistencyCheckNeeded() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        assertThat(pipelineJobAPI.isDataConsistencyCheckNeeded(jobId.get()), is(true));
    }
    
    @Test
    public void assertDataConsistencyCheck() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobConfiguration jobConfig = pipelineJobAPI.getJobConfig(jobId.get());
        initTableData(jobConfig.getRuleConfig());
        Map<String, DataConsistencyCheckResult> checkResultMap = pipelineJobAPI.dataConsistencyCheck(jobId.get());
        assertThat(checkResultMap.size(), is(1));
    }
    
    @Test
    public void assertDataConsistencyCheckWithAlgorithm() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobConfiguration jobConfig = pipelineJobAPI.getJobConfig(jobId.get());
        initTableData(jobConfig.getRuleConfig());
        Map<String, DataConsistencyCheckResult> checkResultMap = pipelineJobAPI.dataConsistencyCheck(jobId.get(), FixtureDataConsistencyCheckAlgorithm.TYPE);
        assertThat(checkResultMap.size(), is(1));
        assertTrue(checkResultMap.get("t_order").isCountValid());
        assertTrue(checkResultMap.get("t_order").isDataValid());
        assertThat(checkResultMap.get("t_order").getTargetCount(), is(2L));
    }
    
    @Test
    public void assertAggregateDataConsistencyCheckResults() {
        long jobId = 1L;
        Map<String, DataConsistencyCheckResult> checkResultMap;
        checkResultMap = Collections.emptyMap();
        assertThat(pipelineJobAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap), is(false));
        DataConsistencyCheckResult trueResult = new DataConsistencyCheckResult(1, 1);
        trueResult.setDataValid(true);
        DataConsistencyCheckResult checkResult;
        checkResult = new DataConsistencyCheckResult(100, 95);
        checkResultMap = ImmutableMap.<String, DataConsistencyCheckResult>builder().put("t", trueResult).put("t_order", checkResult).build();
        assertThat(pipelineJobAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap), is(false));
        checkResult = new DataConsistencyCheckResult(100, 100);
        checkResult.setDataValid(false);
        checkResultMap = ImmutableMap.<String, DataConsistencyCheckResult>builder().put("t", trueResult).put("t_order", checkResult).build();
        assertThat(pipelineJobAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap), is(false));
        checkResult = new DataConsistencyCheckResult(100, 100);
        checkResult.setDataValid(true);
        checkResultMap = ImmutableMap.<String, DataConsistencyCheckResult>builder().put("t", trueResult).put("t_order", checkResult).build();
        assertThat(pipelineJobAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap), is(true));
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertResetTargetTable() {
        Optional<Long> jobId = pipelineJobAPI.start(ResourceUtil.mockJobConfig());
        assertTrue(jobId.isPresent());
        JobConfiguration jobConfig = pipelineJobAPI.getJobConfig(jobId.get());
        initTableData(jobConfig.getRuleConfig());
        pipelineJobAPI.reset(jobId.get());
        Map<String, DataConsistencyCheckResult> checkResultMap = pipelineJobAPI.dataConsistencyCheck(jobId.get(), FixtureDataConsistencyCheckAlgorithm.TYPE);
        assertThat(checkResultMap.get("t_order").getTargetCount(), is(0L));
    }
    
    @SneakyThrows(SQLException.class)
    private void initTableData(final RuleConfiguration ruleConfig) {
        JDBCDataSourceConfiguration sourceConfig = new JDBCDataSourceYamlConfigurationSwapper().swapToObject(ruleConfig.getSource()).unwrap();
        initTableData(JDBCDataSourceCreatorFactory.getInstance(sourceConfig.getType()).createDataSource(sourceConfig.getDataSourceConfiguration()));
        JDBCDataSourceConfiguration targetConfig = new JDBCDataSourceYamlConfigurationSwapper().swapToObject(ruleConfig.getTarget()).unwrap();
        initTableData(JDBCDataSourceCreatorFactory.getInstance(targetConfig.getType()).createDataSource(targetConfig.getDataSourceConfiguration()));
    }
    
    private void initTableData(final DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS t_order");
            statement.execute("CREATE TABLE t_order (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO t_order (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
}
