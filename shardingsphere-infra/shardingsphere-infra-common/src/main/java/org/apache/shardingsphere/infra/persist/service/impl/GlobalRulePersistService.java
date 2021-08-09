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

package org.apache.shardingsphere.infra.persist.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.persist.node.GlobalNode;
import org.apache.shardingsphere.infra.persist.service.GlobalPersistService;
import org.apache.shardingsphere.infra.persist.repository.DistMetaDataPersistRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Collections;

/**
 * Global rule persist service.
 */
@RequiredArgsConstructor
public final class GlobalRulePersistService implements GlobalPersistService<Collection<RuleConfiguration>> {
    
    private final DistMetaDataPersistRepository repository;
    
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs, final boolean isOverwrite) {
        if (!globalRuleConfigs.isEmpty() && (isOverwrite || !isExisted())) {
            repository.persist(GlobalNode.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load() {
        return isExisted()
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(GlobalNode.getGlobalRuleNode()), Collection.class))
                : Collections.emptyList();
    }
    
    private boolean isExisted() {
        return !Strings.isNullOrEmpty(repository.get(GlobalNode.getGlobalRuleNode()));
    }
}
