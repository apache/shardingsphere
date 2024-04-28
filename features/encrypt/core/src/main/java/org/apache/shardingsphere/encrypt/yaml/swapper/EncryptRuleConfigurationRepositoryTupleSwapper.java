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
import org.apache.shardingsphere.encrypt.metadata.nodepath.EncryptRuleNodePathProvider;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.rule.YamlEncryptTableRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfigurationSwapper;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;
import org.apache.shardingsphere.mode.path.RuleNodePath;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Encrypt rule configuration repository tuple swapper.
 */
public final class EncryptRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<EncryptRuleConfiguration> {
    
    private final YamlEncryptTableRuleConfigurationSwapper tableSwapper = new YamlEncryptTableRuleConfigurationSwapper();
    
    private final YamlAlgorithmConfigurationSwapper algorithmSwapper = new YamlAlgorithmConfigurationSwapper();
    
    private final RuleNodePath encryptRuleNodePath = new EncryptRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final EncryptRuleConfiguration data) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        for (Entry<String, AlgorithmConfiguration> entry : data.getEncryptors().entrySet()) {
            result.add(new RepositoryTuple(
                    encryptRuleNodePath.getNamedItem(EncryptRuleNodePathProvider.ENCRYPTORS).getPath(entry.getKey()), YamlEngine.marshal(algorithmSwapper.swapToYamlConfiguration(entry.getValue()))));
        }
        for (EncryptTableRuleConfiguration each : data.getTables()) {
            result.add(new RepositoryTuple(
                    encryptRuleNodePath.getNamedItem(EncryptRuleNodePathProvider.TABLES).getPath(each.getName()), YamlEngine.marshal(tableSwapper.swapToYamlConfiguration(each))));
        }
        return result;
    }
    
    @Override
    public Optional<EncryptRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> encryptRuleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        Collection<EncryptTableRuleConfiguration> tables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> encryptors = new LinkedHashMap<>();
        for (RepositoryTuple each : validTuples) {
            encryptRuleNodePath.getNamedItem(EncryptRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.add(tableSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlEncryptTableRuleConfiguration.class))));
            encryptRuleNodePath.getNamedItem(EncryptRuleNodePathProvider.ENCRYPTORS).getName(each.getKey())
                    .ifPresent(optional -> encryptors.put(optional, algorithmSwapper.swapToObject(YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class))));
        }
        return Optional.of(new EncryptRuleConfiguration(tables, encryptors));
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
