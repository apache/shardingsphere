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

package org.apache.shardingsphere.data.pipeline.core.job;

import io.vertx.core.impl.ConcurrentHashSet;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPI;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.JobProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.data.pipeline.spi.lock.RowBasedJobLock;
import org.apache.shardingsphere.data.pipeline.spi.lock.RuleBasedJobLock;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private final RuleAlteredJobAPI ruleAlteredJobAPI = RuleAlteredJobAPIFactory.getInstance();
    
    private final Set<String> onCheckJobIds = new ConcurrentHashSet<>();
    
    // TODO only one proxy node could do data consistency check in proxy cluster
    @Override
    public void execute(final ShardingContext shardingContext) {
        List<JobInfo> jobInfos = ruleAlteredJobAPI.list();
        for (JobInfo jobInfo : jobInfos) {
            if (!jobInfo.isActive()) {
                continue;
            }
            String jobId = jobInfo.getJobId();
            if (onCheckJobIds.contains(jobId)) {
                log.info("check not completed for job {}, ignore", jobId);
                continue;
            }
            if (isNotAllowDataCheck(jobId)) {
                continue;
            }
            onCheckJobIds.add(jobId);
            try {
                // TODO refactor: dispatch to different job types
                RuleAlteredJobConfiguration jobConfig = YamlEngine.unmarshal(jobInfo.getJobParameter(), RuleAlteredJobConfiguration.class, true);
                RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
                if (null == ruleAlteredContext.getCompletionDetectAlgorithm()) {
                    log.info("completionDetector not configured, auto switch will not be enabled. You could query job progress and switch config manually with DistSQL.");
                    continue;
                }
                RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobInfo.getShardingTotalCount(), ruleAlteredJobAPI.getProgress(jobConfig).values());
                if (!ruleAlteredContext.getCompletionDetectAlgorithm().isAlmostCompleted(parameter)) {
                    continue;
                }
                log.info("scaling job {} almost finished.", jobId);
                RowBasedJobLock rowBasedJobLock = ruleAlteredContext.getRowBasedJobLock();
                String databaseName = jobConfig.getDatabaseName();
                try {
                    if (null != rowBasedJobLock) {
                        rowBasedJobLock.lock(databaseName, jobId + "");
                    }
                    if (!ruleAlteredJobAPI.isDataConsistencyCheckNeeded(jobConfig)) {
                        log.info("DataConsistencyCalculatorAlgorithm is not configured, data consistency check is ignored.");
                        ruleAlteredJobAPI.switchClusterConfiguration(jobConfig);
                        continue;
                    }
                    if (!dataConsistencyCheck(jobConfig)) {
                        log.error("data consistency check failed, job {}", jobId);
                        continue;
                    }
                    RuleBasedJobLock ruleBasedJobLock = ruleAlteredContext.getRuleBasedJobLock();
                    switchClusterConfiguration(databaseName, jobConfig, ruleBasedJobLock);
                } finally {
                    if (null != rowBasedJobLock) {
                        rowBasedJobLock.releaseLock(databaseName, jobId + "");
                    }
                }
                log.info("job {} finished", jobId);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("scaling job {} finish check failed!", jobId, ex);
            } finally {
                onCheckJobIds.remove(jobId);
            }
        }
    }
    
    private boolean isNotAllowDataCheck(final String jobId) {
        Map<Integer, JobProgress> jobProgressMap = ruleAlteredJobAPI.getProgress(jobId);
        for (JobProgress each : jobProgressMap.values()) {
            if (null == each || !JobStatus.EXECUTE_INCREMENTAL_TASK.equals(each.getStatus())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean dataConsistencyCheck(final RuleAlteredJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        log.info("dataConsistencyCheck for job {}", jobId);
        return ruleAlteredJobAPI.aggregateDataConsistencyCheckResults(jobId, ruleAlteredJobAPI.dataConsistencyCheck(jobConfig));
    }
    
    private void switchClusterConfiguration(final String databaseName, final RuleAlteredJobConfiguration jobConfig, final RuleBasedJobLock ruleBasedJobLock) {
        String jobId = jobConfig.getJobId();
        try {
            if (null != ruleBasedJobLock) {
                ruleBasedJobLock.lock(databaseName, jobId + "");
            }
            ruleAlteredJobAPI.switchClusterConfiguration(jobConfig);
        } finally {
            if (null != ruleBasedJobLock) {
                ruleBasedJobLock.releaseLock(databaseName, jobId + "");
            }
        }
    }
}
