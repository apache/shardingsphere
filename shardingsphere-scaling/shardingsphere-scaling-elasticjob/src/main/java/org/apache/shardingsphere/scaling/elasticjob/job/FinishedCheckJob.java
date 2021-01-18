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

package org.apache.shardingsphere.scaling.elasticjob.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.elasticjob.api.ShardingContext;
import org.apache.shardingsphere.elasticjob.simple.job.SimpleJob;
import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.service.RegistryRepositoryHolder;
import org.apache.shardingsphere.scaling.core.service.impl.DistributedScalingJobService;
import org.apache.shardingsphere.scaling.core.utils.ScalingTaskUtil;
import org.apache.shardingsphere.scaling.core.utils.ThreadUtil;
import org.apache.shardingsphere.scaling.core.workflow.ScalingServiceHolder;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class FinishedCheckJob implements SimpleJob {
    
    private static final RegistryRepository REGISTRY_REPOSITORY = RegistryRepositoryHolder.getInstance();
    
    private final DistributedScalingJobService scalingJobService = new DistributedScalingJobService();
    
    @Override
    public void execute(final ShardingContext shardingContext) {
        List<String> jobs = REGISTRY_REPOSITORY.getChildrenKeys(ScalingConstant.SCALING_LISTENER_PATH);
        for (String each : jobs) {
            long jobId = Long.parseLong(each);
            try {
                ScalingJob scalingJob = scalingJobService.getJob(jobId);
                WorkflowConfiguration workflowConfig = scalingJob.getScalingConfig().getJobConfiguration().getWorkflowConfig();
                if (workflowConfig == null) {
                    continue;
                }
                if (ScalingTaskUtil.allTasksAlmostFinished(scalingJobService.getProgress(jobId), scalingJob.getScalingConfig().getJobConfiguration())) {
                    log.info("scaling job {} almost finished.", jobId);
                    trySwitch(jobId, workflowConfig);
                }
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("scaling job {} finish check failed!", jobId, ex);
            }
        }
    }
    
    private void trySwitch(final long jobId, final WorkflowConfiguration workflowConfig) {
        if (LockContext.getLockStrategy().tryLock(30L, TimeUnit.SECONDS) && LockContext.getLockStrategy().checkLock()) {
            try {
                ThreadUtil.sleep(10 * 1000L);
                if (ScalingServiceHolder.getInstance().checkScalingResult(jobId)) {
                    ScalingServiceHolder.getInstance().stopScalingJob(jobId);
                    ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(workflowConfig.getSchemaName(), workflowConfig.getRuleCacheId()));
                }
            } finally {
                LockContext.getLockStrategy().releaseLock();
            }
        } else {
            log.warn("can not get lock.");
        }
    }
}
