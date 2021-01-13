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

package org.apache.shardingsphere.ha.rule;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.ha.spi.HAType;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmFactory;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.rule.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.rule.type.StatusContainedRule;
import org.apache.shardingsphere.infra.rule.event.RuleChangedEvent;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.ha.algorithm.config.AlgorithmProvidedHARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.HARuleConfiguration;
import org.apache.shardingsphere.ha.api.config.rule.HADataSourceRuleConfiguration;
import org.apache.shardingsphere.ha.spi.ReplicaLoadBalanceAlgorithm;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * HA rule.
 */
public final class HARule implements DataSourceContainedRule, StatusContainedRule {
    
    static {
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
        ShardingSphereServiceLoader.register(HAType.class);
    }
    
    private final Map<String, ReplicaLoadBalanceAlgorithm> loadBalancers = new LinkedHashMap<>();
    
    private final Map<String, HAType> haTypes = new LinkedHashMap<>();
    
    private final Map<String, HADataSourceRule> dataSourceRules;
    
    public HARule(final HARuleConfiguration config, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String schemaName) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "HA data source rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkArgument(null != databaseType, "Database type cannot be null.");
        config.getLoadBalancers().forEach((key, value) -> loadBalancers.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, ReplicaLoadBalanceAlgorithm.class)));
        config.getHaTypes().forEach((key, value) -> haTypes.put(key, ShardingSphereAlgorithmFactory.createAlgorithm(value, HAType.class)));
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (HADataSourceRuleConfiguration each : config.getDataSources()) {
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            HAType haType = Strings.isNullOrEmpty(each.getHaTypeName()) || !haTypes.containsKey(each.getHaTypeName())
                    ? TypedSPIRegistry.getRegisteredService(HAType.class) : haTypes.get(each.getHaTypeName());
            dataSourceRules.put(each.getName(), new HADataSourceRule(each, loadBalanceAlgorithm, haType));
        }
        for (Entry<String, HADataSourceRule> entry : dataSourceRules.entrySet()) {
            String groupName = entry.getKey();
            HADataSourceRule haDataSourceRule = entry.getValue();
            HAType haType = haDataSourceRule.getHaType();
            Map<String, DataSource> originalDataSourceMap = new HashMap<>(dataSourceMap);
            Collection<String> disabledDataSourceNames = haDataSourceRule.getDisabledDataSourceNames();
            String primaryDataSourceName = haDataSourceRule.getPrimaryDataSourceName();
            haType.updatePrimaryDataSource(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
            haDataSourceRule.updatePrimaryDataSourceName(haType.getPrimaryDataSource());
            haType.updateMemberState(originalDataSourceMap, schemaName, disabledDataSourceNames);
            try {
                haType.checkHAConfig(dataSourceMap, schemaName);
                haType.startPeriodicalUpdate(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
            } catch (final SQLException ex) {
                throw new ShardingSphereException(ex);
            }
        }
    }
    
    public HARule(final AlgorithmProvidedHARuleConfiguration config, final DatabaseType databaseType, final Map<String, DataSource> dataSourceMap, final String schemaName) {
        Preconditions.checkArgument(!config.getDataSources().isEmpty(), "HA data source rules can not be empty.");
        Preconditions.checkArgument(null != dataSourceMap && !dataSourceMap.isEmpty(), "Data sources cannot be empty.");
        Preconditions.checkArgument(null != databaseType, "Database type cannot be null.");
        loadBalancers.putAll(config.getLoadBalanceAlgorithms());
        dataSourceRules = new HashMap<>(config.getDataSources().size(), 1);
        for (HADataSourceRuleConfiguration each : config.getDataSources()) {
            ReplicaLoadBalanceAlgorithm loadBalanceAlgorithm = Strings.isNullOrEmpty(each.getLoadBalancerName()) || !loadBalancers.containsKey(each.getLoadBalancerName())
                    ? TypedSPIRegistry.getRegisteredService(ReplicaLoadBalanceAlgorithm.class) : loadBalancers.get(each.getLoadBalancerName());
            HAType haType = Strings.isNullOrEmpty(each.getHaTypeName()) || !haTypes.containsKey(each.getHaTypeName())
                    ? TypedSPIRegistry.getRegisteredService(HAType.class) : haTypes.get(each.getHaTypeName());
            dataSourceRules.put(each.getName(), new HADataSourceRule(each, loadBalanceAlgorithm, haType));
        }
        for (Entry<String, HADataSourceRule> entry : dataSourceRules.entrySet()) {
            String groupName = entry.getKey();
            HADataSourceRule haDataSourceRule = entry.getValue();
            HAType haType = haDataSourceRule.getHaType();
            Map<String, DataSource> originalDataSourceMap = new HashMap<>(dataSourceMap);
            Collection<String> disabledDataSourceNames = haDataSourceRule.getDisabledDataSourceNames();
            String primaryDataSourceName = haDataSourceRule.getPrimaryDataSourceName();
            haType.updatePrimaryDataSource(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
            haDataSourceRule.updatePrimaryDataSourceName(haType.getPrimaryDataSource());
            haType.updateMemberState(originalDataSourceMap, schemaName, disabledDataSourceNames);
            try {
                haType.checkHAConfig(dataSourceMap, schemaName);
                haType.startPeriodicalUpdate(originalDataSourceMap, schemaName, disabledDataSourceNames, groupName, primaryDataSourceName);
            } catch (final SQLException ex) {
                throw new ShardingSphereException(ex);
            }
        }
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
    public HADataSourceRule getSingleDataSourceRule() {
        return dataSourceRules.values().iterator().next();
    }
    
    /**
     * Find data source rule.
     * 
     * @param dataSourceName data source name
     * @return HA data source rule
     */
    public Optional<HADataSourceRule> findDataSourceRule(final String dataSourceName) {
        return Optional.ofNullable(dataSourceRules.get(dataSourceName));
    }
    
    @Override
    public Map<String, Collection<String>> getDataSourceMapper() {
        Map<String, Collection<String>> result = new HashMap<>();
        for (Entry<String, HADataSourceRule> entry : dataSourceRules.entrySet()) {
            result.putAll(entry.getValue().getDataSourceMapper());
        }
        return result;
    }
    
    @Override
    public void updateRuleStatus(final RuleChangedEvent event) {
        if (event instanceof DataSourceNameDisabledEvent) {
            for (Entry<String, HADataSourceRule> entry : dataSourceRules.entrySet()) {
                entry.getValue().updateDisabledDataSourceNames(((DataSourceNameDisabledEvent) event).getDataSourceName(), ((DataSourceNameDisabledEvent) event).isDisabled());
            }
        } else if (event instanceof PrimaryDataSourceEvent) {
            for (Entry<String, HADataSourceRule> entry : dataSourceRules.entrySet()) {
                if (entry.getValue().getName().equals(((PrimaryDataSourceEvent) event).getGroupName())) {
                    entry.getValue().updatePrimaryDataSourceName(((PrimaryDataSourceEvent) event).getDataSourceName());
                }
            }
        }
    }
}
