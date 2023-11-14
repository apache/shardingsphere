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
import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.common.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.common.registrycenter.repository.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.common.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobCreationWithInvalidShardingCountException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyStartedException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.service.PipelineJobAPI;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Abstract pipeline job API impl.
 */
@Slf4j
public abstract class AbstractPipelineJobAPIImpl implements PipelineJobAPI {
    
    protected static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
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
        repositoryAPI.persist(PipelineMetaDataNode.getJobRootPath(jobId), getPipelineJobClass().getName());
        repositoryAPI.persist(jobConfigKey, YamlEngine.marshal(jobConfig.convertToJobConfigurationPOJO()));
        return Optional.of(jobId);
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
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
        pipelineDistributedBarrier.await(barrierEnablePath, 5, TimeUnit.SECONDS);
    }
    
    @Override
    public void stop(final String jobId) {
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
        pipelineDistributedBarrier.await(barrierPath, 5, TimeUnit.SECONDS);
    }
    
    protected void dropJob(final String jobId) {
        PipelineContextKey contextKey = PipelineJobIdUtils.parseContextKey(jobId);
        PipelineAPIFactory.getJobOperateAPI(contextKey).remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI(contextKey).deleteJob(jobId);
    }
    
    @Override
    public String getJobItemErrorMessage(final String jobId, final int shardingItem) {
        return Optional.ofNullable(PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).getJobItemErrorMessage(jobId, shardingItem)).orElse("");
    }
    
    @Override
    public void updateJobItemErrorMessage(final String jobId, final int shardingItem, final Object error) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        String value = "";
        if (null != error) {
            value = error instanceof Throwable ? ExceptionUtils.getStackTrace((Throwable) error) : error.toString();
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).update(key, value);
    }
    
    @Override
    public void cleanJobItemErrorMessage(final String jobId, final int shardingItem) {
        String key = PipelineMetaDataNode.getJobItemErrorMessagePath(jobId, shardingItem);
        PipelineAPIFactory.getGovernanceRepositoryAPI(PipelineJobIdUtils.parseContextKey(jobId)).persist(key, "");
    }
}
