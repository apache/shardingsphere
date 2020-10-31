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

package org.apache.shardingsphere.scaling.core;

import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.execute.engine.ShardingScalingExecuteEngine;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.resume.FakeResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.IncrementalPositionResumeBreakPointManager;
import org.apache.shardingsphere.scaling.core.job.position.resume.ResumeBreakPointManagerFactory;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.apache.shardingsphere.scaling.core.util.ReflectionUtil;
import org.apache.shardingsphere.scaling.core.util.ScalingConfigurationUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public final class ScalingJobControllerTest {
    
    private final ScalingJobController scalingJobController = new ScalingJobController();
    
    @Before
    @SneakyThrows(ReflectiveOperationException.class)
    public void setUp() {
        if (null == ScalingContext.getInstance().getServerConfig()) {
            ScalingContext.getInstance().init(new ServerConfiguration());
            ReflectionUtil.setFieldValue(ScalingContext.getInstance(), "taskExecuteEngine", mock(ShardingScalingExecuteEngine.class));
        }
    }
    
    @Test
    public void assertStartJob() {
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertTrue(progress instanceof ScalingJobProgress);
        assertThat(((ScalingJobProgress) progress).getId(), is(shardingScalingJob.getJobId()));
        assertThat(((ScalingJobProgress) progress).getJobName(), is(shardingScalingJob.getJobName()));
        assertThat(((ScalingJobProgress) progress).getIncrementalDataTasks().size(), is(1));
        assertThat(((ScalingJobProgress) progress).getInventoryDataTasks().size(), is(1));
    }
    
    @Test
    public void assertStopExistJob() {
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
        scalingJobController.stop(shardingScalingJob.getJobId());
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertTrue(progress instanceof ScalingJobProgress);
        assertThat(((ScalingJobProgress) progress).getStatus(), is(SyncTaskControlStatus.STOPPED.name()));
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertStopNotExistJob() {
        scalingJobController.stop(99);
        scalingJobController.getProgresses(99);
    }
    
    @Test
    public void assertListJobs() {
        assertThat(scalingJobController.listShardingScalingJobs().size(), is(0));
        scalingJobController.start(mockShardingScalingJob());
        assertThat(scalingJobController.listShardingScalingJobs().size(), is(1));
    }
    
    @Test
    public void assertIncrementalDataTasksOnly() throws NoSuchFieldException, IllegalAccessException {
        ReflectionUtil.setFieldValue(ResumeBreakPointManagerFactory.class, null, "clazz", IncrementalPositionResumeBreakPointManager.class);
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
        SyncProgress progress = scalingJobController.getProgresses(shardingScalingJob.getJobId());
        assertThat(((ScalingJobProgress) progress).getIncrementalDataTasks().size(), is(1));
        assertThat(((ScalingJobProgress) progress).getInventoryDataTasks().size(), is(0));
        ReflectionUtil.setFieldValue(ResumeBreakPointManagerFactory.class, null, "clazz", FakeResumeBreakPointManager.class);
    }
    
    @Test
    public void assertCheckExistJob() {
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        scalingJobController.start(shardingScalingJob);
        shardingScalingJob.setDataConsistencyChecker(new DataConsistencyChecker() {
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
        Map<String, DataConsistencyCheckResult> checkResult = scalingJobController.check(shardingScalingJob.getJobId());
        assertTrue(checkResult.get("t1").isCountValid());
        assertTrue(checkResult.get("t1").isDataValid());
    }
    
    @Test(expected = ScalingJobNotFoundException.class)
    public void assertCheckNotExistJob() {
        ShardingScalingJob shardingScalingJob = mockShardingScalingJob();
        scalingJobController.check(shardingScalingJob.getJobId());
    }
    
    @SneakyThrows(IOException.class)
    private ShardingScalingJob mockShardingScalingJob() {
        return ScalingConfigurationUtil.initJob("/config.json");
    }
}
