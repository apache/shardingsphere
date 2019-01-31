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

package org.apache.shardingsphere.orchestration.yaml.loader;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
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
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * YAML loader.
 *
 * @author panjuan
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlLoader {
    
    /**
     * Load data source configurations.
     *
     * @param data data
     * @return data source configurations
     */
    @SuppressWarnings("unchecked")
    public static Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String data) {
        Map<String, YamlDataSourceConfiguration> result = (Map) new Yaml().load(data);
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load for orchestration.");
        return Maps.transformValues(result, new Function<YamlDataSourceConfiguration, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final YamlDataSourceConfiguration input) {
                DataSourceConfiguration result = new DataSourceConfiguration(input.getDataSourceClassName());
                result.getProperties().putAll(input.getProperties());
                return result;
            }
        });
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param data data
     * @return sharding rule configuration
     */
    public static ShardingRuleConfiguration loadShardingRuleConfiguration(final String data) {
        return new Yaml().loadAs(data, YamlShardingRuleConfiguration.class).getShardingRuleConfiguration();
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @param data data
     * @return master-slave rule configuration
     */
    public static MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String data) {
        return new Yaml().loadAs(data, YamlMasterSlaveRuleConfiguration.class).getMasterSlaveRuleConfiguration();
    }
    
    /**
     * Load authentication.
     *
     * @param data data
     * @return authentication
     */
    public static Authentication loadAuthentication(final String data) {
        YamlAuthentication result = new Yaml().loadAs(data, YamlAuthentication.class);
        Preconditions.checkState(!Strings.isNullOrEmpty(result.getUsername()), "Authority configuration is invalid.");
        return new Authentication(result.getUsername(), result.getPassword());
    }
    
    /**
     * Load config map.
     *
     * @param data data
     * @return config map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadConfigMap(final String data) {
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, Object>() : (Map) new Yaml().load(data);
    }
    
    /**
     * Load properties.
     *
     * @param data data
     * @return properties
     */
    public static Properties loadProperties(final String data) {
        return Strings.isNullOrEmpty(data) ? new Properties() : new Yaml().loadAs(data, Properties.class);
    }
}
