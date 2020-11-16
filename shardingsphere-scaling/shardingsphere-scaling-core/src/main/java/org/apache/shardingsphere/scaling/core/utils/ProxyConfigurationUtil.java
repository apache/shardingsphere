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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.apache.shardingsphere.proxy.config.yaml.YamlProxyRuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.RuleConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.ShardingSphereJDBCScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
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
        JobConfiguration jobConfiguration = new JobConfiguration();
        jobConfiguration.setShardingTables(groupByDataSource(getShouldScalingActualDataNodes(result)));
        result.setJobConfiguration(jobConfiguration);
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
    
    private static String getRuleConfiguration(final YamlProxyRuleConfiguration proxyRuleConfiguration) {
        YamlProxyRuleConfiguration result = new YamlProxyRuleConfiguration();
        result.setRules(proxyRuleConfiguration.getRules());
        return YamlEngine.marshal(result);
    }
    
    private static List<String> getShouldScalingActualDataNodes(final ScalingConfiguration scalingConfiguration) {
        ShardingSphereJDBCScalingDataSourceConfiguration source =
                (ShardingSphereJDBCScalingDataSourceConfiguration) scalingConfiguration.getRuleConfiguration().getSource().toTypedDataSourceConfiguration();
        ShardingSphereJDBCScalingDataSourceConfiguration target =
                (ShardingSphereJDBCScalingDataSourceConfiguration) scalingConfiguration.getRuleConfiguration().getTarget().toTypedDataSourceConfiguration();
        List<String> result = new ArrayList<>();
        Set<String> modifiedDataSources = getModifiedDataSources(source.getDataSource(), target.getDataSource());
        Map<String, ShardingTableRuleConfiguration> oldShardingRuleConfigurationMap = getShardingRuleConfigurationMap(source.getRule());
        Map<String, ShardingTableRuleConfiguration> newShardingRuleConfigurationMap = getShardingRuleConfigurationMap(target.getRule());
        newShardingRuleConfigurationMap.keySet().forEach(each -> {
            if (!oldShardingRuleConfigurationMap.containsKey(each)) {
                return;
            }
            List<String> oldActualDataNodes = new InlineExpressionParser(oldShardingRuleConfigurationMap.get(each).getActualDataNodes()).splitAndEvaluate();
            List<String> newActualDataNodes = new InlineExpressionParser(newShardingRuleConfigurationMap.get(each).getActualDataNodes()).splitAndEvaluate();
            if (!CollectionUtils.isEqualCollection(oldActualDataNodes, newActualDataNodes) || includeModifiedDataSources(newActualDataNodes, modifiedDataSources)) {
                result.add(newShardingRuleConfigurationMap.get(each).getActualDataNodes());
            }
        });
        return result;
    }
    
    private static Set<String> getModifiedDataSources(final String oldConfiguration, final String newConfiguration) {
        Set<String> result = new HashSet<>();
        Map<String, String> oldDataSourceUrlMap = getDataSourceUrlMap(oldConfiguration);
        Map<String, String> newDataSourceUrlMap = getDataSourceUrlMap(newConfiguration);
        newDataSourceUrlMap.forEach((key, value) -> {
            if (!value.equals(oldDataSourceUrlMap.get(key))) {
                result.add(key);
            }
        });
        return result;
    }
    
    private static Map<String, String> getDataSourceUrlMap(final String configuration) {
        Map<String, String> result = new HashMap<>();
        ConfigurationYamlConverter.loadDataSourceConfigurations(configuration).forEach((key, value) -> {
            JdbcUri uri = new JdbcUri(value.getProps().getOrDefault("url", value.getProps().get("jdbcUrl")).toString());
            result.put(key, String.format("%s/%s", uri.getHost(), uri.getDatabase()));
        });
        return result;
    }
    
    private static boolean includeModifiedDataSources(final List<String> actualDataNodes, final Set<String> modifiedDataSources) {
        return actualDataNodes.stream().anyMatch(each -> modifiedDataSources.contains(each.split("\\.")[0]));
    }
    
    private static Map<String, ShardingTableRuleConfiguration> getShardingRuleConfigurationMap(final String configuration) {
        ShardingRuleConfiguration oldShardingRuleConfiguration = ConfigurationYamlConverter.loadShardingRuleConfiguration(configuration);
        return oldShardingRuleConfiguration.getTables().stream().collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, Function.identity()));
    }
    
    private static String[] groupByDataSource(final List<String> actualDataNodeList) {
        List<String> result = new ArrayList<>();
        Multimap<String, String> multiMap = getNodeMultiMap(actualDataNodeList);
        for (String key : multiMap.keySet()) {
            List<String> list = new ArrayList<>();
            for (String value : multiMap.get(key)) {
                list.add(String.format("%s.%s", key, value));
            }
            result.add(String.join(",", list));
        }
        return result.toArray(new String[0]);
    }
    
    private static Multimap<String, String> getNodeMultiMap(final List<String> actualDataNodeList) {
        Multimap<String, String> result = HashMultimap.create();
        for (String actualDataNodes : actualDataNodeList) {
            for (String actualDataNode : actualDataNodes.split(",")) {
                String[] nodeArray = split(actualDataNode);
                for (String dataSource : new InlineExpressionParser(nodeArray[0]).splitAndEvaluate()) {
                    result.put(dataSource, nodeArray[1]);
                }
            }
        }
        return result;
    }
    
    private static String[] split(final String actualDataNode) {
        boolean flag = true;
        int i = 0;
        for (; i < actualDataNode.length(); i++) {
            char each = actualDataNode.charAt(i);
            if (each == '{') {
                flag = false;
            } else if (each == '}') {
                flag = true;
            } else if (flag && each == '.') {
                break;
            }
        }
        return new String[]{actualDataNode.substring(0, i), actualDataNode.substring(i + 1)};
    }
    
    private static YamlDataSourceConfiguration toYamlDataSourceConfiguration(final YamlProxyRuleConfiguration proxyRuleConfiguration, final YamlDataSourceParameter yamlDataSourceParameter) {
        YamlDataSourceConfiguration result = new YamlDataSourceConfiguration();
        result.setDataSourceClassName("com.zaxxer.hikari.HikariDataSource");
        Map<String, Object> props = new HashMap<>();
        props.putAll(ReflectionUtils.getFieldMap(yamlDataSourceParameter));
        props.putAll(proxyRuleConfiguration.getDataSourceCommon());
        result.setProps(props);
        return result;
    }
}
