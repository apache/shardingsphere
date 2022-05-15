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

package org.apache.shardingsphere.dbdiscovery.rule;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.dbdiscovery.algorithm.DatabaseDiscoveryEngine;
import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryHeartBeatConfiguration;
import org.apache.shardingsphere.dbdiscovery.factory.DatabaseDiscoveryProviderAlgorithmFactory;
import org.apache.shardingsphere.dbdiscovery.heartbeat.HeartbeatJob;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm;
import org.apache.shardingsphere.infra.datasource.strategy.DynamicDataSourceStrategyFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.mode.metadata.storage.event.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.schedule.core.api.CronJob;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContext;
import org.apache.shardingsphere.schedule.core.api.ModeScheduleContextFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Database discovery rule.
 */
public final class DatabaseDiscoveryRule implements SchemaRule, DataSourceContainedRule, StatusContainedRule, ExportableRule {
    
    private final Map<String, DatabaseDiscoveryProviderAlgorithm> discoveryTypes;
    
    @Getter
    private final Map<String, DatabaseDiscoveryDataSourceRule> dataSourceRules;
    
    public DatabaseDiscoveryRule(final String databaseName, final Map<String, DataSource> dataSourceMap, final DatabaseDiscoveryRuleConfiguration config) {
        this(databaseName, dataSourceMap, config.getDataSources(), config.getDiscoveryHeartbeats(), getDiscoveryProviderAlgorithms(config.getDiscoveryTypes()));
    }
    
    public DatabaseDiscoveryRule(final String databaseName, final Map<String, DataSource> dataSourceMap, final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config) {
        this(databaseName, dataSourceMap, config.getDataSources(), config.getDiscoveryHeartbeats(), config.getDiscoveryTypes());
    }
    
    private DatabaseDiscoveryRule(final String databaseName, final Map<String, DataSource> dataSourceMap, final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfigs,
                                  final Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartBeatConfig, final Map<String, DatabaseDiscoveryProviderAlgorithm> discoveryTypes) {
        this.discoveryTypes = discoveryTypes;
        dataSourceRules = getDataSourceRules(dataSourceRuleConfigs, heartBeatConfig);
        findMasterSlaveRelation(databaseName, dataSourceMap);
        initAware();
        initHeartBeatJobs(databaseName, dataSourceMap);
    }
    
    private static Map<String, DatabaseDiscoveryProviderAlgorithm> getDiscoveryProviderAlgorithms(final Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypesConfig) {
        Map<String, DatabaseDiscoveryProviderAlgorithm> result = new LinkedHashMap<>(discoveryTypesConfig.size(), 1);
        for (Entry<String, ShardingSphereAlgorithmConfiguration> entry : discoveryTypesConfig.entrySet()) {
            result.put(entry.getKey(), DatabaseDiscoveryProviderAlgorithmFactory.newInstance(entry.getValue()));
        }
        return result;
    }
    
    private Map<String, DatabaseDiscoveryDataSourceRule> getDataSourceRules(final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources,
                                                                            final Map<String, DatabaseDiscoveryHeartBeatConfiguration> heartbeatConfig) {
        Map<String, DatabaseDiscoveryDataSourceRule> result = new HashMap<>(dataSources.size(), 1);
        for (DatabaseDiscoveryDataSourceRuleConfiguration each : dataSources) {
            result.put(each.getGroupName(), new DatabaseDiscoveryDataSourceRule(each, Strings.isNullOrEmpty(each.getDiscoveryHeartbeatName()) ? new Properties()
                    : heartbeatConfig.get(each.getDiscoveryHeartbeatName()).getProps(), discoveryTypes.get(each.getDiscoveryTypeName())));
        }
        return result;
    }
    
    private void findMasterSlaveRelation(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            String groupName = entry.getKey();
            DatabaseDiscoveryDataSourceRule dataSourceRule = entry.getValue();
            DatabaseDiscoveryEngine engine = new DatabaseDiscoveryEngine(dataSourceRule.getDatabaseDiscoveryProviderAlgorithm());
            Map<String, DataSource> originalDataSourceMap = new HashMap<>(dataSourceMap);
            try {
                engine.checkEnvironment(databaseName, originalDataSourceMap);
            } catch (final SQLException ex) {
                throw new ShardingSphereException(ex);
            }
            dataSourceRule.changePrimaryDataSourceName(engine.changePrimaryDataSource(
                    databaseName, groupName, entry.getValue().getPrimaryDataSourceName(), originalDataSourceMap, dataSourceRule.getDisabledDataSourceNames()));
        }
    }
    
    private void initAware() {
        DynamicDataSourceStrategyFactory.findInstance().ifPresent(optional -> optional.init(this));
    }
    
    /**
     * Get single data source rule.
     *
     * @return data source rule
     */
    public DatabaseDiscoveryDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     *
     * @param dataSourceName data source name
     * @return found data source rule
     */
    public Optional<DatabaseDiscoveryDataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                if (((DataSourceNameDisabledEvent) event).isDisabled()) {
                    entry.getValue().disableDataSource(((DataSourceNameDisabledEvent) event).getQualifiedDatabase().getDataSourceName());
                } else {
                    entry.getValue().enableDataSource(((DataSourceNameDisabledEvent) event).getQualifiedDatabase().getDataSourceName());
                }
            }
        } else if (event instanceof PrimaryDataSourceChangedEvent) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                if (entry.getValue().getGroupName().equals(((PrimaryDataSourceChangedEvent) event).getQualifiedDatabase().getGroupName())) {
                    entry.getValue().changePrimaryDataSourceName(((PrimaryDataSourceChangedEvent) event).getQualifiedDatabase().getDataSourceName());
                }
            }
        }
    }
    
    @Override
    public Map<String, Supplier<Object>> getExportedMethods() {
        Map<String, Supplier<Object>> result = new HashMap<>(1, 1);
        result.put(ExportableConstants.EXPORTABLE_KEY_PRIMARY_DATA_SOURCE, this::exportPrimaryDataSourceMap);
        return result;
    }
    
    private Map<String, String> exportPrimaryDataSourceMap() {
        Map<String, String> result = new HashMap<>(dataSourceRules.size(), 1);
        dataSourceRules.forEach((name, dataSourceRule) -> result.put(dataSourceRule.getGroupName(), dataSourceRule.getPrimaryDataSourceName()));
        return result;
    }
    
    private void initHeartBeatJobs(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        Optional<ModeScheduleContext> modeScheduleContext = ModeScheduleContextFactory.getInstance().get();
        if (modeScheduleContext.isPresent()) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                DatabaseDiscoveryDataSourceRule rule = entry.getValue();
                Map<String, DataSource> dataSources = dataSourceMap.entrySet().stream().filter(each -> !rule.getDisabledDataSourceNames().contains(each.getKey()))
                        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
                String jobName = rule.getDatabaseDiscoveryProviderAlgorithm().getType() + "-" + databaseName + "-" + rule.getGroupName();
                CronJob job = new CronJob(jobName, each -> new HeartbeatJob(databaseName, rule.getGroupName(), rule.getPrimaryDataSourceName(), dataSources,
                        rule.getDatabaseDiscoveryProviderAlgorithm(), rule.getDisabledDataSourceNames()).execute(null), rule.getHeartbeatProps().getProperty("keep-alive-cron"));
                modeScheduleContext.get().startCronJob(job);
            }
        }
    }
    
    @Override
    public String getType() {
        return DatabaseDiscoveryRule.class.getSimpleName();
    }
}
