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
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.api.config.MaskRuleConfiguration;
import org.apache.shardingsphere.mask.api.config.rule.MaskTableRuleConfiguration;
import org.apache.shardingsphere.mask.constant.MaskOrder;
import org.apache.shardingsphere.mask.yaml.swapper.rule.YamlMaskTableRuleConfigurationSwapper;
import org.apache.shardingsphere.mask.metadata.converter.MaskNodeConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

/**
 * TODO Rename to YamlMaskRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML mask rule configuration swapper.
 */
public final class NewYamlMaskRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<MaskRuleConfiguration> {
    
    private final YamlMaskTableRuleConfigurationSwapper tableSwapper = new YamlMaskTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final MaskRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (MaskTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(MaskNodeConverter.getTableNamePath(each.getName()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        for (Entry<String, AlgorithmConfiguration> entry : data.getMaskAlgorithms().entrySet()) {
            result.add(new YamlDataNode(MaskNodeConverter.getMaskAlgorithmNamePath(entry.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        return result;
    }
    
    @Override
    public MaskRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        // TODO to be completed.
        return new MaskRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
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
