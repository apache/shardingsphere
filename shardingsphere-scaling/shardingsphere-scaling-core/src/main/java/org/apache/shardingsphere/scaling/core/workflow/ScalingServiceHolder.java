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

package org.apache.shardingsphere.scaling.core.workflow;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.job.check.DataConsistencyCheckResult;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;
import org.apache.shardingsphere.scaling.core.service.ScalingJobServiceFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Scaling service holder.
 */
@Slf4j
public final class ScalingServiceHolder {
    
    private static final ScalingServiceHolder INSTANCE = new ScalingServiceHolder();
    
    private final ScalingJobService scalingJobService = ScalingJobServiceFactory.getInstance();
    
    /**
     * Get scaling service holder instance.
     *
     * @return scaling service holder instance
     */
    public static ScalingServiceHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init.
     */
    public void init() {
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Start scaling job.
     *
     * @param event rule configurations altered event.
     */
    @Subscribe
    public void startScalingJob(final RuleConfigurationsAlteredEvent event) {
        Optional<ScalingJob> scalingJob = scalingJobService.start(event);
        if (!scalingJob.isPresent()) {
            ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(event.getSchemaName(), event.getRuleCacheId()));
        }
    }
    
    /**
     * Check scaling result.
     *
     * @param jobId job Id
     * @return true if scaling result check successfully, else false
     */
    public boolean checkScalingResult(final long jobId) {
        Map<String, DataConsistencyCheckResult> scalingResult = scalingJobService.check(jobId);
        if (scalingResult.isEmpty()) {
            return false;
        }
        for (String key : scalingResult.keySet()) {
            boolean isDataValid = scalingResult.get(key).isDataValid();
            boolean isCountValid = scalingResult.get(key).isCountValid();
            if (!isDataValid || !isCountValid) {
                log.error("Scaling job: {}, table: {} data consistency check failed, dataValid: {}, countValid: {}", jobId, key, isDataValid, isCountValid);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Stop scaling job.
     *
     * @param jobId job id
     */
    public void stopScalingJob(final long jobId) {
        scalingJobService.stop(jobId);
    }
}
