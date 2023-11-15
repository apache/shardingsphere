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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Pipeline job manager.
 */
@RequiredArgsConstructor
@Slf4j
public final class PipelineJobManager {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final PipelineJobAPI pipelineJobAPI;
    
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
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), pipelineJobAPI.getPipelineJobClass().getName());
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfig.convertToJobConfigurationPOJO()));
        return Optional.of(jobId);
    }
    
    /**
     * Stop pipeline job.
     *
     * @param jobId job id
     */
    public void stop(final String jobId) {
        pipelineJobAPI.getToBeStoppedPreviousJobType().ifPresent(optional -> stopPreviousJob(jobId, optional));
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
    public List<PipelineJobInfo> getPipelineJobInfos(final PipelineContextKey contextKey) {
        return getJobBriefInfos(contextKey, pipelineJobAPI.getType()).map(each -> pipelineJobAPI.getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos(final PipelineContextKey contextKey, final String jobType) {
        return PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"))
                .filter(each -> jobType.equals(PipelineJobIdUtils.parseJobType(each.getJobName()).getType()));
    }
    
    /**
     * Get job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @return map, key is sharding item, value is error message
     */
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return Optional.ofNullable(PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemErrorMessage(jobId, shardingItem)).orElse("");
    }
    
    /**
     * Update job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     * @param error error
     */
    public void updateJobItemErrorMessage(final String jobId, final int shardingItem, final Object error) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        String value = "";
        if (null != error) {
            value = error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).update(key, value);
    }
    
    /**
     * Clean job item error message.
     *
     * @param jobId job id
     * @param shardingItem sharding item
     */
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persist(key, "");
    }
}
