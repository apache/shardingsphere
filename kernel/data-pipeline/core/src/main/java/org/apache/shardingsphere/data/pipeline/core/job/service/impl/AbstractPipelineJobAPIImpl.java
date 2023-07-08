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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.shardingsphere.data.pipeline.common.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.common.listener.PipelineElasticJobListener;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.common.pojo.PipelineJobMetaData;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyStartedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Abstract pipeline job API impl.
 */
@Slf4j
public abstract class AbstractPipelineJobAPIImpl implements PipelineJobAPI {
    
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public final String marshalJobId(final PipelineJobId pipelineJobId) {
        return PipelineJobIdUtils.marshalJobIdCommonPrefix(pipelineJobId) + marshalJobIdLeftPart(pipelineJobId);
    }
    
    protected abstract String marshalJobIdLeftPart(PipelineJobId pipelineJobId);
    
    @Override
    public List<PipelineJobInfo> list(final PipelineContextKey contextKey) {
        return getJobBriefInfos(contextKey).map(each -> getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos(final PipelineContextKey contextKey) {
        return PipelineAPIFactory.getJobStatisticsAPI(contextKey).getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"))
                .filter(each -> PipelineJobIdUtils.parseJobType(each.getJobName()).getCode().equals(getJobType().getCode()));
    }
    
    // TODO Add getJobInfo
    protected abstract PipelineJobInfo getJobInfo(String jobId);
    
    protected PipelineJobMetaData buildPipelineJobMetaData(final JobConfigurationPOJO jobConfigPOJO) {
        return new PipelineJobMetaData(jobConfigPOJO.getJobName(), !jobConfigPOJO.isDisabled(),
                jobConfigPOJO.getShardingTotalCount(), jobConfigPOJO.getProps().getProperty("create_time"), jobConfigPOJO.getProps().getProperty("stop_time"), jobConfigPOJO.getJobParameter());
    }
    
    @Override
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        ShardingSpherePreconditions.checkState(0 != jobConfig.getJobShardingCount(), () -> new PipelineJobCreationWithInvalidShardingCountException(jobId));
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId));
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), getJobClassName());
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(convertJobConfiguration(jobConfig)));
        return Optional.of(jobId);
    }
    
    protected abstract String getJobClassName();
    
    protected JobConfigurationPOJO convertJobConfiguration(final PipelineJobConfiguration jobConfig) {
        JobConfigurationPOJO result = new JobConfigurationPOJO();
        result.setJobName(jobConfig.getJobId());
        result.setShardingTotalCount(jobConfig.getJobShardingCount());
        result.setJobParameter(YamlEngine.marshal(swapToYamlJobConfiguration(jobConfig)));
        String createTimeFormat = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        result.getProps().setProperty("create_time", createTimeFormat);
        result.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        result.setJobListenerTypes(Collections.singletonList(PipelineElasticJobListener.class.getName()));
        return result;
    }
    
    protected abstract YamlPipelineJobConfiguration swapToYamlJobConfiguration(PipelineJobConfiguration jobConfig);
    
    protected abstract PipelineJobConfiguration getJobConfiguration(JobConfigurationPOJO jobConfigPOJO);
    
    @Override
    public void startDisabledJob(final String jobId) {
        PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        pipelineDistributedBarrier.unregister(PipelineMetaDataNode.getJobBarrierDisablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        ShardingSpherePreconditions.checkState(jobConfigPOJO.isDisabled(), () -> new PipelineJobHasAlreadyStartedException(jobId));
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().setProperty("start_time_millis", String.valueOf(System.currentTimeMillis()));
        jobConfigPOJO.getProps().remove("stop_time");
        jobConfigPOJO.getProps().remove("stop_time_millis");
        String barrierEnablePath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        pipelineDistributedBarrier.register(barrierEnablePath, jobConfigPOJO.getShardingTotalCount());
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobConfiguration(jobConfigPOJO);
        pipelineDistributedBarrier.await(barrierEnablePath, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void stop(final String jobId) {
        PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance(PipelineJobIdUtils.parseContextKey(jobId));
        pipelineDistributedBarrier.unregister(PipelineMetaDataNode.getJobBarrierEnablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        if (jobConfigPOJO.isDisabled()) {
            return;
        }
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        jobConfigPOJO.getProps().setProperty("stop_time_millis", String.valueOf(System.currentTimeMillis()));
        String barrierPath = PipelineMetaDataNode.getJobBarrierDisablePath(jobId);
        pipelineDistributedBarrier.register(barrierPath, jobConfigPOJO.getShardingTotalCount());
        PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).updateJobConfiguration(jobConfigPOJO);
        pipelineDistributedBarrier.await(barrierPath, 5, TimeUnit.SECONDS);
    }
    
    protected void dropJob(final String jobId) {
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        PipelineAPIFactory.getJobOperateAPI(contextKey).remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).deleteJob(jobId);
    }
    
    protected final JobConfigurationPOJO getElasticJobConfigPOJO(final String jobId) {
        JobConfigurationPOJO result = PipelineAPIFactory.getJobConfigurationAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobConfiguration(jobId);
        ShardingSpherePreconditions.checkNotNull(result, () -> new PipelineJobNotFoundException(jobId));
        return result;
    }
    
    @Override
    public String getType() {
        return getJobType().getType();
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return Optional.ofNullable(PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemErrorMessage(jobId, shardingItem)).orElse("");
    }
    
    @Override
    public void persistJobItemErrorMessage(final String jobId, final int shardingItem, final Object error) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        String value = "";
        if (null != error) {
            value = error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persist(key, value);
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).cleanJobItemErrorMessage(jobId, shardingItem);
    }
}
