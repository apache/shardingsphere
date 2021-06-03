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

package org.apache.shardingsphere.governance.core.registry.config.service.impl;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.registry.config.service.SchemaBasedRegistryService;
import org.apache.shardingsphere.governance.core.registry.config.node.SchemaMetadataNode;
import org.apache.shardingsphere.infra.rule.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.RuleConfigurationsAlteredSQLNotificationEvent;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Schema rule registry service.
 */
public final class SchemaRuleRegistryService implements SchemaBasedRegistryService<Collection<RuleConfiguration>> {
    
    private final RegistryCenterRepository repository;
    
    public SchemaRuleRegistryService(final RegistryCenterRepository repository) {
        this.repository = repository;
        ShardingSphereEventBus.getInstance().register(this);
    }
    
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
    
    /**
     * Update rule configurations for alter.
     *
     * @param event rule configurations altered event
     */
    @Subscribe
    public void update(final RuleConfigurationsAlteredSQLNotificationEvent event) {
        persist(event.getSchemaName(), event.getRuleConfigurations());
    }
}
