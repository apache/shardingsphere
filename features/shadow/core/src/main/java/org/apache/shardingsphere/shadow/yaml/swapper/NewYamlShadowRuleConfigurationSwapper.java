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
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.datasource.ShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.table.ShadowTableConfiguration;
import org.apache.shardingsphere.shadow.constant.ShadowOrder;
import org.apache.shardingsphere.shadow.metadata.nodepath.ShadowRuleNodePathProvider;
import org.apache.shardingsphere.shadow.yaml.config.datasource.YamlShadowDataSourceConfiguration;
import org.apache.shardingsphere.shadow.yaml.config.table.YamlShadowTableConfiguration;
import org.apache.shardingsphere.shadow.yaml.swapper.table.YamlShadowTableConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename YamlShadowRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML shadow rule configuration swapper.
 */
public final class NewYamlShadowRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<ShadowRuleConfiguration> {
    
    private final YamlShadowTableConfigurationSwapper tableSwapper = new YamlShadowTableConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath shadowRuleNodePath = new ShadowRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final ShadowRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getShadowAlgorithms().entrySet()) {
            result.add(new YamlDataNode(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.ALGORITHMS).getPath(entry.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        if (!Strings.isNullOrEmpty(data.getDefaultShadowAlgorithmName())) {
            result.add(new YamlDataNode(shadowRuleNodePath.getUniqueItem(ShadowRuleNodePathProvider.DEFAULT_ALGORITHM).getPath(), data.getDefaultShadowAlgorithmName()));
        }
        for (ShadowDataSourceConfiguration each : data.getDataSources()) {
            result.add(new YamlDataNode(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.DATA_SOURCES).getPath(each.getName()),
                    YamlEngine.marshal(swapToDataSourceYamlConfiguration(each))));
        }
        for (Entry<String, ShadowTableConfiguration> entry : data.getTables().entrySet()) {
            result.add(new YamlDataNode(shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.TABLES).getPath(entry.getKey()),
                    YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        return result;
    }
    
    private YamlShadowDataSourceConfiguration swapToDataSourceYamlConfiguration(final ShadowDataSourceConfiguration data) {
        YamlShadowDataSourceConfiguration result = new YamlShadowDataSourceConfiguration();
        result.setProductionDataSourceName(data.getProductionDataSourceName());
        result.setShadowDataSourceName(data.getShadowDataSourceName());
        return result;
    }
    
    @Override
    public Optional<ShadowRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> shadowRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validDataNodes.isEmpty()) {
            return Optional.empty();
        }
        ShadowRuleConfiguration result = new ShadowRuleConfiguration();
        for (YamlDataNode each : validDataNodes) {
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.DATA_SOURCES).getName(each.getKey())
                    .ifPresent(optional -> result.getDataSources().add(swapDataSource(optional, YamlEngine.unmarshal(each.getValue(), YamlShadowDataSourceConfiguration.class))));
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> result.getTables().put(optional, tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlShadowTableConfiguration.class))));
            shadowRuleNodePath.getNamedItem(ShadowRuleNodePathProvider.ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> result.getShadowAlgorithms().put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
            if (shadowRuleNodePath.getUniqueItem(ShadowRuleNodePathProvider.DEFAULT_ALGORITHM).isValidatedPath(each.getKey())) {
                result.setDefaultShadowAlgorithmName(each.getValue());
            }
        }
        return Optional.of(result);
    }
    
    private ShadowDataSourceConfiguration swapDataSource(final String name, final YamlShadowDataSourceConfiguration yamlConfig) {
        return new ShadowDataSourceConfiguration(name, yamlConfig.getProductionDataSourceName(), yamlConfig.getShadowDataSourceName());
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
