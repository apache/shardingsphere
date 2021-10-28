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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.scaling.core.config.HandleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.executor.job.FinishedCheckJobExecutor;
import org.apache.shardingsphere.scaling.core.executor.job.ScalingJobExecutor;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Scaling worker.
 */
@Slf4j
public final class ScalingWorker {
    
    private static final ScalingWorker INSTANCE = new ScalingWorker();
    
    @Getter
    private static boolean enabled;
    
    private final ScalingAPI scalingAPI = ScalingAPIFactory.getScalingAPI();
    
    /**
     * Init scaling worker.
     */
    public static void init() {
        ShardingSphereEventBus.getInstance().register(INSTANCE);
        new FinishedCheckJobExecutor().start();
        new ScalingJobExecutor().start();
        enabled = true;
    }
    
    /**
     * Start scaling job.
     *
     * @param event start scaling event.
     */
    @Subscribe
    public void start(final StartScalingEvent event) {
        log.info("Start scaling job by {}", event);
        Optional<JobConfiguration> jobConfigOptional = createJobConfig(event);
        Optional<Long> jobId = jobConfigOptional.isPresent() ? scalingAPI.start(jobConfigOptional.get()) : Optional.empty();
        if (!jobId.isPresent()) {
            log.info("Switch rule configuration immediately.");
            YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(event.getSchemaName(), event.getTargetDataSource(), event.getTargetRule());
            ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(event.getSchemaName(), targetRootConfig, event.getRuleCacheId());
            ShardingSphereEventBus.getInstance().post(taskFinishedEvent);
        }
    }
    
    private Optional<JobConfiguration> createJobConfig(final StartScalingEvent event) {
        YamlRootConfiguration sourceRootConfig = getYamlRootConfiguration(event.getSchemaName(), event.getSourceDataSource(), event.getSourceRule());
        YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(event.getSchemaName(), event.getTargetDataSource(), event.getTargetRule());
        Optional<YamlShardingRuleConfiguration> sourceShardingConfigOptional = getYamlShardingRuleConfiguration(sourceRootConfig);
        Optional<YamlShardingRuleConfiguration> targetShardingConfigOptional = getYamlShardingRuleConfiguration(targetRootConfig);
        if (!sourceShardingConfigOptional.isPresent() || !targetShardingConfigOptional.isPresent()) {
            log.info("sourceShardingConfig or targetShardingConfig not present, ignore");
            return Optional.empty();
        }
        if (isShardingRulesTheSame(sourceShardingConfigOptional.get(), targetShardingConfigOptional.get())) {
            log.info("source and target sharding configuration is the same, ignore");
            return Optional.empty();
        }
        RuleConfiguration ruleConfig = getRuleConfiguration(sourceRootConfig, targetRootConfig);
        HandleConfiguration handleConfig = new HandleConfiguration(new WorkflowConfiguration(event.getSchemaName(), event.getRuleCacheId()));
        return Optional.of(new JobConfiguration(ruleConfig, handleConfig));
    }
    
    private Optional<YamlShardingRuleConfiguration> getYamlShardingRuleConfiguration(final YamlRootConfiguration rootConfig) {
        return rootConfig.getRules().stream().filter(each -> each instanceof YamlShardingRuleConfiguration).map(each -> (YamlShardingRuleConfiguration) each).findFirst();
    }
    
    private boolean isShardingRulesTheSame(final YamlShardingRuleConfiguration sourceShardingConfig, final YamlShardingRuleConfiguration targetShardingConfig) {
        for (Entry<String, YamlTableRuleConfiguration> entry : sourceShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        for (Entry<String, YamlTableRuleConfiguration> entry : targetShardingConfig.getTables().entrySet()) {
            entry.getValue().setLogicTable(null);
        }
        String sourceShardingConfigYaml = YamlEngine.marshal(sourceShardingConfig);
        String targetShardingConfigYaml = YamlEngine.marshal(targetShardingConfig);
        return sourceShardingConfigYaml.equals(targetShardingConfigYaml);
    }
    
    private RuleConfiguration getRuleConfiguration(final YamlRootConfiguration sourceRootConfig, final YamlRootConfiguration targetRootConfig) {
        RuleConfiguration result = new RuleConfiguration();
        result.setSource(new ShardingSphereJDBCDataSourceConfiguration(sourceRootConfig).wrap());
        result.setTarget(new ShardingSphereJDBCDataSourceConfiguration(targetRootConfig).wrap());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private YamlRootConfiguration getYamlRootConfiguration(final String schemaName, final String dataSources, final String rules) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setSchemaName(schemaName);
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSources, Map.class);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigs = YamlEngine.unmarshal(rules, Collection.class);
        result.setRules(yamlRuleConfigs);
        return result;
    }
}
