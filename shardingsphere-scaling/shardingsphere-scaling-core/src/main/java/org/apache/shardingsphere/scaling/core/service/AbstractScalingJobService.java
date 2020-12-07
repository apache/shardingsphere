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

package org.apache.shardingsphere.scaling.core.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract scaling job service.
 */
public abstract class AbstractScalingJobService implements ScalingJobService {
    
    private static final ScheduledExecutorService FINISH_CHECK_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("Scaling-finish-check-%d"));
    
    @Override
    public Optional<ScalingJob> start(final String sourceDataSource, final String sourceRule, final String targetDataSource, final String targetRule, final ScalingCallback scalingCallback) {
        Optional<ScalingJob> result = start(sourceDataSource, sourceRule, targetDataSource, targetRule);
        if (!result.isPresent()) {
            scalingCallback.onSuccess();
            return result;
        }
        FINISH_CHECK_EXECUTOR.scheduleWithFixedDelay(new JobFinishChecker(result.get(), scalingCallback), 3, 1, TimeUnit.MINUTES);
        return result;
    }
    
    private Optional<ScalingJob> start(final String sourceDataSource, final String sourceRule, final String targetDataSource, final String targetRule) {
        ScalingConfiguration scalingConfig = new ScalingConfiguration();
        scalingConfig.setRuleConfiguration(
                new RuleConfiguration(new ShardingSphereJDBCDataSourceConfiguration(sourceDataSource, sourceRule), new ShardingSphereJDBCDataSourceConfiguration(targetDataSource, targetRule)));
        scalingConfig.setJobConfiguration(new JobConfiguration());
        return start(scalingConfig);
    }
    
    @Override
    public void reset(final long jobId) {
        // TODO reset target tables.
    }
    
    /**
     * Do data consistency check.
     *
     * @param scalingJob scaling job
     * @return data consistency check result
     */
    protected Map<String, DataConsistencyCheckResult> dataConsistencyCheck(final ScalingJob scalingJob) {
        DataConsistencyChecker dataConsistencyChecker = scalingJob.getDataConsistencyChecker();
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        return result;
    }
    
    @RequiredArgsConstructor
    private class JobFinishChecker implements Runnable {
        
        private final ScalingJob scalingJob;
        
        private final ScalingCallback scalingCallback;
        
        private boolean finished;
        
        @Override
        public void run() {
            if (finished) {
                return;
            }
            JobProgress jobProgress = getProgress(scalingJob.getJobId());
            if (jobProgress.getStatus().contains("FAILURE")) {
                finished = true;
                scalingCallback.onFailure();
            } else if (ScalingTaskUtil.allTasksAlmostFinished(jobProgress, scalingJob.getScalingConfig().getJobConfiguration())) {
                finished = true;
                scalingCallback.onSuccess();
            }
        }
    }
}
