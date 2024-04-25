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

package org.apache.shardingsphere.readwritesplitting.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeRuleConfigurationSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceGroupRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.transaction.TransactionalReadQueryStrategy;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.metadata.nodepath.ReadwriteSplittingRuleNodePathProvider;
import org.apache.shardingsphere.readwritesplitting.yaml.config.rule.YamlReadwriteSplittingDataSourceGroupRuleConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * YAML readwrite-splitting data node rule configuration swapper.
 */
public final class YamlReadwriteSplittingDataNodeRuleConfigurationSwapper implements YamlDataNodeRuleConfigurationSwapper<ReadwriteSplittingRuleConfiguration> {
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath readwriteSplittingRuleNodePath = new ReadwriteSplittingRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ReadwriteSplittingRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getLoadBalancers().entrySet()) {
            result.add(new YamlDataNode(readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS).getPath(entry.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (ReadwriteSplittingDataSourceGroupRuleConfiguration each : data.getDataSourceGroups()) {
            result.add(new YamlDataNode(readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES).getPath(each.getName()),
                    YamlEngine.marshal(swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    private YamlReadwriteSplittingDataSourceGroupRuleConfiguration swapToYamlConfiguration(final ReadwriteSplittingDataSourceGroupRuleConfiguration dataSourceGroupRuleConfig) {
        YamlReadwriteSplittingDataSourceGroupRuleConfiguration result = new YamlReadwriteSplittingDataSourceGroupRuleConfiguration();
        result.setWriteDataSourceName(dataSourceGroupRuleConfig.getWriteDataSourceName());
        result.setReadDataSourceNames(dataSourceGroupRuleConfig.getReadDataSourceNames());
        result.setTransactionalReadQueryStrategy(dataSourceGroupRuleConfig.getTransactionalReadQueryStrategy().name());
        result.setLoadBalancerName(dataSourceGroupRuleConfig.getLoadBalancerName());
        return result;
    }
    
    @Override
    public Optional<ReadwriteSplittingRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> readwriteSplittingRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validDataNodes.isEmpty()) {
            return Optional.empty();
        }
        Collection<ReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = new LinkedList<>();
        Map<String, AlgorithmConfiguration> loadBalancerMap = new LinkedHashMap<>();
        for (YamlDataNode each : validDataNodes) {
            readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES).getName(each.getKey())
                    .ifPresent(optional -> dataSourceGroups.add(swapDataSourceGroup(optional, YamlEngine.unmarshal(each.getValue(), YamlReadwriteSplittingDataSourceGroupRuleConfiguration.class))));
            readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS).getName(each.getKey())
                    .ifPresent(optional -> loadBalancerMap.put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
        }
        return Optional.of(new ReadwriteSplittingRuleConfiguration(dataSourceGroups, loadBalancerMap));
    }
    
    private ReadwriteSplittingDataSourceGroupRuleConfiguration swapDataSourceGroup(final String name, final YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceRuleConfig) {
        return new ReadwriteSplittingDataSourceGroupRuleConfiguration(name, yamlDataSourceRuleConfig.getWriteDataSourceName(), yamlDataSourceRuleConfig.getReadDataSourceNames(),
                getTransactionalReadQueryStrategy(yamlDataSourceRuleConfig), yamlDataSourceRuleConfig.getLoadBalancerName());
    }
    
    private TransactionalReadQueryStrategy getTransactionalReadQueryStrategy(final YamlReadwriteSplittingDataSourceGroupRuleConfiguration yamlDataSourceGroupRuleConfig) {
        return Strings.isNullOrEmpty(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy())
                ? TransactionalReadQueryStrategy.DYNAMIC
                : TransactionalReadQueryStrategy.valueOf(yamlDataSourceGroupRuleConfig.getTransactionalReadQueryStrategy());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getTypeClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "READWRITE_SPLITTING";
    }
    
    @Override
    public int getOrder() {
        return ReadwriteSplittingOrder.ORDER;
    }
}
