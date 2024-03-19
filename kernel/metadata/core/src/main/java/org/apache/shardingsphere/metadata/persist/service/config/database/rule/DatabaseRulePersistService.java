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

package org.apache.shardingsphere.metadata.persist.service.config.database.rule;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlDataNodeRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.config.AbstractPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseBasedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Database rule persist service.
 */
public final class DatabaseRulePersistService extends AbstractPersistService implements DatabaseBasedPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    public DatabaseRulePersistService(final PersistRepository repository) {
        super(repository);
        this.repository = repository;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final String databaseName, final Collection<RuleConfiguration> configs) {
        Map<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> yamlConfigs = new YamlDataNodeRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Entry<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            persistDataNodes(databaseName, entry.getValue().getRuleTagName().toLowerCase(), dataNodes);
        }
    }
    
    @Override
    public Collection<RuleConfiguration> load(final String databaseName) {
        Collection<YamlDataNode> dataNodes = getDataNodes(DatabaseMetaDataNode.getRulesNode(databaseName));
        return dataNodes.isEmpty() ? Collections.emptyList() : new YamlDataNodeRuleConfigurationSwapperEngine().swapToRuleConfigurations(dataNodes);
    }
    
    @Override
    public void delete(final String databaseName, final String name) {
        repository.delete(DatabaseMetaDataNode.getDatabaseRuleNode(databaseName, name));
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<MetaDataVersion> persistConfig(final String databaseName, final Collection<RuleConfiguration> configs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        Map<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> yamlConfigs = new YamlDataNodeRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Entry<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            result.addAll(persistDataNodes(databaseName, entry.getValue().getRuleTagName().toLowerCase(), dataNodes));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> persistDataNodes(final String databaseName, final String ruleName, final Collection<YamlDataNode> dataNodes) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (YamlDataNode each : dataNodes) {
            List<String> versions = repository.getChildrenKeys(DatabaseMetaDataNode.getDatabaseRuleVersionsNode(databaseName, ruleName, each.getKey()));
            String nextVersion = versions.isEmpty() ? DEFAULT_VERSION : String.valueOf(Integer.parseInt(versions.get(0)) + 1);
            repository.persist(DatabaseMetaDataNode.getDatabaseRuleVersionNode(databaseName, ruleName, each.getKey(), nextVersion), each.getValue());
            if (Strings.isNullOrEmpty(getActiveVersion(databaseName, ruleName, each.getKey()))) {
                repository.persist(DatabaseMetaDataNode.getDatabaseRuleActiveVersionNode(databaseName, ruleName, each.getKey()), DEFAULT_VERSION);
            }
            result.add(new MetaDataVersion(DatabaseMetaDataNode.getDatabaseRuleNode(databaseName, ruleName, each.getKey()), getActiveVersion(databaseName, ruleName, each.getKey()), nextVersion));
        }
        return result;
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public Collection<MetaDataVersion> deleteConfig(final String databaseName, final Collection<RuleConfiguration> configs) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        Map<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> yamlConfigs = new YamlDataNodeRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Entry<RuleConfiguration, YamlDataNodeRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            List<YamlDataNode> newDataNodes = new LinkedList<>(dataNodes);
            Collections.reverse(newDataNodes);
            result.addAll(deleteDataNodes(databaseName, entry.getValue().getRuleTagName().toLowerCase(), newDataNodes));
        }
        return result;
    }
    
    private Collection<MetaDataVersion> deleteDataNodes(final String databaseName, final String ruleName, final Collection<YamlDataNode> dataNodes) {
        Collection<MetaDataVersion> result = new LinkedList<>();
        for (YamlDataNode each : dataNodes) {
            String delKey = DatabaseMetaDataNode.getDatabaseRuleNode(databaseName, ruleName, each.getKey());
            repository.delete(delKey);
            result.add(new MetaDataVersion(delKey));
        }
        return result;
    }
    
    private String getActiveVersion(final String databaseName, final String ruleName, final String key) {
        return repository.getDirectly(DatabaseMetaDataNode.getDatabaseRuleActiveVersionNode(databaseName, ruleName, key));
    }
}
