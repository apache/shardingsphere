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

package org.apache.shardingsphere.data.pipeline.scenario.consistencycheck;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.PipelineTaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.yaml.job.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlPipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.process.PipelineProcessConfiguration;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineJobItemContext;
import org.apache.shardingsphere.data.pipeline.api.context.PipelineProcessContext;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJobId;
import org.apache.shardingsphere.data.pipeline.api.job.progress.ConsistencyCheckJobProgress;
import org.apache.shardingsphere.data.pipeline.api.job.progress.PipelineJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.CreateConsistencyCheckJobParameter;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.api.impl.AbstractPipelineJobAPIImpl;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyFinishedException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobNotFoundException;
import org.apache.shardingsphere.data.pipeline.core.exception.job.UncompletedConsistencyCheckJobExistsException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgressSwapper;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Consistency check job API impl.
 */
@Slf4j
public final class ConsistencyCheckJobAPIImpl extends AbstractPipelineJobAPIImpl implements ConsistencyCheckJobAPI {
    
    private final YamlConsistencyCheckJobProgressSwapper swapper = new YamlConsistencyCheckJobProgressSwapper();
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        ConsistencyCheckJobId jobId = (ConsistencyCheckJobId) pipelineJobId;
        return jobId.getParentJobId() + jobId.getSequence();
    }
    
    @Override
    public String createJobAndStart(final CreateConsistencyCheckJobParameter parameter) {
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String parentJobId = parameter.getJobId();
        Optional<String> checkLatestJobId = repositoryAPI.getCheckLatestJobId(parentJobId);
        if (checkLatestJobId.isPresent()) {
            PipelineJobItemProgress progress = getJobItemProgress(checkLatestJobId.get(), 0);
            if (null == progress || JobStatus.FINISHED != progress.getStatus()) {
                log.info("check job already exists and status is not FINISHED, progress={}", progress);
                throw new UncompletedConsistencyCheckJobExistsException(checkLatestJobId.get());
            }
        }
        int sequence = checkLatestJobId.map(optional -> ConsistencyCheckJobId.parseSequence(optional) + 1).orElse(ConsistencyCheckJobId.MIN_SEQUENCE);
        String result = marshalJobId(new ConsistencyCheckJobId(parentJobId, sequence));
        repositoryAPI.persistCheckLatestJobId(parentJobId, result);
        repositoryAPI.deleteCheckJobResult(parentJobId, result);
        dropJob(result);
        YamlConsistencyCheckJobConfiguration yamlConfig = new YamlConsistencyCheckJobConfiguration();
        yamlConfig.setJobId(result);
        yamlConfig.setParentJobId(parentJobId);
        yamlConfig.setAlgorithmTypeName(parameter.getAlgorithmTypeName());
        yamlConfig.setAlgorithmProps(parameter.getAlgorithmProps());
        start(new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(yamlConfig));
        return result;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> getLatestDataConsistencyCheckResult(final String jobId) {
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(jobId);
        if (!checkLatestJobId.isPresent()) {
            return Collections.emptyMap();
        }
        return PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckJobResult(jobId, checkLatestJobId.get());
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        ConsistencyCheckJobProgress jobProgress = new ConsistencyCheckJobProgress();
        jobProgress.setStatus(jobItemContext.getStatus());
        YamlConsistencyCheckJobProgress yamlJobProgress = swapper.swapToYamlConfiguration(jobProgress);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), YamlEngine.marshal(yamlJobProgress));
    }
    
    @Override
    public ConsistencyCheckJobProgress getJobItemProgress(final String jobId, final int shardingItem) {
        String progress = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        if (StringUtils.isBlank(progress)) {
            return null;
        }
        ConsistencyCheckJobProgress jobProgress = swapper.swapToObject(YamlEngine.unmarshal(progress, YamlConsistencyCheckJobProgress.class, true));
        ConsistencyCheckJobProgress result = new ConsistencyCheckJobProgress();
        result.setStatus(jobProgress.getStatus());
        return result;
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        ConsistencyCheckJobProgress jobProgress = getJobItemProgress(jobId, shardingItem);
        if (null == jobProgress) {
            log.warn("updateJobItemStatus, jobProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobProgress.setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(swapper.swapToYamlConfiguration(jobProgress)));
    }
    
    @Override
    public void startDisabledJob(final String jobId) {
        log.info("Start disable check job {}", jobId);
        PipelineJobItemProgress jobProgress = getJobItemProgress(jobId, 0);
        ShardingSpherePreconditions.checkState(null == jobProgress || JobStatus.FINISHED != jobProgress.getStatus(), () -> new PipelineJobHasAlreadyFinishedException(jobId));
        super.startDisabledJob(jobId);
    }
    
    @Override
    public void startByParentJobId(final String parentJobId) {
        log.info("Start check job by parent job id: {}", parentJobId);
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(parentJobId);
        ShardingSpherePreconditions.checkState(checkLatestJobId.isPresent(), () -> new PipelineJobNotFoundException(parentJobId));
        startDisabledJob(checkLatestJobId.get());
    }
    
    @Override
    public void stopByParentJobId(final String parentJobId) {
        log.info("Stop check job by parent job id: {}", parentJobId);
        Optional<String> checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(parentJobId);
        ShardingSpherePreconditions.checkState(checkLatestJobId.isPresent(), () -> new PipelineJobNotFoundException(parentJobId));
        stop(checkLatestJobId.get());
    }
    
    @Override
    public ConsistencyCheckJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected ConsistencyCheckJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlConsistencyCheckJobConfigurationSwapper().swapToYamlConfiguration((ConsistencyCheckJobConfiguration) jobConfig);
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PipelineTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public PipelineProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected PipelineJobInfo getJobInfo(final String jobId) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected String getJobClassName() {
        return ConsistencyCheckJob.class.getName();
    }
    
    @Override
    public JobType getJobType() {
        return JobType.CONSISTENCY_CHECK;
    }
}
