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

package org.apache.shardingsphere.scaling.core.service.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.execute.engine.TaskExecuteEngine;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.schedule.JobStatus;
import org.apache.shardingsphere.scaling.core.schedule.ScalingTaskScheduler;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.util.JobConfigurationUtil;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public final class StandaloneScalingJobServiceTest {
    
    private final ScalingJobService scalingJobService = new StandaloneScalingJobService();
    
    @Mock
    private JobContext jobContext;
    
    @Mock
    private ScalingTaskScheduler scalingTaskScheduler;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "inventoryDumperExecuteEngine", mock(TaskExecuteEngine.class));
    }
    
    @Test
    public void assertStartJob() {
        Optional<JobContext> jobContext = scalingJobService.start(mockJobConfiguration());
        assertTrue(jobContext.isPresent());
        long jobId = jobContext.get().getJobId();
        JobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getIncrementalTaskProgress().size(), is(1));
        assertThat(progress.getInventoryTaskProgress().size(), is(1));
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertGetNotExistJob() {
        scalingJobService.getJob(0);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    @SneakyThrows(ReflectiveOperationException.class)
    public void assertStopJob() {
        Map<Long, JobContext> jobContextMap = ReflectionUtil.getFieldValue(scalingJobService, "jobContextMap", Map.class);
        Map<Long, ScalingTaskScheduler> scalingTaskSchedulerMap = ReflectionUtil.getFieldValue(scalingJobService, "scalingTaskSchedulerMap", Map.class);
        assertNotNull(jobContextMap);
        assertNotNull(scalingTaskSchedulerMap);
        long jobId = 1L;
        jobContextMap.put(jobId, jobContext);
        scalingTaskSchedulerMap.put(jobId, scalingTaskScheduler);
        scalingJobService.stop(jobId);
        verify(jobContext).setStatus(JobStatus.STOPPED.name());
    }
    
    @Test
    public void assertListJobs() {
        assertThat(scalingJobService.listJobs().size(), is(0));
        scalingJobService.start(mockJobConfiguration());
        assertThat(scalingJobService.listJobs().size(), is(1));
    }
    
    @Test
    public void assertCheckJob() {
        Optional<JobContext> jobContextOptional = scalingJobService.start(mockJobConfiguration());
        assertTrue(jobContextOptional.isPresent());
        JobContext jobContext = jobContextOptional.get();
        jobContext.setDatabaseType("H2");
        jobContext.getTaskConfigs().clear();
        Map<String, DataConsistencyCheckResult> checkResult = scalingJobService.check(jobContext.getJobId());
        assertTrue(checkResult.isEmpty());
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertResetJob() {
        Optional<JobContext> jobContextOptional = scalingJobService.start(mockJobConfiguration());
        assertTrue(jobContextOptional.isPresent());
        JobContext jobContext = jobContextOptional.get();
        ScalingDataSourceConfiguration dataSourceConfig = jobContext.getTaskConfigs().get(0).getImporterConfig().getDataSourceConfig();
        initTableData(dataSourceConfig);
        assertThat(countTableData(dataSourceConfig), is(2L));
        scalingJobService.reset(jobContext.getJobId());
        assertThat(countTableData(dataSourceConfig), is(0L));
    }
    
    @SneakyThrows(IOException.class)
    private JobConfiguration mockJobConfiguration() {
        return JobConfigurationUtil.initJobConfig("/config.json");
    }
    
    private void initTableData(final ScalingDataSourceConfiguration dataSourceConfig) throws SQLException {
        DataSource dataSource = new DataSourceManager().getDataSource(dataSourceConfig);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DROP TABLE IF EXISTS `t1`");
            statement.execute("CREATE TABLE `t1` (id INT PRIMARY KEY, user_id VARCHAR(12))");
            statement.execute("INSERT INTO `t1` (id, user_id) VALUES (1, 'xxx'), (999, 'yyy')");
        }
    }
    
    private long countTableData(final ScalingDataSourceConfiguration dataSourceConfig) throws SQLException {
        DataSource dataSource = new DataSourceManager().getDataSource(dataSourceConfig);
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM `t1`");
            resultSet.next();
            return resultSet.getLong(1);
        }
    }
}
