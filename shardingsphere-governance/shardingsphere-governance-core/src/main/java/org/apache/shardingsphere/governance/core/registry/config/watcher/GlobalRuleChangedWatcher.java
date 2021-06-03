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

package org.apache.shardingsphere.governance.core.registry.config.watcher;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.governance.core.registry.config.node.GlobalNode;
import org.apache.shardingsphere.governance.core.registry.GovernanceWatcher;
import org.apache.shardingsphere.governance.core.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent;
import org.apache.shardingsphere.governance.repository.api.listener.DataChangedEvent.Type;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Global rule changed watcher.
 */
public final class GlobalRuleChangedWatcher implements GovernanceWatcher<GlobalRuleConfigurationsChangedEvent> {
    
    @Override
    public Collection<String> getWatchingKeys(final Collection<String> schemaNames) {
        return Collections.singleton(GlobalNode.getGlobalRuleNode());
    }
    
    @Override
    public Collection<Type> getWatchingTypes() {
        return Collections.singleton(Type.UPDATED);
    }
    
    @Override
    public Optional<GlobalRuleConfigurationsChangedEvent> createGovernanceEvent(final DataChangedEvent event) {
        return Optional.of(new GlobalRuleConfigurationsChangedEvent(getGlobalRuleConfigurations(event)));
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> getGlobalRuleConfigurations(final DataChangedEvent event) {
        Collection<YamlRuleConfiguration> globalRuleConfigs = YamlEngine.unmarshal(event.getValue(), Collection.class);
        Preconditions.checkState(!globalRuleConfigs.isEmpty(), "No available global rule to load for governance.");
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(globalRuleConfigs);
    }
}
