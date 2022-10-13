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

package org.apache.shardingsphere.data.pipeline.core.api.impl;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemIncrementalTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobItemInventoryTasksProgress;
import org.apache.shardingsphere.data.pipeline.api.task.progress.InventoryTaskProgress;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobItemAPI;
import org.apache.shardingsphere.data.pipeline.core.context.InventoryIncrementalJobItemContext;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlInventoryIncrementalJobItemProgressSwapper;
import org.apache.shardingsphere.data.pipeline.core.task.IncrementalTask;
import org.apache.shardingsphere.data.pipeline.core.task.InventoryTask;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Inventory incremental job item API implementation.
 */
@Slf4j
public final class InventoryIncrementalJobItemAPIImpl implements PipelineJobItemAPI {
    
    private static final YamlInventoryIncrementalJobItemProgressSwapper SWAPPER = new YamlInventoryIncrementalJobItemProgressSwapper();
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        InventoryIncrementalJobItemContext context = (InventoryIncrementalJobItemContext) jobItemContext;
        InventoryIncrementalJobItemProgress jobItemProgress = new InventoryIncrementalJobItemProgress();
        jobItemProgress.setStatus(jobItemContext.getStatus());
        jobItemProgress.setSourceDatabaseType(jobItemContext.getJobConfig().getSourceDatabaseType());
        jobItemProgress.setDataSourceName(jobItemContext.getDataSourceName());
        jobItemProgress.setIncremental(getIncrementalTasksProgress(context.getIncrementalTasks()));
        jobItemProgress.setInventory(getInventoryTasksProgress(context.getInventoryTasks()));
        jobItemProgress.setProcessedRecordsCount(context.getProcessedRecordsCount());
        String value = YamlEngine.marshal(SWAPPER.swapToYamlConfiguration(jobItemProgress));
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), value);
    }
    
    private JobItemIncrementalTasksProgress getIncrementalTasksProgress(final Collection<IncrementalTask> incrementalTasks) {
        IncrementalTask incrementalTask = incrementalTasks.size() > 0 ? incrementalTasks.iterator().next() : null;
        return new JobItemIncrementalTasksProgress(null != incrementalTask ? incrementalTask.getTaskProgress() : null);
    }
    
    private JobItemInventoryTasksProgress getInventoryTasksProgress(final Collection<InventoryTask> inventoryTasks) {
        Map<String, InventoryTaskProgress> inventoryTaskProgressMap = new HashMap<>();
        for (InventoryTask each : inventoryTasks) {
            inventoryTaskProgressMap.put(each.getTaskId(), each.getTaskProgress());
        }
        return new JobItemInventoryTasksProgress(inventoryTaskProgressMap);
    }
    
    @Override
    public InventoryIncrementalJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        String data = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        return Strings.isNullOrEmpty(data) ? null : SWAPPER.swapToObject(YamlEngine.unmarshal(data, YamlInventoryIncrementalJobItemProgress.class));
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        InventoryIncrementalJobItemProgress jobItemProgress = getJobItemProgress(jobId, shardingItem);
        if (null == jobItemProgress) {
            log.warn("updateJobItemStatus, jobItemProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobItemProgress.setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(SWAPPER.swapToYamlConfiguration(jobItemProgress)));
    }
}
