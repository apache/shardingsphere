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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.NewGlobalNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TODO Rename GlobalRulePersistService when metadata structure adjustment completed. #25485
 * New Global rule persist service.
 */
@RequiredArgsConstructor
public final class NewGlobalRulePersistService implements GlobalPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        Map<RuleConfiguration, NewYamlRuleConfigurationSwapper> yamlConfigs = new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs);
        for (Entry<RuleConfiguration, NewYamlRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            persistDataNodes(dataNodes);
        }
    }
    
    private void persistDataNodes(final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            if (Strings.isNullOrEmpty(NewGlobalNode.getGlobalRuleActiveVersionNode(each.getKey()))) {
                repository.persist(NewGlobalNode.getGlobalRuleActiveVersionNode(each.getKey()), DEFAULT_VERSION);
            }
            repository.persist(NewGlobalNode.getGlobalRuleVersionNode(each.getKey(), DEFAULT_VERSION), each.getValue());
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load() {
        Collection<String> result = new LinkedHashSet<>();
        getAllNodes(result, NewGlobalNode.getGlobalRuleRootNode());
        if (1 == result.size()) {
            return Collections.emptyList();
        }
        return new NewYamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(getDataNodes(result));
    }
    
    // TODO Consider merge NewGlobalRulePersistService and NewDatabaseRulePersistService load method.
    private void getAllNodes(final Collection<String> keys, final String path) {
        keys.add(path);
        List<String> childrenKeys = repository.getChildrenKeys(path);
        if (childrenKeys.isEmpty()) {
            return;
        }
        for (String each : childrenKeys) {
            getAllNodes(keys, String.join("/", "", path, each));
        }
    }
    
    private Collection<YamlDataNode> getDataNodes(final Collection<String> keys) {
        Collection<YamlDataNode> result = new LinkedHashSet<>();
        for (String each : keys) {
            result.add(new YamlDataNode(each, repository.getDirectly(each)));
        }
        return result;
    }
    
    /**
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
