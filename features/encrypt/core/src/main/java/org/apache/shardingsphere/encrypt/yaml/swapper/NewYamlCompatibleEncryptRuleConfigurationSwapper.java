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

import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.metadata.nodepath.CompatibleEncryptRuleNodePathProvider;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlCompatibleEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlCompatibleEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.pojo.algorithm.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO Rename to YamlCompatibleEncryptRuleConfigurationSwapper when metadata structure adjustment
 * YAML encrypt rule configuration swapper.
 *
 * @deprecated Should use new api, compatible api will remove in next version.
 */
@Deprecated
public final class NewYamlCompatibleEncryptRuleConfigurationSwapper implements NewYamlRuleConfigurationSwapper<CompatibleEncryptRuleConfiguration> {
    
    private final YamlCompatibleEncryptTableRuleConfigurationSwapper tableSwapper = new YamlCompatibleEncryptTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath encryptRuleNodePath = new CompatibleEncryptRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final CompatibleEncryptRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getEncryptors().entrySet()) {
            result.add(new YamlDataNode(encryptRuleNodePath.getNamedItem(CompatibleEncryptRuleNodePathProvider.ENCRYPTORS).getPath(entry.getKey()),
                    YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (EncryptTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(
                    encryptRuleNodePath.getNamedItem(CompatibleEncryptRuleNodePathProvider.TABLES).getPath(each.getName()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    @Override
    public Optional<CompatibleEncryptRuleConfiguration> swapToObject(final Collection<YamlDataNode> dataNodes) {
        List<YamlDataNode> validDataNodes = dataNodes.stream().filter(each -> encryptRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validDataNodes.isEmpty()) {
            return Optional.empty();
        }
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> encryptors = new LinkedHashMap<>();
        for (YamlDataNode each : validDataNodes) {
            encryptRuleNodePath.getNamedItem(CompatibleEncryptRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlCompatibleEncryptTableRuleConfiguration.class))));
            encryptRuleNodePath.getNamedItem(CompatibleEncryptRuleNodePathProvider.ENCRYPTORS).getName(each.getKey())
                    .ifPresent(optional -> encryptors.put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
        }
        return Optional.of(new CompatibleEncryptRuleConfiguration(tables, encryptors));
    }
    
    @Override
    public Class<CompatibleEncryptRuleConfiguration> getTypeClass() {
        return CompatibleEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTagName() {
        return "COMPATIBLE_ENCRYPT";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.COMPATIBLE_ORDER;
    }
}
