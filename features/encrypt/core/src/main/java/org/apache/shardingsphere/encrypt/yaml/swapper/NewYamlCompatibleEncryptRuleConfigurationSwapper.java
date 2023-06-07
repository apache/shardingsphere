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
import org.apache.shardingsphere.encrypt.metadata.converter.EncryptNodeConverter;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlCompatibleEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.algorithm.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

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
    
    @Override
    public Collection<YamlDataNode> swapToDataNodes(final CompatibleEncryptRuleConfiguration data) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (EncryptTableRuleConfiguration each : data.getTables()) {
            result.add(new YamlDataNode(EncryptNodeConverter.getTableNamePath(each.getName()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        for (Entry<String, AlgorithmConfiguration> entry : data.getEncryptors().entrySet()) {
            result.add(new YamlDataNode(EncryptNodeConverter.getEncryptorPath(entry.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        return result;
    }
    
    @Override
    public CompatibleEncryptRuleConfiguration swapToObject(final Collection<YamlDataNode> dataNodes) {
        // TODO to be completed
        return new CompatibleEncryptRuleConfiguration(Collections.emptyList(), Collections.emptyMap());
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
