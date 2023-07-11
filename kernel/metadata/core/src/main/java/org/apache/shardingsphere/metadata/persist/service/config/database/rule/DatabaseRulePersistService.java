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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metadata.persist.node.DatabaseMetaDataNode;
import org.apache.shardingsphere.metadata.persist.service.config.database.DatabaseBasedPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Database rule persist service.
 */
@RequiredArgsConstructor
public final class DatabaseRulePersistService implements DatabaseBasedPersistService<Collection<RuleConfiguration>> {
    
    private static final String DEFAULT_VERSION = "0";
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String databaseName, final Collection<RuleConfiguration> configs) {
        if (Strings.isNullOrEmpty(getDatabaseActiveVersion(databaseName))) {
            repository.persist(DatabaseMetaDataNode.getActiveVersionPath(databaseName), DEFAULT_VERSION);
        }
        repository.persist(DatabaseMetaDataNode.getRulePath(databaseName, getDatabaseActiveVersion(databaseName)),
                YamlEngine.marshal(createYamlRuleConfigurations(configs)));
    }
    
    private Collection<YamlRuleConfiguration> createYamlRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs) {
        return new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(ruleConfigs);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<RuleConfiguration> load(final String databaseName) {
        return isExisted(databaseName)
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.getDirectly(DatabaseMetaDataNode.getRulePath(databaseName,
                        getDatabaseActiveVersion(databaseName))), Collection.class, true))
                : new LinkedList<>();
    }
    
    private boolean isExisted(final String databaseName) {
        return !Strings.isNullOrEmpty(getDatabaseActiveVersion(databaseName))
                && !Strings.isNullOrEmpty(repository.getDirectly(DatabaseMetaDataNode.getRulePath(databaseName, getDatabaseActiveVersion(databaseName))));
    }
    
    private String getDatabaseActiveVersion(final String databaseName) {
        return repository.getDirectly(DatabaseMetaDataNode.getActiveVersionPath(databaseName));
    }
}
