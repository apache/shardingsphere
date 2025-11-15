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
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.core.job.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.config.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.core.job.id.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.core.job.type.PipelineJobType;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.core.registrycenter.repository.PipelineGovernanceFacade;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.ShardingInfo;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.datetime.DateTimeFormatterFactory;

import java.time.LocalDateTime;
import java.util.Collection;
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
    
    @SuppressWarnings("rawtypes")
    private final PipelineJobType jobType;
    
    /**
     * Start job.
     *
     * @param jobConfig job configuration
     */
    public void start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobId));
        PipelineGovernanceFacade governanceFacade = PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId));
        if (governanceFacade.getJobFacade().getConfiguration().isExisted(jobId)) {
            log.warn("jobId already exists in registry center, ignore, job id is `{}`", jobId);
            return;
        }
        governanceFacade.getJobFacade().getJob().create(jobId, jobType.getOption().getJobClass());
        governanceFacade.getJobFacade().getConfiguration().persist(jobId, new PipelineJobConfigurationManager(jobType.getOption()).convertToJobConfigurationPOJO(jobConfig));
    }
    
    /**
     * Resume disabled job.
     *
     * @param jobId job id
     */
    public void resume(final String jobId) {
        if (jobType.getOption().isIgnoreToStartDisabledJobWhenJobItemProgressIsFinished()) {
            Optional<? extends PipelineJobItemProgress> jobItemProgress = new PipelineJobItemManager<>(jobType.getOption().getYamlJobItemProgressSwapper()).getProgress(jobId, 0);
            if (jobItemProgress.isPresent() && JobStatus.FINISHED == jobItemProgress.get().getStatus()) {
                log.info("job status is FINISHED, ignore, jobId={}", jobId);
                return;
            }
        }
        startCurrentDisabledJob(jobId);
        String toBeStartDisabledNextJobType = jobType.getOption().getToBeStartDisabledNextJobType();
        if (null != toBeStartDisabledNextJobType) {
            startNextDisabledJob(jobId, toBeStartDisabledNextJobType);
        }
    }
    
    private void startCurrentDisabledJob(final String jobId) {
        PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        pipelineDistributedBarrier.unregister(PipelineMetaDataNode.getJobBarrierDisablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = PipelineJobIdUtils.getElasticJobConfigurationPOJO(jobId);
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        jobConfigPOJO.getProps().remove("stop_time");
        jobConfigPOJO.getProps().setProperty("run_count", String.valueOf(Integer.parseInt(jobConfigPOJO.getProps().getProperty("run_count", "0")) + 1));
        String barrierEnablePath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        pipelineDistributedBarrier.register(barrierEnablePath, jobConfigPOJO.getShardingTotalCount());
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobConfiguration(jobConfigPOJO);
        pipelineDistributedBarrier.await(barrierEnablePath, 5L, TimeUnit.SECONDS);
    }
    
    private void startNextDisabledJob(final String jobId, final String toBeStartDisabledNextJobType) {
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobFacade().getCheck().findLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobType.class, toBeStartDisabledNextJobType)).resume(optional);
                // CHECKSTYLE:OFF
            } catch (final RuntimeException ex) {
                // CHECKSTYLE:ON
                log.warn("start related check job failed, check job id: {}, error: {}", optional, ex.getMessage());
            }
        });
    }
    
    /**
     * Stop job.
     *
     * @param jobId job id
     */
    public void stop(final String jobId) {
        String toBeStoppedPreviousJobType = jobType.getOption().getToBeStoppedPreviousJobType();
        if (null != toBeStoppedPreviousJobType) {
            stopPreviousJob(jobId, toBeStoppedPreviousJobType);
        }
        stopCurrentJob(jobId);
    }
    
    private void stopPreviousJob(final String jobId, final String toBeStoppedPreviousJobType) {
        PipelineAPIFactory.getPipelineGovernanceFacade(PipelineJobIdUtils.parseContextKey(jobId)).getJobFacade().getCheck().findLatestCheckJobId(jobId).ifPresent(optional -> {
            try {
                new PipelineJobManager(TypedSPILoader.getService(PipelineJobType.class, toBeStoppedPreviousJobType)).stop(optional);
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
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DateTimeFormatterFactory.getDatetimeFormatter()));
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
        PipelineAPIFactory.getPipelineGovernanceFacade(contextKey).getJobFacade().getJob().delete(jobId);
    }
    
    /**
     * Get pipeline jobs info.
     *
     * @param contextKey context key
     * @return jobs info
     */
    @SuppressWarnings("unchecked")
    public List<PipelineJobInfo> getJobInfos(final PipelineContextKey contextKey) {
        try {
            return PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo().stream().filter(this::isValidJob)
                    .map(each -> new PipelineJobInfo(new PipelineJobMetaData(PipelineJobIdUtils.getElasticJobConfigurationPOJO(each.getJobName())),
                            jobType.getJobTarget(new PipelineJobConfigurationManager(jobType.getOption()).getJobConfiguration(each.getJobName()))))
                    .collect(Collectors.toList());
        } catch (final UnsupportedOperationException ex) {
            return Collections.emptyList();
        }
    }
    
    private boolean isValidJob(final JobBriefInfo jobInfo) {
        return !jobInfo.getJobName().startsWith("_") && jobType.getOption().isTransmissionJob() && jobType.getType().equals(PipelineJobIdUtils.parseJobType(jobInfo.getJobName()).getType());
    }
    
    /**
     * Get pipeline job sharding info.
     *
     * @param contextKey context key
     * @param jobId job id
     * @return job sharding info
     */
    public Collection<ShardingInfo> getJobShardingInfos(final PipelineContextKey contextKey, final String jobId) {
        try {
            return PipelineAPIFactory.getShardingStatisticsAPI(contextKey).getShardingInfo(jobId);
        } catch (final UnsupportedOperationException ex) {
            return Collections.emptyList();
        }
    }
}
