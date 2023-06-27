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

package org.apache.shardingsphere.encrypt.yaml.swapper;

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.metadata.converter.EncryptNodeConverter;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO Rename to YamlEncryptRuleConfigurationSwapper when metadata structure adjustment completed.
 * New YAML encrypt rule configuration swapper.
 */
public final class NewYamlEncryptRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<EncryptRuleConfiguration> {
    
    private final YamlEncryptTableRuleConfigurationSwapper tableSwapper = new YamlEncryptTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath encryptRuleNodePath = EncryptNodeConverter.getInstance();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final EncryptRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getEncryptors().entrySet()) {
            result.add(new YamlDataNode(encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.ENCRYPTORS).getPath(entry.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (EncryptTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.TABLES).getPath(each.getName()),
                    YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    @Override
    public EncryptRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> encryptors = new HashMap<>();
        for (YamlDataNode each : dataNodes) {
            encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlEncryptTableRuleConfiguration.class))));
            encryptRuleNodePath.getNamedRuleItemNodePath(EncryptNodeConverter.ENCRYPTORS).getName(each.getKey())
                    .ifPresent(optional -> encryptors.put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
        }
        return new EncryptRuleConfiguration(tables, encryptors);
    }
    
    @Override
    public Class<EncryptRuleConfiguration> getTypeClass() {
        return EncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
}
