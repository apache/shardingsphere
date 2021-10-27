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

package org.apache.shardingsphere.mode.metadata.persist.service.impl;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.mode.metadata.persist.node.CacheNode;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.metadata.persist.node.SchemaMetaDataNode;
import org.apache.shardingsphere.mode.metadata.persist.service.SchemaBasedPersistService;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema rule persist service.
 */
@RequiredArgsConstructor
public final class SchemaRulePersistService implements SchemaBasedPersistService<Collection<RuleConfiguration>> {
    
    private final PersistRepository repository;
    
    @Override
    public void persist(final String schemaName, final Collection<RuleConfiguration> configs, final boolean isOverwrite) {
        if (!configs.isEmpty() && (isOverwrite || !isExisted(schemaName))) {
            persist(schemaName, configs);
        }
    }
    
    @Override
    public void persist(final String schemaName, final Collection<RuleConfiguration> configs) {
        repository.persist(SchemaMetaDataNode.getRulePath(schemaName), YamlEngine.marshal(createYamlRuleConfigurations(configs)));
    }
    
    private Collection<YamlRuleConfiguration> createYamlRuleConfigurations(final Collection<RuleConfiguration> ruleConfigs) {
        return new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(ruleConfigs);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public Collection<RuleConfiguration> load(final String schemaName) {
        return isExisted(schemaName)
                // TODO process algorithm provided configuration 
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(SchemaMetaDataNode.getRulePath(schemaName)), Collection.class))
                : new LinkedList<>();
    }
    
    @Override
    public boolean isExisted(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(SchemaMetaDataNode.getRulePath(schemaName)));
    }
    
    @Override
    public void cache(final String schemaName, final Collection<RuleConfiguration> configs) {
        repository.persist(CacheNode.getCachePath(SchemaMetaDataNode.getRulePath(schemaName)), YamlEngine.marshal(createYamlRuleConfigurations(configs)));
    }
}
