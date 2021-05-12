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
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.scaling.core.api.GovernanceRepositoryAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPI;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.job.check.consistency.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.util.ScalingTaskUtil;
import org.apache.shardingsphere.scaling.core.util.ThreadUtil;

import java.util.Map;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    private final GovernanceRepositoryAPI governanceRepositoryAPI = ScalingAPIFactory.getGovernanceRepositoryAPI();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        governanceRepositoryAPI.getChildrenKeys(ScalingConstant.SCALING_ROOT).stream()
                .filter(each -> !each.startsWith("_"))
                .forEach(each -> {
                    long jobId = Long.parseLong(each);
                    try {
                        JobConfiguration jobConfig = scalingAPI.getJobConfig(jobId);
                        WorkflowConfiguration workflowConfig = jobConfig.getHandleConfig().getWorkflowConfig();
                        if (workflowConfig == null) {
                            return;
                        }
                        if (ScalingTaskUtil.almostFinished(scalingAPI.getProgress(jobId), jobConfig.getHandleConfig())) {
                            log.info("scaling job {} almost finished.", jobId);
                            trySwitch(jobId, workflowConfig);
                        }
                        // CHECKSTYLE:OFF
                    } catch (final Exception ex) {
                        // CHECKSTYLE:ON
                        log.error("scaling job {} finish check failed!", jobId, ex);
                    }
                });
    }
    
    private void trySwitch(final long jobId, final WorkflowConfiguration workflowConfig) {
        // TODO lock proxy
        ThreadUtil.sleep(10 * 1000L);
        if (dataConsistencyCheck(jobId)) {
            scalingAPI.stop(jobId);
            ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(workflowConfig.getSchemaName(), workflowConfig.getRuleCacheId()));
        }
    }
    
    private boolean dataConsistencyCheck(final long jobId) {
        Map<String, DataConsistencyCheckResult> scalingResult = scalingAPI.dataConsistencyCheck(jobId);
        if (scalingResult.isEmpty()) {
            return false;
        }
        for (String each : scalingResult.keySet()) {
            boolean isDataValid = scalingResult.get(each).isDataValid();
            boolean isCountValid = scalingResult.get(each).isCountValid();
            if (!isDataValid || !isCountValid) {
                log.error("Scaling job: {}, table: {} data consistency check failed, dataValid: {}, countValid: {}", jobId, each, isDataValid, isCountValid);
                return false;
            }
        }
        return true;
    }
}
