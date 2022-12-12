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

package org.apache.shardingsphere.mode.metadata.persist.service.config.global;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.authority.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Global rule persist service.
 */
@RequiredArgsConstructor
public final class GlobalRulePersistService implements GlobalPersistService<Collection<RuleConfiguration>> {
    
    private final PersistRepository repository;
    
    @Override
    public void conditionalPersist(final Collection<RuleConfiguration> globalRuleConfigs) {
        if (!globalRuleConfigs.isEmpty() && !isExisted()) {
            persist(globalRuleConfigs);
        }
    }
    
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        repository.persist(GlobalNode.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load() {
        return isExisted()
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.getDirectly(GlobalNode.getGlobalRuleNode()), Collection.class))
                : Collections.emptyList();
    }
    
    private boolean isExisted() {
        return !Strings.isNullOrEmpty(repository.getDirectly(GlobalNode.getGlobalRuleNode()));
    }
    
    /**
     * Load all users.
     * 
     * @return collection of user
     */
    public Collection<ShardingSphereUser> loadUsers() {
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = load().stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .map(each -> (AuthorityRuleConfiguration) each).findFirst();
        return authorityRuleConfig.isPresent() ? authorityRuleConfig.get().getUsers() : Collections.emptyList();
    }
}
