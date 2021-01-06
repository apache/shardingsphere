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

package org.apache.shardingsphere.governance.core.scaling;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.scaling.callback.ScalingResultCallback;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.scaling.core.job.ScalingJob;
import org.apache.shardingsphere.scaling.core.service.ScalingJobService;

import java.util.Optional;

/**
 * Scaling service holder.
 */
public final class ScalingServiceHolder {
    
    private static final ScalingServiceHolder INSTANCE = new ScalingServiceHolder();
    
    private volatile ScalingJobService scalingJobService;
    
    private ScalingServiceHolder() {
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Get scaling service holder instance.
     * 
     * @return scaling service holder instance
     */
    public static ScalingServiceHolder getInstance() {
        return INSTANCE;
    }
    
    /**
     * Init scaling service.
     * 
     * @param scalingJobService scaling job service
     */
    public void init(final ScalingJobService scalingJobService) {
        this.scalingJobService = scalingJobService;
    }
    
    /**
     * Start scaling job.
     * 
     * @param event rule configurations altered event.
     */
    @Subscribe
    public void startScalingJob(final RuleConfigurationsAlteredEvent event) {
        Optional<ScalingJob> scalingJob = scalingJobService.start(event.getYamlDataSourceContent(), 
                event.getYamlRuleConfigurationsContent(), event.getYamlDataSourceContent(), 
                event.getCachedYamlRuleConfigurationsContent(), new ScalingResultCallback(event.getSchemaName(), event.getRuleConfigurationCacheId()));
        if (!scalingJob.isPresent()) {
            ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(event.getSchemaName(), event.getRuleConfigurationCacheId()));
        }
    }
}
