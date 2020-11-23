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

import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.preparer.ShardingScalingJobPreparer;
import org.apache.shardingsphere.scaling.core.schedule.ScalingTaskScheduler;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.apache.shardingsphere.scaling.core.service.AbstractScalingJobService;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Standalone scaling job service.
 */
public final class StandaloneScalingJobService extends AbstractScalingJobService implements ScalingJobService {
    
    private final Map<Long, ShardingScalingJob> scalingJobMap = new ConcurrentHashMap<>();
    
    private final Map<Long, ScalingTaskScheduler> scalingTaskSchedulerMap = new ConcurrentHashMap<>();
    
    private final ShardingScalingJobPreparer shardingScalingJobPreparer = new ShardingScalingJobPreparer();
    
    @Override
    public List<ShardingScalingJob> listJobs() {
        return new LinkedList<>(scalingJobMap.values());
    }
    
    @Override
    public Optional<ShardingScalingJob> start(final ScalingConfiguration scalingConfig) {
        ShardingScalingJob shardingScalingJob = new ShardingScalingJob(scalingConfig);
        scalingJobMap.put(shardingScalingJob.getJobId(), shardingScalingJob);
        shardingScalingJobPreparer.prepare(shardingScalingJob);
        if (!SyncTaskControlStatus.PREPARING_FAILURE.name().equals(shardingScalingJob.getStatus())) {
            ScalingTaskScheduler scalingTaskScheduler = new ScalingTaskScheduler(shardingScalingJob);
            scalingTaskScheduler.start();
            scalingTaskSchedulerMap.put(shardingScalingJob.getJobId(), scalingTaskScheduler);
        }
        return Optional.of(shardingScalingJob);
    }
    
    @Override
    public void stop(final long jobId) {
        if (!scalingJobMap.containsKey(jobId)) {
            throw new ScalingJobNotFoundException(String.format("Can't find scaling job id %s", jobId));
        }
        scalingTaskSchedulerMap.get(jobId).stop();
        scalingJobMap.get(jobId).setStatus(SyncTaskControlStatus.STOPPED.name());
    }
    
    @Override
    public ShardingScalingJob getJob(final long jobId) {
        return scalingJobMap.get(jobId);
    }
    
    @Override
    public ScalingJobProgress getProgress(final long jobId) {
        if (!scalingJobMap.containsKey(jobId)) {
            throw new ScalingJobNotFoundException(String.format("Can't find scaling job id %s", jobId));
        }
        ShardingScalingJob shardingScalingJob = scalingJobMap.get(jobId);
        ScalingJobProgress result = new ScalingJobProgress(jobId, shardingScalingJob.getStatus());
        if (scalingTaskSchedulerMap.containsKey(jobId)) {
            result.getInventoryDataSyncTaskProgress().put("0", scalingTaskSchedulerMap.get(jobId).getInventoryDataTaskProgress());
            result.getIncrementalDataSyncTaskProgress().put("0", scalingTaskSchedulerMap.get(jobId).getIncrementalDataTaskProgress());
        }
        return result;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final long jobId) {
        if (!scalingJobMap.containsKey(jobId)) {
            throw new ScalingJobNotFoundException(String.format("Can't find scaling job id %s", jobId));
        }
        return dataConsistencyCheck(scalingJobMap.get(jobId));
    }
    
    @Override
    public void remove(final long jobId) {
        stop(jobId);
        scalingJobMap.remove(jobId);
        scalingTaskSchedulerMap.remove(jobId);
    }
}
