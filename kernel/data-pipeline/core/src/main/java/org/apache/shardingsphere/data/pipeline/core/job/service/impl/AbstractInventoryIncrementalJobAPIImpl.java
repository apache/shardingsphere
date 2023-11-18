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

package org.apache.shardingsphere.data.pipeline.core.job.service.impl;

import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.pojo.InventoryIncrementalJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.TableBasedPipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.job.service.InventoryIncrementalJobManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobItemManager;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobManager;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Abstract inventory incremental job API implementation.
 */
public abstract class AbstractInventoryIncrementalJobAPIImpl implements InventoryIncrementalJobAPI {
    
    @Override
    public List<InventoryIncrementalJobItemInfo> getJobItemInfos(final String jobId) {
        PipelineJobManager jobManager = new PipelineJobManager(this);
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        PipelineJobConfiguration jobConfig = jobManager.getJobConfiguration(jobConfigPOJO);
        long startTimeMillis = Long.parseLong(Optional.ofNullable(jobConfigPOJO.getProps().getProperty("start_time_millis")).orElse("0"));
        InventoryIncrementalJobManager inventoryIncrementalJobManager = new InventoryIncrementalJobManager(this);
        Map<Integer, InventoryIncrementalJobItemProgress> jobProgress = inventoryIncrementalJobManager.getJobProgress(jobConfig);
        List<InventoryIncrementalJobItemInfo> result = new LinkedList<>();
        PipelineJobItemManager<InventoryIncrementalJobItemProgress> jobItemManager = new PipelineJobItemManager<>(getYamlJobItemProgressSwapper());
        for (Entry<Integer, InventoryIncrementalJobItemProgress> entry : jobProgress.entrySet()) {
            int shardingItem = entry.getKey();
            TableBasedPipelineJobInfo jobInfo = (TableBasedPipelineJobInfo) getJobInfo(jobId);
            InventoryIncrementalJobItemProgress jobItemProgress = entry.getValue();
            String errorMessage = jobItemManager.getErrorMessage(jobId, shardingItem);
            if (null == jobItemProgress) {
                result.add(new InventoryIncrementalJobItemInfo(shardingItem, jobInfo.getTable(), null, startTimeMillis, 0, errorMessage));
                continue;
            }
            int inventoryFinishedPercentage = 0;
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus() || JobStatus.FINISHED == jobItemProgress.getStatus()) {
                inventoryFinishedPercentage = 100;
            } else if (0 != jobItemProgress.getProcessedRecordsCount() && 0 != jobItemProgress.getInventoryRecordsCount()) {
                inventoryFinishedPercentage = (int) Math.min(100, jobItemProgress.getProcessedRecordsCount() * 100 / jobItemProgress.getInventoryRecordsCount());
            }
            result.add(new InventoryIncrementalJobItemInfo(shardingItem, jobInfo.getTable(), jobItemProgress, startTimeMillis, inventoryFinishedPercentage, errorMessage));
        }
        return result;
    }
}
