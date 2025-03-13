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
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlGlobalRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.global.config.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleNodeTuple;
import org.apache.shardingsphere.mode.node.rule.tuple.YamlRuleNodeTupleSwapperEngine;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Global rule persist service.
 */
public final class GlobalRulePersistService {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionPersistService;
    
    private final YamlRuleConfigurationSwapperEngine yamlSwapperEngine;
    
    private final YamlRuleNodeTupleSwapperEngine tupleSwapperEngine;
    
    public GlobalRulePersistService(final PersistRepository repository, final VersionPersistService versionPersistService) {
        this.repository = repository;
        this.versionPersistService = versionPersistService;
        yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        tupleSwapperEngine = new YamlRuleNodeTupleSwapperEngine();
    }
    
    /**
     * Load global rule configurations.
     *
     * @return global rule configurations
     */
    public Collection<RuleConfiguration> load() {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new GlobalRuleNodePath(null))).stream().map(this::load).collect(Collectors.toList());
    }
    
    /**
     * Load global rule configuration.
     *
     * @param ruleType rule type to be loaded
     * @return global rule configuration
     */
    public RuleConfiguration load(final String ruleType) {
        String ruleContent = versionPersistService.loadContent(new VersionNodePath(new GlobalRuleNodePath(ruleType)));
        return yamlSwapperEngine.swapToRuleConfiguration(tupleSwapperEngine.swapToYamlGlobalRuleConfiguration(ruleType, ruleContent));
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     */
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        for (YamlRuleConfiguration each : yamlSwapperEngine.swapToYamlRuleConfigurations(globalRuleConfigs)) {
            persistTuple(tupleSwapperEngine.swapToTuple((YamlGlobalRuleConfiguration) each));
        }
    }
    
    private void persistTuple(final RuleNodeTuple tuple) {
        versionPersistService.persist(new VersionNodePath(tuple.getNodePath()), tuple.getContent());
    }
}
