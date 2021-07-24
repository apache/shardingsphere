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

package org.apache.shardingsphere.infra.config.persist.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.persist.node.SchemaMetadataNode;
import org.apache.shardingsphere.infra.config.persist.service.SchemaBasedPersistService;
import org.apache.shardingsphere.infra.config.persist.repository.ConfigCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema rule persist service.
 */
@RequiredArgsConstructor
public final class SchemaRulePersistService implements SchemaBasedPersistService<Collection<RuleConfiguration>> {
    
    private final ConfigCenterRepository repository;
    
    @Override
    public void persist(final String schemaName, final Collection<RuleConfiguration> configs, final boolean isOverwrite) {
        if (!configs.isEmpty() && (isOverwrite || !isExisted(schemaName))) {
            persist(schemaName, configs);
        }
    }
    
    @Override
    public void persist(final String schemaName, final Collection<RuleConfiguration> configs) {
        repository.persist(SchemaMetadataNode.getRulePath(schemaName), YamlEngine.marshal(createYamlRuleConfigurations(schemaName, configs)));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<YamlRuleConfiguration> createYamlRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigs) {
            RuleConfigurationCheckerFactory.newInstance(each).check(schemaName, each);
            configs.add(each);
        }
        return new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> load(final String schemaName) {
        return isExisted(schemaName)
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(SchemaMetadataNode.getRulePath(schemaName)), Collection.class))
                : new LinkedList<>();
    }
    
    @Override
    public boolean isExisted(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(SchemaMetadataNode.getRulePath(schemaName)));
    }
}
