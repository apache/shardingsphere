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
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.mode.spi.PersistRepository;

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
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        repository.persist(GlobalNode.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
    }
    
    @Override
    public Collection<MetaDataVersion> persistConfig(final Collection<RuleConfiguration> globalRuleConfigs) {
        return Collections.emptyList();
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load() {
        String globalRule = repository.getDirectly(GlobalNode.getGlobalRuleNode());
        return Strings.isNullOrEmpty(globalRule)
                ? Collections.emptyList()
                : new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(globalRule, Collection.class));
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
