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

package org.apache.shardingsphere.scaling.core.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.scaling.core.api.JobInfo;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.util.ScalingTaskUtil;

import java.util.List;
import java.util.Map;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        List<JobInfo> jobInfos = scalingAPI.list();
        for (JobInfo jobInfo : jobInfos) {
            if (!jobInfo.isActive()) {
                continue;
            }
            long jobId = jobInfo.getJobId();
            try {
                JobConfiguration jobConfig = YamlEngine.unmarshal(jobInfo.getJobParameter(), JobConfiguration.class, true);
                if (!ScalingTaskUtil.almostFinished(scalingAPI.getProgress(jobId), jobConfig.getHandleConfig())) {
                    continue;
                }
                log.info("scaling job {} almost finished.", jobId);
                // TODO lock proxy
                if (!scalingAPI.isDataConsistencyCheckNeeded()) {
                    log.info("dataConsistencyCheckAlgorithm is not configured, data consistency check is ignored.");
                    scalingAPI.switchClusterConfiguration(jobId);
                    continue;
                }
                if (!dataConsistencyCheck(jobId)) {
                    log.error("data consistency check failed, job {}", jobId);
                    continue;
                }
                scalingAPI.switchClusterConfiguration(jobId);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("scaling job {} finish check failed!", jobId, ex);
            }
        }
    }
    
    private boolean dataConsistencyCheck(final long jobId) {
        Map<String, DataConsistencyCheckResult> checkResultMap = scalingAPI.dataConsistencyCheck(jobId);
        return scalingAPI.aggregateDataConsistencyCheckResults(jobId, checkResultMap);
    }
}
