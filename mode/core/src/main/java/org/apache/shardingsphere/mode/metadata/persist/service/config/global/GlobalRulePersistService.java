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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.service.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.GlobalRuleNodePath;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTupleSwapperEngine;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Global rule persist service.
 */
public final class GlobalRulePersistService {
    
    private final PersistRepository repository;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final RepositoryTuplePersistService repositoryTuplePersistService;
    
    public GlobalRulePersistService(final PersistRepository repository, final MetaDataVersionPersistService metaDataVersionPersistService) {
        this.repository = repository;
        this.metaDataVersionPersistService = metaDataVersionPersistService;
        repositoryTuplePersistService = new RepositoryTuplePersistService(repository);
    }
    
    /**
     * Load global rule configurations.
     *
     * @return global rule configurations
     */
    public Collection<RuleConfiguration> load() {
        return new RepositoryTupleSwapperEngine().swapToRuleConfigurations(repositoryTuplePersistService.load(GlobalRuleNodePath.getRootPath()));
    }
    
    /**
     * Load global rule configuration.
     *
     * @param ruleTypeName rule type name to be loaded
     * @return global rule configuration
     */
    public Optional<RuleConfiguration> load(final String ruleTypeName) {
        return new RepositoryTupleSwapperEngine().swapToRuleConfiguration(ruleTypeName, repositoryTuplePersistService.load(GlobalRuleNodePath.getRulePath(ruleTypeName)));
    }
    
    /**
     * Persist global rule configurations.
     *
     * @param globalRuleConfigs global rule configurations
     */
    public void persist(final Collection<RuleConfiguration> globalRuleConfigs) {
        Collection<MetaDataVersion> metaDataVersions = new LinkedList<>();
        RepositoryTupleSwapperEngine repositoryTupleSwapperEngine = new RepositoryTupleSwapperEngine();
        for (YamlRuleConfiguration each : new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)) {
            metaDataVersions.addAll(persistTuples(repositoryTupleSwapperEngine.swapToRepositoryTuples(each)));
        }
        metaDataVersionPersistService.switchActiveVersion(metaDataVersions);
    }
    
    private Collection<MetaDataVersion> persistTuples(final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            List<String> versions = metaDataVersionPersistService.getVersions(GlobalRuleNodePath.getVersionRootPath(each.getKey()));
            String nextActiveVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(GlobalRuleNodePath.getVersionPath(each.getKey(), nextActiveVersion), each.getValue());
            String ruleActiveVersionPath = GlobalRuleNodePath.getActiveVersionPath(each.getKey());
            if (Strings.isNullOrEmpty(repository.query(ruleActiveVersionPath))) {
                repository.persist(ruleActiveVersionPath, MetaDataVersion.DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(GlobalRuleNodePath.getRulePath(each.getKey()), repository.query(ruleActiveVersionPath), nextActiveVersion));
        }
        return result;
    }
}
