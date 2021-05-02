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

package org.apache.shardingsphere.dbdiscovery.common.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.common.algorithm.config.AlgorithmProvidedDatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryType;
import org.apache.shardingsphere.infra.aware.DataSourceNameAware;
import org.apache.shardingsphere.infra.aware.DataSourceNameAwareFactory;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.rule.level.FeatureRule;
import org.apache.shardingsphere.infra.rule.scope.SchemaRule;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.type.StatusContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;

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
public final class DatabaseDiscoveryRule implements FeatureRule, SchemaRule, DataSourceContainedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(DatabaseDiscoveryType.class);
        ShardingSphereServiceLoader.register(DataSourceNameAware.class);
    }
    
    private final Map<String, DatabaseDiscoveryType> discoveryTypes = new LinkedHashMap<>();
    
    @Getter
    private final Map<String, DatabaseDiscoveryDataSourceRule> dataSourceRules;
    
    public DatabaseDiscoveryRule(final DatabaseDiscoveryRuleConfiguration config, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String schemaName) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "HA data source rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkArgument(null != databaseType, "Database type cannot be null.");
        config.getDiscoveryTypes().forEach((key, value) -> discoveryTypes.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, DatabaseDiscoveryType.class)));
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (DatabaseDiscoveryDataSourceRuleConfiguration each : config.getDataSources()) {
            DatabaseDiscoveryType databaseDiscoveryType = Strings.isNullOrEmpty(each.getDiscoveryTypeName()) || !discoveryTypes.containsKey(each.getDiscoveryTypeName())
                    ? TypedSPIRegistry.getRegisteredService(DatabaseDiscoveryType.class) : discoveryTypes.get(each.getDiscoveryTypeName());
            dataSourceRules.put(each.getName(), new DatabaseDiscoveryDataSourceRule(each, databaseDiscoveryType));
        }
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
        initAware();
    }
    
    public DatabaseDiscoveryRule(final AlgorithmProvidedDatabaseDiscoveryRuleConfiguration config, final DatabaseType databaseType, 
                                 final Map<String, DataSource> dataSourceMap, final String schemaName) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "HA data source rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkArgument(null != databaseType, "Database type cannot be null.");
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (DatabaseDiscoveryDataSourceRuleConfiguration each : config.getDataSources()) {
            DatabaseDiscoveryType databaseDiscoveryType = Strings.isNullOrEmpty(each.getDiscoveryTypeName()) || !discoveryTypes.containsKey(each.getDiscoveryTypeName())
                    ? TypedSPIRegistry.getRegisteredService(DatabaseDiscoveryType.class) : discoveryTypes.get(each.getDiscoveryTypeName());
            dataSourceRules.put(each.getName(), new DatabaseDiscoveryDataSourceRule(each, databaseDiscoveryType));
        }
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
        initAware();
    }
    
    private void initAware() {
        DataSourceNameAwareFactory.getInstance().getDataSourceNameAware().ifPresent(optional -> optional.setRule(this));
    }
    
    /**
     * Get all logic data source names.
     *
     * @return all logic data source names
     */
    public Collection<String> getAllLogicDataSourceNames() {
        return dataSourceRules.keySet();
    }
    
    /**
     * Get single data source rule.
     *
     * @return HA data source rule
     */
    public DatabaseDiscoveryDataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     * 
     * @param dataSourceName data source name
     * @return HA data source rule
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
