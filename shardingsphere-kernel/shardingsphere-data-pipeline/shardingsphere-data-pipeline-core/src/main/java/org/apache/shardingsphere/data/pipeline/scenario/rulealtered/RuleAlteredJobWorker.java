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

package org.apache.shardingsphere.data.pipeline.scenario.rulealtered;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.config.job.MigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.job.yaml.YamlMigrationJobConfigurationSwapper;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.job.JobType;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.api.PipelineAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.scenario.migration.MigrationJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetector;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetectorFactory;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule altered job worker.
 */
@SuppressWarnings("UnstableApiUsage")
@Slf4j
public final class RuleAlteredJobWorker {
    
    private static final RuleAlteredJobWorker INSTANCE = new RuleAlteredJobWorker();
    
    /**
     * Get instance.
     *
     * @return instance
     */
    public static RuleAlteredJobWorker getInstance() {
        return INSTANCE;
    }
    
    /**
     * Is on rule altered action enabled.
     *
     * @param ruleConfig rule configuration
     * @return enabled or not
     */
    public static boolean isOnRuleAlteredActionEnabled(final RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return false;
        }
        Optional<RuleAlteredDetector> detector = RuleAlteredDetectorFactory.findInstance(ruleConfig);
        return detector.isPresent() && detector.get().getOnRuleAlteredActionConfig(ruleConfig).isPresent();
    }
    
    /**
     * Start scaling job.
     *
     * @param event start scaling event.
     */
    @Subscribe
    public void start(final StartScalingEvent event) {
        log.info("Start scaling job by {}", event);
        if (hasUncompletedJobOfSameDatabaseName(event.getDatabaseName())) {
            log.warn("There is uncompleted job with the same database name, please handle it first, current job will be ignored");
            return;
        }
        Optional<MigrationJobConfiguration> jobConfig = createJobConfig(event);
        if (jobConfig.isPresent()) {
            MigrationJobAPIFactory.getInstance().start(jobConfig.get());
        } else {
            log.info("Switch rule configuration immediately.");
            ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(event.getDatabaseName(), event.getActiveVersion(), event.getNewVersion());
            PipelineContext.getContextManager().getInstanceContext().getEventBusContext().post(taskFinishedEvent);
        }
    }
    
    private Optional<MigrationJobConfiguration> createJobConfig(final StartScalingEvent event) {
        YamlRootConfiguration sourceRootConfig = getYamlRootConfiguration(event.getDatabaseName(), event.getSourceDataSource(), event.getSourceRule());
        YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(event.getDatabaseName(), event.getTargetDataSource(), event.getTargetRule());
        Map<String, List<String>> alteredRuleYamlClassNameTablesMap = new HashMap<>();
        for (Pair<YamlRuleConfiguration, YamlRuleConfiguration> each : groupSourceTargetRuleConfigsByType(sourceRootConfig.getRules(), targetRootConfig.getRules())) {
            YamlRuleConfiguration yamlRuleConfig = null == each.getLeft() ? each.getRight() : each.getLeft();
            Optional<RuleAlteredDetector> detector = RuleAlteredDetectorFactory.findInstance(yamlRuleConfig);
            if (!detector.isPresent()) {
                continue;
            }
            List<String> ruleAlteredLogicTables = detector.get().findRuleAlteredLogicTables(each.getLeft(), each.getRight(), sourceRootConfig.getDataSources(), targetRootConfig.getDataSources());
            log.info("type={}, ruleAlteredLogicTables={}", yamlRuleConfig.getClass().getName(), ruleAlteredLogicTables);
            if (!ruleAlteredLogicTables.isEmpty()) {
                alteredRuleYamlClassNameTablesMap.put(yamlRuleConfig.getClass().getName(), ruleAlteredLogicTables);
            }
        }
        if (alteredRuleYamlClassNameTablesMap.isEmpty()) {
            log.error("no altered rule");
            throw new PipelineJobCreationException("no altered rule");
        }
        if (alteredRuleYamlClassNameTablesMap.size() > 1) {
            log.error("more than 1 rule altered");
            throw new PipelineJobCreationException("more than 1 rule altered");
        }
        YamlMigrationJobConfiguration result = new YamlMigrationJobConfiguration();
        result.setTargetDatabaseName(event.getDatabaseName());
        result.setSource(createYamlPipelineDataSourceConfiguration(sourceRootConfig));
        result.setTarget(createYamlPipelineDataSourceConfiguration(targetRootConfig));
        PipelineAPIFactory.getPipelineJobAPI(JobType.MIGRATION).extendYamlJobConfiguration(result);
        return Optional.of(new YamlMigrationJobConfigurationSwapper().swapToObject(result));
    }
    
    private Collection<Pair<YamlRuleConfiguration, YamlRuleConfiguration>> groupSourceTargetRuleConfigsByType(final Collection<YamlRuleConfiguration> sourceRules,
                                                                                                              final Collection<YamlRuleConfiguration> targetRules) {
        Map<Class<? extends YamlRuleConfiguration>, YamlRuleConfiguration> sourceRulesMap = sourceRules.stream().collect(Collectors.toMap(YamlRuleConfiguration::getClass, Function.identity()));
        Map<Class<? extends YamlRuleConfiguration>, YamlRuleConfiguration> targetRulesMap = targetRules.stream().collect(Collectors.toMap(YamlRuleConfiguration::getClass, Function.identity()));
        Collection<Pair<YamlRuleConfiguration, YamlRuleConfiguration>> result = new LinkedList<>();
        for (Entry<Class<? extends YamlRuleConfiguration>, YamlRuleConfiguration> entry : sourceRulesMap.entrySet()) {
            YamlRuleConfiguration targetRule = targetRulesMap.get(entry.getKey());
            result.add(Pair.of(entry.getValue(), targetRule));
        }
        for (Entry<Class<? extends YamlRuleConfiguration>, YamlRuleConfiguration> entry : targetRulesMap.entrySet()) {
            if (!sourceRulesMap.containsKey(entry.getKey())) {
                result.add(Pair.of(null, entry.getValue()));
            }
        }
        return result;
    }
    
    private YamlPipelineDataSourceConfiguration createYamlPipelineDataSourceConfiguration(final YamlRootConfiguration yamlConfig) {
        PipelineDataSourceConfiguration config = new ShardingSpherePipelineDataSourceConfiguration(yamlConfig);
        YamlPipelineDataSourceConfiguration result = new YamlPipelineDataSourceConfiguration();
        result.setType(config.getType());
        result.setParameter(config.getParameter());
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private YamlRootConfiguration getYamlRootConfiguration(final String databaseName, final String dataSources, final String rules) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setDatabaseName(databaseName);
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSources, Map.class);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigs = YamlEngine.unmarshal(rules, Collection.class, true);
        result.setRules(yamlRuleConfigs);
        return result;
    }
    
    private boolean hasUncompletedJobOfSameDatabaseName(final String databaseName) {
        boolean result = false;
        for (JobInfo each : MigrationJobAPIFactory.getInstance().list()) {
            if (MigrationJobAPIFactory.getInstance().getJobProgress(each.getJobId()).values().stream()
                    .allMatch(progress -> null != progress && progress.getStatus().equals(JobStatus.FINISHED))) {
                continue;
            }
            MigrationJobConfiguration jobConfig = YamlMigrationJobConfigurationSwapper.swapToObject(each.getJobParameter());
            if (databaseName.equals(jobConfig.getTargetDatabaseName())) {
                result = true;
                break;
            }
        }
        return result;
    }
}
