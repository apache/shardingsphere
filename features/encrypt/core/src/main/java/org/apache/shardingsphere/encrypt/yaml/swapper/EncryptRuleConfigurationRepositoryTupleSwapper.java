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

import org.apache.shardingsphere.encrypt.constant.EncryptOrder;
import org.apache.shardingsphere.encrypt.metadata.nodepath.EncryptRuleNodePathProvider;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.rule.YamlEncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.algorithm.core.yaml.YamlAlgorithmConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.mode.path.rule.RuleNodePath;
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
 * Encrypt rule configuration repository tuple swapper.
 */
public final class EncryptRuleConfigurationRepositoryTupleSwapper implements RepositoryTupleSwapper<YamlEncryptRuleConfiguration> {
    
    private final RuleNodePath ruleNodePath = new EncryptRuleNodePathProvider().getRuleNodePath();
    
    @Override
    public Collection<RepositoryTuple> swapToRepositoryTuples(final YamlEncryptRuleConfiguration yamlRuleConfig) {
        Collection<RepositoryTuple> result = new LinkedList<>();
        for (Entry<String, YamlAlgorithmConfiguration> entry : yamlRuleConfig.getEncryptors().entrySet()) {
            result.add(new RepositoryTuple(ruleNodePath.getNamedItem(EncryptRuleNodePathProvider.ENCRYPTORS).getPath(entry.getKey()), YamlEngine.marshal(entry.getValue())));
        }
        for (YamlEncryptTableRuleConfiguration each : yamlRuleConfig.getTables().values()) {
            result.add(new RepositoryTuple(ruleNodePath.getNamedItem(EncryptRuleNodePathProvider.TABLES).getPath(each.getName()), YamlEngine.marshal(each)));
        }
        return result;
    }
    
    @Override
    public Optional<YamlEncryptRuleConfiguration> swapToObject(final Collection<RepositoryTuple> repositoryTuples) {
        List<RepositoryTuple> validTuples = repositoryTuples.stream().filter(each -> ruleNodePath.getRoot().isValidatedPath(each.getKey())).collect(Collectors.toList());
        if (validTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlEncryptRuleConfiguration yamlRuleConfig = new YamlEncryptRuleConfiguration();
        Map<String, YamlEncryptTableRuleConfiguration> tables = new LinkedHashMap<>();
        Map<String, YamlAlgorithmConfiguration> encryptors = new LinkedHashMap<>();
        for (RepositoryTuple each : validTuples) {
            ruleNodePath.getNamedItem(EncryptRuleNodePathProvider.TABLES).getName(each.getKey())
                    .ifPresent(optional -> tables.put(optional, YamlEngine.unmarshal(each.getValue(), YamlEncryptTableRuleConfiguration.class)));
            ruleNodePath.getNamedItem(EncryptRuleNodePathProvider.ENCRYPTORS).getName(each.getKey())
                    .ifPresent(optional -> encryptors.put(optional, YamlEngine.unmarshal(each.getValue(), YamlAlgorithmConfiguration.class)));
        }
        yamlRuleConfig.setTables(tables);
        yamlRuleConfig.setEncryptors(encryptors);
        return Optional.of(yamlRuleConfig);
    }
    
    @Override
    public Class<YamlEncryptRuleConfiguration> getTypeClass() {
        return YamlEncryptRuleConfiguration.class;
    }
    
    @Override
    public String getRuleTypeName() {
        return "encrypt";
    }
    
    @Override
    public int getOrder() {
        return EncryptOrder.ORDER;
    }
}
