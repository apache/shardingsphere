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

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobDetail;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyStartedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.elasticjob.lite.lifecycle.domain.JobBriefInfo;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    
    private final PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance();
    
    @Override
    public final String marshalJobId(final PipelineJobId pipelineJobId) {
        return PipelineJobIdUtils.marshalJobIdCommonPrefix(pipelineJobId) + marshalJobIdLeftPart(pipelineJobId);
    }
    
    protected abstract String marshalJobIdLeftPart(PipelineJobId pipelineJobId);
    
    @Override
    public List<? extends PipelineJobInfo> list() {
        checkModeConfig();
        return getJobBriefInfos().map(each -> getJobInfo(each.getJobName())).collect(Collectors.toList());
    }
    
    protected void checkModeConfig() {
        ModeConfiguration modeConfig = PipelineContext.getModeConfig();
        Preconditions.checkNotNull(modeConfig, "Mode configuration is required.");
        Preconditions.checkArgument("Cluster".equalsIgnoreCase(modeConfig.getType()), "Mode must be `Cluster`.");
    }
    
    private Stream<JobBriefInfo> getJobBriefInfos() {
        return PipelineAPIFactory.getJobStatisticsAPI().getAllJobsBriefInfo().stream().filter(each -> !each.getJobName().startsWith("_"))
                .filter(each -> PipelineJobIdUtils.parseJobType(each.getJobName()) == getJobType());
    }
    
    protected abstract PipelineJobInfo getJobInfo(String jobId);
    
    protected void fillJobDetail(final PipelineJobDetail jobDetail, final JobConfigurationPOJO jobConfigPOJO) {
        jobDetail.setActive(!jobConfigPOJO.isDisabled());
        jobDetail.setShardingTotalCount(jobConfigPOJO.getShardingTotalCount());
        jobDetail.setCreateTime(jobConfigPOJO.getProps().getProperty("create_time"));
        jobDetail.setStopTime(jobConfigPOJO.getProps().getProperty("stop_time"));
        jobDetail.setJobParameter(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        Preconditions.checkState(0 != jobConfig.getJobShardingCount(), new PipelineJobCreationWithInvalidShardingCountException(jobId));
        log.info("Start job by {}", jobConfig);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String jobConfigKey = PipelineMetaDataNode.getJobConfigPath(jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), getJobClassName());
        repositoryAPI.persist(jobConfigKey, convertJobConfigurationToText(jobConfig));
        return Optional.of(jobId);
    }
    
    protected abstract String getJobClassName();
    
    private String convertJobConfigurationToText(final PipelineJobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(jobConfig.getJobId());
        jobConfigPOJO.setShardingTotalCount(jobConfig.getJobShardingCount());
        jobConfigPOJO.setJobParameter(YamlEngine.marshal(swapToYamlJobConfiguration(jobConfig)));
        jobConfigPOJO.getProps().setProperty("create_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    protected abstract YamlPipelineJobConfiguration swapToYamlJobConfiguration(PipelineJobConfiguration jobConfig);
    
    protected abstract PipelineJobConfiguration getJobConfiguration(JobConfigurationPOJO jobConfigPOJO);
    
    @Override
    public void startDisabledJob(final String jobId) {
        log.info("Start disabled pipeline job {}", jobId);
        pipelineDistributedBarrier.removeParentNode(PipelineMetaDataNode.getJobBarrierDisablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        ShardingSpherePreconditions.checkState(jobConfigPOJO.isDisabled(), () -> new PipelineJobHasAlreadyStartedException(jobId));
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().remove("stop_time");
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
        String barrierPath = PipelineMetaDataNode.getJobBarrierEnablePath(jobId);
        pipelineDistributedBarrier.register(barrierPath, jobConfigPOJO.getShardingTotalCount());
        pipelineDistributedBarrier.await(barrierPath, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void stop(final String jobId) {
        log.info("Stop pipeline job {}", jobId);
        pipelineDistributedBarrier.removeParentNode(PipelineMetaDataNode.getJobBarrierEnablePath(jobId));
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
        String barrierPath = PipelineMetaDataNode.getJobBarrierDisablePath(jobId);
        pipelineDistributedBarrier.register(barrierPath, jobConfigPOJO.getShardingTotalCount());
        pipelineDistributedBarrier.await(barrierPath, 5, TimeUnit.SECONDS);
    }
    
    protected void dropJob(final String jobId) {
        log.info("Drop job {}", jobId);
        PipelineAPIFactory.getJobOperateAPI().remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI().deleteJob(jobId);
    }
    
    protected final JobConfigurationPOJO getElasticJobConfigPOJO(final String jobId) {
        JobConfigurationPOJO result = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        Preconditions.checkNotNull(result, new PipelineJobNotFoundException(jobId));
        return result;
    }
    
    @Override
    public String getType() {
        return getJobType().getTypeName();
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return ObjectUtils.defaultIfNull(PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemErrorMessage(jobId, shardingItem), "");
    }
    
    @Override
    public void persistJobItemErrorMessage(final String jobId, final int shardingItem, final Object error) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        String value = "";
        if (null != error) {
            value = error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().persist(key, value);
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        PipelineAPIFactory.getGovernanceRepositoryAPI().cleanJobItemErrorMessage(jobId, shardingItem);
    }
}
