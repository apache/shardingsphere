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
import org.apache.shardingsphere.data.pipeline.api.PipelineJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.HandleConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.JobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.PipelineConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.WorkflowConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfigurationFactory;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.impl.ShardingSpherePipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlPipelineDataSourceConfiguration;
import org.apache.shardingsphere.data.pipeline.api.job.JobStatus;
import org.apache.shardingsphere.data.pipeline.api.pojo.JobInfo;
import org.apache.shardingsphere.data.pipeline.core.context.PipelineContext;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineJobCreationException;
import org.apache.shardingsphere.data.pipeline.core.execute.FinishedCheckJobExecutor;
import org.apache.shardingsphere.data.pipeline.core.execute.PipelineJobExecutor;
import org.apache.shardingsphere.data.pipeline.core.lock.PipelineSimpleLock;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetector;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparer;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingReleaseSchemaNameLockEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;
import org.apache.shardingsphere.spi.type.singleton.SingletonSPIRegistry;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Rule altered job worker.
 */
@Slf4j
public final class RuleAlteredJobWorker {
    
    static {
        ShardingSphereServiceLoader.register(RuleAlteredJobConfigurationPreparer.class);
        ShardingSphereServiceLoader.register(RuleAlteredDetector.class);
    }
    
    private static final RuleAlteredJobWorker INSTANCE = new RuleAlteredJobWorker();
    
    private static final Map<String, RuleAlteredDetector> RULE_CLASS_NAME_DETECTOR_MAP = SingletonSPIRegistry.getSingletonInstancesMap(
            RuleAlteredDetector.class, RuleAlteredDetector::getRuleConfigClassName);
    
    private static final Map<String, RuleAlteredDetector> YAML_RULE_CLASS_NAME_DETECTOR_MAP = SingletonSPIRegistry.getSingletonInstancesMap(
            RuleAlteredDetector.class, RuleAlteredDetector::getYamlRuleConfigClassName);
        
    private static final YamlRuleConfigurationSwapperEngine SWAPPER_ENGINE = new YamlRuleConfigurationSwapperEngine();
    
