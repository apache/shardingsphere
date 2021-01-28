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

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingContext;
import org.apache.shardingsphere.scaling.core.config.ServerConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.execute.executor.job.FinishedCheckJobExecutor;
import org.apache.shardingsphere.scaling.core.execute.executor.job.ScalingJobExecutor;
import org.apache.shardingsphere.scaling.core.job.JobContext;
import org.apache.shardingsphere.scaling.core.service.ScalingJobServiceFactory;

import java.util.Optional;

/**
 * Scaling worker.
 */
@Slf4j
public final class ScalingWorker {
    
    private static final ScalingWorker INSTANCE = new ScalingWorker();
    
    /**
     * Init scaling worker.
     */
    public static void init() {
        ServerConfiguration serverConfig = ScalingContext.getInstance().getServerConfig();
        Preconditions.checkArgument(null != serverConfig && null != serverConfig.getGovernanceConfig(), "Scaling server config and governance config is required.");
        ShardingSphereEventBus.getInstance().register(INSTANCE);
        new FinishedCheckJobExecutor().start();
        new ScalingJobExecutor().start();
    }
    
    /**
     * Start scaling job.
     *
     * @param event rule configurations altered event.
     */
    @Subscribe
    public void start(final RuleConfigurationsAlteredEvent event) {
        log.info("Start scaling job by {}", event);
        Optional<JobContext> jobContext = ScalingJobServiceFactory.getInstance().start(createJobConfig(event));
        if (!jobContext.isPresent()) {
            log.info("Switch rule configuration ruleCacheId = {} immediately.", event.getRuleCacheId());
            ShardingSphereEventBus.getInstance().post(new SwitchRuleConfigurationEvent(event.getSchemaName(), event.getRuleCacheId()));
        }
    }
    
    private JobConfiguration createJobConfig(final RuleConfigurationsAlteredEvent event) {
        JobConfiguration result = new JobConfiguration();
        result.setRuleConfig(new RuleConfiguration(
                new ShardingSphereJDBCDataSourceConfiguration(event.getSourceDataSource(), event.getSourceRule()),
                new ShardingSphereJDBCDataSourceConfiguration(event.getTargetDataSource(), event.getTargetRule())));
        result.setHandleConfig(new HandleConfiguration(new WorkflowConfiguration(event.getSchemaName(), event.getRuleCacheId())));
        return result;
    }
}
