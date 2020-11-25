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

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.execute.engine.ShardingScalingExecuteEngine;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.position.resume.FakeResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.IncrementalPositionResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
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
        ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "taskExecuteEngine", mock(ShardingScalingExecuteEngine.class));
    }
    
    @Test
    public void assertStartJob() {
        Optional<ShardingScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        long jobId = shardingScalingJob.get().getJobId();
        ScalingJobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getIncrementalDataSyncTaskProgress().size(), is(1));
        assertThat(progress.getInventoryDataSyncTaskProgress().size(), is(1));
    }
    
    @Test
    public void assertStopExistJob() {
        Optional<ShardingScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        long jobId = shardingScalingJob.get().getJobId();
        scalingJobService.stop(jobId);
        ScalingJobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getStatus(), is(SyncTaskControlStatus.STOPPED.name()));
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
    public void assertIncrementalDataTasksOnly() throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", IncrementalPositionResumeBreakPointManager.class);
        Optional<ShardingScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        long jobId = shardingScalingJob.get().getJobId();
        ScalingJobProgress progress = scalingJobService.getProgress(jobId);
        assertThat(progress.getIncrementalDataSyncTaskProgress().size(), is(1));
        assertThat(progress.getInventoryDataSyncTaskProgress().size(), is(1));
        ReflectionUtil.setStaticFieldValue(ResumeBreakPointManagerFactory.class, "clazz", FakeResumeBreakPointManager.class);
    }
    
    @Test
    public void assertCheckExistJob() {
        Optional<ShardingScalingJob> shardingScalingJob = scalingJobService.start(mockScalingConfiguration());
        assertTrue(shardingScalingJob.isPresent());
        shardingScalingJob.get().setDataConsistencyChecker(new DataConsistencyChecker() {
            @Override
            public Map<String, DataConsistencyCheckResult> countCheck() {
                Map<String, DataConsistencyCheckResult> result = Maps.newHashMapWithExpectedSize(1);
                result.put("t1", new DataConsistencyCheckResult(1, 1));
                return result;
            }
            
            @Override
            public Map<String, Boolean> dataCheck() {
                Map<String, Boolean> result = Maps.newHashMapWithExpectedSize(1);
                result.put("t1", true);
                return result;
            }
        });
        Map<String, DataConsistencyCheckResult> checkResult = scalingJobService.check(shardingScalingJob.get().getJobId());
        assertTrue(checkResult.get("t1").isCountValid());
        assertTrue(checkResult.get("t1").isDataValid());
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
