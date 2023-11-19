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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.common.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyStartedException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Pipeline job manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobManager {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final PipelineJobAPI jobAPI;
    
    /**
     * Get job configuration.
     *
     * @param jobConfigPOJO job configuration POJO
     * @param <T> type of pipeline job configuration
     * @return pipeline job configuration
     */
    @SuppressWarnings("unchecked")
    public <T extends PipelineJobConfiguration> T getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return (T) jobAPI.getYamlJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     * @return job id
     */
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobId));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId));
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), jobAPI.getJobClass().getName());
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfig.convertToJobConfigurationPOJO()));
        return Optional.of(jobId);
    }
    
    /**
     * Start disabled job.
     *
     * @param jobId job id
     */
    public void startDisabledJob(final String jobId) {
        if (jobAPI.isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished()) {
            Optional<? extends PipelineJobItemProgress> jobItemProgress = new PipelineJobItemManager<>(jobAPI.getYamlJobItemProgressSwapper()).getProgress(jobId, 0);
            if (jobItemProgress.isPresent() && JobStatus.FINISHED == jobItemProgress.get().getStatus()) {
                log.info("job status is FINISHED, ignore, jobId={}", jobId);
                return;
            }
        }
        startCurrentDisabledJob(jobId);
        jobAPI.getToBeStartDisabledNextJobType().ifPresent(optional -> startNextDisabledJob(jobId, optional));
        
    }
    
    private void startCurrentDisabledJob(final String jobId) {
        PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        pipelineDistributedBarrier.unregister(PipelineMetaDataNode.getJobBarrierDisablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        ShardingSpherePreconditions.checkState(jobConfigPOJO.isDisabled(), () -> new PipelineJobHasAlreadyStartedException(jobId));
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        jobConfigPOJO.getProps().remove("stop_time");
        jobConfigPOJO.getProps().remove("stop_time_millis");
        jobConfigPOJO.getProps().setProperty("run_count", String.valueOf(Integer.parseInt(jobConfigPOJO.getProps().getProperty("run_count", "0")) + 1));
        String barrierEnablePath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        pipelineDistributedBarrier.register(barrierEnablePath, jobConfigPOJO.getShardingTotalCount());
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobConfiguration(jobConfigPOJO);
        pipelineDistributedBarrier.await(barrierEnablePath, 5L, TimeUnit.SECONDS);
    }
    
    private void startNextDisabledJob(final String jobId, final String toBeStartDisabledNextJobType) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobAPI.class, toBeStartDisabledNextJobType)).startDisabledJob(optional);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.warn("start related check job failed, check job id: {}, error: {}", optional, ex.getMessage());
            }
        });
    }
    
    /**
     * Stop pipeline job.
     *
     * @param jobId job id
     */
    public void stop(final String jobId) {
        jobAPI.getToBeStoppedPreviousJobType().ifPresent(optional -> stopPreviousJob(jobId, optional));
        stopCurrentJob(jobId);
    }
    
    private void stopPreviousJob(final String jobId, final String toBeStoppedPreviousJobType) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobAPI.class, toBeStoppedPreviousJobType)).stop(optional);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.warn("stop related check job failed, check job id: {}, error: {}", optional, ex.getMessage());
            }
        });
    }
    
    private void stopCurrentJob(final String jobId) {
        PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        pipelineDistributedBarrier.unregister(PipelineMetaDataNode.getJobBarrierEnablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        if (jobConfigPOJO.isDisabled()) {
            return;
        }
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        jobConfigPOJO.getProps().setProperty("stop_time_millis", String.valueOf(System.currentTimeMillis()));
        String barrierPath = PipelineMetaDataNode.getJobBarrierDisablePath(jobId);
        pipelineDistributedBarrier.register(barrierPath, jobConfigPOJO.getShardingTotalCount());
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobConfiguration(jobConfigPOJO);
        pipelineDistributedBarrier.await(barrierPath, 5L, TimeUnit.SECONDS);
    }
    
    /**
     * Drop job.
     * 
     * @param jobId to be drooped job id
     */
    public void drop(final String jobId) {
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        PipelineAPIFactory.getJobOperateAPI(contextKey).remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).deleteJob(jobId);
    }
    
    /**
     * Get pipeline jobs info.
     *
     * @param contextKey context key
     * @return jobs info
     */
    public List<PipelineJobInfo> getJobInfos(final PipelineContextKey contextKey) {
        if (jobAPI instanceof InventoryIncrementalJobAPI) {
            return PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo().stream()
                    .filter(each -> !each.getJobName().startsWith("_") && jobAPI.getType().equals(PipelineJobIdUtils.parseJobType(each.getJobName()).getType()))
                    .map(each -> ((InventoryIncrementalJobAPI) jobAPI).getJobInfo(each.getJobName())).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
