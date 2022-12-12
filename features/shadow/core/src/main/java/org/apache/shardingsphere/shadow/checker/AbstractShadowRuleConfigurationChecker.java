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

package org.apache.shardingsphere.shadow.checker;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.hint.HintShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Abstract shadow rule configuration checker.
 *
 * @param <T> type of rule configuration
 */
public abstract class AbstractShadowRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String databaseName, final T config, final Map<String, DataSource> dataSourceMap, final Collection<ShardingSphereRule> rules) {
        checkShadowRuleConfiguration(config, dataSourceMap);
    }
    
    protected abstract void checkShadowRuleConfiguration(T config, Map<String, DataSource> dataSources);
    
    protected void checkDataSources(final Map<String, ShadowDataSourceConfiguration> shadowDataSources, final Map<String, DataSource> dataSourceMap) {
        Set<String> dataSource = dataSourceMap.keySet();
        for (Entry<String, ShadowDataSourceConfiguration> entry : shadowDataSources.entrySet()) {
            ShadowDataSourceConfiguration shadowConfig = entry.getValue();
            boolean shadowDataSourceState = dataSource.contains(shadowConfig.getProductionDataSourceName()) && dataSource.contains(shadowConfig.getShadowDataSourceName());
            Preconditions.checkState(shadowDataSourceState, "No available data source for shadow data source mapping configuration");
        }
    }
    
    protected void shadowTableDataSourcesAutoReferences(final Map<String, ShadowTableConfiguration> shadowTables, final Map<String, ShadowDataSourceConfiguration> dataSources) {
        if (1 == dataSources.size()) {
            String dataSourceName = dataSources.keySet().iterator().next();
            shadowTables.values().stream().map(ShadowTableConfiguration::getDataSourceNames).filter(Collection::isEmpty).forEach(dataSourceNames -> dataSourceNames.add(dataSourceName));
        }
    }
    
    protected void shadowTableDataSourcesReferencesCheck(final Map<String, ShadowTableConfiguration> shadowTables, final Map<String, ShadowDataSourceConfiguration> dataSources) {
        Set<String> dataSourceNames = dataSources.keySet();
        shadowTables.forEach((key, value) -> {
            for (String each : value.getDataSourceNames()) {
                Preconditions.checkState(dataSourceNames.contains(each), "No available shadow data sources mappings in shadow table `%s`.", key);
            }
        });
    }
    
    protected void defaultShadowAlgorithmConfigurationCheck(final String defaultShadowAlgorithmName, final Map<String, AlgorithmConfiguration> shadowAlgorithmConfigs) {
        if (null != defaultShadowAlgorithmName) {
            AlgorithmConfiguration algorithmConfig = shadowAlgorithmConfigs.get(defaultShadowAlgorithmName);
            boolean state = null != algorithmConfig && "SIMPLE_HINT".equals(algorithmConfig.getType());
            Preconditions.checkState(state, "Default shadow algorithm class should be implement HintShadowAlgorithm.");
        }
    }
    
    protected void defaultShadowAlgorithmCheck(final String defaultShadowAlgorithmName, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        if (null != defaultShadowAlgorithmName) {
            boolean isHintShadowAlgorithmState = shadowAlgorithms.get(defaultShadowAlgorithmName) instanceof HintShadowAlgorithm;
            Preconditions.checkState(isHintShadowAlgorithmState, "Default shadow algorithm class should be implement HintShadowAlgorithm.");
        }
    }
    
    protected void shadowTableAlgorithmsAutoReferences(final Map<String, ShadowTableConfiguration> shadowTables, final Set<String> shadowAlgorithmNames, final String defaultShadowAlgorithmName) {
        for (Entry<String, ShadowTableConfiguration> entry : shadowTables.entrySet()) {
            Collection<String> names = entry.getValue().getShadowAlgorithmNames();
            names.removeIf(next -> !shadowAlgorithmNames.contains(next));
            if (null != defaultShadowAlgorithmName && names.isEmpty()) {
                names.add(defaultShadowAlgorithmName);
            }
        }
    }
    
    protected void shadowTableAlgorithmsReferencesCheck(final Map<String, ShadowTableConfiguration> shadowTables) {
        shadowTables.forEach((key, value) -> Preconditions.checkState(!value.getShadowAlgorithmNames().isEmpty(), "No available shadow Algorithm configuration in shadow table `%s`.", key));
    }
    
    protected Map<String, ShadowDataSourceConfiguration> initShadowDataSources(final Collection<ShadowDataSourceConfiguration> dataSourceConfigurations) {
        Map<String, ShadowDataSourceConfiguration> result = new LinkedHashMap<>();
        for (ShadowDataSourceConfiguration each : dataSourceConfigurations) {
            result.put(each.getName(), each);
        }
        return result;
    }
}