    private static final AtomicBoolean WORKER_INITIALIZED = new AtomicBoolean(false);
    
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
            if (jobConfig.getWorkflowConfig().getAlteredRuleYamlClassNameTablesMap().containsKey(each.getClass().getName())) {
                yamlRuleConfig = each;
                break;
            }
        }
        if (null == yamlRuleConfig) {
            throw new PipelineJobCreationException("could not find altered rule");
        }
        RuleConfiguration ruleConfig = SWAPPER_ENGINE.swapToRuleConfiguration(yamlRuleConfig);
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
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobConfig.getPipelineConfig().getTarget().getType(), jobConfig.getPipelineConfig().getTarget().getParameter());
        if (targetDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            return ((ShardingSpherePipelineDataSourceConfiguration) targetDataSourceConfig).getRootConfig();
        }
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(
                jobConfig.getPipelineConfig().getSource().getType(), jobConfig.getPipelineConfig().getSource().getParameter());
        return ((ShardingSpherePipelineDataSourceConfiguration) sourceDataSourceConfig).getRootConfig();
    }
    
    /**
     * Start scaling job.
     *
     * @param event start scaling event.
     */
    @Subscribe
    public void start(final StartScalingEvent event) {
        log.info("Start scaling job by {}", event);
        if (!isUncompletedJobOfSameSchemaInJobList(event.getSchemaName())) {
            log.warn("There is an outstanding job with the same schema name");
            return;
        }
        Optional<JobConfiguration> jobConfigOptional = createJobConfig(event);
        if (jobConfigOptional.isPresent()) {
            PipelineJobAPIFactory.getRuleAlteredJobAPI().start(jobConfigOptional.get());
        } else {
            log.info("Switch rule configuration immediately.");
            ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(event.getSchemaName(), event.getActiveVersion(), event.getNewVersion());
            ShardingSphereEventBus.getInstance().post(taskFinishedEvent);
        }
    }
    
    private Optional<JobConfiguration> createJobConfig(final StartScalingEvent event) {
        YamlRootConfiguration sourceRootConfig = getYamlRootConfiguration(event.getSchemaName(), event.getSourceDataSource(), event.getSourceRule());
        YamlRootConfiguration targetRootConfig = getYamlRootConfiguration(event.getSchemaName(), event.getTargetDataSource(), event.getTargetRule());
        Map<String, List<String>> alteredRuleYamlClassNameTablesMap = new HashMap<>();
        for (Pair<YamlRuleConfiguration, YamlRuleConfiguration> each : groupSourceTargetRuleConfigsByType(sourceRootConfig.getRules(), targetRootConfig.getRules())) {
            String type = (null != each.getLeft() ? each.getLeft() : each.getRight()).getClass().getName();
            RuleAlteredDetector detector = YAML_RULE_CLASS_NAME_DETECTOR_MAP.get(type);
            if (null == detector) {
                continue;
            }
            List<String> ruleAlteredLogicTables = detector.findRuleAlteredLogicTables(each.getLeft(), each.getRight(),
                    sourceRootConfig.getDataSources(), targetRootConfig.getDataSources());
            log.info("type={}, ruleAlteredLogicTables={}", type, ruleAlteredLogicTables);
            if (ruleAlteredLogicTables.size() > 0) {
                alteredRuleYamlClassNameTablesMap.put(type, ruleAlteredLogicTables);
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
        WorkflowConfiguration workflowConfig = new WorkflowConfiguration(event.getSchemaName(), alteredRuleYamlClassNameTablesMap, event.getActiveVersion(), event.getNewVersion());
        PipelineConfiguration pipelineConfig = getPipelineConfiguration(sourceRootConfig, targetRootConfig);
        return Optional.of(new JobConfiguration(workflowConfig, pipelineConfig));
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
    
    private PipelineConfiguration getPipelineConfiguration(final YamlRootConfiguration sourceRootConfig, final YamlRootConfiguration targetRootConfig) {
        PipelineConfiguration result = new PipelineConfiguration();
        result.setSource(createYamlPipelineDataSourceConfiguration(sourceRootConfig));
        result.setTarget(createYamlPipelineDataSourceConfiguration(targetRootConfig));
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
    private YamlRootConfiguration getYamlRootConfiguration(final String schemaName, final String dataSources, final String rules) {
        YamlRootConfiguration result = new YamlRootConfiguration();
        result.setSchemaName(schemaName);
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(dataSources, Map.class);
        disableSSLForMySQL(yamlDataSources);
        result.setDataSources(yamlDataSources);
        Collection<YamlRuleConfiguration> yamlRuleConfigs = YamlEngine.unmarshal(rules, Collection.class, true);
        result.setRules(yamlRuleConfigs);
        return result;
    }
    
    private void disableSSLForMySQL(final Map<String, Map<String, Object>> yamlDataSources) {
        Map<String, Object> firstDataSourceProps = yamlDataSources.entrySet().iterator().next().getValue();
        String jdbcUrlKey = firstDataSourceProps.containsKey("url") ? "url" : "jdbcUrl";
        String jdbcUrl = (String) firstDataSourceProps.get(jdbcUrlKey);
        if (null == jdbcUrl) {
            log.warn("disableSSLForMySQL, could not get jdbcUrl, jdbcUrlKey={}", jdbcUrlKey);
            return;
        }
        DatabaseType databaseType = DatabaseTypeRegistry.getDatabaseTypeByURL(jdbcUrl);
        if (!(databaseType instanceof MySQLDatabaseType)) {
            return;
        }
        Properties queryProps = new Properties();
        queryProps.setProperty("useSSL", Boolean.FALSE.toString());
        for (Entry<String, Map<String, Object>> entry : yamlDataSources.entrySet()) {
            entry.getValue().put(jdbcUrlKey, new JdbcUrlAppender().appendQueryProperties((String) entry.getValue().get(jdbcUrlKey), queryProps));
        }
    }
    
    /**
     * Build task configuration.
     *
     * @param pipelineConfig pipeline configuration
     * @param handleConfig handle configuration
     * @param onRuleAlteredActionConfig action configuration
     * @return task configuration
     */
    public static TaskConfiguration buildTaskConfig(final PipelineConfiguration pipelineConfig, final HandleConfiguration handleConfig,
                                                    final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig) {
        RuleAlteredJobConfigurationPreparer preparer = RequiredSPIRegistry.getRegisteredService(RuleAlteredJobConfigurationPreparer.class);
        return preparer.createTaskConfiguration(pipelineConfig, handleConfig, onRuleAlteredActionConfig);
    }
    
    private boolean isUncompletedJobOfSameSchemaInJobList(final String schema) {
        boolean isUncompletedJobOfSameSchema = false;
        for (JobInfo each : PipelineJobAPIFactory.getRuleAlteredJobAPI().list()) {
            if (PipelineJobAPIFactory.getRuleAlteredJobAPI().getProgress(each.getJobId()).values().stream()
                    .allMatch(progress -> null != progress && progress.getStatus().equals(JobStatus.FINISHED))) {
                continue;
            }
            JobConfiguration jobConfiguration = YamlEngine.unmarshal(each.getJobParameter(), JobConfiguration.class, true);
            if (isUncompletedJobOfSameSchema(jobConfiguration, each.getJobId(), schema)) {
                isUncompletedJobOfSameSchema = true;
                break;
            }
        }
        return !isUncompletedJobOfSameSchema;
    }
    
    private boolean isUncompletedJobOfSameSchema(final JobConfiguration jobConfig, final String jobId, final String currentSchema) {
        HandleConfiguration handleConfig = jobConfig.getHandleConfig();
        WorkflowConfiguration workflowConfig;
        if (null == handleConfig || null == (workflowConfig = jobConfig.getWorkflowConfig())) {
            log.warn("handleConfig or workflowConfig null, jobId={}", jobId);
            return false;
        }
        return currentSchema.equals(workflowConfig.getSchemaName());
    }
    
    /**
     * scaling release schema name lock.
     *
     * @param event scaling release schema name lock event
     */
    @Subscribe
    public void scalingReleaseSchemaNameLock(final ScalingReleaseSchemaNameLockEvent event) {
        String schemaName = event.getSchemaName();
        try {
            restoreSourceWriting(schemaName);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("restore source writing failed, schemaName={}", schemaName, ex);
        }
        PipelineSimpleLock.getInstance().releaseLock(event.getSchemaName());
    }
    
    private void restoreSourceWriting(final String schemaName) {
        log.info("restoreSourceWriting, schemaName={}", schemaName);
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        ShardingSphereLock lock = lockContext.getSchemaLock(schemaName).orElse(null);
        if (null != lock && lock.isLocked(schemaName)) {
            log.info("Source writing is still stopped on schema '{}', restore it now", schemaName);
            lock.releaseLock(schemaName);
        }
    }
}
