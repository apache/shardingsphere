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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.shardingsphere.scaling.core.config.JobConfiguration;
import org.apache.shardingsphere.scaling.core.config.ScalingConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ConfigurationYamlConverter;
import org.apache.shardingsphere.scaling.core.config.datasource.ScalingDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.metadata.JdbcUri;
import org.apache.shardingsphere.sharding.algorithm.keygen.SnowflakeKeyGenerateAlgorithm;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineExpressionParser;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Scaling configuration util.
 */
public final class ScalingConfigurationUtil {
    
    private static final SnowflakeKeyGenerateAlgorithm ID_AUTO_INCREASE_GENERATOR = initIdAutoIncreaseGenerator();
    
    private static SnowflakeKeyGenerateAlgorithm initIdAutoIncreaseGenerator() {
        SnowflakeKeyGenerateAlgorithm result = new SnowflakeKeyGenerateAlgorithm();
        result.init();
        return result;
    }
    
    private static Long generateKey() {
        return (Long) ID_AUTO_INCREASE_GENERATOR.generateKey();
    }
    
    /**
     * Fill in properties for scaling config.
     *
     * @param scalingConfig scaling config
     */
    public static void fillInProperties(final ScalingConfiguration scalingConfig) {
        JobConfiguration jobConfig = scalingConfig.getJobConfiguration();
        if (null == jobConfig.getJobId()) {
            jobConfig.setJobId(generateKey());
        }
        if (Strings.isNullOrEmpty(jobConfig.getDatabaseType())) {
            jobConfig.setDatabaseType(scalingConfig.getRuleConfiguration().getSource().unwrap().getDatabaseType().getName());
        }
        if (null == scalingConfig.getJobConfiguration().getShardingTables()) {
            jobConfig.setShardingTables(groupByDataSource(getShouldScalingActualDataNodes(scalingConfig)));
        }
    }
    
    private static List<String> getShouldScalingActualDataNodes(final ScalingConfiguration scalingConfig) {
        ScalingDataSourceConfiguration sourceConfig = scalingConfig.getRuleConfiguration().getSource().unwrap();
        Preconditions.checkState(sourceConfig instanceof ShardingSphereJDBCDataSourceConfiguration,
                "Only ShardingSphereJdbc type of source ScalingDataSourceConfiguration is supported.");
        ShardingSphereJDBCDataSourceConfiguration source = (ShardingSphereJDBCDataSourceConfiguration) sourceConfig;
        if (!(scalingConfig.getRuleConfiguration().getTarget().unwrap() instanceof ShardingSphereJDBCDataSourceConfiguration)) {
            return getShardingRuleConfigMap(source.getRule()).values().stream().map(ShardingTableRuleConfiguration::getActualDataNodes).collect(Collectors.toList());
        }
        ShardingSphereJDBCDataSourceConfiguration target = (ShardingSphereJDBCDataSourceConfiguration) scalingConfig.getRuleConfiguration().getTarget().unwrap();
        return getShouldScalingActualDataNodes(getModifiedDataSources(source.getDataSource(), target.getDataSource()),
                getShardingRuleConfigMap(source.getRule()), getShardingRuleConfigMap(target.getRule()));
    }
    
    private static List<String> getShouldScalingActualDataNodes(final Set<String> modifiedDataSources,
                                                                final Map<String, ShardingTableRuleConfiguration> oldShardingRuleConfigMap,
                                                                final Map<String, ShardingTableRuleConfiguration> newShardingRuleConfigMap) {
        List<String> result = new ArrayList<>();
        newShardingRuleConfigMap.keySet().forEach(each -> {
            if (!oldShardingRuleConfigMap.containsKey(each)) {
                return;
            }
            List<String> oldActualDataNodes = new InlineExpressionParser(oldShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            List<String> newActualDataNodes = new InlineExpressionParser(newShardingRuleConfigMap.get(each).getActualDataNodes()).splitAndEvaluate();
            if (!CollectionUtils.isEqualCollection(oldActualDataNodes, newActualDataNodes) || includeModifiedDataSources(newActualDataNodes, modifiedDataSources)) {
                result.add(oldShardingRuleConfigMap.get(each).getActualDataNodes());
            }
        });
        return result;
    }
    
    private static Set<String> getModifiedDataSources(final String oldConfig, final String newConfig) {
        Set<String> result = new HashSet<>();
        Map<String, String> oldDataSourceUrlMap = getDataSourceUrlMap(oldConfig);
        Map<String, String> newDataSourceUrlMap = getDataSourceUrlMap(newConfig);
        newDataSourceUrlMap.forEach((key, value) -> {
            if (!value.equals(oldDataSourceUrlMap.get(key))) {
                result.add(key);
            }
        });
        return result;
    }
    
    private static Map<String, String> getDataSourceUrlMap(final String configuration) {
        Map<String, String> result = new HashMap<>();
        ConfigurationYamlConverter.loadDataSourceConfigs(configuration).forEach((key, value) -> {
            JdbcUri uri = new JdbcUri(value.getProps().getOrDefault("url", value.getProps().get("jdbcUrl")).toString());
            result.put(key, String.format("%s/%s", uri.getHost(), uri.getDatabase()));
        });
        return result;
    }
    
    private static boolean includeModifiedDataSources(final List<String> actualDataNodes, final Set<String> modifiedDataSources) {
        return actualDataNodes.stream().anyMatch(each -> modifiedDataSources.contains(each.split("\\.")[0]));
    }
    
    private static Map<String, ShardingTableRuleConfiguration> getShardingRuleConfigMap(final String configuration) {
        ShardingRuleConfiguration oldShardingRuleConfig = ConfigurationYamlConverter.loadShardingRuleConfig(configuration);
        return oldShardingRuleConfig.getTables().stream().collect(Collectors.toMap(ShardingTableRuleConfiguration::getLogicTable, Function.identity()));
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
}
