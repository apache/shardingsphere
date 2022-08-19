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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.detect.RuleAlteredJobAlmostCompletedParameter;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.progress.InventoryIncrementalJobItemProgress;
import org.apache.shardingsphere.data.pipeline.api.pojo.PipelineJobInfo;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationProcessContext;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private final MigrationJobAPI jobAPI = MigrationJobAPIFactory.getInstance();
    
    private final Set<String> onCheckJobIds = new ConcurrentSkipListSet<>();
    
    // TODO only one proxy node could do data consistency check in proxy cluster
    @Override
    public void execute(final ShardingContext shardingContext) {
        List<PipelineJobInfo> jobInfos = jobAPI.list();
        for (PipelineJobInfo jobInfo : jobInfos) {
            if (!jobInfo.isActive()) {
                continue;
            }
            String jobId = jobInfo.getJobId();
            if (onCheckJobIds.contains(jobId)) {
                log.info("check not completed for job {}, ignore", jobId);
                continue;
            }
            // TODO merge to CompletionDetectAlgorithm
            if (isNotAllowDataCheck(jobId)) {
                continue;
            }
            onCheckJobIds.add(jobId);
            try {
                // TODO refactor: dispatch to different job types
                MigrationJobConfiguration jobConfig = YamlMigrationJobConfigurationSwapper.swapToObject(jobInfo.getJobParameter());
                MigrationProcessContext processContext = jobAPI.buildPipelineProcessContext(jobConfig);
                if (null == processContext.getCompletionDetectAlgorithm()) {
                    log.info("completionDetector not configured, auto switch will not be enabled. You could query job progress and switch config manually with DistSQL.");
                    continue;
                }
                RuleAlteredJobAlmostCompletedParameter parameter = new RuleAlteredJobAlmostCompletedParameter(jobInfo.getShardingTotalCount(), jobAPI.getJobProgress(jobConfig).values());
                if (!processContext.getCompletionDetectAlgorithm().isAlmostCompleted(parameter)) {
                    continue;
                }
                log.info("scaling job {} almost finished.", jobId);
                try {
                    jobAPI.stopClusterWriteDB(jobConfig);
                    if (!jobAPI.isDataConsistencyCheckNeeded(jobConfig)) {
                        log.info("DataConsistencyCalculatorAlgorithm is not configured, data consistency check is ignored.");
                        jobAPI.switchClusterConfiguration(jobConfig);
                        continue;
                    }
                    if (!dataConsistencyCheck(jobConfig)) {
                        log.error("data consistency check failed, job {}", jobId);
                        continue;
                    }
                    switchClusterConfiguration(jobConfig);
                } finally {
                    jobAPI.restoreClusterWriteDB(jobConfig);
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
        Map<Integer, InventoryIncrementalJobItemProgress> jobItemProgressMap = jobAPI.getJobProgress(jobId);
        for (InventoryIncrementalJobItemProgress each : jobItemProgressMap.values()) {
            if (null == each || !JobStatus.EXECUTE_INCREMENTAL_TASK.equals(each.getStatus())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean dataConsistencyCheck(final MigrationJobConfiguration jobConfig) {
        String jobId = jobConfig.getJobId();
        log.info("dataConsistencyCheck for job {}", jobId);
        return jobAPI.aggregateDataConsistencyCheckResults(jobId, jobAPI.dataConsistencyCheck(jobConfig));
    }
    
    private void switchClusterConfiguration(final MigrationJobConfiguration jobConfig) {
        jobAPI.switchClusterConfiguration(jobConfig);
    }
}
