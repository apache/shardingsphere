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

package org.apache.shardingsphere.migration.common.api;

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.spi.JobRuleAlteredDetector;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.scaling.core.api.ScalingAPIFactory;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.WorkflowConfiguration;
import org.apache.shardingsphere.scaling.core.executor.job.FinishedCheckJobExecutor;
import org.apache.shardingsphere.scaling.core.executor.job.ScalingJobExecutor;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.typed.TypedSPI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Scaling worker.
 */
@Slf4j
public final class ScalingWorker {
    
    static {
        ShardingSphereServiceLoader.register(JobRuleAlteredDetector.class);
    }
    
    private static final ScalingWorker INSTANCE = new ScalingWorker();
    
    @Getter
    private static boolean enabled;
    
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
        Optional<Long> jobId = jobConfigOptional.isPresent() ? ScalingAPIFactory.getScalingAPI().start(jobConfigOptional.get()) : Optional.empty();
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
        Map<String, JobRuleAlteredDetector> typeDetectorMap = ShardingSphereServiceLoader.getSingletonServiceInstances(JobRuleAlteredDetector.class).stream()
                .collect(Collectors.toMap(TypedSPI::getType, Function.identity()));
        for (Pair<YamlRuleConfiguration, YamlRuleConfiguration> each : groupSourceTargetRuleConfigsByType(sourceRootConfig.getRules(), targetRootConfig.getRules())) {
            String type = each.getClass().getName();
            JobRuleAlteredDetector detector = typeDetectorMap.get(type);
            if (null == detector) {
                continue;
            }
            boolean ruleAltered = detector.isRuleAltered(each.getLeft(), each.getRight());
            log.info("type={}, ruleAltered={}", type, ruleAltered);
            if (ruleAltered) {
                break;
            }
        }
        WorkflowConfiguration workflowConfig = new WorkflowConfiguration(event.getSchemaName(), event.getRuleCacheId());
        RuleConfiguration ruleConfig = getRuleConfiguration(sourceRootConfig, targetRootConfig);
        return Optional.of(new JobConfiguration(workflowConfig, ruleConfig));
    }
    
    private Collection<Pair<YamlRuleConfiguration, YamlRuleConfiguration>> groupSourceTargetRuleConfigsByType(
            final Collection<YamlRuleConfiguration> sourceRules, final Collection<YamlRuleConfiguration> targetRules) {
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
        disableSSLForMySQL(yamlDataSources);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigs = YamlEngine.unmarshal(rules, Collection.class);
        result.setRules(yamlRuleConfigs);
        return result;
    }
    
    private void disableSSLForMySQL(final Map<String, Map<String, Object>> yamlDataSources) {
        String jdbcUrl = (String) yamlDataSources.entrySet().iterator().next().getValue().get("jdbcUrl");
        DatabaseType databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl);
        if (!(databaseType instanceof MySQLDatabaseType)) {
            return;
        }
        Map<String, String> parameters = ImmutableMap.of("useSSL", "false");
        for (Entry<String, Map<String, Object>> entry : yamlDataSources.entrySet()) {
            jdbcUrl = (String) entry.getValue().get("jdbcUrl");
            entry.getValue().put("jdbcUrl", new JdbcUri(jdbcUrl).appendParameters(parameters));
        }
    }
}
