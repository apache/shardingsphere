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

package org.apache.shardingsphere.scaling.core.api.impl;

import org.apache.shardingsphere.governance.repository.api.config.GovernanceCenterConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.fixture.EmbedTestingServer;
import org.apache.shardingsphere.scaling.core.job.JobStatus;
import org.apache.shardingsphere.scaling.core.job.progress.JobProgress;
import org.apache.shardingsphere.scaling.core.util.JobConfigurationUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ScalingAPIImplTest {
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    @BeforeClass
    public static void init() {
        EmbedTestingServer.start();
        ScalingContext.getInstance().init(mockServerConfig());
    }
    
    private static ServerConfiguration mockServerConfig() {
        ServerConfiguration result = new ServerConfiguration();
        result.setGovernanceConfig(new GovernanceConfiguration("test",
                new GovernanceCenterConfiguration("Zookeeper", EmbedTestingServer.getConnectionString(), new Properties()), true));
        return result;
    }
    
    @Test
    public void assertStartAndList() {
        Optional<Long> jobId = scalingAPI.start(JobConfigurationUtil.initJobConfig("/config.json"));
        assertTrue(jobId.isPresent());
        JobInfo jobInfo = getNonNullJobInfo(jobId.get());
        assertTrue(jobInfo.isActive());
        assertThat(jobInfo.getStatus(), is(JobStatus.RUNNING.name()));
        assertThat(jobInfo.getTables(), is(new String[]{"ds_0.t1", "ds_0.t2"}));
        assertThat(jobInfo.getShardingTotalCount(), is(2));
        assertThat(jobInfo.getInventoryFinishedPercentage(), is(0));
        assertThat(jobInfo.getIncrementalAverageDelayMilliseconds(), is(-1L));
    }
    
    private Optional<JobInfo> getJobInfo(final long jobId) {
        return scalingAPI.list().stream()
                .filter(each -> each.getJobId() == jobId)
                .reduce((a, b) -> a);
    }
    
    private JobInfo getNonNullJobInfo(final long jobId) {
        Optional<JobInfo> result = getJobInfo(jobId);
        assertTrue(result.isPresent());
        return result.get();
    }
    
    @Test
    public void assertStartOrStopById() {
        Optional<Long> jobId = scalingAPI.start(JobConfigurationUtil.initJobConfig("/config.json"));
        assertTrue(jobId.isPresent());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
        scalingAPI.stop(jobId.get());
        assertFalse(getNonNullJobInfo(jobId.get()).isActive());
        scalingAPI.start(jobId.get());
        assertTrue(getNonNullJobInfo(jobId.get()).isActive());
    }
    
    @Test
    public void assertRemove() {
        Optional<Long> jobId = scalingAPI.start(JobConfigurationUtil.initJobConfig("/config.json"));
        assertTrue(jobId.isPresent());
        assertTrue(getJobInfo(jobId.get()).isPresent());
        scalingAPI.remove(jobId.get());
        assertFalse(getJobInfo(jobId.get()).isPresent());
    }
    
    @Test
    public void assertGetProgress() {
        Optional<Long> jobId = scalingAPI.start(JobConfigurationUtil.initJobConfig("/config.json"));
        assertTrue(jobId.isPresent());
        Map<Integer, JobProgress> jobProgressMap = scalingAPI.getProgress(jobId.get());
        assertThat(jobProgressMap.size(), is(2));
    }
}
