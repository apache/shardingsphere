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
import org.apache.shardingsphere.mode.node.path.version.MetaDataVersion;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.metadata.persist.version.VersionPersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.database.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeGenerator;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleNodeTuple;
import org.apache.shardingsphere.mode.node.rule.tuple.YamlRuleNodeTupleSwapperEngine;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Database rule persist service.
 */
public final class DatabaseRulePersistService {
    
    private final PersistRepository repository;
    
    private final VersionPersistService versionPersistService;
    
    private final YamlRuleConfigurationSwapperEngine yamlSwapperEngine;
    
    private final YamlRuleNodeTupleSwapperEngine tupleSwapperEngine;
    
    public DatabaseRulePersistService(final PersistRepository repository) {
        this.repository = repository;
        versionPersistService = new VersionPersistService(repository);
        yamlSwapperEngine = new YamlRuleConfigurationSwapperEngine();
        tupleSwapperEngine = new YamlRuleNodeTupleSwapperEngine();
    }
    
    /**
     * Load configurations.
     *
     * @param databaseName database name
     * @return configurations
     */
    public Collection<RuleConfiguration> load(final String databaseName) {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, null, null))).stream()
                .map(each -> load(databaseName, each)).collect(Collectors.toList());
    }
    
    private RuleConfiguration load(final String databaseName, final String ruleType) {
        return yamlSwapperEngine.swapToRuleConfiguration(
                tupleSwapperEngine.swapToYamlDatabaseRuleConfiguration(databaseName, ruleType, load(databaseName, DatabaseRuleNodeGenerator.generate(ruleType))));
    }
    
    private Collection<RuleNodeTuple> load(final String databaseName, final DatabaseRuleNode databaseRuleNode) {
        Collection<DatabaseRuleNodePath> nodePaths = new LinkedList<>();
        nodePaths.addAll(getUniqueItemNodePaths(databaseName, databaseRuleNode.getRuleType(), databaseRuleNode.getUniqueItems()));
        nodePaths.addAll(getNamedItemNodePaths(databaseName, databaseRuleNode.getRuleType(), databaseRuleNode.getNamedItems()));
        return nodePaths.stream()
                .map(each -> new RuleNodeTuple(each, versionPersistService.loadContent(new VersionNodePath(each)))).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getUniqueItemNodePaths(final String databaseName, final String ruleType, final Collection<String> uniqueItems) {
        return uniqueItems.stream().map(each -> new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(each))).collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getNamedItemNodePaths(final String databaseName, final String ruleType, final Collection<String> namedItems) {
        return namedItems.stream().flatMap(each -> getNamedItemNodePaths(databaseName, ruleType, each).stream()).collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getNamedItemNodePaths(final String databaseName, final String ruleType, final String namedItem) {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(namedItem)))).stream()
                .map(each -> new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(namedItem, each))).collect(Collectors.toList());
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
        for (YamlRuleConfiguration each : yamlSwapperEngine.swapToYamlRuleConfigurations(configs)) {
            result.addAll(persistTuples(tupleSwapperEngine.swapToTuples(databaseName, each)));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistTuples(final Collection<RuleNodeTuple> tuples) {
        return tuples.stream().map(each -> new MetaDataVersion(each.getNodePath(),
                Math.max(MetaDataVersion.INIT_VERSION, versionPersistService.persist(new VersionNodePath(each.getNodePath()), each.getContent()) - 1))).collect(Collectors.toList());
    }
    
    /**
     * Delete configurations.
     *
     * @param databaseName database name
     * @param ruleType rule type
     */
    public void delete(final String databaseName, final String ruleType) {
        repository.delete(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, null)));
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
        for (YamlRuleConfiguration each : yamlSwapperEngine.swapToYamlRuleConfigurations(configs)) {
            List<RuleNodeTuple> tuples = new LinkedList<>(tupleSwapperEngine.swapToTuples(databaseName, each));
            Collections.reverse(tuples);
            result.addAll(deleteTuples(tuples));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> deleteTuples(final Collection<RuleNodeTuple> tuples) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (RuleNodeTuple each : tuples) {
            repository.delete(each.getPath());
            result.add(new MetaDataVersion(each.getNodePath()));
        }
        return result;
    }
}
