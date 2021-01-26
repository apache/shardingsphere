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

package org.apache.shardingsphere.governance.core.config.listener.metadata;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.governance.core.config.ConfigCenterNode;
import org.apache.shardingsphere.governance.core.event.listener.PostGovernanceRepositoryEventListener;
import org.apache.shardingsphere.governance.core.event.model.GovernanceEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Optional;

/**
 * Rule changed listener.
 */
public final class RuleChangedListener extends PostGovernanceRepositoryEventListener<GovernanceEvent> {
    
    private final ConfigCenterNode configurationNode;
    
    public RuleChangedListener(final ConfigurationRepository configurationRepository, final Collection<String> schemaNames) {
        super(configurationRepository, new ConfigCenterNode().getAllRulePaths(schemaNames));
        configurationNode = new ConfigCenterNode();
    }
    
    @Override
    protected Optional<GovernanceEvent> createEvent(final DataChangedEvent event) {
        String schemaName = configurationNode.getSchemaName(event.getKey());
        if (Strings.isNullOrEmpty(schemaName) || Strings.isNullOrEmpty(event.getValue())) {
            return Optional.empty();
        }
        if (isRuleChangedEvent(schemaName, event.getKey())) {
            return Optional.of(createRuleChangedEvent(schemaName, event));
        } else if (isRuleCachedEvent(schemaName, event.getKey())) {
            return Optional.of(createRuleConfigurationCachedEvent(schemaName, event));
        }
        return Optional.empty();
    }
    
    private boolean isRuleChangedEvent(final String schemaName, final String eventPath) {
        String rulePath = configurationNode.getRulePath(schemaName);
        return rulePath.equals(eventPath);
    }
    
    private boolean isRuleCachedEvent(final String schemaName, final String key) {
        String ruleCachePath = configurationNode.getCachePath(configurationNode.getRulePath(schemaName));
        return ruleCachePath.equals(key);
    }
    
    private GovernanceEvent createRuleChangedEvent(final String schemaName, final DataChangedEvent event) {
        return new RuleConfigurationsChangedEvent(schemaName, getRuleConfigurations(event.getValue()));
    }
    
    private Collection<RuleConfiguration> getRuleConfigurations(final String yamlContent) {
        YamlRootRuleConfigurations configurations = YamlEngine.unmarshal(yamlContent, YamlRootRuleConfigurations.class, true);
        Preconditions.checkState(null != configurations, "No available rule to load for governance.");
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules());
    }
    
    private GovernanceEvent createRuleConfigurationCachedEvent(final String schemaName, final DataChangedEvent event) {
        return new RuleConfigurationCachedEvent(event.getValue(), schemaName);
    }
}
