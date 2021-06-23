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

package org.apache.shardingsphere.scaling.core.api;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.executor.job.FinishedCheckJobExecutor;
import org.apache.shardingsphere.scaling.core.executor.job.ScalingJobExecutor;

import java.util.Optional;

/**
 * Scaling worker.
 */
@Slf4j
public final class ScalingWorker {
    
    private static final ScalingWorker INSTANCE = new ScalingWorker();
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    /**
     * Init scaling worker.
     */
    public static void init() {
        ShardingSphereEventBus.getInstance().register(INSTANCE);
        new FinishedCheckJobExecutor().start();
        new ScalingJobExecutor().start();
    }
    
    /**
     * Start scaling job.
     *
     * @param event start scaling event.
     */
    @Subscribe
    public void start(final StartScalingEvent event) {
        log.info("Start scaling job by {}", event);
        Optional<Long> jobId = scalingAPI.start(createJobConfig(event));
        if (!jobId.isPresent()) {
            log.info("Switch rule configuration ruleCacheId = {} immediately.", event.getRuleCacheId());
            ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(event.getSchemaName(), event.getRuleCacheId()));
        }
    }
    
    private JobConfiguration createJobConfig(final StartScalingEvent event) {
        JobConfiguration result = new JobConfiguration();
        result.setRuleConfig(getRuleConfiguration(event));
        result.setHandleConfig(new HandleConfiguration(new WorkflowConfiguration(event.getSchemaName(), event.getRuleCacheId())));
        return result;
    }
    
    private RuleConfiguration getRuleConfiguration(final StartScalingEvent event) {
        RuleConfiguration result = new RuleConfiguration();
        result.setSource(new ShardingSphereJDBCDataSourceConfiguration(event.getSourceDataSource(), event.getSourceRule()).wrap());
        result.setTarget(new ShardingSphereJDBCDataSourceConfiguration(event.getTargetDataSource(), event.getTargetRule()).wrap());
        return result;
    }
}
