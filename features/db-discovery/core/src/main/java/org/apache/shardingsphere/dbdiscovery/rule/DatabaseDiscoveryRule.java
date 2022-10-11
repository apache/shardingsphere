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

import com.google.common.base.Preconditions;
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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.constant.ExportableConstants;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.schema.QualifiedDatabase;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.DatabaseRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DynamicDataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.exportable.ExportableRule;
import org.apache.shardingsphere.infra.schedule.CronJob;
import org.apache.shardingsphere.mode.metadata.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.metadata.storage.event.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.mode.metadata.storage.event.StorageNodeDataSourceChangedEvent;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

/**
 * Database discovery rule.
 */
public final class DatabaseDiscoveryRule implements DatabaseRule, DataSourceContainedRule, DynamicDataSourceContainedRule, ExportableRule {
    
    @Getter
    private final RuleConfiguration configuration;
    
    private final String databaseName;
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final Map<String, DatabaseDiscoveryProviderAlgorithm> discoveryTypes;
    
    @Getter
    private final Map<String, DatabaseDiscoveryDataSourceRule> dataSourceRules;
    
    private final InstanceContext instanceContext;
    
    public DatabaseDiscoveryRule(final String databaseName, final Map<String, DataSource> dataSourceMap, final DatabaseDiscoveryRuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        configuration = ruleConfig;
        this.databaseName = databaseName;
        this.dataSourceMap = dataSourceMap;
        this.instanceContext = instanceContext;
        discoveryTypes = getDiscoveryProviderAlgorithms(ruleConfig.getDiscoveryTypes());
        dataSourceRules = getDataSourceRules(ruleConfig.getDataSources(), ruleConfig.getDiscoveryHeartbeats());
        findPrimaryReplicaRelationship(databaseName, dataSourceMap);
        initHeartBeatJobs();
    }
    
    public DatabaseDiscoveryRule(final String databaseName,
                                 final Map<String, DataSource> dataSourceMap, final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration ruleConfig, final InstanceContext instanceContext) {
        configuration = ruleConfig;
        this.databaseName = databaseName;
        this.dataSourceMap = dataSourceMap;
        this.instanceContext = instanceContext;
        discoveryTypes = ruleConfig.getDiscoveryTypes();
        dataSourceRules = getDataSourceRules(ruleConfig.getDataSources(), ruleConfig.getDiscoveryHeartbeats());
        findPrimaryReplicaRelationship(databaseName, dataSourceMap);
        initHeartBeatJobs();
    }
    
    private static Map<String, DatabaseDiscoveryProviderAlgorithm> getDiscoveryProviderAlgorithms(final Map<String, AlgorithmConfiguration> discoveryTypesConfig) {
        Map<String, DatabaseDiscoveryProviderAlgorithm> result = new LinkedHashMap<>(discoveryTypesConfig.size(), 1);
        for (Entry<String, AlgorithmConfiguration> entry : discoveryTypesConfig.entrySet()) {
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
    
    private void findPrimaryReplicaRelationship(final String databaseName, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            String groupName = entry.getKey();
            DatabaseDiscoveryDataSourceRule dataSourceRule = entry.getValue();
            Map<String, DataSource> originalDataSourceMap = dataSourceRule.getDataSourceGroup(dataSourceMap);
            DatabaseDiscoveryEngine engine = new DatabaseDiscoveryEngine(dataSourceRule.getDatabaseDiscoveryProviderAlgorithm(), instanceContext.getEventBusContext());
            engine.checkEnvironment(databaseName, originalDataSourceMap);
            dataSourceRule.changePrimaryDataSourceName(engine.changePrimaryDataSource(
                    databaseName, groupName, entry.getValue().getPrimaryDataSourceName(), originalDataSourceMap, dataSourceRule.getDisabledDataSourceNames()));
        }
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
    public void restartHeartBeatJob(final DataSourceStatusChangedEvent event) {
        PrimaryDataSourceChangedEvent dataSourceEvent = (PrimaryDataSourceChangedEvent) event;
        QualifiedDatabase qualifiedDatabase = dataSourceEvent.getQualifiedDatabase();
        DatabaseDiscoveryDataSourceRule dataSourceRule = dataSourceRules.get(qualifiedDatabase.getGroupName());
        Preconditions.checkNotNull(dataSourceRule, "Can not find database discovery data source rule in database `%s`", databaseName);
        dataSourceRule.changePrimaryDataSourceName(qualifiedDatabase.getDataSourceName());
        initHeartBeatJobs();
    }
    
    @Override
    public void closeHeartBeatJob() {
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            DatabaseDiscoveryDataSourceRule rule = entry.getValue();
            instanceContext.getScheduleContext().closeSchedule(rule.getDatabaseDiscoveryProviderAlgorithm().getType() + "-" + databaseName + "-" + rule.getGroupName());
        }
    }
    
    private void initHeartBeatJobs() {
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            DatabaseDiscoveryDataSourceRule rule = entry.getValue();
            String jobName = rule.getDatabaseDiscoveryProviderAlgorithm().getType() + "-" + databaseName + "-" + rule.getGroupName();
            CronJob job = new CronJob(jobName, each -> new HeartbeatJob(databaseName, rule.getGroupName(), rule.getPrimaryDataSourceName(), rule.getDataSourceGroup(dataSourceMap),
                    rule.getDatabaseDiscoveryProviderAlgorithm(), rule.getDisabledDataSourceNames(), instanceContext.getEventBusContext()).execute(null),
                    rule.getHeartbeatProps().getProperty("keep-alive-cron"));
            instanceContext.getScheduleContext().startSchedule(job);
        }
    }
    
    @Override
    public String getPrimaryDataSourceName(final String dataSourceName) {
        return dataSourceRules.get(dataSourceName).getPrimaryDataSourceName();
    }
    
    @Override
    public Collection<String> getReplicaDataSourceNames(final String dataSourceName) {
        return dataSourceRules.get(dataSourceName).getReplicaDataSourceNames();
    }
    
    @Override
    public void updateStatus(final DataSourceStatusChangedEvent event) {
        StorageNodeDataSourceChangedEvent dataSourceChangedEvent = (StorageNodeDataSourceChangedEvent) event;
        DatabaseDiscoveryDataSourceRule dataSourceRule = dataSourceRules.get(dataSourceChangedEvent.getQualifiedDatabase().getGroupName());
        Preconditions.checkNotNull(dataSourceRule, "Can not find database discovery data source rule in database `%s`", databaseName);
        if (StorageNodeStatus.isDisable(dataSourceChangedEvent.getDataSource().getStatus())) {
            dataSourceRule.disableDataSource(dataSourceChangedEvent.getQualifiedDatabase().getDataSourceName());
        } else {
            dataSourceRule.enableDataSource(dataSourceChangedEvent.getQualifiedDatabase().getDataSourceName());
        }
    }
    
    @Override
    public Map<String, Object> getExportData() {
        return Collections.singletonMap(ExportableConstants.EXPORT_DB_DISCOVERY_PRIMARY_DATA_SOURCES, exportPrimaryDataSourceMap());
    }
    
    private Map<String, String> exportPrimaryDataSourceMap() {
        Map<String, String> result = new HashMap<>(dataSourceRules.size(), 1);
        dataSourceRules.forEach((name, dataSourceRule) -> result.put(dataSourceRule.getGroupName(), dataSourceRule.getPrimaryDataSourceName()));
        return result;
    }
    
    @Override
    public String getType() {
        return DatabaseDiscoveryRule.class.getSimpleName();
    }
}
