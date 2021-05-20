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

package org.apache.shardingsphere.governance.core.registry.rule;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.governance.core.registry.RegistryCenterNode;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Collections;

/**
 * Global rule registry center.
 */
@RequiredArgsConstructor
public final class GlobalRuleRegistryCenter {
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node = new RegistryCenterNode();
    
    /**
     * Persist global rule configurations.
     * 
     * @param globalRuleConfigs global rule configurations
     * @param isOverwrite is overwrite
     */
    public void persistGlobalRuleConfigurations(final Collection<RuleConfiguration> globalRuleConfigs, final boolean isOverwrite) {
        if (!globalRuleConfigs.isEmpty() && (isOverwrite || !hasGlobalRuleConfigurations())) {
            repository.persist(node.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
        }
    }
    
    private boolean hasGlobalRuleConfigurations() {
        return !Strings.isNullOrEmpty(repository.get(node.getGlobalRuleNode()));
    }
    
    /**
     * Load global rule configurations.
     * 
     * @return global rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> loadGlobalRuleConfigurations() {
        return hasGlobalRuleConfigurations()
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(node.getGlobalRuleNode()), Collection.class)) : Collections.emptyList();
    }
}
