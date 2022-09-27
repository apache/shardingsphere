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
import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlConsistencyCheckJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.job.PipelineJob;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.job.AbstractPipelineJob;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.core.metadata.node.PipelineMetaDataNode;
import org.apache.shardingsphere.data.pipeline.core.util.PipelineDistributedBarrier;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.Map;

/**
 * Consistency check job.
 */
@Slf4j
public final class ConsistencyCheckJob extends AbstractPipelineJob implements SimpleJob, PipelineJob {
    
    private final ConsistencyCheckJobAPI jobAPI = ConsistencyCheckJobAPIFactory.getInstance();
    
    private final PipelineDistributedBarrier pipelineDistributedBarrier = PipelineDistributedBarrier.getInstance();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        String checkJobId = shardingContext.getJobName();
        setJobId(checkJobId);
        ConsistencyCheckJobConfiguration consistencyCheckJobConfig = YamlConsistencyCheckJobConfigurationSwapper.swapToObject(shardingContext.getJobParameter());
        ConsistencyCheckJobItemContext jobItemContext = new ConsistencyCheckJobItemContext(consistencyCheckJobConfig, 0, JobStatus.FINISHED);
        jobAPI.persistJobItemProgress(jobItemContext);
        String referredJobId = consistencyCheckJobConfig.getReferredJobId();
        log.info("execute consistency check, job id:{}, referred job id:{}", checkJobId, referredJobId);
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckLatestJobId(referredJobId, checkJobId);
        JobType jobType = PipelineJobIdUtils.parseJobType(referredJobId);
        InventoryIncrementalJobPublicAPI jobPublicAPI = PipelineJobPublicAPIFactory.getInventoryIncrementalJobPublicAPI(jobType.getTypeName());
        Map<String, DataConsistencyCheckResult> dataConsistencyCheckResult;
        if (StringUtils.isBlank(consistencyCheckJobConfig.getAlgorithmTypeName())) {
            dataConsistencyCheckResult = jobPublicAPI.dataConsistencyCheck(referredJobId);
        } else {
            dataConsistencyCheckResult = jobPublicAPI.dataConsistencyCheck(referredJobId, consistencyCheckJobConfig.getAlgorithmTypeName(), consistencyCheckJobConfig.getAlgorithmProperties());
        }
        PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckJobResult(referredJobId, checkJobId, dataConsistencyCheckResult);
        jobItemContext.setStatus(JobStatus.FINISHED);
        jobAPI.persistJobItemProgress(jobItemContext);
        jobAPI.stop(checkJobId);
        log.info("execute consistency check job finished, job id:{}, referred job id:{}", checkJobId, referredJobId);
    }
    
    @Override
    public void stop() {
        setStopping(true);
        if (null != getOneOffJobBootstrap()) {
            getOneOffJobBootstrap().shutdown();
        }
        if (null == getJobId()) {
            log.info("stop consistency check job, jobId is null, ignore");
            return;
        }
        String jobBarrierDisablePath = PipelineMetaDataNode.getJobBarrierDisablePath(getJobId());
        pipelineDistributedBarrier.persistEphemeralChildrenNode(jobBarrierDisablePath, 0);
    }
}
