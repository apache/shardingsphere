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

package org.apache.shardingsphere.metadata.persist.service.config.database;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.util.yaml.datanode.YamlDataNode;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.NewYamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.Map;

/**
 * TODO Rename DatabaseRulePersistService when metadata structure adjustment completed. #25485
 * Database rule persist service.
 */
@RequiredArgsConstructor
public final class NewDatabaseRulePersistService implements NewDatabaseRuleBasedPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private static final String ACTIVE_VERSION = "active_version";
    
    private static final String VERSIONS = "versions";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final Map<String, DataSource> dataSources,
                        final Collection<ShardingSphereRule> rules, final Collection<RuleConfiguration> configs) {
        // TODO Load single table refer to #22887
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void persist(final String databaseName, final Collection<RuleConfiguration> configs) {
        Map<RuleConfiguration, NewYamlRuleConfigurationSwapper> yamlConfigs = new NewYamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
        for (Map.Entry<RuleConfiguration, NewYamlRuleConfigurationSwapper> entry : yamlConfigs.entrySet()) {
            Collection<YamlDataNode> dataNodes = entry.getValue().swapToDataNodes(entry.getKey());
            if (dataNodes.isEmpty()) {
                continue;
            }
            persistDataNodes(entry.getValue().getRuleTagName().toLowerCase(), dataNodes);
        }
    }
    
    private void persistDataNodes(final String ruleName, final Collection<YamlDataNode> dataNodes) {
        for (YamlDataNode each : dataNodes) {
            if (Strings.isNullOrEmpty(repository.getDirectly(each.getKey()))) {
                repository.persist(appendActiveVersion(ruleName, each.getKey()), DEFAULT_VERSION);
            }
            List<String> versions = repository.getChildrenKeys(String.join("/", "", ruleName, each.getKey(), VERSIONS));
            repository.persist(appendVersion(ruleName, each.getKey(), versions.isEmpty()
                    ? DEFAULT_VERSION
                    : String.valueOf(Integer.parseInt(versions.get(0)) + 1)), each.getValue());
        }
    }
    
    private String appendActiveVersion(final String ruleName, final String key) {
        return String.join("/", "", ruleName, key, ACTIVE_VERSION);
    }
    
    private String appendVersion(final String ruleName, final String key, final String nextVersion) {
        return String.join("/", "", ruleName, key, VERSIONS, nextVersion);
    }
    
    @Override
    public Collection<RuleConfiguration> load(final String databaseName, final String ruleName) {
        // TODO
        return Collections.emptyList();
    }
}
