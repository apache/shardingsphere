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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.datanode.RepositoryTuple;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeGlobalRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeGlobalRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.GlobalNode;
import org.apache.shardingsphere.metadata.persist.service.config.AbstractPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Global rule persist service.
 */
public final class GlobalRulePersistService extends AbstractPersistService implements GlobalPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    public GlobalRulePersistService(final PersistRepository repository) {
        super(repository);
        this.repository = repository;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        Map<RuleConfiguration, YamlDataNodeGlobalRuleConfigurationSwapper> yamlConfigs = new YamlDataNodeGlobalRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs);
        for (Entry<RuleConfiguration, YamlDataNodeGlobalRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<RepositoryTuple> repositoryTuples = entry.getValue().swapToRepositoryTuples(entry.getKey());
            if (!repositoryTuples.isEmpty()) {
                persistDataNodes(repositoryTuples);
            }
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<MetaDataVersion> persistConfigurations(final Collection<RuleConfiguration> globalRuleConfigs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        Map<RuleConfiguration, YamlDataNodeGlobalRuleConfigurationSwapper> yamlConfigs = new YamlDataNodeGlobalRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs);
        for (Entry<RuleConfiguration, YamlDataNodeGlobalRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<RepositoryTuple> repositoryTuples = entry.getValue().swapToRepositoryTuples(entry.getKey());
            if (!repositoryTuples.isEmpty()) {
                result.addAll(persistDataNodes(repositoryTuples));
            }
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistDataNodes(final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            List<String> versions = repository.getChildrenKeys(GlobalNode.getGlobalRuleVersionsNode(each.getKey()));
            String nextActiveVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            String persistKey = GlobalNode.getGlobalRuleVersionNode(each.getKey(), nextActiveVersion);
            repository.persist(persistKey, each.getValue());
            if (Strings.isNullOrEmpty(getActiveVersion(GlobalNode.getGlobalRuleActiveVersionNode(each.getKey())))) {
                repository.persist(GlobalNode.getGlobalRuleActiveVersionNode(each.getKey()), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(GlobalNode.getGlobalRuleNode(each.getKey()), getActiveVersion(GlobalNode.getGlobalRuleActiveVersionNode(each.getKey())), nextActiveVersion));
        }
        return result;
    }
    
    @Override
    public Collection<RuleConfiguration> load() {
        Collection<RepositoryTuple> repositoryTuple = getRepositoryTuple(GlobalNode.getGlobalRuleRootNode());
        return repositoryTuple.isEmpty() ? Collections.emptyList() : new YamlDataNodeGlobalRuleConfigurationSwapperEngine().swapToRuleConfigurations(repositoryTuple);
    }
    
    @Override
    public RuleConfiguration load(final String ruleName) {
        Collection<RepositoryTuple> repositoryTuple = getRepositoryTuple(GlobalNode.getGlobalRuleNode(ruleName));
        return new YamlDataNodeGlobalRuleConfigurationSwapperEngine().swapSingleRuleToRuleConfiguration(ruleName, repositoryTuple).orElse(null);
    }
}
