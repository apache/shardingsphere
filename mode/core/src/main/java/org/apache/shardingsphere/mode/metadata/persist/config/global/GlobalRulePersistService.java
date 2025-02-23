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

package org.apache.shardingsphere.mode.metadata.persist.config.global;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.config.global.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.node.tuple.YamlRepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.Optional;

/**
 * Global rule persist service.
 */
public final class GlobalRulePersistService {
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final RepositoryTuplePersistService repositoryTuplePersistService;
    
    private final YamlRepositoryTupleSwapperEngine yamlRepositoryTupleSwapperEngine;
    
    private final YamlRuleConfigurationSwapperEngine yamlRuleConfigurationSwapperEngine;
    
    public GlobalRulePersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.metaDataVersionPersistService = metaDataVersionPersistService;
        repositoryTuplePersistService = new RepositoryTuplePersistService(repository);
        yamlRepositoryTupleSwapperEngine = new YamlRepositoryTupleSwapperEngine();
        yamlRuleConfigurationSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    }
    
    /**
     * Load global rule configurations.
     *
     * @return global rule configurations
     */
    public Collection<RuleConfiguration> load() {
        return yamlRepositoryTupleSwapperEngine.swapToRuleConfigurations(repositoryTuplePersistService.load(NodePathGenerator.toPath(new GlobalRuleNodePath(null), false)));
    }
    
    /**
     * Load global rule configuration.
     *
     * @param ruleType rule type to be loaded
     * @return global rule configuration
     */
    public Optional<RuleConfiguration> load(final String ruleType) {
        return yamlRepositoryTupleSwapperEngine.swapToRuleConfiguration(ruleType, repositoryTuplePersistService.load(NodePathGenerator.toPath(new GlobalRuleNodePath(ruleType), false)));
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     */
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        for (YamlRuleConfiguration each : yamlRuleConfigurationSwapperEngine.swapToYamlRuleConfigurations(globalRuleConfigs)) {
            persistTuples(yamlRepositoryTupleSwapperEngine.swapToRepositoryTuples(each));
        }
    }
    
    private void persistTuples(final Collection<RepositoryTuple> tuples) {
        for (RepositoryTuple each : tuples) {
            metaDataVersionPersistService.persist(NodePathGenerator.toVersionPath(new GlobalRuleNodePath(each.getKey())), each.getValue());
        }
    }
}
