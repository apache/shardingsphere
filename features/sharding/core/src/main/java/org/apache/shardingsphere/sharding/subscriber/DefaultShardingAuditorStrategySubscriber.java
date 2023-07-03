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

package org.apache.shardingsphere.sharding.subscriber;

import com.google.common.eventbus.Subscribe;
import lombok.Setter;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.mode.event.config.DatabaseRuleConfigurationChangedEvent;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.subsciber.RuleChangedSubscriber;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.audit.ShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.event.strategy.audit.AlterDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.event.strategy.audit.DropDefaultShardingAuditorStrategyEvent;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.strategy.audit.YamlShardingAuditStrategyConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.strategy.YamlShardingAuditStrategyConfigurationSwapper;

/**
 * Default sharding auditor strategy subscriber.
 */
@SuppressWarnings("UnstableApiUsage")
@Setter
public final class DefaultShardingAuditorStrategySubscriber implements RuleChangedSubscriber<AlterDefaultShardingAuditorStrategyEvent, DropDefaultShardingAuditorStrategyEvent> {
    
    private ContextManager contextManager;
    
    @Subscribe
    @Override
    public synchronized void renew(final AlterDefaultShardingAuditorStrategyEvent event) {
        if (!event.getActiveVersion().equals(contextManager.getInstanceContext().getModeContextManager().getActiveVersionByKey(event.getActiveVersionKey()))) {
            return;
        }
        String yamlContent = contextManager.getInstanceContext().getModeContextManager().getVersionPathByActiveVersionKey(event.getActiveVersionKey(), event.getActiveVersion());
        ShardingAuditStrategyConfiguration toBeChangedConfig = new YamlShardingAuditStrategyConfigurationSwapper().swapToObject(
                YamlEngine.unmarshal(yamlContent, YamlShardingAuditStrategyConfiguration.class));
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = database.getRuleMetaData().findSingleRule(ShardingRule.class)
                .map(optional -> (ShardingRuleConfiguration) optional.getConfiguration()).orElseGet(ShardingRuleConfiguration::new);
        config.setDefaultAuditStrategy(toBeChangedConfig);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
    
    @Subscribe
    @Override
    public synchronized void renew(final DropDefaultShardingAuditorStrategyEvent event) {
        if (!contextManager.getMetaDataContexts().getMetaData().containsDatabase(event.getDatabaseName())) {
            return;
        }
        ShardingSphereDatabase database = contextManager.getMetaDataContexts().getMetaData().getDatabases().get(event.getDatabaseName());
        ShardingRuleConfiguration config = (ShardingRuleConfiguration) database.getRuleMetaData().getSingleRule(ShardingRule.class).getConfiguration();
        config.setDefaultAuditStrategy(null);
        contextManager.getInstanceContext().getEventBusContext().post(new DatabaseRuleConfigurationChangedEvent(event.getDatabaseName(), config));
    }
}
