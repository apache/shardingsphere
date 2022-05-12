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

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shardingsphere.data.pipeline.api.RuleAlteredJobAPIFactory;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.RuleAlteredJobConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.rulealtered.TaskConfiguration;
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
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredDetectorFactory;
import org.apache.shardingsphere.data.pipeline.spi.rulealtered.RuleAlteredJobConfigurationPreparerFactory;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rulealtered.OnRuleAlteredActionConfiguration;
import org.apache.shardingsphere.infra.database.metadata.url.JdbcUrlAppender;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.cache.event.StartScalingEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingReleaseDatabaseLevelLockEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.ScalingTaskFinishedEvent;

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
    
    private static final RuleAlteredJobWorker INSTANCE = new RuleAlteredJobWorker();
    
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
        Optional<RuleAlteredDetector> detector = RuleAlteredDetectorFactory.findInstance(ruleConfig);
        return detector.isPresent() && detector.get().getOnRuleAlteredActionConfig(ruleConfig).isPresent();
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
    public static RuleAlteredContext createRuleAlteredContext(final RuleAlteredJobConfiguration jobConfig) {
        YamlRootConfiguration targetRootConfig = getYamlRootConfig(jobConfig);
        YamlRuleConfiguration yamlRuleConfig = null;
        for (YamlRuleConfiguration each : targetRootConfig.getRules()) {
            if (jobConfig.getAlteredRuleYamlClassNameTablesMap().containsKey(each.getClass().getName())) {
                yamlRuleConfig = each;
                break;
            }
        }
        if (null == yamlRuleConfig) {
            throw new PipelineJobCreationException("could not find altered rule");
        }
        RuleConfiguration ruleConfig = SWAPPER_ENGINE.swapToRuleConfiguration(yamlRuleConfig);
        Optional<RuleAlteredDetector> detector = RuleAlteredDetectorFactory.findInstance(ruleConfig);
        Preconditions.checkState(detector.isPresent());
        Optional<OnRuleAlteredActionConfiguration> onRuleAlteredActionConfig = detector.get().getOnRuleAlteredActionConfig(ruleConfig);
        if (!onRuleAlteredActionConfig.isPresent()) {
            log.error("rule altered action enabled but actor is not configured, ignored, ruleConfig={}", ruleConfig);
            throw new PipelineJobCreationException("rule altered actor not configured");
        }
        return new RuleAlteredContext(onRuleAlteredActionConfig.get());
    }
    
    /**
     * Get YAML root configuration, which should include rule altered action configuration.
     *
     * @param jobConfig job configuration
     * @return YAML root configuration
     */
    private static YamlRootConfiguration getYamlRootConfig(final RuleAlteredJobConfiguration jobConfig) {
        PipelineDataSourceConfiguration targetDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getTarget().getType(), jobConfig.getTarget().getParameter());
        if (targetDataSourceConfig instanceof ShardingSpherePipelineDataSourceConfiguration) {
            return ((ShardingSpherePipelineDataSourceConfiguration) targetDataSourceConfig).getRootConfig();
        }
        PipelineDataSourceConfiguration sourceDataSourceConfig = PipelineDataSourceConfigurationFactory.newInstance(jobConfig.getSource().getType(), jobConfig.getSource().getParameter());
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
        if (hasUncompletedJobOfSameDatabaseName(event.getDatabaseName())) {
            log.warn("There is uncompleted job with the same database name, please handle it first, current job will be ignored");
            return;
        }
        Optional<RuleAlteredJobConfiguration> jobConfigOptional = createJobConfig(event);
        if (jobConfigOptional.isPresent()) {
            RuleAlteredJobAPIFactory.getInstance().start(jobConfigOptional.get());
        } else {
            log.info("Switch rule configuration immediately.");
            ScalingTaskFinishedEvent taskFinishedEvent = new ScalingTaskFinishedEvent(event.getDatabaseName(), event.getActiveVersion(), event.getNewVersion());
            ShardingSphereEventBus.getInstance().post(taskFinishedEvent);
            ShardingSphereEventBus.getInstance().post(new ScalingReleaseDatabaseLevelLockEvent(event.getDatabaseName()));
        }
    }
    
    private Optional<RuleAlteredJobConfiguration> createJobConfig(final StartScalingEvent event) {
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
        RuleAlteredJobConfiguration result = new RuleAlteredJobConfiguration();
        result.setDatabaseName(event.getDatabaseName());
        result.setAlteredRuleYamlClassNameTablesMap(alteredRuleYamlClassNameTablesMap);
        result.setActiveVersion(event.getActiveVersion());
        result.setNewVersion(event.getNewVersion());
        result.setSource(createYamlPipelineDataSourceConfiguration(sourceRootConfig));
        result.setTarget(createYamlPipelineDataSourceConfiguration(targetRootConfig));
        return Optional.of(result);
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
        DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(jdbcUrl);
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
     * @param jobConfig job configuration
     * @param onRuleAlteredActionConfig action configuration
     * @return task configuration
     */
    public static TaskConfiguration buildTaskConfig(final RuleAlteredJobConfiguration jobConfig, final OnRuleAlteredActionConfiguration onRuleAlteredActionConfig) {
        return RuleAlteredJobConfigurationPreparerFactory.getInstance().createTaskConfiguration(jobConfig, onRuleAlteredActionConfig);
    }
    
    private boolean hasUncompletedJobOfSameDatabaseName(final String databaseName) {
        boolean result = false;
        for (JobInfo each : RuleAlteredJobAPIFactory.getInstance().list()) {
            if (RuleAlteredJobAPIFactory.getInstance().getProgress(each.getJobId()).values().stream()
                    .allMatch(progress -> null != progress && progress.getStatus().equals(JobStatus.FINISHED))) {
                continue;
            }
            RuleAlteredJobConfiguration jobConfig = YamlEngine.unmarshal(each.getJobParameter(), RuleAlteredJobConfiguration.class, true);
            if (hasUncompletedJobOfSameDatabaseName(jobConfig, databaseName)) {
                result = true;
                break;
            }
        }
        return result;
    }
    
    private boolean hasUncompletedJobOfSameDatabaseName(final RuleAlteredJobConfiguration jobConfig, final String currentDatabaseName) {
        return currentDatabaseName.equals(jobConfig.getDatabaseName());
    }
    
    /**
     * scaling release database level lock.
     *
     * @param event scaling release database level lock event
     */
    @Subscribe
    public void scalingReleaseDatabaseLevelLock(final ScalingReleaseDatabaseLevelLockEvent event) {
        String databaseName = event.getDatabaseName();
        try {
            restoreSourceWriting(databaseName);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            log.error("restore source writing failed, databaseName={}", databaseName, ex);
        }
        PipelineSimpleLock.getInstance().releaseLock(event.getDatabaseName());
    }
    
    private void restoreSourceWriting(final String databaseName) {
        log.info("restoreSourceWriting, databaseName={}", databaseName);
        LockContext lockContext = PipelineContext.getContextManager().getInstanceContext().getLockContext();
        ShardingSphereLock lock = lockContext.getGlobalLock(databaseName);
        if (null != lock && lock.isLocked(databaseName)) {
            log.info("Source writing is still stopped on database '{}', restore it now", databaseName);
            lock.releaseLock(databaseName);
        }
    }
}
