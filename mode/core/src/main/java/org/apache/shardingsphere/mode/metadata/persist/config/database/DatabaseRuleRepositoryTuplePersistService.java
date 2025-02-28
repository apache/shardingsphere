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

import org.apache.shardingsphere.mode.metadata.persist.config.RuleRepositoryTuplePersistService;
import org.apache.shardingsphere.mode.node.path.engine.generator.NodePathGenerator;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleItem;
import org.apache.shardingsphere.mode.node.path.type.metadata.rule.DatabaseRuleNodePath;
import org.apache.shardingsphere.mode.node.path.type.version.VersionNodePath;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNode;
import org.apache.shardingsphere.mode.node.rule.node.DatabaseRuleNodeGenerator;
import org.apache.shardingsphere.mode.node.rule.tuple.RuleRepositoryTuple;
import org.apache.shardingsphere.mode.spi.repository.PersistRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Database rule repository tuple persist service.
 */
public final class DatabaseRuleRepositoryTuplePersistService {
    
    private final PersistRepository repository;
    
    private final RuleRepositoryTuplePersistService tuplePersistService;
    
    public DatabaseRuleRepositoryTuplePersistService(final PersistRepository repository) {
        this.repository = repository;
        tuplePersistService = new RuleRepositoryTuplePersistService(repository);
    }
    
    /**
     * Load rule repository tuples.
     *
     * @param databaseName database name
     * @return loaded tuple map, key is rule type
     */
    public Map<String, Collection<RuleRepositoryTuple>> load(final String databaseName) {
        Map<String, Collection<RuleRepositoryTuple>> result = new HashMap<>();
        for (String each : repository.getChildrenKeys(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, null, null), false))) {
            result.put(each, load(databaseName, each));
        }
        return result;
    }
    
    private Collection<RuleRepositoryTuple> load(final String databaseName, final String ruleType) {
        Collection<DatabaseRuleNodePath> nodePaths = new LinkedList<>();
        DatabaseRuleNode databaseRuleNode = DatabaseRuleNodeGenerator.generate(ruleType);
        nodePaths.addAll(getUniqueItemNodePaths(databaseName, ruleType, databaseRuleNode.getUniqueItems()));
        nodePaths.addAll(getNamedItemNodePaths(databaseName, ruleType, databaseRuleNode.getNamedItems()));
        return nodePaths.stream().map(each -> new VersionNodePath(each).getActiveVersionPath()).map(tuplePersistService::load)
                .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getUniqueItemNodePaths(final String databaseName, final String ruleType, final Collection<String> uniqueItems) {
        return uniqueItems.stream().map(each -> new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(each))).collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getNamedItemNodePaths(final String databaseName, final String ruleType, final Collection<String> namedItems) {
        return namedItems.stream().flatMap(each -> getNamedItemNodePaths(databaseName, ruleType, each).stream()).collect(Collectors.toList());
    }
    
    private Collection<DatabaseRuleNodePath> getNamedItemNodePaths(final String databaseName, final String ruleType, final String namedItem) {
        return repository.getChildrenKeys(NodePathGenerator.toPath(new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(namedItem)), false)).stream()
                .map(each -> new DatabaseRuleNodePath(databaseName, ruleType, new DatabaseRuleItem(namedItem, each))).collect(Collectors.toList());
    }
}
