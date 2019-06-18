
/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingui.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthentication;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.DefaultYamlRepresenter;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * YAML converter for configuration.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationYamlConverter {

    /**
     * Load data source configurations.
     *
     * @param data data
     * @return data source configurations
     */
    @SuppressWarnings("unchecked")
    public static Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String data) {
        Map<String, YamlDataSourceConfiguration> result = (Map) YamlEngine.unmarshal(data);
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load for orchestration.");
        return Maps.transformValues(result, new Function<YamlDataSourceConfiguration, DataSourceConfiguration>() {

            @Override
            public DataSourceConfiguration apply(final YamlDataSourceConfiguration input) {
                return new DataSourceConfigurationYamlSwapper().swap(input);
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
        return new ShardingRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(data, YamlShardingRuleConfiguration.class));
    }

    /**
     * Load master-slave rule configuration.
     *
     * @param data data
     * @return master-slave rule configuration
     */
    public static MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String data) {
        return new MasterSlaveRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(data, YamlMasterSlaveRuleConfiguration.class));
    }

    /**
     * Load authentication.
     *
     * @param data data
     * @return authentication
     */
    public static Authentication loadAuthentication(final String data) {
        Authentication result = new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(data, YamlAuthentication.class));
        Preconditions.checkState(!Strings.isNullOrEmpty(result.getUsername()), "Authority configuration is invalid.");
        return result;
    }

    /**
     * Load config map. 4.0.0-RC1 has removed this directory.
     *
     * @param data data
     * @return config map
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static Map<String, Object> loadConfigMap(final String data) {
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, Object>() : (Map) new Yaml().load(data);
    }

    /**
     * Load properties configuration.
     *
     * @param data data
     * @return properties
     */
    public static Properties loadProperties(final String data) {
        return YamlEngine.unmarshalProperties(data);
    }

    /**
     * Dump data sources configuration.
     *
     * @param dataSourceConfigs data sources configurations
     * @return YAML string
     */
    @SuppressWarnings("unchecked")
    public static String dumpDataSourceConfigurations(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(
                Maps.transformValues(dataSourceConfigs, new Function<DataSourceConfiguration, YamlDataSourceConfiguration>() {

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
     * @param shardingRuleConfig sharding rule configuration
     * @return YAML string
     */
    public static String dumpShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfig));
    }

    /**
     * Dump master-slave rule configuration.
     *
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @return YAML string
     */
    public static String dumpMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfig) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfig));
    }

    /**
     * Dump authentication.
     *
     * @param authentication authentication
     * @return YAML string
     */
    public static String dumpAuthentication(final Authentication authentication) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(authentication);
    }

    /**
     * Dump config map. 4.0.0-RC1 has removed it.
     *
     * @param configMap config map
     * @return YAML string
     */
    @Deprecated
    public static String dumpConfigMap(final Map<String, Object> configMap) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(configMap);
    }

    /**
     * Dump properties configuration.
     *
     * @param props props
     * @return YAML string
     */
    public static String dumpProperties(final Properties props) {
        return new Yaml(new DefaultYamlRepresenter()).dumpAsMap(props);
    }
}
