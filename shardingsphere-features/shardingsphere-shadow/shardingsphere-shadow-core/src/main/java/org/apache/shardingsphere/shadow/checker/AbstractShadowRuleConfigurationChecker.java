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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.api.shadow.note.NoteShadowAlgorithm;
import org.apache.shardingsphere.shadow.spi.ShadowAlgorithm;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Abstract shadow rule configuration checker.
 * 
 * @param <T> type of rule configuration
 */
public abstract class AbstractShadowRuleConfigurationChecker<T extends RuleConfiguration> implements RuleConfigurationChecker<T> {
    
    @Override
    public final void check(final String schemaName, final T config) {
        checkShadowRuleConfiguration(config);
    }
    
    protected abstract void checkShadowRuleConfiguration(T config);
    
    protected void sizeCheck(final Map<String, ShadowDataSourceConfiguration> dataSources, final Map<String, ShadowTableConfiguration> shadowTables, final String defaultShadowAlgorithmName) {
        Preconditions.checkState(!dataSources.isEmpty(), "No available shadow data sources mappings in shadow configuration.");
        if (null == defaultShadowAlgorithmName) {
            Preconditions.checkState(!shadowTables.isEmpty(), "No available shadow tables in shadow configuration.");
        }
    }
    
    protected void shadowAlgorithmsSizeCheck(final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        Preconditions.checkState(!shadowAlgorithms.isEmpty(), "No available shadow algorithms in shadow configuration.");
    }
    
    protected void shadowAlgorithmConfigurationsSizeCheck(final Map<String, ShardingSphereAlgorithmConfiguration> shadowAlgorithmConfigurations) {
        Preconditions.checkState(!shadowAlgorithmConfigurations.isEmpty(), "No available shadow data algorithms in shadow configuration.");
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
    
    protected void defaultShadowAlgorithmConfigurationCheck(final String defaultShadowAlgorithmName, final Map<String, ShardingSphereAlgorithmConfiguration> shadowAlgorithmConfigurations) {
        if (null != defaultShadowAlgorithmName) {
            ShardingSphereAlgorithmConfiguration shardingSphereAlgorithmConfiguration = shadowAlgorithmConfigurations.get(defaultShadowAlgorithmName);
            boolean state = null != shardingSphereAlgorithmConfiguration && "SIMPLE_NOTE".equals(shardingSphereAlgorithmConfiguration.getType());
            Preconditions.checkState(state, "Default shadow algorithm class should be implement NoteShadowAlgorithm.");
        }
    }
    
    protected void defaultShadowAlgorithmCheck(final String defaultShadowAlgorithmName, final Map<String, ShadowAlgorithm> shadowAlgorithms) {
        if (null != defaultShadowAlgorithmName) {
            boolean isNoteShadowAlgorithmState = shadowAlgorithms.get(defaultShadowAlgorithmName) instanceof NoteShadowAlgorithm;
            Preconditions.checkState(isNoteShadowAlgorithmState, "Default shadow algorithm class should be implement NoteShadowAlgorithm.");
        }
    }
    
    protected void shadowTableAlgorithmsAutoReferences(final Map<String, ShadowTableConfiguration> shadowTables, final Set<String> shadowAlgorithmNames, final String defaultShadowAlgorithmName) {
        for (Map.Entry<String, ShadowTableConfiguration> entry : shadowTables.entrySet()) {
            Collection<String> names = entry.getValue().getShadowAlgorithmNames();
            names.forEach(each -> {
                if (!shadowAlgorithmNames.contains(each)) {
                    names.remove(each);
                }
            });
            if (null != defaultShadowAlgorithmName && names.isEmpty()) {
                names.add(defaultShadowAlgorithmName);
            }
        }
    }
    
    protected void shadowTableAlgorithmsReferencesCheck(final Map<String, ShadowTableConfiguration> shadowTables) {
        shadowTables.forEach((key, value) -> Preconditions.checkState(!value.getShadowAlgorithmNames().isEmpty(), "No available shadow Algorithm configuration in shadow table `%s`.", key));
    }
}
