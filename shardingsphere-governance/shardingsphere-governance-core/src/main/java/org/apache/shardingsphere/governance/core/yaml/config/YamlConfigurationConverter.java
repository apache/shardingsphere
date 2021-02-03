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

package org.apache.shardingsphere.governance.core.yaml.config;

import org.apache.shardingsphere.governance.core.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.builtin.yaml.config.YamlUserRuleConfiguration;
import org.apache.shardingsphere.infra.auth.builtin.yaml.swapper.UserRuleYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuration converter for YAML content.
 */
public final class YamlConfigurationConverter {
    
    /**
     * Convert data source configurations from YAML content.
     *
     * @param yamlContent YAML content
     * @return data source configurations
     */
    public static Map<String, DataSourceConfiguration> convertDataSourceConfigurations(final String yamlContent) {
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshalWithFilter(yamlContent, YamlDataSourceConfigurationWrap.class);
        if (null == result.getDataSources() || result.getDataSources().isEmpty()) {
            return new LinkedHashMap<>();
        }
        return result.getDataSources().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper()
                .swapToObject(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    /**
     * Convert rule configurations from YAML content.
     *
     * @param yamlContent YAML content
     * @return rule configurations
     */
    public static Collection<RuleConfiguration> convertRuleConfigurations(final String yamlContent) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshalWithFilter(yamlContent, YamlRuleConfigurationWrap.class).getRules());
    }
    
    /**
     * Convert user rule from YAML content.
     *
     * @param yamlContent YAML content
     * @return authentication
     */
    public static Collection<ShardingSphereUser> convertUserRule(final String yamlContent) {
        return new UserRuleYamlSwapper().swapToObject(YamlEngine.unmarshal(yamlContent, YamlUserRuleConfiguration.class));
    }
    
    /**
     * Convert properties configuration from YAML content.
     *
     * @param yamlContent YAML content
     * @return properties
     */
    public static Properties convertProperties(final String yamlContent) {
        return YamlEngine.unmarshalWithFilter(yamlContent, Properties.class);
    }
}
