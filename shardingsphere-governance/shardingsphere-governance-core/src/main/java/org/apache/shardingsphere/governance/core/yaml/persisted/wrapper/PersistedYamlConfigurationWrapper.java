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

package org.apache.shardingsphere.governance.core.yaml.persisted.wrapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.governance.core.yaml.persisted.pojo.PersistedYamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.persisted.pojo.PersistedYamlRuleConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Configuration wrapper for YAML content.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PersistedYamlConfigurationWrapper {
    
    /**
     * Unwrap data source configuration map from YAML content.
     *
     * @param yamlContent YAML content
     * @return data source configuration map
     */
    public static Map<String, DataSourceConfiguration> unwrapDataSourceConfigurations(final String yamlContent) {
        PersistedYamlDataSourceConfiguration persistedConfig = YamlEngine.unmarshal(yamlContent, PersistedYamlDataSourceConfiguration.class);
        return null == persistedConfig.getDataSources() || persistedConfig.getDataSources().isEmpty() ? new LinkedHashMap<>() : unwrapDataSourceConfigurations(persistedConfig.getDataSources());
    }
    
    /**
     * Unwrap data source configurations from YAML content.
     *
     * @param yamlDataSourceConfigs YAML data source configurations
     * @return data source configurations
     */
    public static Map<String, DataSourceConfiguration> unwrapDataSourceConfigurations(final Map<String, Map<String, Object>> yamlDataSourceConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
    
    /**
     * Unwrap rule configurations from YAML content.
     *
     * @param yamlContent YAML content
     * @return rule configurations
     */
    public static Collection<RuleConfiguration> unwrapRuleConfigurations(final String yamlContent) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(yamlContent, PersistedYamlRuleConfiguration.class).getRules());
    }
}
