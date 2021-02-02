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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEventListener;
import org.apache.shardingsphere.scaling.core.api.RegistryRepositoryAPI;
import org.apache.shardingsphere.scaling.core.internal.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.job.position.JobProgress;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTask;
import org.apache.shardingsphere.scaling.core.job.task.incremental.IncrementalTaskProgress;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTask;
import org.apache.shardingsphere.scaling.core.job.task.inventory.InventoryTaskProgress;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry repository API impl.
 */
@RequiredArgsConstructor
@Slf4j
public final class RegistryRepositoryAPIImpl implements RegistryRepositoryAPI {
    
    private final RegistryRepository registryRepository;
    
    @Override
    public void persistJobProgress(final JobContext jobContext) {
        JobProgress jobPosition = new JobProgress();
        jobPosition.setStatus(jobContext.getStatus());
        jobPosition.setDatabaseType(jobContext.getJobConfig().getHandleConfig().getDatabaseType());
        jobPosition.setIncrementalTaskProgressMap(getIncrementalTaskProgressMap(jobContext));
        jobPosition.setInventoryTaskProgressMap(getInventoryTaskProgressMap(jobContext));
        registryRepository.persist(getOffsetPath(jobContext.getJobId(), jobContext.getShardingItem()), jobPosition.toJson());
    }
    
    private Map<String, IncrementalTaskProgress> getIncrementalTaskProgressMap(final JobContext jobContext) {
        Map<String, IncrementalTaskProgress> result = new HashMap<>();
        for (IncrementalTask each : jobContext.getIncrementalTasks()) {
            result.put(each.getTaskId(), each.getProgress());
        }
        return result;
    }
    
    private Map<String, InventoryTaskProgress> getInventoryTaskProgressMap(final JobContext jobContext) {
        Map<String, InventoryTaskProgress> result = new HashMap<>();
        for (InventoryTask each : jobContext.getInventoryTasks()) {
            result.put(each.getTaskId(), each.getProgress());
        }
        return result;
    }
    
    @Override
    public JobProgress getJobProgress(final long jobId, final int shardingItem) {
        String data = null;
        try {
            data = registryRepository.get(getOffsetPath(jobId, shardingItem));
        } catch (final NullPointerException ex) {
            log.info("job {}-{} without break point.", jobId, shardingItem);
        }
        return Strings.isNullOrEmpty(data) ? null : JobProgress.fromJson(data);
    }
    
    @Override
    public void deleteJob(final long jobId) {
        log.info("delete job {}", jobId);
        registryRepository.delete(String.format("%s/%d", ScalingConstant.SCALING_ROOT, jobId));
    }
    
    @Override
    public List<String> getChildrenKeys(final String key) {
        return registryRepository.getChildrenKeys(key);
    }
    
    @Override
    public void watch(final String key, final DataChangedEventListener listener) {
        registryRepository.watch(key, listener);
    }
    
    @Override
    public void persist(final String key, final String value) {
        registryRepository.persist(key, value);
    }
    
    private String getOffsetPath(final long jobId, final int shardingItem) {
        return String.format("%s/%d/offset/%d", ScalingConstant.SCALING_ROOT, jobId, shardingItem);
    }
}
