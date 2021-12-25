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

import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.execute.FinishedCheckJobExecutor;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineJobExecutor;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetector;
import org.apache.shardingsphere.infra.config.datasource.JdbcUri;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.JDBCDataSourceConfigurationFactory;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.impl.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.jdbc.config.yaml.YamlJDBCDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule altered job worker.
 */
@Slf4j
public final class RuleAlteredJobWorker {
    
    static {
        ShardingSphereServiceLoader.register(RuleAlteredDetector.class);
    }
    
    private static final RuleAlteredJobWorker INSTANCE = new RuleAlteredJobWorker();
    
    // TODO refactor
    private static final Map<String, RuleAlteredDetector> RULE_CLASS_NAME_DETECTOR_MAP = ShardingSphereServiceLoader.getSingletonServiceInstances(RuleAlteredDetector.class).stream()
            .collect(Collectors.toMap(RuleAlteredDetector::getRuleConfigClassName, Function.identity()));
    
    private static final Map<String, RuleAlteredDetector> YAML_RULE_CLASS_NAME_DETECTOR_MAP = ShardingSphereServiceLoader.getSingletonServiceInstances(RuleAlteredDetector.class).stream()
            .collect(Collectors.toMap(RuleAlteredDetector::getYamlRuleConfigClassName, Function.identity()));
    
    private static final YamlRuleConfigurationSwapperEngine SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
    /**
     * Is on rule altered action enabled.
     *
     * @param ruleConfig rule configuration
     * @return enabled or not
     */
    public static boolean isOnRuleAlteredActionEnabled(final org.apache.shardingsphere.infra.config.RuleConfiguration ruleConfig) {
        if (null == ruleConfig) {
            return false;
        }
        RuleAlteredDetector detector = RULE_CLASS_NAME_DETECTOR_MAP.get(ruleConfig.getClass().getName());
        return null != detector && detector.getOnRuleAlteredActionConfig(ruleConfig).isPresent();
    }
    
    /**
     * Initialize job worker if necessary.
     */
    public static void initWorkerIfNecessary() {
        if (WORKER_INITIALIZED.get()) {
            return;
        }
        synchronized (WORKER_INITIALIZED) {
            if (WORKER_INITIALIZED.get()) {
                return;
            }
            log.info("start worker initialization");
            ShardingSphereEventBus.getInstance().register(INSTANCE);
            new FinishedCheckJobExecutor().start();
            new PipelineJobExecutor().start();
            WORKER_INITIALIZED.set(true);
            log.info("worker initialization done");
        }
    }
    
    /**
     * Create rule altered context.
     *
     * @param jobConfig job configuration
     * @return rule altered context
     */
    public static RuleAlteredContext createRuleAlteredContext(final JobConfiguration jobConfig) {
        YamlRootConfiguration targetRootConfig = getYamlRootConfig(jobConfig);
        YamlRuleConfiguration yamlRuleConfig = null;
        for (YamlRuleConfiguration each : targetRootConfig.getRules()) {
            if (jobConfig.getWorkflowConfig().getAlteredRuleYamlClassNames().contains(each.getClass().getName())) {
                yamlRuleConfig = each;
                break;
            }
        }
        if (null == yamlRuleConfig) {
            throw new PipelineJobCreationException("could not find altered rule");
        }
        org.apache.shardingsphere.infra.config.RuleConfiguration ruleConfig = SWAPPER_ENGINE.swapToRuleConfiguration(yamlRuleConfig);
        RuleAlteredDetector detector = RULE_CLASS_NAME_DETECTOR_MAP.get(ruleConfig.getClass().getName());
        Optional<OnRuleAlteredActionConfiguration> onRuleAlteredActionConfigOptional = detector.getOnRuleAlteredActionConfig(ruleConfig);
        if (!onRuleAlteredActionConfigOptional.isPresent()) {
            log.error("rule altered action enabled but actor is not configured, ignored, ruleConfig={}", ruleConfig);
            throw new PipelineJobCreationException("rule altered actor not configured");
        }
        return new RuleAlteredContext(onRuleAlteredActionConfigOptional.get());
    }
    
