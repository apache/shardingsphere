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
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.execute.engine.TaskExecuteEngine;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.position.resume.FakeResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.IncrementalPositionResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.schedule.JobStatus;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.apache.shardingsphere.scaling.core.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class StandaloneScalingJobServiceTest {
    
    private final ScalingJobService scalingJobService = new StandaloneScalingJobService();
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "serverConfig", new ServerConfiguration());
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "inventoryDumperExecuteEngine", mock(TaskExecuteEngine.class));
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
    
    @Test
    public void assertStopExistJob() {
        Optional<ScalingJob> scalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJob.isPresent());
        long jobId = scalingJob.get().getJobId();
        scalingJobService.stop(jobId);
        JobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getStatus(), is(JobStatus.STOPPED.name()));
        scalingJobService.remove(jobId);
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertStopNotExistJob() {
        scalingJobService.stop(0);
    }
    
    @Test
    public void assertListJobs() {
        assertThat(scalingJobService.listJobs().size(), is(0));
        scalingJobService.start(mockScalingConfiguration());
        assertThat(scalingJobService.listJobs().size(), is(1));
    }
    
    @Test
    public void assertIncrementalTasksOnly() throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", IncrementalPositionResumeBreakPointManager.class);
        Optional<ScalingJob> scalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJob.isPresent());
        long jobId = scalingJob.get().getJobId();
        JobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getIncrementalTaskProgress().size(), is(1));
        assertThat(progress.getInventoryTaskProgress().size(), is(1));
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", FakeResumeBreakPointManager.class);
    }
    
    @Test
    public void assertCheckExistJob() {
        Optional<ScalingJob> scalingJobOptional = scalingJobService.start(mockScalingConfiguration());
        assertTrue(scalingJobOptional.isPresent());
        ScalingJob scalingJob = scalingJobOptional.get();
        scalingJob.setDatabaseType("H2");
        scalingJob.getTaskConfigs().clear();
        Map<String, DataConsistencyCheckResult> checkResult = scalingJobService.check(scalingJob.getJobId());
        assertTrue(checkResult.isEmpty());
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertCheckNotExistJob() {
        scalingJobService.check(0);
    }
    
    @SneakyThrows(IOException.class)
    private ScalingConfiguration mockScalingConfiguration() {
        return ScalingConfigurationUtil.initConfig("/config.json");
    }
}
