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

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.constant.ReadwriteSplittingOrder;
import org.apache.shardingsphere.readwritesplitting.metadata.nodepath.ReadwriteSplittingRuleNodePathProvider;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
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
 * Readwrite-splitting rule configuration repository tuple swapper.
 */
public final class ReadwriteSplittingRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<ReadwriteSplittingRuleConfiguration> {
    
    private final YamlReadwriteSplittingRuleConfigurationSwapper ruleConfigSwapper = new YamlReadwriteSplittingRuleConfigurationSwapper();
    
    private final RuleNodePath readwriteSplittingRuleNodePath = new ReadwriteSplittingRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final ReadwriteSplittingRuleConfiguration data) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        YamlReadwriteSplittingRuleConfiguration yamlRuleConfig = ruleConfigSwapper.swapToYamlConfiguration(data);
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlRuleConfig.getLoadBalancers().entrySet()) {
            result.add(new RepositoryTuple(readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS).getPath(entry.getKey()),
                    YamlEngine.marshal(entry.getValue())));
        }
        for (Entry<String, YamlReadwriteSplittingDataSourceGroupRuleConfiguration> entry : yamlRuleConfig.getDataSourceGroups().entrySet()) {
            result.add(new RepositoryTuple(
                    readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        return result;
    }
    
    @Override
    public Optional<ReadwriteSplittingRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validRepositoryTuples = repositoryTuples.stream().filter(each -> readwriteSplittingRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validRepositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        Map<String, YamlReadwriteSplittingDataSourceGroupRuleConfiguration> dataSourceGroups = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> loadBalancers = new LinkedHashMap<>();
        for (RepositoryTuple each : validRepositoryTuples) {
            readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.DATA_SOURCES).getName(each.getKey())
                    .ifPresent(optional -> dataSourceGroups.put(optional, YamlEngine.unmarshal(each.getValue(), YamlReadwriteSplittingDataSourceGroupRuleConfiguration.class)));
            readwriteSplittingRuleNodePath.getNamedItem(ReadwriteSplittingRuleNodePathProvider.LOAD_BALANCERS).getName(each.getKey())
                    .ifPresent(optional -> loadBalancers.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
        }
        YamlReadwriteSplittingRuleConfiguration yamlRuleConfig = new YamlReadwriteSplittingRuleConfiguration();
        yamlRuleConfig.setDataSourceGroups(dataSourceGroups);
        yamlRuleConfig.setLoadBalancers(loadBalancers);
        return Optional.of(ruleConfigSwapper.swapToObject(yamlRuleConfig));
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
