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
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.datasource.DataSourceManager;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.execute.engine.TaskExecuteEngine;
import org.apache.shardingsphere.scaling.core.fixture.FixtureResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.position.resume.FileSystemResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.schedule.JobStatus;
import org.apache.shardingsphere.scaling.core.schedule.ScalingTaskScheduler;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.After;
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
    private ScalingJob scalingJob;
    
    @Mock
    private ScalingTaskScheduler scalingTaskScheduler;
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "inventoryDumperExecuteEngine", mock(TaskExecuteEngine.class));
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", FixtureResumeBreakPointManager.class);
    }
    
    @Test
    public void assertStartJob() {
        Optional<ScalingJob> scalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJob.isPresent());
        long jobId = scalingJob.get().getJobId();
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
        Map<Long, ScalingJob> scalingJobMap = ReflectionUtil.getFieldValue(scalingJobService, "scalingJobMap", Map.class);
        Map<Long, ScalingTaskScheduler> scalingTaskSchedulerMap = ReflectionUtil.getFieldValue(scalingJobService, "scalingTaskSchedulerMap", Map.class);
        assertNotNull(scalingJobMap);
        assertNotNull(scalingTaskSchedulerMap);
        long jobId = 1L;
        scalingJobMap.put(jobId, scalingJob);
        scalingTaskSchedulerMap.put(jobId, scalingTaskScheduler);
        scalingJobService.stop(jobId);
        verify(scalingJob).setStatus(JobStatus.STOPPED.name());
    }
    
    @Test
    public void assertListJobs() {
        assertThat(scalingJobService.listJobs().size(), is(0));
        scalingJobService.start(mockScalingConfiguration());
        assertThat(scalingJobService.listJobs().size(), is(1));
    }
    
    @Test
    public void assertCheckJob() {
        Optional<ScalingJob> scalingJobOptional = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJobOptional.isPresent());
        ScalingJob scalingJob = scalingJobOptional.get();
        scalingJob.setDatabaseType("H2");
        scalingJob.getTaskConfigs().clear();
        Map<String, DataConsistencyCheckResult> checkResult = scalingJobService.check(scalingJob.getJobId());
        assertTrue(checkResult.isEmpty());
    }
    
    @Test
    @SneakyThrows(SQLException.class)
    public void assertResetJob() {
        Optional<ScalingJob> scalingJobOptional = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJobOptional.isPresent());
        ScalingJob scalingJob = scalingJobOptional.get();
        ScalingDataSourceConfiguration dataSourceConfig = scalingJob.getTaskConfigs().get(0).getImporterConfig().getDataSourceConfig();
        initTableData(dataSourceConfig);
        assertThat(countTableData(dataSourceConfig), is(2L));
        scalingJobService.reset(scalingJob.getJobId());
        assertThat(countTableData(dataSourceConfig), is(0L));
    }
    
    @SneakyThrows(IOException.class)
    private ScalingConfiguration mockScalingConfiguration() {
        return ScalingConfigurationUtil.initConfig("/config.json");
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
    
    @After
    @SneakyThrows(ReflectiveOperationException.class)
    public void tearDown() {
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", FileSystemResumeBreakPointManager.class);
    }
}
