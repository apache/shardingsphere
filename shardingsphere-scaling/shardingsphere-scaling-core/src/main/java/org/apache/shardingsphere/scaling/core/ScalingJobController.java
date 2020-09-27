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

import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.exception.ScalingJobNotFoundException;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.preparer.ShardingScalingJobPreparer;
import org.apache.shardingsphere.scaling.core.schedule.ScalingTaskScheduler;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyChecker;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Scaling job controller.
 */
public final class ScalingJobController {
    
    private final ConcurrentMap<Integer, ShardingScalingJob> scalingJobMap = new ConcurrentHashMap<>();
    
    private final ConcurrentMap<Integer, ScalingTaskScheduler> scalingTaskSchedulerMap = new ConcurrentHashMap<>();
    
    private final ShardingScalingJobPreparer shardingScalingJobPreparer = new ShardingScalingJobPreparer();
    
    /**
     * Start data nodes migrate.
     *
     * @param shardingScalingJob sharding scaling job
     */
    public void start(final ShardingScalingJob shardingScalingJob) {
        scalingJobMap.put(shardingScalingJob.getJobId(), shardingScalingJob);
        shardingScalingJobPreparer.prepare(shardingScalingJob);
        if (SyncTaskControlStatus.PREPARING_FAILURE.name().equals(shardingScalingJob.getStatus())) {
            return;
        }
        ScalingTaskScheduler scalingTaskScheduler = new ScalingTaskScheduler(shardingScalingJob);
        scalingTaskScheduler.start();
        scalingTaskSchedulerMap.put(shardingScalingJob.getJobId(), scalingTaskScheduler);
    }
    
    /**
     * Stop data nodes migrate.
     *
     * @param shardingScalingJobId sharding scaling job id
     */
    public void stop(final int shardingScalingJobId) {
        if (!scalingJobMap.containsKey(shardingScalingJobId)) {
            return;
        }
        scalingTaskSchedulerMap.get(shardingScalingJobId).stop();
        scalingJobMap.get(shardingScalingJobId).setStatus(SyncTaskControlStatus.STOPPED.name());
    }
    
    /**
     * Get data nodes migrate progresses.
     *
     * @param shardingScalingJobId sharding scaling job id
     * @return data nodes migrate progress
     */
    public SyncProgress getProgresses(final int shardingScalingJobId) {
        if (!scalingJobMap.containsKey(shardingScalingJobId)) {
            throw new ScalingJobNotFoundException(String.format("Can't find scaling job id %s", shardingScalingJobId));
        }
        ShardingScalingJob shardingScalingJob = scalingJobMap.get(shardingScalingJobId);
        ScalingJobProgress result = new ScalingJobProgress(shardingScalingJobId, shardingScalingJob.getJobName(), shardingScalingJob.getStatus());
        if (scalingTaskSchedulerMap.containsKey(shardingScalingJobId)) {
            result.getInventoryDataTasks().addAll(scalingTaskSchedulerMap.get(shardingScalingJobId).getInventoryDataTaskProgress());
            result.getIncrementalDataTasks().addAll(scalingTaskSchedulerMap.get(shardingScalingJobId).getIncrementalDataTaskProgress());
        }
        return result;
    }
    
    /**
     * Execute data consistency check.
     *
     * @param shardingScalingJobId sharding scaling job id
     * @return check result
     */
    public Map<String, DataConsistencyCheckResult> check(final int shardingScalingJobId) {
        if (!scalingJobMap.containsKey(shardingScalingJobId)) {
            throw new ScalingJobNotFoundException(String.format("Can't find scaling job id %s", shardingScalingJobId));
        }
        DataConsistencyChecker dataConsistencyChecker = scalingJobMap.get(shardingScalingJobId).getDataConsistencyChecker();
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        return result;
    }
    
    /**
     * List all sharding scaling jobs.
     *
     * @return list of sharding scaling jobs
     */
    public List<ShardingScalingJob> listShardingScalingJobs() {
        return new LinkedList<>(scalingJobMap.values());
    }
}
