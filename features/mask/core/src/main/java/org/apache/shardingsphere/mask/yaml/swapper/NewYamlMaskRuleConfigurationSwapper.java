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

package org.apache.shardingsphere.mask.yaml.swapper;

import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename to YamlMaskRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML mask rule configuration swapper.
 */
public final class NewYamlMaskRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<MaskRuleConfiguration> {
    
    private final YamlMaskTableRuleConfigurationSwapper tableSwapper = new YamlMaskTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath maskRuleNodePath = new MaskRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final MaskRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getMaskAlgorithms().entrySet()) {
            result.add(new YamlDataNode(maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.MASK_ALGORITHMS).getPath(entry.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (MaskTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.TABLES).getPath(each.getName()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    @Override
    public Optional<MaskRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> maskRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validDataNodes.isEmpty()) {
            return Optional.empty();
        }
        Collection<MaskTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> algorithms = new LinkedHashMap<>();
        for (YamlDataNode each : validDataNodes) {
            maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlMaskTableRuleConfiguration.class))));
            maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.MASK_ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> algorithms.put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
        }
        return Optional.of(new MaskRuleConfiguration(tables, algorithms));
    }
    
    @Override
    public Class<MaskRuleConfiguration> getTypeClass() {
        return MaskRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "MASK";
    }
    
    @Override
    public int getOrder() {
        return MaskOrder.ORDER;
    }
}
