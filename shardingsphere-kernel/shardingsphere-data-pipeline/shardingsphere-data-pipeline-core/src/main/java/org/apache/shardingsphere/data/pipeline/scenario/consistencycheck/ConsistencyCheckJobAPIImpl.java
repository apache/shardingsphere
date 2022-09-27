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
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlConsistencyCheckJobConfigurationSwapper;
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
import org.apache.shardingsphere.data.pipeline.core.exception.job.PipelineJobHasAlreadyExistedException;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgress;
import org.apache.shardingsphere.data.pipeline.core.job.progress.yaml.YamlConsistencyCheckJobProgressSwapper;
import org.apache.shardingsphere.elasticjob.infra.pojo.JobConfigurationPOJO;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;

import java.util.Collections;
import java.util.Map;

/**
 * Consistency check job API impl.
 */
@Slf4j
public final class ConsistencyCheckJobAPIImpl extends AbstractPipelineJobAPIImpl implements ConsistencyCheckJobAPI {
    
    private static final YamlConsistencyCheckJobProgressSwapper PROGRESS_SWAPPER = new YamlConsistencyCheckJobProgressSwapper();
    
    @Override
    protected String marshalJobIdLeftPart(final PipelineJobId pipelineJobId) {
        ConsistencyCheckJobId jobId = (ConsistencyCheckJobId) pipelineJobId;
        return jobId.getPipelineJobId() + jobId.getSequence();
    }
    
    @Override
    public String createJobAndStart(final CreateConsistencyCheckJobParameter parameter) {
        GovernanceRepositoryAPI repositoryAPI = PipelineAPIFactory.getGovernanceRepositoryAPI();
        String checkLatestJobId = repositoryAPI.getCheckLatestJobId(parameter.getJobId());
        if (StringUtils.isNotBlank(checkLatestJobId)) {
            PipelineJobItemProgress progress = getJobItemProgress(checkLatestJobId, 0);
            if (null != progress && JobStatus.FINISHED != progress.getStatus()) {
                log.info("check job already existed and status isn't FINISHED, status {}", progress.getStatus());
                throw new PipelineJobHasAlreadyExistedException(checkLatestJobId);
            }
        }
        int consistencyCheckVersionNew = null == checkLatestJobId ? 0 : ConsistencyCheckJobId.getSequence(checkLatestJobId) + 1;
        YamlConsistencyCheckJobConfiguration yamlConfig = new YamlConsistencyCheckJobConfiguration();
        ConsistencyCheckJobId checkJobId = new ConsistencyCheckJobId(parameter.getJobId(), consistencyCheckVersionNew);
        String result = marshalJobId(checkJobId);
        yamlConfig.setJobId(result);
        yamlConfig.setParentJobId(parameter.getJobId());
        yamlConfig.setAlgorithmTypeName(parameter.getAlgorithmTypeName());
        yamlConfig.setAlgorithmProperties(parameter.getAlgorithmProps());
        ConsistencyCheckJobConfiguration jobConfig = new YamlConsistencyCheckJobConfigurationSwapper().swapToObject(yamlConfig);
        start(jobConfig);
        return result;
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> getLatestDataConsistencyCheckResult(final String jobId) {
        String checkLatestJobId = PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckLatestJobId(jobId);
        if (StringUtils.isBlank(checkLatestJobId)) {
            return Collections.emptyMap();
        }
        return PipelineAPIFactory.getGovernanceRepositoryAPI().getCheckJobResult(jobId, checkLatestJobId);
    }
    
    @Override
    public void persistJobItemProgress(final PipelineJobItemContext jobItemContext) {
        ConsistencyCheckJobProgress jobProgress = new ConsistencyCheckJobProgress();
        jobProgress.setStatus(jobItemContext.getStatus());
        YamlConsistencyCheckJobProgress yamlJobProgress = PROGRESS_SWAPPER.swapToYamlConfiguration(jobProgress);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobItemContext.getJobId(), jobItemContext.getShardingItem(), YamlEngine.marshal(yamlJobProgress));
    }
    
    @Override
    public PipelineJobItemProgress getJobItemProgress(final String jobId, final int shardingItem) {
        String progress = PipelineAPIFactory.getGovernanceRepositoryAPI().getJobItemProgress(jobId, shardingItem);
        if (StringUtils.isBlank(progress)) {
            return null;
        }
        ConsistencyCheckJobProgress jobProgress = PROGRESS_SWAPPER.swapToObject(YamlEngine.unmarshal(progress, YamlConsistencyCheckJobProgress.class, true));
        ConsistencyCheckJobProgress result = new ConsistencyCheckJobProgress();
        result.setStatus(jobProgress.getStatus());
        return result;
    }
    
    @Override
    public void updateJobItemStatus(final String jobId, final int shardingItem, final JobStatus status) {
        ConsistencyCheckJobProgress jobProgress = (ConsistencyCheckJobProgress) getJobItemProgress(jobId, shardingItem);
        if (null == jobProgress) {
            log.warn("updateJobItemStatus, jobProgress is null, jobId={}, shardingItem={}", jobId, shardingItem);
            return;
        }
        jobProgress.setStatus(status);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistJobItemProgress(jobId, shardingItem, YamlEngine.marshal(PROGRESS_SWAPPER.swapToYamlConfiguration(jobProgress)));
    }
    
    @Override
    public ConsistencyCheckJobConfiguration getJobConfiguration(final String jobId) {
        return getJobConfiguration(getElasticJobConfigPOJO(jobId));
    }
    
    @Override
    protected ConsistencyCheckJobConfiguration getJobConfiguration(final JobConfigurationPOJO jobConfigPOJO) {
        return YamlConsistencyCheckJobConfigurationSwapper.swapToObject(jobConfigPOJO.getJobParameter());
    }
    
    @Override
    protected YamlPipelineJobConfiguration swapToYamlJobConfiguration(final PipelineJobConfiguration jobConfig) {
        return new YamlConsistencyCheckJobConfigurationSwapper().swapToYamlConfiguration((ConsistencyCheckJobConfiguration) jobConfig);
    }
    
    @Override
    public void extendYamlJobConfiguration(final YamlPipelineJobConfiguration yamlJobConfig) {
    }
    
    @Override
    public PipelineTaskConfiguration buildTaskConfiguration(final PipelineJobConfiguration pipelineJobConfig, final int jobShardingItem, final PipelineProcessConfiguration pipelineProcessConfig) {
        return null;
    }
    
    @Override
    public PipelineProcessContext buildPipelineProcessContext(final PipelineJobConfiguration pipelineJobConfig) {
        return null;
    }
    
    @Override
    protected PipelineJobInfo getJobInfo(final String jobId) {
        return null;
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
