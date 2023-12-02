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
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.process.PipelineProcessConfigurationUtils;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.TransmissionJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.pojo.TransmissionJobItemInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.option.TransmissionJobOption;
import org.apache.shardingsphere.data.pipeline.core.metadata.PipelineProcessConfigurationPersistService;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;

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
    
    private final TransmissionJobOption jobOption;
    
    private final PipelineProcessConfigurationPersistService processConfigPersistService = new PipelineProcessConfigurationPersistService();
    
    /**
     * Alter process configuration.
     *
     * @param contextKey context key
     * @param processConfig process configuration
     */
    public void alterProcessConfiguration(final PipelineContextKey contextKey, final PipelineProcessConfiguration processConfig) {
        // TODO check rateLimiter type match or not
        processConfigPersistService.persist(contextKey, jobOption.getType(), processConfig);
    }
    
    /**
     * Show pipeline process configuration.
     *
     * @param jobId job id
     * @return pipeline process configuration
     */
    public PipelineProcessConfiguration showProcessConfiguration(final String jobId) {
        return showProcessConfiguration(PipelineJobIdUtils.parseContextKey(jobId));
    }
    
    /**
     * Show pipeline process configuration.
     *
     * @param contextKey context key
     * @return pipeline process configuration
     */
    public PipelineProcessConfiguration showProcessConfiguration(final PipelineContextKey contextKey) {
        return PipelineProcessConfigurationUtils.convertWithDefaultValue(processConfigPersistService.load(contextKey, jobOption.getType()));
    }
    
    /**
     * Get job infos.
     *
     * @param jobId job ID
     * @return job item infos
     */
    public List<TransmissionJobItemInfo> getJobItemInfos(final String jobId) {
        PipelineJobConfiguration jobConfig = new PipelineJobConfigurationManager(jobOption).getJobConfiguration(jobId);
        long startTimeMillis = Long.parseLong(Optional.ofNullable(PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId).getProps().getProperty("start_time_millis")).orElse("0"));
        Map<Integer, TransmissionJobItemProgress> jobProgress = getJobProgress(jobConfig);
        List<TransmissionJobItemInfo> result = new LinkedList<>();
        PipelineJobInfo jobInfo = jobOption.getJobInfo(jobId);
        for (Entry<Integer, TransmissionJobItemProgress> entry : jobProgress.entrySet()) {
            int shardingItem = entry.getKey();
            TransmissionJobItemProgress jobItemProgress = entry.getValue();
            String errorMessage = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemFacade().getErrorMessage().load(jobId, shardingItem);
            if (null == jobItemProgress) {
                result.add(new TransmissionJobItemInfo(shardingItem, jobInfo.getTableName(), null, startTimeMillis, 0, errorMessage));
                continue;
            }
            int inventoryFinishedPercentage = 0;
            if (JobStatus.EXECUTE_INCREMENTAL_TASK == jobItemProgress.getStatus() || JobStatus.FINISHED == jobItemProgress.getStatus()) {
                inventoryFinishedPercentage = 100;
            } else if (0 != jobItemProgress.getProcessedRecordsCount() && 0 != jobItemProgress.getInventoryRecordsCount()) {
                inventoryFinishedPercentage = (int) Math.min(100, jobItemProgress.getProcessedRecordsCount() * 100 / jobItemProgress.getInventoryRecordsCount());
            }
            result.add(new TransmissionJobItemInfo(shardingItem, jobInfo.getTableName(), jobItemProgress, startTimeMillis, inventoryFinishedPercentage, errorMessage));
        }
        return result;
    }
    
    /**
     * Get job progress.
     *
     * @param jobConfig pipeline job configuration
     * @return each sharding item progress
     */
    public Map<Integer, TransmissionJobItemProgress> getJobProgress(final PipelineJobConfiguration jobConfig) {
        PipelineJobItemManager<TransmissionJobItemProgress> jobItemManager = new PipelineJobItemManager<>(jobOption.getYamlJobItemProgressSwapper());
        String jobId = jobConfig.getJobId();
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        return IntStream.range(0, jobConfig.getJobShardingCount()).boxed().collect(LinkedHashMap::new, (map, each) -> {
            Optional<TransmissionJobItemProgress> jobItemProgress = jobItemManager.getProgress(jobId, each);
            jobItemProgress.ifPresent(optional -> optional.setActive(!jobConfigPOJO.isDisabled()));
            map.put(each, jobItemProgress.orElse(null));
        }, LinkedHashMap::putAll);
    }
}
