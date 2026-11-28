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

package org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.swapper;

import org.apache.shardingsphere.distsql.handler.executor.configuration.yaml.YamlDataSourceConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.creator.DataSourcePoolPropertiesCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * YAML database configuration swapper for import.
 */
public final class YamlDatabaseConfigurationSwapper {
    
    private final YamlDataSourceConfigurationSwapper dataSourceConfigSwapper = new YamlDataSourceConfigurationSwapper();
    
    /**
     * Swap YAML data sources to data source pool properties.
     *
     * @param yamlDataSources YAML data sources
     * @return data source pool properties
     */
    public Map<String, DataSourcePoolProperties> swapDataSourceProperties(final Map<String, YamlDataSourceConfiguration> yamlDataSources) {
        Map<String, DataSourcePoolProperties> result = new LinkedHashMap<>(yamlDataSources.size(), 1F);
        for (Entry<String, YamlDataSourceConfiguration> entry : yamlDataSources.entrySet()) {
            result.put(entry.getKey(), DataSourcePoolPropertiesCreator.create(dataSourceConfigSwapper.swap(entry.getValue())));
        }
        return result;
    }
    
    /**
     * Swap YAML rules to rule configurations in import order.
     *
     * @param yamlRuleConfigs YAML rule configurations, or {@code null} if absent
     * @return rule configurations
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Collection<RuleConfiguration> swapRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigs) {
        if (null == yamlRuleConfigs || yamlRuleConfigs.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Integer, RuleConfiguration> result = new TreeMap<>(Comparator.reverseOrder());
        for (YamlRuleConfiguration each : yamlRuleConfigs) {
            YamlRuleConfigurationSwapper swapper = OrderedSPILoader.getServicesByClass(YamlRuleConfigurationSwapper.class, Collections.singleton(each.getRuleConfigurationType()))
                    .get(each.getRuleConfigurationType());
            result.put(swapper.getOrder(), (RuleConfiguration) swapper.swapToObject(each));
        }
        return result.values();
    }
}
