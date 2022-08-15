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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineVerifyFailedException;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJob;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

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
    public Optional<String> start(final PipelineJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        if (0 == jobConfig.getJobShardingCount()) {
            log.warn("Invalid job config since job sharding count is 0, jobId={}", jobId);
            throw new PipelineJobCreationException("job sharding count is 0, jobId: " + jobId);
        }
        log.info("Start job by {}", jobConfig);
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String jobConfigKey = PipelineMetaDataNode.getScalingJobConfigPath(jobId);
        if (repositoryAPI.isExisted(jobConfigKey)) {
            log.warn("jobId already exists in registry center, ignore, jobConfigKey={}", jobConfigKey);
            return Optional.of(jobId);
        }
        repositoryAPI.persist(PipelineMetaDataNode.getScalingJobPath(jobId), MigrationJob.class.getName());
        repositoryAPI.persist(jobConfigKey, convertJobConfigurationToText(jobConfig));
        return Optional.of(jobId);
    }
    
    private String convertJobConfigurationToText(final PipelineJobConfiguration jobConfig) {
        JobConfigurationPOJO jobConfigPOJO = new JobConfigurationPOJO();
        jobConfigPOJO.setJobName(jobConfig.getJobId());
        jobConfigPOJO.setShardingTotalCount(jobConfig.getJobShardingCount());
        jobConfigPOJO.setJobParameter(YamlEngine.marshal(swapToYamlJobConfiguration(jobConfig)));
        jobConfigPOJO.getProps().setProperty("create_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        return YamlEngine.marshal(jobConfigPOJO);
    }
    
    protected abstract YamlPipelineJobConfiguration swapToYamlJobConfiguration(PipelineJobConfiguration jobConfig);
    
    @Override
    public void startDisabledJob(final String jobId) {
        log.info("Start disabled pipeline job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        if (!jobConfigPOJO.isDisabled()) {
            throw new PipelineVerifyFailedException("Job is already started.");
        }
        jobConfigPOJO.setDisabled(false);
        jobConfigPOJO.getProps().remove("stop_time");
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void stop(final String jobId) {
        log.info("Stop pipeline job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        jobConfigPOJO.setDisabled(true);
        jobConfigPOJO.getProps().setProperty("stop_time", LocalDateTime.now().format(DATE_TIME_FORMATTER));
        // TODO updateJobConfiguration might doesn't work
        PipelineAPIFactory.getJobConfigurationAPI().updateJobConfiguration(jobConfigPOJO);
    }
    
    @Override
    public void remove(final String jobId) {
        log.info("Remove pipeline job {}", jobId);
        JobConfigurationPOJO jobConfigPOJO = getElasticJobConfigPOJO(jobId);
        verifyJobStopped(jobConfigPOJO);
        // TODO release lock
        PipelineAPIFactory.getJobOperateAPI().remove(String.valueOf(jobId), null);
        PipelineAPIFactory.getGovernanceRepositoryAPI().deleteJob(jobId);
    }
    
    protected final JobConfigurationPOJO getElasticJobConfigPOJO(final String jobId) {
        JobConfigurationPOJO result = PipelineAPIFactory.getJobConfigurationAPI().getJobConfiguration(jobId);
        if (null == result) {
            throw new PipelineJobNotFoundException(jobId);
        }
        return result;
    }
    
    protected final void verifyJobNotStopped(final JobConfigurationPOJO jobConfigPOJO) {
        if (jobConfigPOJO.isDisabled()) {
            throw new PipelineVerifyFailedException("Job is stopped, it's not necessary to do it.");
        }
    }
    
    protected final void verifyJobStopped(final JobConfigurationPOJO jobConfigPOJO) {
        if (!jobConfigPOJO.isDisabled()) {
            throw new PipelineVerifyFailedException("Job is not stopped. You could run `STOP SCALING {jobId}` to stop it.");
        }
    }
}
