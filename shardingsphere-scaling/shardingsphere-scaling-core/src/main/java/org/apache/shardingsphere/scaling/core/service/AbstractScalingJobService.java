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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.executor.kernel.thread.ExecutorThreadFactoryBuilder;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.job.JobProgress;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyChecker;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckerFactory;
import org.apache.shardingsphere.scaling.core.job.environmental.ScalingEnvironmentalManager;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;

import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Abstract scaling job service.
 */
@Slf4j
public abstract class AbstractScalingJobService implements ScalingJobService {
    
    private static final ScheduledExecutorService FINISH_CHECK_EXECUTOR = Executors.newSingleThreadScheduledExecutor(ExecutorThreadFactoryBuilder.build("Scaling-finish-check-%d"));
    
    @Override
    public Optional<ScalingJob> start(final String sourceDataSource, final String sourceRule, final String targetDataSource, final String targetRule, final ScalingCallback scalingCallback) {
        Optional<ScalingJob> result = start(sourceDataSource, sourceRule, targetDataSource, targetRule);
        if (!result.isPresent()) {
            return result;
        }
        FINISH_CHECK_EXECUTOR.scheduleWithFixedDelay(new JobFinishChecker(result.get(), scalingCallback), 1, 1, TimeUnit.MINUTES);
        return result;
    }
    
    private Optional<ScalingJob> start(final String sourceDataSource, final String sourceRule, final String targetDataSource, final String targetRule) {
        ScalingConfiguration scalingConfig = new ScalingConfiguration();
        scalingConfig.setRuleConfiguration(new RuleConfiguration(
                new ShardingSphereJDBCDataSourceConfiguration(sourceDataSource, sourceRule),
                new ShardingSphereJDBCDataSourceConfiguration(targetDataSource, targetRule)));
        scalingConfig.setJobConfiguration(new JobConfiguration());
        return start(scalingConfig);
    }
    
    @Override
    public Map<String, DataConsistencyCheckResult> check(final long jobId) {
        DataConsistencyChecker dataConsistencyChecker = DataConsistencyCheckerFactory.newInstance(getJob(jobId));
        Map<String, DataConsistencyCheckResult> result = dataConsistencyChecker.countCheck();
        if (result.values().stream().allMatch(DataConsistencyCheckResult::isCountValid)) {
            Map<String, Boolean> dataCheckResult = dataConsistencyChecker.dataCheck();
            result.forEach((key, value) -> value.setDataValid(dataCheckResult.getOrDefault(key, false)));
        }
        return result;
    }
    
    @Override
    public void reset(final long jobId) throws SQLException {
        new ScalingEnvironmentalManager().resetTargetTable(getJob(jobId));
    }
    
    @RequiredArgsConstructor
    private class JobFinishChecker implements Runnable {
        
        private final ScalingJob scalingJob;
        
        private final ScalingCallback scalingCallback;
        
        private boolean executed;
        
        @Override
        public void run() {
            if (executed) {
                return;
            }
            long jobId = scalingJob.getJobId();
            try {
                JobProgress jobProgress = getProgress(jobId);
                if (jobProgress.getStatus().contains("FAILURE")) {
                    log.warn("scaling job {} failure.", jobId);
                    executed = true;
                    scalingCallback.onFailure(jobId);
                } else if (ScalingTaskUtil.allTasksAlmostFinished(jobProgress, scalingJob.getScalingConfig().getJobConfiguration())) {
                    log.info("scaling job {} almost finished.", jobId);
                    executed = true;
                    scalingCallback.onSuccess(jobId);
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("scaling job {} finish check failed!", jobId, ex);
            }
        }
    }
}
