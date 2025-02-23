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

package org.apache.shardingsphere.mode.metadata.persist.config.database;

import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.node.tuple.YamlRepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Database rule persist service.
 */
public final class DatabaseRulePersistService {
    
    private final PersistRepository repository;
    
    private final RepositoryTuplePersistService repositoryTuplePersistService;
    
    private final MetaDataVersionPersistService metaDataVersionPersistService;
    
    private final YamlRepositoryTupleSwapperEngine yamlRepositoryTupleSwapperEngine;
    
    private final YamlRuleConfigurationSwapperEngine yamlRuleConfigurationSwapperEngine;
    
    public DatabaseRulePersistService(final PersistRepository repository) {
        this.repository = repository;
        repositoryTuplePersistService = new RepositoryTuplePersistService(repository);
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
        yamlRepositoryTupleSwapperEngine = new YamlRepositoryTupleSwapperEngine();
        yamlRuleConfigurationSwapperEngine = new YamlRuleConfigurationSwapperEngine();
    }
    
    /**
     * Load configurations.
     *
     * @param databaseName database name
     * @return configurations
     */
    public Collection<RuleConfiguration> load(final String databaseName) {
        return yamlRepositoryTupleSwapperEngine.swapToRuleConfigurations(
                repositoryTuplePersistService.load(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, null, null), false)));
    }
    
    /**
     * Persist configurations.
     *
     * @param databaseName database name
     * @param configs to be persisted configurations
     * @return meta data versions
     */
    public Collection<MetaDataVersion> persist(final String databaseName, final Collection<RuleConfiguration> configs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (YamlRuleConfiguration each : yamlRuleConfigurationSwapperEngine.swapToYamlRuleConfigurations(configs)) {
            Collection<RepositoryTuple> repositoryTuples = yamlRepositoryTupleSwapperEngine.swapToRepositoryTuples(each);
            if (!repositoryTuples.isEmpty()) {
                result.addAll(persistDataNodes(databaseName, Objects.requireNonNull(each.getClass().getAnnotation(RepositoryTupleEntity.class)).value(), repositoryTuples));
            }
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistDataNodes(final String databaseName, final String ruleType, final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            DatabaseRuleItem databaseRuleItem = new DatabaseRuleItem(each.getKey());
            DatabaseRuleNodePath databaseRuleNodePath = new DatabaseRuleNodePath(databaseName, ruleType, databaseRuleItem);
            int nextVersion = metaDataVersionPersistService.persist(NodePathGenerator.toVersionPath(databaseRuleNodePath), each.getValue());
            result.add(new MetaDataVersion(NodePathGenerator.toPath(databaseRuleNodePath, false), Math.max(MetaDataVersion.INIT_VERSION, nextVersion - 1)));
        }
        return result;
    }
    
    /**
     * Delete configurations.
     *
     * @param databaseName database name
     * @param ruleType rule type
     */
    public void delete(final String databaseName, final String ruleType) {
        repository.delete(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, null), false));
    }
    
    /**
     * Delete configurations.
     *
     * @param databaseName database name
     * @param configs to be deleted configurations
     * @return meta data versions
     */
    public Collection<MetaDataVersion> delete(final String databaseName, final Collection<RuleConfiguration> configs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (YamlRuleConfiguration each : yamlRuleConfigurationSwapperEngine.swapToYamlRuleConfigurations(configs)) {
            List<RepositoryTuple> repositoryTuples = new LinkedList<>(yamlRepositoryTupleSwapperEngine.swapToRepositoryTuples(each));
            if (repositoryTuples.isEmpty()) {
                continue;
            }
            Collections.reverse(repositoryTuples);
            result.addAll(delete(databaseName, Objects.requireNonNull(each.getClass().getAnnotation(RepositoryTupleEntity.class)).value(), repositoryTuples));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> delete(final String databaseName, final String ruleType, final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            String toBeDeletedKey = NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(each.getKey())), false);
            repository.delete(toBeDeletedKey);
            result.add(new MetaDataVersion(toBeDeletedKey));
        }
        return result;
    }
}
