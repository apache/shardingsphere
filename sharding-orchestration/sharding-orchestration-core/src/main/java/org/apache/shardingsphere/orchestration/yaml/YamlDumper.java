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

package org.apache.shardingsphere.orchestration.yaml;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.YamlAuthentication;
import org.apache.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Properties;

/**
 * YAML dumper.
 *
 * @author panjuan
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlDumper {
    
    /**
     * Dump data source configurations.
     *
     * @param stringDataSourceConfigurations data sources configurations
     * @return YAML string
     */
    @SuppressWarnings("unchecked")
    public static String dumpDataSourceConfigurations(final Map<String, DataSourceConfiguration> stringDataSourceConfigurations) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(
                Maps.transformValues(stringDataSourceConfigurations, new Function<DataSourceConfiguration, YamlDataSourceConfiguration>() {
                    
                    @Override
                    public YamlDataSourceConfiguration apply(final DataSourceConfiguration input) {
                        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
                        result.setDataSourceClassName(input.getDataSourceClassName());
                        result.setProperties(input.getProperties());
                        return result;
                    }
                }));
    }
    
    /**
     * Dump sharding rule configuration.
     *
     * @param shardingRuleConfiguration sharding rule configuration
     * @return YAML string
     */
    public static String dumpShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfiguration) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(new YamlShardingRuleConfiguration(shardingRuleConfiguration));
    }
    
    /**
     * Dump master-slave rule configuration.
     *
     * @param masterSlaveRuleConfiguration master-slave rule configuration
     * @return YAML string
     */
    public static String dumpMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(new YamlMasterSlaveRuleConfiguration(masterSlaveRuleConfiguration));
    }
    
    /**
     * Dump authentication.
     *
     * @param authentication authentication
     * @return YAML string
     */
    public static String dumpAuthentication(final Authentication authentication) {
        YamlAuthentication yamlAuthentication = new YamlAuthentication();
        yamlAuthentication.setUsername(authentication.getUsername());
        yamlAuthentication.setPassword(authentication.getPassword());
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(yamlAuthentication);
    }
    
    /**
     * Dump config map.
     *
     * @param configMap config map
     * @return YAML string
     */
    public static String dumpConfigMap(final Map<String, Object> configMap) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(configMap);
    }
    
    /**
     * Dump properties.
     *
     * @param props properties
     * @return YAML string
     */
    public static String dumpProperties(final Properties props) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(props);
    }
}
