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
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredContext;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobProgressDetector;
import org.apache.shardingsphere.data.pipeline.scenario.rulealtered.RuleAlteredJobWorker;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

import java.util.List;
import java.util.Map;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private final PipelineJobAPI pipelineJobAPI = PipelineJobAPIFactory.getPipelineJobAPI();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        List<JobInfo> jobInfos = pipelineJobAPI.list();
        for (JobInfo jobInfo : jobInfos) {
            if (!jobInfo.isActive()) {
                continue;
            }
            long jobId = jobInfo.getJobId();
            try {
                // TODO refactor: dispatch to different job types
                JobConfiguration jobConfig = YamlEngine.unmarshal(jobInfo.getJobParameter(), JobConfiguration.class, true);
                RuleAlteredContext ruleAlteredContext = RuleAlteredJobWorker.createRuleAlteredContext(jobConfig);
                if (!RuleAlteredJobProgressDetector.almostFinished(pipelineJobAPI.getProgress(jobId), jobConfig.getHandleConfig(), ruleAlteredContext)) {
                    continue;
                }
                log.info("scaling job {} almost finished.", jobId);
                if (null == ruleAlteredContext.getCompletionDetectAlgorithm()) {
                    log.info("completionDetector not configured, auto switch will not be enabled. You could query migration progress and switch manually with DistSQL.");
                }
                // TODO lock proxy
                if (!pipelineJobAPI.isDataConsistencyCheckNeeded(jobId)) {
                    log.info("dataConsistencyCheckAlgorithm is not configured, data consistency check is ignored.");
                    pipelineJobAPI.switchClusterConfiguration(jobId);
                    continue;
                }
                if (!dataConsistencyCheck(jobId)) {
                    log.error("data consistency check failed, job {}", jobId);
                    continue;
                }
                pipelineJobAPI.switchClusterConfiguration(jobId);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("scaling job {} finish check failed!", jobId, ex);
            }
        }
    }
    
    private boolean dataConsistencyCheck(final long jobId) {
        Map<String, DataConsistencyCheckResult> checkResultMap = pipelineJobAPI.dataConsistencyCheck(jobId);
        return pipelineJobAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap);
    }
}
