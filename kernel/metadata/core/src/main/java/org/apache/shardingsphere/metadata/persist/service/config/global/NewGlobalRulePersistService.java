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

package org.apache.shardingsphere.metadata.persist.service.config.global;

import com.google.common.base.Strings;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlGlobalRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.NewGlobalNode;
import org.apache.shardingsphere.metadata.persist.service.config.AbstractPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * TODO Rename GlobalRulePersistService when metadata structure adjustment completed. #25485
 * New Global rule persist service.
 */
public final class NewGlobalRulePersistService extends AbstractPersistService implements GlobalPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    public NewGlobalRulePersistService(final PersistRepository repository) {
        super(repository);
        this.repository = repository;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        Map<RuleConfiguration, NewYamlGlobalRuleConfigurationSwapper> yamlConfigs = new NewYamlGlobalRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs);
        for (Entry<RuleConfiguration, NewYamlGlobalRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            persistDataNodes(dataNodes);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<MetaDataVersion> persistConfig(final Collection<RuleConfiguration> globalRuleConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        Map<RuleConfiguration, NewYamlGlobalRuleConfigurationSwapper> yamlConfigs = new NewYamlGlobalRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs);
        for (Entry<RuleConfiguration, NewYamlGlobalRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            result.addAll(persistDataNodes(dataNodes));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistDataNodes(final Collection<YamlDataNode> dataNodes) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (YamlDataNode each : dataNodes) {
            List<String> versions = repository.getChildrenKeys(NewGlobalNode.getGlobalRuleVersionsNode(each.getKey()));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            String persistKey = NewGlobalNode.getGlobalRuleVersionNode(each.getKey(), nextActiveVersion);
            repository.persist(persistKey, each.getValue());
            if (Strings.isNullOrEmpty(getActiveVersion(NewGlobalNode.getGlobalRuleActiveVersionNode(each.getKey())))) {
                repository.persist(NewGlobalNode.getGlobalRuleActiveVersionNode(each.getKey()), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(NewGlobalNode.getGlobalRuleNode(each.getKey()), getActiveVersion(NewGlobalNode.getGlobalRuleActiveVersionNode(each.getKey())), nextActiveVersion));
        }
        return result;
    }
    
    @Override
    public Collection<RuleConfiguration> load() {
        Collection<YamlDataNode> dataNodes = getDataNodes(NewGlobalNode.getGlobalRuleRootNode());
        return dataNodes.isEmpty() ? Collections.emptyList() : new NewYamlGlobalRuleConfigurationSwapperEngine().swapToRuleConfigurations(dataNodes);
    }
    
    @Override
    public RuleConfiguration load(final String ruleName) {
        Collection<YamlDataNode> dataNodes = getDataNodes(NewGlobalNode.getGlobalRuleNode(ruleName));
        return new NewYamlGlobalRuleConfigurationSwapperEngine().swapSingleRuleToRuleConfiguration(ruleName, dataNodes).orElse(null);
    }
    
    /**
     * TODO Avoid load all keys.
     * Load all users.
     * 
     * @return collection of user
     */
    @Override
    public Collection<ShardingSphereUser> loadUsers() {
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = load().stream()
                .filter(AuthorityRuleConfiguration.class::isInstance).map(AuthorityRuleConfiguration.class::cast).findFirst();
        return authorityRuleConfig.isPresent() ? authorityRuleConfig.get().getUsers() : Collections.emptyList();
    }
}
