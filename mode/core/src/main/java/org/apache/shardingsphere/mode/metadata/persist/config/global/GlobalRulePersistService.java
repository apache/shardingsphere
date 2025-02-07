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

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.config.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.node.tuple.YamlRepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Global rule persist service.
 */
public final class GlobalRulePersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final RepositoryTuplePersistService repositoryTuplePersistService;
    
    private final YamlRepositoryTupleSwapperEngine yamlRepositoryTupleSwapperEngine;
    
    private final YamlRuleConfigurationSwapperEngine yamlRuleConfigurationSwapperEngine;
    
    public GlobalRulePersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.repository = repository;
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
        return yamlRepositoryTupleSwapperEngine.swapToRuleConfigurations(repositoryTuplePersistService.load(GlobalRuleNodePath.getRootPath()));
    }
    
    /**
     * Load global rule configuration.
     *
     * @param ruleTypeName rule type name to be loaded
     * @return global rule configuration
     */
    public Optional<RuleConfiguration> load(final String ruleTypeName) {
        return yamlRepositoryTupleSwapperEngine.swapToRuleConfiguration(ruleTypeName, repositoryTuplePersistService.load(GlobalRuleNodePath.getRulePath(ruleTypeName)));
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     */
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        for (YamlRuleConfiguration each : yamlRuleConfigurationSwapperEngine.swapToYamlRuleConfigurations(globalRuleConfigs)) {
            metaDataVersions.addAll(persistTuples(yamlRepositoryTupleSwapperEngine.swapToRepositoryTuples(each)));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private Collection<MetaDataVersion> persistTuples(final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            int nextActiveVersion = metaDataVersionPersistService.getNextVersion(GlobalRuleNodePath.getVersionNodePathGenerator(each.getKey()).getVersionsPath());
            repository.persist(GlobalRuleNodePath.getVersionNodePathGenerator(each.getKey()).getVersionPath(nextActiveVersion), each.getValue());
            String ruleActiveVersionPath = GlobalRuleNodePath.getVersionNodePathGenerator(each.getKey()).getActiveVersionPath();
            if (null == getRuleActiveVersion(ruleActiveVersionPath)) {
                repository.persist(ruleActiveVersionPath, String.valueOf(MetaDataVersion.DEFAULT_VERSION));
            }
            Integer ruleActiveVersion = getRuleActiveVersion(ruleActiveVersionPath);
            result.add(new MetaDataVersion(GlobalRuleNodePath.getRulePath(each.getKey()), null == ruleActiveVersion ? MetaDataVersion.DEFAULT_VERSION : ruleActiveVersion, nextActiveVersion));
        }
        return result;
    }
    
    private Integer getRuleActiveVersion(final String ruleActiveVersionPath) {
        String value = repository.query(ruleActiveVersionPath);
        return Strings.isNullOrEmpty(value) ? null : Integer.parseInt(value);
    }
}
