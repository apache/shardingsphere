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

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shardingsphere.data.pipeline.api.InventoryIncrementalJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;

import java.util.Map;

/**
 * Consistency check tasks runner.
 */
@Slf4j
public final class ConsistencyCheckTasksRunner implements PipelineTasksRunner {
    
    private final ConsistencyCheckJobAPI jobAPI = ConsistencyCheckJobAPIFactory.getInstance();
    
    @Getter
    private final ConsistencyCheckJobItemContext jobItemContext;
    
    private final ConsistencyCheckJobConfiguration jobConfig;
    
    private final String checkJobId;
    
    private final String parentJobId;
    
    private final LifecycleExecutor lifecycleExecutor;
    
    private final ExecuteCallback executeCallback;
    
    public ConsistencyCheckTasksRunner(final ConsistencyCheckJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        jobConfig = jobItemContext.getJobConfig();
        checkJobId = jobConfig.getJobId();
        parentJobId = jobConfig.getParentJobId();
        lifecycleExecutor = createLifecycleExecutor();
        executeCallback = createExecuteCallback();
    }
    
    private LifecycleExecutor createLifecycleExecutor() {
        return new AbstractLifecycleExecutor() {
            @Override
            protected void runBlocking() {
                log.info("execute consistency check, check job id: {}, parent job id: {}", checkJobId, parentJobId);
                jobAPI.persistJobItemProgress(jobItemContext);
                JobType jobType = PipelineJobIdUtils.parseJobType(parentJobId);
                InventoryIncrementalJobPublicAPI jobPublicAPI = PipelineJobPublicAPIFactory.getInventoryIncrementalJobPublicAPI(jobType.getTypeName());
                // TODO calculate algorithm
                Map<String, DataConsistencyCheckResult> dataConsistencyCheckResult = StringUtils.isBlank(jobConfig.getAlgorithmTypeName())
                        ? jobPublicAPI.dataConsistencyCheck(parentJobId)
                        : jobPublicAPI.dataConsistencyCheck(parentJobId, jobConfig.getAlgorithmTypeName(), jobConfig.getAlgorithmProps());
                PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
            }
            
            @Override
            protected void doStop() {
                // TODO calculate algorithm
            }
        };
    }
    
    private ExecuteCallback createExecuteCallback() {
        return new ExecuteCallback() {
            @Override
            public void onSuccess() {
                log.info("onSuccess, check job id: {}, parent job id: {}", checkJobId, parentJobId);
                jobItemContext.setStatus(JobStatus.FINISHED);
                jobAPI.persistJobItemProgress(jobItemContext);
                jobAPI.stop(checkJobId);
            }
            
            @Override
            public void onFailure(final Throwable throwable) {
                log.info("onFailure, check job id: {}, parent job id: {}", checkJobId, parentJobId);
                jobAPI.persistJobItemErrorMessage(checkJobId, 0, throwable);
                jobItemContext.setStatus(JobStatus.CONSISTENCY_CHECK_FAILURE);
                jobAPI.persistJobItemProgress(jobItemContext);
                jobAPI.stop(checkJobId);
            }
        };
    }
    
    @Override
    public void start() {
        if (jobItemContext.isStopping()) {
            log.info("job stopping, ignore consistency check");
            return;
        }
        PipelineAPIFactory.getPipelineJobAPI(PipelineJobIdUtils.parseJobType(jobItemContext.getJobId())).persistJobItemProgress(jobItemContext);
        ExecuteEngine executeEngine = ExecuteEngine.newFixedThreadInstance(1, checkJobId + "-check");
        executeEngine.submit(lifecycleExecutor, executeCallback);
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        log.info("stop, jobId={}, shardingItem={}", jobItemContext.getJobId(), jobItemContext.getShardingItem());
        lifecycleExecutor.stop();
    }
}
