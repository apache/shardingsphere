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

package org.apache.shardingsphere.data.pipeline.core.job.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobTarget;
import org.apache.shardingsphere.data.pipeline.core.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.IntStream;

/**
 * Transmission job manager.
 */
@RequiredArgsConstructor
public final class TransmissionJobManager {
    
    @SuppressWarnings("rawtypes")
    private final PipelineJobType jobType;
    
    /**
     * Get job infos.
     *
     * @param jobId job ID
     * @return job item infos
     */
    @SuppressWarnings("unchecked")
    public Collection<TransmissionJobItemInfo> getJobItemInfos(final String jobId) {
        PipelineJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(jobId);
        long startTimeMillis = Long.parseLong(Optional.ofNullable(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).getProps().getProperty("start_time_millis")).orElse("0"));
        Map<Integer, TransmissionJobItemProgress> jobProgress = getJobProgress(jobConfig);
        List<TransmissionJobItemInfo> result = new LinkedList<>();
        PipelineJobTarget jobTarget = jobType.getJobTarget(jobConfig);
        for (Entry<Integer, TransmissionJobItemProgress> entry : jobProgress.entrySet()) {
            int shardingItem = entry.getKey();
            TransmissionJobItemProgress jobItemProgress = entry.getValue();
            String errorMessage = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().load(jobId, shardingItem);
            if (null == jobItemProgress) {
                result.add(new TransmissionJobItemInfo(shardingItem, jobTarget.getTableName(), null, startTimeMillis, 0, errorMessage));
                continue;
            }
            int inventoryFinishedPercentage = getInventoryFinishedPercentage(jobItemProgress);
            result.add(new TransmissionJobItemInfo(shardingItem, jobTarget.getTableName(), jobItemProgress, startTimeMillis, inventoryFinishedPercentage, errorMessage));
        }
        return result;
    }
    
    /**
     * Get inventory finished percentage.
     *
     * @param jobItemProgress job item progress
     * @return inventory finished percentage
     */
    public static int getInventoryFinishedPercentage(final TransmissionJobItemProgress jobItemProgress) {
        if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus() || JobStatus.FINISHED == jobItemProgress.getStatus()) {
            return 100;
        }
        if (0L != jobItemProgress.getProcessedRecordsCount() && 0L != jobItemProgress.getInventoryRecordsCount()) {
            return (int) Math.min(100L, jobItemProgress.getProcessedRecordsCount() * 100L / jobItemProgress.getInventoryRecordsCount());
        }
        return 0;
    }
    
    /**
     * Get job progress.
     *
     * @param jobConfig pipeline job configuration
     * @return each sharding item progress
     */
    public Map<Integer, TransmissionJobItemProgress> getJobProgress(final PipelineJobConfiguration jobConfig) {
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper());
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        return IntStream.range(0, jobConfig.getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            Optional<TransmissionJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobId, each);
            jobItemProgress.ifPresent(optional -> optional.setActive(!jobConfigPOJO.isDisabled()));
            map.put(each, jobItemProgress.orElse(null));
        }, LinkedHashMap::putAll);
    }
}
