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

import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.metadata.nodepath.MaskRuleNodePathProvider;
import org.apache.shardingsphere.mask.yaml.config.YamlMaskRuleConfiguration;
import org.apache.shardingsphere.mask.yaml.config.rule.YamlMaskTableRuleConfiguration;
import org.apache.shardingsphere.mode.path.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mask rule configuration repository tuple swapper.
 */
public final class MaskRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<MaskRuleConfiguration, YamlMaskRuleConfiguration> {
    
    private final RuleNodePath maskRuleNodePath = new MaskRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlMaskRuleConfiguration yamlRuleConfig) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlRuleConfig.getMaskAlgorithms().entrySet()) {
            result.add(new RepositoryTuple(maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.MASK_ALGORITHMS).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        for (YamlMaskTableRuleConfiguration each : yamlRuleConfig.getTables().values()) {
            result.add(new RepositoryTuple(maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.TABLES).getPath(each.getName()), YamlEngine.marshal(each)));
        }
        return result;
    }
    
    @Override
    public Optional<YamlMaskRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> maskRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlMaskRuleConfiguration yamlRuleConfig = new YamlMaskRuleConfiguration();
        Map<String, YamlMaskTableRuleConfiguration> tables = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> maskAlgorithms = new LinkedHashMap<>();
        for (RepositoryTuple each : validTuples) {
            maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlMaskTableRuleConfiguration.class)));
            maskRuleNodePath.getNamedItem(MaskRuleNodePathProvider.MASK_ALGORITHMS).getName(each.getKey())
                    .ifPresent(optional -> maskAlgorithms.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
        }
        yamlRuleConfig.setTables(tables);
        yamlRuleConfig.setMaskAlgorithms(maskAlgorithms);
        return Optional.of(yamlRuleConfig);
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
