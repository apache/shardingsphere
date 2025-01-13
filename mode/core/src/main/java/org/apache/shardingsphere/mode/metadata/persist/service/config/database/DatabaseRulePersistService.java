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

package org.apache.shardingsphere.mode.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.node.path.metadata.DatabaseRuleMetaDataNodePath;
import org.apache.shardingsphere.mode.metadata.persist.service.config.RepositoryTuplePersistService;
import org.apache.shardingsphere.mode.metadata.persist.service.version.MetaDataVersionPersistService;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTuple;
import org.apache.shardingsphere.mode.node.tuple.RepositoryTupleSwapperEngine;
import org.apache.shardingsphere.mode.node.tuple.annotation.RepositoryTupleEntity;

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
    
    public DatabaseRulePersistService(final PersistRepository repository) {
        this.repository = repository;
        repositoryTuplePersistService = new RepositoryTuplePersistService(repository);
        metaDataVersionPersistService = new MetaDataVersionPersistService(repository);
    }
    
    /**
     * Load configurations.
     *
     * @param databaseName database name
     * @return configurations
     */
    public Collection<RuleConfiguration> load(final String databaseName) {
        return new RepositoryTupleSwapperEngine().swapToRuleConfigurations(repositoryTuplePersistService.load(DatabaseRuleMetaDataNodePath.getRootPath(databaseName)));
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
        RepositoryTupleSwapperEngine repositoryTupleSwapperEngine = new RepositoryTupleSwapperEngine();
        for (YamlRuleConfiguration each : new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs)) {
            Collection<RepositoryTuple> repositoryTuples = repositoryTupleSwapperEngine.swapToRepositoryTuples(each);
            if (!repositoryTuples.isEmpty()) {
                result.addAll(persistDataNodes(databaseName, Objects.requireNonNull(each.getClass().getAnnotation(RepositoryTupleEntity.class)).value(), repositoryTuples));
            }
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistDataNodes(final String databaseName, final String ruleName, final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            List<String> versions = metaDataVersionPersistService.getVersions(DatabaseRuleMetaDataNodePath.getVersionsPath(databaseName, ruleName, each.getKey()));
            String nextVersion = versions.isEmpty() ? MetaDataVersion.DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DatabaseRuleMetaDataNodePath.getVersionPath(databaseName, ruleName, each.getKey(), nextVersion), each.getValue());
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, ruleName, each.getKey()))) {
                repository.persist(DatabaseRuleMetaDataNodePath.getActiveVersionPath(databaseName, ruleName, each.getKey()), MetaDataVersion.DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DatabaseRuleMetaDataNodePath.getRulePath(databaseName, ruleName, each.getKey()), getActiveVersion(databaseName, ruleName, each.getKey()), nextVersion));
        }
        return result;
    }
    
    private String getActiveVersion(final String databaseName, final String ruleName, final String key) {
        return repository.query(DatabaseRuleMetaDataNodePath.getActiveVersionPath(databaseName, ruleName, key));
    }
    
    /**
     * Delete configurations.
     *
     * @param databaseName database name
     * @param ruleTypeName rule type name
     */
    public void delete(final String databaseName, final String ruleTypeName) {
        repository.delete(DatabaseRuleMetaDataNodePath.getRulePath(databaseName, ruleTypeName));
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
        RepositoryTupleSwapperEngine repositoryTupleSwapperEngine = new RepositoryTupleSwapperEngine();
        for (YamlRuleConfiguration each : new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs)) {
            Collection<RepositoryTuple> repositoryTuples = repositoryTupleSwapperEngine.swapToRepositoryTuples(each);
            if (repositoryTuples.isEmpty()) {
                continue;
            }
            List<RepositoryTuple> newRepositoryTuples = new LinkedList<>(repositoryTuples);
            Collections.reverse(newRepositoryTuples);
            result.addAll(delete(databaseName, Objects.requireNonNull(each.getClass().getAnnotation(RepositoryTupleEntity.class)).value(), newRepositoryTuples));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> delete(final String databaseName, final String ruleName, final Collection<RepositoryTuple> repositoryTuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RepositoryTuple each : repositoryTuples) {
            String toBeDeletedKey = DatabaseRuleMetaDataNodePath.getRulePath(databaseName, ruleName, each.getKey());
            repository.delete(toBeDeletedKey);
            result.add(new MetaDataVersion(toBeDeletedKey));
        }
        return result;
    }
}
