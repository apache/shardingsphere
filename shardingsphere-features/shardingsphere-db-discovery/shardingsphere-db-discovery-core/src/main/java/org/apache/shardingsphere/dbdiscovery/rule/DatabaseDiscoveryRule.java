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
import lombok.Getter;
import org.apache.shardingsphere.dbdiscovery.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.constant.DatabaseDiscoveryRuleConstants;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.DataSourceStatusChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.ExportableRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Database discovery rule.
 */
public final class DatabaseDiscoveryRule implements SchemaRule, DataSourceContainedRule, StatusContainedRule, ExportableRule {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
        ShardingSphereServiceLoader.register(DataSourceNameAware.class);
    }
    
    private final Map<String, DatabaseDiscoveryType> discoveryTypes;
    
    @Getter
    private final Map<String, DatabaseDiscoveryDataSourceRule> dataSourceRules;
    
    public DatabaseDiscoveryRule(final DatabaseDiscoveryRuleConfiguration config, final String schemaName, final Map<String, DataSource> dataSourceMap) {
        this(config.getDataSources(), getDiscoveryTypes(config.getDiscoveryTypes()), schemaName, dataSourceMap);
    }
    
    public DatabaseDiscoveryRule(final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config, final String schemaName, final Map<String, DataSource> dataSourceMap) {
        this(config.getDataSources(), config.getDiscoveryTypes(), schemaName, dataSourceMap);
    }
    
    private DatabaseDiscoveryRule(final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfigs, final Map<String, DatabaseDiscoveryType> discoveryTypes,
                                  final String schemaName, final Map<String, DataSource> dataSourceMap) {
        checkDataSourcesArguments(dataSourceRuleConfigs, dataSourceMap);
        this.discoveryTypes = discoveryTypes;
        dataSourceRules = getDataSourceRules(dataSourceRuleConfigs);
        startMonitor(schemaName, dataSourceMap);
        initAware();
    }
    
    private static Map<String, DatabaseDiscoveryType> getDiscoveryTypes(final Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypesConfig) {
        Map<String, DatabaseDiscoveryType> result = new LinkedHashMap<>(discoveryTypesConfig.size(), 1);
        for (Entry<String, ShardingSphereAlgorithmConfiguration> entry : discoveryTypesConfig.entrySet()) {
            result.put(entry.getKey(), ShardingSphereAlgorithmFactory.createAlgorithm(entry.getValue(), DatabaseDiscoveryType.class));
        }
        return result;
    }
    
    private void checkDataSourcesArguments(final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(!dataSources.isEmpty(), "Database discovery rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
    }
    
    private Map<String, DatabaseDiscoveryDataSourceRule> getDataSourceRules(final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources) {
        Map<String, DatabaseDiscoveryDataSourceRule> result = new HashMap<>(dataSources.size(), 1);
        for (DatabaseDiscoveryDataSourceRuleConfiguration each : dataSources) {
            checkDatabaseDiscoveryDataSourceRuleConfigurationArguments(each);
            result.put(each.getName(), new DatabaseDiscoveryDataSourceRule(each, discoveryTypes.get(each.getDiscoveryTypeName())));
        }
        return result;
    }
    
    private void checkDatabaseDiscoveryDataSourceRuleConfigurationArguments(final DatabaseDiscoveryDataSourceRuleConfiguration dataSourceRuleConfig) {
        Preconditions.checkNotNull(dataSourceRuleConfig.getDiscoveryTypeName(), "Discovery type cannot be null of rule name `%s`.", dataSourceRuleConfig.getName());
        Preconditions.checkArgument(discoveryTypes.containsKey(dataSourceRuleConfig.getDiscoveryTypeName()), "Can not find discovery type of rule name `%s`.", dataSourceRuleConfig.getName());
    }
    
    private void startMonitor(final String schemaName, final Map<String, DataSource> dataSourceMap) {
        for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
            String groupName = entry.getKey();
            DatabaseDiscoveryDataSourceRule dataSourceRule = entry.getValue();
            DatabaseDiscoveryType databaseDiscoveryType = dataSourceRule.getDatabaseDiscoveryType();
            Map<String, DataSource> originalDataSourceMap = new HashMap<>(dataSourceMap);
            Collection<String> disabledDataSourceNames = dataSourceRule.getDisabledDataSourceNames();
            databaseDiscoveryType.updatePrimaryDataSource(schemaName, originalDataSourceMap, disabledDataSourceNames, groupName);
            dataSourceRule.updatePrimaryDataSourceName(databaseDiscoveryType.getPrimaryDataSource());
            databaseDiscoveryType.updateMemberState(schemaName, originalDataSourceMap, disabledDataSourceNames);
            try {
                databaseDiscoveryType.checkDatabaseDiscoveryConfiguration(schemaName, dataSourceMap);
                databaseDiscoveryType.startPeriodicalUpdate(schemaName, originalDataSourceMap, disabledDataSourceNames, groupName);
            } catch (final SQLException ex) {
                throw new ShardingSphereException(ex);
            }
        }
    }
    
    private void initAware() {
        DataSourceNameAwareFactory.getInstance().getDataSourceNameAware().ifPresent(optional -> optional.setRule(this));
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
                    entry.getValue().disableDataSource(((DataSourceNameDisabledEvent) event).getDataSourceName());
                } else {
                    entry.getValue().enableDataSource(((DataSourceNameDisabledEvent) event).getDataSourceName());
                }
            }
        } else if (event instanceof PrimaryDataSourceChangedEvent) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                if (entry.getValue().getName().equals(((PrimaryDataSourceChangedEvent) event).getGroupName())) {
                    entry.getValue().updatePrimaryDataSourceName(((PrimaryDataSourceChangedEvent) event).getDataSourceName());
                }
            }
        }
    }
    
    @Override
    public Map<String, Object> export() {
        Map<String, Object> result = new HashMap<>(1, 1);
        result.put(DatabaseDiscoveryRuleConstants.PRIMARY_DATA_SOURCE_KEY, exportPrimaryDataSourceMap());
        return result;
    }
    
    private Map<String, String> exportPrimaryDataSourceMap() {
        Map<String, String> result = new HashMap<>(dataSourceRules.size(), 1);
        dataSourceRules.forEach((name, dataSourceRule) -> result.put(dataSourceRule.getName(), dataSourceRule.getPrimaryDataSourceName()));
        return result;
    }
    
    @Override
    public String getType() {
        return DatabaseDiscoveryRule.class.getSimpleName();
    }
}
