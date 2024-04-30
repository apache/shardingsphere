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

package org.apache.shardingsphere.shadow.yaml.swapper;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;
import org.apache.shardingsphere.shadow.yaml.config.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shadow rule configuration repository tuple swapper.
 */
public final class ShadowRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<ShadowRuleConfiguration, YamlShadowRuleConfiguration> {
    
    private final RuleNodePath shadowRuleNodePath = new ShadowRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlShadowRuleConfiguration yamlRuleConfig) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlRuleConfig.getShadowAlgorithms().entrySet()) {
            result.add(new RepositoryTuple(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.ALGORITHMS).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        if (!Strings.isNullOrEmpty(yamlRuleConfig.getDefaultShadowAlgorithmName())) {
            result.add(new RepositoryTuple(shadowRuleNodePath.getUniqueItem(ShadowRuleNodePathProvider.DEFAULT_ALGORITHM).getPath(), yamlRuleConfig.getDefaultShadowAlgorithmName()));
        }
        for (Entry<String, YamlShadowDataSourceConfiguration> entry : yamlRuleConfig.getDataSources().entrySet()) {
            result.add(new RepositoryTuple(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.DATA_SOURCES).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        for (Entry<String, YamlShadowTableConfiguration> entry : yamlRuleConfig.getTables().entrySet()) {
            result.add(new RepositoryTuple(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.TABLES).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        return result;
    }
    
    @Override
    public Optional<YamlShadowRuleConfiguration> swapToObject0(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validRepositoryTuples = repositoryTuples.stream().filter(each -> shadowRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validRepositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlShadowRuleConfiguration yamlRuleConfig = new YamlShadowRuleConfiguration();
        Map<String, YamlShadowDataSourceConfiguration> dataSources = new LinkedHashMap<>();
        Map<String, YamlShadowTableConfiguration> tables = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> shadowAlgorithms = new LinkedHashMap<>();
        for (RepositoryTuple each : validRepositoryTuples) {
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.DATA_SOURCES).getName(each.getKey())
                    .ifPresent(optional -> dataSources.put(optional, YamlEngine.unmarshal(each.getValue(), YamlShadowDataSourceConfiguration.class)));
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlShadowTableConfiguration.class)));
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> shadowAlgorithms.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
            if (shadowRuleNodePath.getUniqueItem(ShadowRuleNodePathProvider.DEFAULT_ALGORITHM).isValidatedPath(each.getKey())) {
                yamlRuleConfig.setDefaultShadowAlgorithmName(each.getValue());
            }
        }
        yamlRuleConfig.setDataSources(dataSources);
        yamlRuleConfig.setTables(tables);
        yamlRuleConfig.setShadowAlgorithms(shadowAlgorithms);
        return Optional.of(yamlRuleConfig);
    }
    
    @Override
    public Class<ShadowRuleConfiguration> getTypeClass() {
        return ShadowRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "SHADOW";
    }
    
    @Override
    public int getOrder() {
        return ShadowOrder.ORDER;
    }
}
