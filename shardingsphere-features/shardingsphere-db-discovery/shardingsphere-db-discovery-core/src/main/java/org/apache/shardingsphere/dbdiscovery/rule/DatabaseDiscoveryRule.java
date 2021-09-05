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
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.rule.identifier.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
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
public final class DatabaseDiscoveryRule implements SchemaRule, DataSourceContainedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
        ShardingSphereServiceLoader.register(DataSourceNameAware.class);
    }
    
    private final Map<String, DatabaseDiscoveryType> discoveryTypes;
    
    @Getter
    private final Map<String, DatabaseDiscoveryDataSourceRule> dataSourceRules;
    
    public DatabaseDiscoveryRule(final DatabaseDiscoveryRuleConfiguration config, final String schemaName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        checkDataSourcesArguments(config.getDataSources(), databaseType, dataSourceMap);
        discoveryTypes = getDiscoveryTypes(config.getDiscoveryTypes());
        dataSourceRules = getDataSourceRules(config.getDataSources());
        startMonitor(schemaName, dataSourceMap);
        initAware();
    }
    
    public DatabaseDiscoveryRule(final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config, 
                                 final String schemaName, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        checkDataSourcesArguments(config.getDataSources(), databaseType, dataSourceMap);
        discoveryTypes = config.getDiscoveryTypes();
        dataSourceRules = getDataSourceRules(config.getDataSources());
        startMonitor(schemaName, dataSourceMap);
        initAware();
    }
    
    private void checkDataSourcesArguments(final Collection<DatabaseDiscoveryDataSourceRuleConfiguration> dataSources, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap) {
        Preconditions.checkArgument(!dataSources.isEmpty(), "Database discovery rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkNotNull(databaseType, "Database type cannot be null.");
    }
    
    private Map<String, DatabaseDiscoveryType> getDiscoveryTypes(final Map<String, ShardingSphereAlgorithmConfiguration> discoveryTypesConfig) {
        Map<String, DatabaseDiscoveryType> result = new LinkedHashMap<>();
        for (Entry<String, ShardingSphereAlgorithmConfiguration> entry : discoveryTypesConfig.entrySet()) {
            result.put(entry.getKey(), ShardingSphereAlgorithmFactory.createAlgorithm(entry.getValue(), DatabaseDiscoveryType.class));
        }
        return result;
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
            DatabaseDiscoveryDataSourceRule dbDiscoveryDataSourceRule = entry.getValue();
            DatabaseDiscoveryType databaseDiscoveryType = dbDiscoveryDataSourceRule.getDatabaseDiscoveryType();
            Map<String, DataSource> originalDataSourceMap = new HashMap<>(dataSourceMap);
            Collection<String> disabledDataSourceNames = dbDiscoveryDataSourceRule.getDisabledDataSourceNames();
            String primaryDataSourceName = dbDiscoveryDataSourceRule.getPrimaryDataSourceName();
            databaseDiscoveryType.updatePrimaryDataSource(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
            dbDiscoveryDataSourceRule.updatePrimaryDataSourceName(databaseDiscoveryType.getPrimaryDataSource());
            databaseDiscoveryType.updateMemberState(originalDataSourceMap, schemaName, disabledDataSourceNames);
            try {
                databaseDiscoveryType.checkDatabaseDiscoveryConfig(dataSourceMap, schemaName);
                databaseDiscoveryType.startPeriodicalUpdate(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
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
    public void updateRuleStatus(final RuleChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getDataSourceName(), ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        } else if (event instanceof PrimaryDataSourceEvent) {
            for (Entry<String, DatabaseDiscoveryDataSourceRule> entry : dataSourceRules.entrySet()) {
                if (entry.getValue().getName().equals(((PrimaryDataSourceEvent) event).getGroupName())) {
                    entry.getValue().updatePrimaryDataSourceName(((PrimaryDataSourceEvent) event).getDataSourceName());
                }
            }
        }
    }
}
