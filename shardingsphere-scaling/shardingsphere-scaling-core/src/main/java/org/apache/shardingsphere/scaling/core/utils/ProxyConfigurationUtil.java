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

package org.apache.shardingsphere.scaling.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.rule.ShardingSphereJDBCDataSourceConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Proxy configuration Util.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProxyConfigurationUtil {
    
    /**
     * Yaml proxy configuration transform to {@code ScalingConfiguration}.
     *
     * @param oldYamlProxyConfig old yaml proxy configuration
     * @param newYamlProxyConfig new yaml proxy configuration
     * @return {@code ScalingConfiguration} instance
     */
    public static ScalingConfiguration toScalingConfig(final String oldYamlProxyConfig, final String newYamlProxyConfig) {
        ScalingConfiguration result = new ScalingConfiguration();
        result.setRuleConfiguration(new RuleConfiguration(toDataSourceConfig(oldYamlProxyConfig), toDataSourceConfig(newYamlProxyConfig)));
        result.setJobConfiguration(new JobConfiguration());
        return result;
    }
    
    private static DataSourceConfiguration toDataSourceConfig(final String yamlProxyConfig) {
        YamlProxyRuleConfiguration proxyRuleConfig = YamlEngine.unmarshal(yamlProxyConfig, YamlProxyRuleConfiguration.class);
        return new ShardingSphereJDBCDataSourceConfiguration(getDataSourceConfig(proxyRuleConfig), getRuleConfig(proxyRuleConfig));
    }
    
    private static String getDataSourceConfig(final YamlProxyRuleConfiguration proxyRuleConfig) {
        YamlDataSourceConfigurationWrap result = new YamlDataSourceConfigurationWrap();
        Map<String, YamlDataSourceConfiguration> dataSources = proxyRuleConfig.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> toYamlDataSourceConfig(proxyRuleConfig, entry.getValue())));
        result.setDataSources(dataSources);
        return YamlEngine.marshal(result);
    }
    
    @SneakyThrows(IllegalAccessException.class)
    private static YamlDataSourceConfiguration toYamlDataSourceConfig(final YamlProxyRuleConfiguration proxyRuleConfig, final YamlDataSourceParameter yamlDataSourceParameter) {
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        Map<String, Object> props = new HashMap<>();
        props.putAll(ReflectionUtil.getFieldMap(yamlDataSourceParameter));
        props.putAll(proxyRuleConfig.getDataSourceCommon());
        result.setProps(props);
        return result;
    }
    
    private static String getRuleConfig(final YamlProxyRuleConfiguration proxyRuleConfig) {
        YamlProxyRuleConfiguration result = new YamlProxyRuleConfiguration();
        result.setRules(proxyRuleConfig.getRules());
        return YamlEngine.marshal(result);
    }
}
