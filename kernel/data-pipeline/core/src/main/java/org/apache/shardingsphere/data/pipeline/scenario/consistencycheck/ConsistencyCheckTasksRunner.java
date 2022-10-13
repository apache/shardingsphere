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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.job.ConsistencyCheckJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.PipelineJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.executor.AbstractLifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.executor.LifecycleExecutor;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.task.PipelineTasksRunner;
import org.apache.shardingsphere.data.pipeline.core.api.InventoryIncrementalJobAPI;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteCallback;
import org.apache.shardingsphere.data.pipeline.core.execute.ExecuteEngine;
import org.apache.shardingsphere.data.pipeline.core.job.PipelineJobIdUtils;
import org.apache.shardingsphere.data.pipeline.spi.check.consistency.DataConsistencyCalculateAlgorithm;

import java.sql.SQLException;
import java.util.Map;

/**
 * Consistency check tasks runner.
 */
@Slf4j
public final class ConsistencyCheckTasksRunner implements PipelineTasksRunner {
    
    private final ConsistencyCheckJobAPI checkJobAPI = ConsistencyCheckJobAPIFactory.getInstance();
    
    @Getter
    private final ConsistencyCheckJobItemContext jobItemContext;
    
    private final ConsistencyCheckJobConfiguration checkJobConfig;
    
    private final String checkJobId;
    
    private final String parentJobId;
    
    private final LifecycleExecutor checkExecutor;
    
    private final ExecuteCallback checkExecuteCallback;
    
    @Setter(AccessLevel.PRIVATE)
    private volatile DataConsistencyCalculateAlgorithm calculateAlgorithm;
    
    public ConsistencyCheckTasksRunner(final ConsistencyCheckJobItemContext jobItemContext) {
        this.jobItemContext = jobItemContext;
        checkJobConfig = jobItemContext.getJobConfig();
        checkJobId = checkJobConfig.getJobId();
        parentJobId = checkJobConfig.getParentJobId();
        checkExecutor = new CheckLifecycleExecutor();
        checkExecuteCallback = new CheckExecuteCallback();
    }
    
    @Override
    public void start() {
        if (jobItemContext.isStopping()) {
            log.info("job stopping, ignore consistency check");
            return;
        }
        PipelineAPIFactory.getPipelineJobAPI(PipelineJobIdUtils.parseJobType(jobItemContext.getJobId())).persistJobItemProgress(jobItemContext);
        ExecuteEngine executeEngine = ExecuteEngine.newFixedThreadInstance(1, checkJobId + "-check");
        executeEngine.submit(checkExecutor, checkExecuteCallback);
    }
    
    @Override
    public void stop() {
        jobItemContext.setStopping(true);
        log.info("stop, jobId={}, shardingItem={}", jobItemContext.getJobId(), jobItemContext.getShardingItem());
        checkExecutor.stop();
    }
    
    private final class CheckLifecycleExecutor extends AbstractLifecycleExecutor {
        
        @Override
        protected void runBlocking() {
            log.info("execute consistency check, check job id: {}, parent job id: {}", checkJobId, parentJobId);
            checkJobAPI.persistJobItemProgress(jobItemContext);
            JobType jobType = PipelineJobIdUtils.parseJobType(parentJobId);
            InventoryIncrementalJobAPI jobAPI = (InventoryIncrementalJobAPI) PipelineAPIFactory.getPipelineJobAPI(jobType);
            PipelineJobConfiguration parentJobConfig = jobAPI.getJobConfiguration(parentJobId);
            DataConsistencyCalculateAlgorithm calculateAlgorithm = jobAPI.buildDataConsistencyCalculateAlgorithm(
                    parentJobConfig, checkJobConfig.getAlgorithmTypeName(), checkJobConfig.getAlgorithmProps());
            setCalculateAlgorithm(calculateAlgorithm);
            Map<String, DataConsistencyCheckResult> dataConsistencyCheckResult = jobAPI.dataConsistencyCheck(parentJobConfig, calculateAlgorithm, jobItemContext);
            PipelineAPIFactory.getGovernanceRepositoryAPI().persistCheckJobResult(parentJobId, checkJobId, dataConsistencyCheckResult);
            jobItemContext.setCheckEndTimeMillis(System.currentTimeMillis());
        }
        
        @Override
        protected void doStop() {
            DataConsistencyCalculateAlgorithm algorithm = calculateAlgorithm;
            log.info("doStop, algorithm={}", algorithm);
            if (null != algorithm) {
                try {
                    algorithm.cancel();
                } catch (final SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }
    
    private final class CheckExecuteCallback implements ExecuteCallback {
        
        @Override
        public void onSuccess() {
            log.info("onSuccess, check job id: {}, parent job id: {}", checkJobId, parentJobId);
            jobItemContext.setStatus(JobStatus.FINISHED);
            checkJobAPI.persistJobItemProgress(jobItemContext);
            checkJobAPI.stop(checkJobId);
        }
        
        @Override
        public void onFailure(final Throwable throwable) {
            DataConsistencyCalculateAlgorithm algorithm = calculateAlgorithm;
            if (null != algorithm && algorithm.isCanceling()) {
                log.info("onFailure, canceling, check job id: {}, parent job id: {}", checkJobId, parentJobId);
                checkJobAPI.stop(checkJobId);
                return;
            }
            log.info("onFailure, check job id: {}, parent job id: {}", checkJobId, parentJobId, throwable);
            checkJobAPI.persistJobItemErrorMessage(checkJobId, 0, throwable);
            jobItemContext.setStatus(JobStatus.CONSISTENCY_CHECK_FAILURE);
            checkJobAPI.persistJobItemProgress(jobItemContext);
            checkJobAPI.stop(checkJobId);
        }
    }
}
