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
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ShardingSphereJDBCScalingDataSourceConfiguration;

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
     * @param oldYamlProxyConfiguration old yaml proxy configuration
     * @param newYamlProxyConfiguration new yaml proxy configuration
     * @return {@code ScalingConfiguration} instance
     */
    public static ScalingConfiguration toScalingConfiguration(final String oldYamlProxyConfiguration, final String newYamlProxyConfiguration) {
        ScalingConfiguration result = new ScalingConfiguration();
        RuleConfiguration ruleConfiguration = new RuleConfiguration();
        ruleConfiguration.setSource(toDataSourceConf(oldYamlProxyConfiguration));
        ruleConfiguration.setTarget(toDataSourceConf(newYamlProxyConfiguration));
        result.setRuleConfiguration(ruleConfiguration);
        result.setJobConfiguration(new JobConfiguration());
        return result;
    }
    
    private static RuleConfiguration.DataSourceConf toDataSourceConf(final String yamlProxyConfiguration) {
        RuleConfiguration.DataSourceConf result = new RuleConfiguration.DataSourceConf();
        ShardingSphereJDBCScalingDataSourceConfiguration scalingDataSourceConfiguration = toScalingDataSourceConfiguration(yamlProxyConfiguration);
        result.setType("shardingSphereJdbc");
        result.setParameter(scalingDataSourceConfiguration.toJsonTree());
        return result;
    }
    
    private static ShardingSphereJDBCScalingDataSourceConfiguration toScalingDataSourceConfiguration(final String yamlProxyConfiguration) {
        YamlProxyRuleConfiguration proxyRuleConfiguration = YamlEngine.unmarshal(yamlProxyConfiguration, YamlProxyRuleConfiguration.class);
        return new ShardingSphereJDBCScalingDataSourceConfiguration(getDataSourceConfiguration(proxyRuleConfiguration), getRuleConfiguration(proxyRuleConfiguration));
    }
    
    private static String getDataSourceConfiguration(final YamlProxyRuleConfiguration proxyRuleConfiguration) {
        YamlDataSourceConfigurationWrap result = new YamlDataSourceConfigurationWrap();
        Map<String, YamlDataSourceConfiguration> dataSources = proxyRuleConfiguration.getDataSources().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> toYamlDataSourceConfiguration(proxyRuleConfiguration, entry.getValue())));
        result.setDataSources(dataSources);
        return YamlEngine.marshal(result);
    }
    
    private static YamlDataSourceConfiguration toYamlDataSourceConfiguration(final YamlProxyRuleConfiguration proxyRuleConfiguration, final YamlDataSourceParameter yamlDataSourceParameter) {
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        Map<String, Object> props = new HashMap<>();
        props.putAll(ReflectionUtil.getFieldMap(yamlDataSourceParameter));
        props.putAll(proxyRuleConfiguration.getDataSourceCommon());
        result.setProps(props);
        return result;
    }
    
    private static String getRuleConfiguration(final YamlProxyRuleConfiguration proxyRuleConfiguration) {
        YamlProxyRuleConfiguration result = new YamlProxyRuleConfiguration();
        result.setRules(proxyRuleConfiguration.getRules());
        return YamlEngine.marshal(result);
    }
}
