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

package org.apache.shardingsphere.metadata.persist.service.config;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.engine.AutoRepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.spi.RepositoryTupleSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Repository tuple swapper engine.
 */
public final class RepositoryTupleSwapperEngine {
    
    /**
     * Swap to rule configurations.
     *
     * @param repositoryTuples repository tuples
     * @return global rule configurations
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<RepositoryTuple> repositoryTuples) {
        if (repositoryTuples.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<RuleConfiguration> result = new LinkedList<>();
        YamlRuleConfigurationSwapperEngine yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        AutoRepositoryTupleSwapperEngine repositoryTupleSwapperEngine = new AutoRepositoryTupleSwapperEngine();
        for (RepositoryTupleSwapper each : OrderedSPILoader.getServices(RepositoryTupleSwapper.class)) {
            repositoryTupleSwapperEngine
                    .swapToObject(repositoryTuples, each.getTypeClass()).ifPresent(optional -> result.add(yamlSwapperEngine.swapToRuleConfiguration((YamlRuleConfiguration) optional)));
        }
        return result;
    }
    
    /**
     * Swap to rule configuration.
     *
     * @param ruleName rule name
     * @param repositoryTuples repository tuples
     * @return global rule configuration
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<RuleConfiguration> swapToRuleConfiguration(final String ruleName, final Collection<RepositoryTuple> repositoryTuples) {
        if (repositoryTuples.isEmpty()) {
            return Optional.empty();
        }
        YamlRuleConfigurationSwapperEngine yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        AutoRepositoryTupleSwapperEngine repositoryTupleSwapperEngine = new AutoRepositoryTupleSwapperEngine();
        for (RepositoryTupleSwapper each : OrderedSPILoader.getServices(RepositoryTupleSwapper.class)) {
            if (ruleName.equals(each.getRuleTypeName())) {
                Optional<YamlRuleConfiguration> yamlRuleConfig = repositoryTupleSwapperEngine.swapToObject(repositoryTuples, each.getTypeClass());
                return yamlRuleConfig.map(yamlSwapperEngine::swapToRuleConfiguration);
            }
        }
        return Optional.empty();
    }
}
