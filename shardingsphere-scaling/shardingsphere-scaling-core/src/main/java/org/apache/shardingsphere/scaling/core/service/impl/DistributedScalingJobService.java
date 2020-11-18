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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.scaling.core.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.ScalingJobProgress;
import org.apache.shardingsphere.scaling.core.job.ShardingScalingJob;
import org.apache.shardingsphere.scaling.core.job.SyncProgress;
import org.apache.shardingsphere.scaling.core.job.position.InventoryPositionGroup;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalDataSyncTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryDataSyncTaskProgress;
import org.apache.shardingsphere.scaling.core.schedule.SyncTaskControlStatus;
import org.apache.shardingsphere.scaling.core.service.AbstractScalingJobService;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Distributed scaling job service.
 */
public final class DistributedScalingJobService extends AbstractScalingJobService implements ScalingJobService {
    
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    @Override
    public List<ShardingScalingJob> listJobs() {
        return REGISTRY_REPOSITORY.getChildrenKeys(ScalingConstant.SCALING_LISTENER).stream().map(each -> getJob(Long.parseLong(each))).collect(Collectors.toList());
    }
    
    @Override
    public Optional<ShardingScalingJob> start(final ScalingConfiguration scalingConfiguration) {
        ShardingScalingJob shardingScalingJob = new ShardingScalingJob();
        REGISTRY_REPOSITORY.persist(ScalingTaskUtil.assemblingPath(shardingScalingJob.getJobId(), ScalingConstant.CONFIG), new Gson().toJson(scalingConfiguration));
        REGISTRY_REPOSITORY.persist(ScalingTaskUtil.assemblingPath(shardingScalingJob.getJobId(), ScalingConstant.STATUS), shardingScalingJob.getStatus());
        return Optional.of(shardingScalingJob);
    }
    
    @Override
    public void stop(final long jobId) {
        REGISTRY_REPOSITORY.persist(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.STATUS), SyncTaskControlStatus.STOPPING.name());
    }
    
    @Override
    public ShardingScalingJob getJob(final long jobId) {
        ShardingScalingJob result = new ShardingScalingJob();
        result.setScalingConfiguration(GSON.fromJson(REGISTRY_REPOSITORY.get(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.CONFIG)), ScalingConfiguration.class));
        result.setStatus(REGISTRY_REPOSITORY.get(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.STATUS)));
        return result;
    }
    
    @Override
    public ScalingJobProgress getProgress(final long jobId) {
        ScalingJobProgress result = new ScalingJobProgress(jobId, REGISTRY_REPOSITORY.get(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.STATUS)));
        List<String> shardingItems = REGISTRY_REPOSITORY.getChildrenKeys(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.POSITION));
        for (String each : shardingItems) {
            result.getInventoryDataSyncTaskProgress().put(each, getInventoryDataSyncTaskProgress(jobId, each));
            result.getIncrementalDataSyncTaskProgress().put(each, getIncrementalDataSyncTaskProgress(jobId, each));
        }
        return result;
    }
    
    private List<SyncProgress> getInventoryDataSyncTaskProgress(final long jobId, final String shardingItem) {
        InventoryPositionGroup inventoryPositionGroup = InventoryPositionGroup.fromJson(
                REGISTRY_REPOSITORY.get(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.POSITION, shardingItem, ScalingConstant.INVENTORY)));
        List<SyncProgress> result = inventoryPositionGroup.getUnfinished().keySet().stream().map(each -> new InventoryDataSyncTaskProgress(each, false)).collect(Collectors.toList());
        result.addAll(inventoryPositionGroup.getFinished().stream().map(each -> new InventoryDataSyncTaskProgress(each, true)).collect(Collectors.toList()));
        return result;
    }
    
    private List<SyncProgress> getIncrementalDataSyncTaskProgress(final long jobId, final String shardingItem) {
        JsonObject jsonObject = GSON.fromJson(REGISTRY_REPOSITORY.get(ScalingTaskUtil.assemblingPath(jobId, ScalingConstant.POSITION, shardingItem, ScalingConstant.INCREMENTAL)), JsonObject.class);
        return jsonObject.entrySet().stream()
                .map(entry -> new IncrementalDataSyncTaskProgress(entry.getKey(), entry.getValue().getAsJsonObject().get(ScalingConstant.DELAY).getAsLong(), null))
                .collect(Collectors.toList());
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final long jobId) {
        return dataConsistencyCheck(getJob(jobId));
    }
    
    @Override
    public void remove(final long jobId) {
        REGISTRY_REPOSITORY.delete(ScalingTaskUtil.assemblingPath(jobId));
    }
}