    /**
     * Get YAML root configuration, which should include rule altered action configuration.
     *
     * @param jobConfig job configuration
     * @return YAML root configuration
     */
    private static YamlRootConfiguration getYamlRootConfig(final JobConfiguration jobConfig) {
        JDBCDataSourceConfiguration targetDataSourceConfig = JDBCDataSourceConfigurationFactory.newInstance(
                jobConfig.getPipelineConfig().getTarget().getType(), jobConfig.getPipelineConfig().getTarget().getParameter());
        if (targetDataSourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration) {
            return ((ShardingSphereJDBCDataSourceConfiguration) targetDataSourceConfig).getRootConfig();
        }
        JDBCDataSourceConfiguration sourceDataSourceConfig = JDBCDataSourceConfigurationFactory.newInstance(
                jobConfig.getPipelineConfig().getSource().getType(), jobConfig.getPipelineConfig().getSource().getParameter());
        return ((ShardingSphereJDBCDataSourceConfiguration) sourceDataSourceConfig).getRootConfig();
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
        Optional<Long> jobId = jobConfigOptional.isPresent() ? PipelineJobAPIFactory.getPipelineJobAPI().start(jobConfigOptional.get()) : Optional.empty();
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
        Set<String> alteredRuleYamlClassNames = new LinkedHashSet<>();
        for (Pair<YamlRuleConfiguration, YamlRuleConfiguration> each : groupSourceTargetRuleConfigsByType(sourceRootConfig.getRules(), targetRootConfig.getRules())) {
            String type = (null != each.getLeft() ? each.getLeft() : each.getRight()).getClass().getName();
            RuleAlteredDetector detector = YAML_RULE_CLASS_NAME_DETECTOR_MAP.get(type);
            if (null == detector) {
                continue;
            }
            boolean ruleAltered = detector.isRuleAltered(each.getLeft(), each.getRight());
            log.info("type={}, ruleAltered={}", type, ruleAltered);
            if (ruleAltered) {
                alteredRuleYamlClassNames.add(type);
            }
        }
        if (alteredRuleYamlClassNames.isEmpty()) {
            log.error("no altered rule");
            throw new PipelineJobCreationException("no altered rule");
        }
        if (alteredRuleYamlClassNames.size() > 1) {
            log.error("more than 1 rule altered");
            throw new PipelineJobCreationException("more than 1 rule altered");
        }
        WorkflowConfiguration workflowConfig = new WorkflowConfiguration(event.getSchemaName(), new ArrayList<>(alteredRuleYamlClassNames), event.getRuleCacheId());
        PipelineConfiguration pipelineConfig = getPipelineConfiguration(sourceRootConfig, targetRootConfig);
        return Optional.of(new JobConfiguration(workflowConfig, pipelineConfig));
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
    
    private PipelineConfiguration getPipelineConfiguration(final YamlRootConfiguration sourceRootConfig, final YamlRootConfiguration targetRootConfig) {
        PipelineConfiguration result = new PipelineConfiguration();
        result.setSource(createYamlJDBCDataSourceConfiguration(sourceRootConfig));
        result.setTarget(createYamlJDBCDataSourceConfiguration(targetRootConfig));
        return result;
    }
    
    private YamlJDBCDataSourceConfiguration createYamlJDBCDataSourceConfiguration(final YamlRootConfiguration yamlConfig) {
        JDBCDataSourceConfiguration config = new ShardingSphereJDBCDataSourceConfiguration(yamlConfig);
        YamlJDBCDataSourceConfiguration result = new YamlJDBCDataSourceConfiguration();
        result.setType(config.getType());
        result.setParameter(config.getParameter());
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
